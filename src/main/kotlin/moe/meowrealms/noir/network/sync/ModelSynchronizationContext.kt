package moe.meowrealms.noir.network.sync

import com.elfmcys.yesstevemodel.model.format.ServerModelData
import io.netty.buffer.Unpooled
import moe.meowrealms.noir.NoirMain
import moe.meowrealms.noir.model.ModelManager
import moe.meowrealms.noir.network.ClientConnectionManager.getYsmConnection
import moe.meowrealms.noir.network.packet.s2c.S2CModelDataPayloadPacket
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import rip.ysm.security.YSMByteBuf
import rip.ysm.security.YsmCrypt
import java.nio.file.Files
import java.util.*
import kotlin.math.min

class ModelSynchronizationContext(
    val player: Player
) {
    private var state: Int = 0
    private lateinit var computedClientKey: ByteArray
    private lateinit var nextKeyInBytes: ByteArray
    private lateinit var subKeyInBytes: ByteArray
    private var allowedModels: Set<ServerModelData> = HashSet()

    fun computeClientKey() {
        this.computedClientKey = ByteArray(56)

        Random(this.player.uniqueId.mostSignificantBits + this.player.uniqueId.leastSignificantBits).nextBytes(this.computedClientKey)
    }

    fun filterAllowedModels() {
        this.allowedModels = HashSet(ModelManager.getCachedModels())
    }

    fun begin() {
        this.state = 1

        this.computeClientKey()
        this.filterAllowedModels()

        val garbageLen = 16 + ModelManager.secureRand.nextInt(48)
        val garbage = ByteArray(garbageLen)
        ModelManager.secureRand.nextBytes(garbage)

        YSMByteBuf(Unpooled.buffer()).use { outBuf ->
            outBuf.writeGarbageHeader(garbageLen, garbage)
            outBuf.writeByte(1.toByte())

            val result = YsmCrypt.encrypt(outBuf.toArray(), YsmCrypt.publicKey, true)
            this.subKeyInBytes = result.nextKey

            player.getYsmConnection().send(S2CModelDataPayloadPacket(result.data))

        }
    }

    fun cleanup() {
        ModelManager.onModelSynchronizationDone(this)
    }

    private fun sendRequestedCaches(requestedHashes: MutableList<LongArray>) {
        Bukkit.getAsyncScheduler().runNow(NoirMain.instance) {
            try {
                for (hashes in requestedHashes) {
                    val hash1 = hashes[0]
                    val hash2 = hashes[1]

                    val fileName = String.format("%016x%016x", hash1, hash2)
                    val file = ModelManager.getCacheFile(fileName)

                    if (Files.exists(file)) {
                        val fileData = Files.readAllBytes(file)
                        val totalSize = fileData.size
                        val maxChunkSize = 30720
                        val chunkCount = (totalSize + maxChunkSize - 1) / maxChunkSize
                        val chunkSize = (totalSize + chunkCount - 1) / chunkCount

                        var outBuf: YSMByteBuf?
                        var offset = 0
                        while (offset < totalSize) {
                            val length = min(chunkSize, totalSize - offset)
                            val garbageLen = 16 + ModelManager.secureRand.nextInt(48)
                            val garbage = ByteArray(garbageLen)
                            ModelManager.secureRand.nextBytes(garbage)
                            outBuf = YSMByteBuf(Unpooled.buffer())

                            outBuf.use {
                                outBuf.writeGarbageHeader(garbageLen, garbage)
                                outBuf.writeVarInt(5)
                                outBuf.writeVarLong(hash1)
                                outBuf.writeVarLong(hash2)
                                outBuf.writeVarInt(totalSize)
                                outBuf.writeVarInt(offset)
                                outBuf.writeVarInt(length)
                                outBuf.rawBuf.writeBytes(fileData, offset, length)
                                val result = YsmCrypt.encrypt(outBuf.toArray(), this.subKeyInBytes, false)

                                this.player.getYsmConnection().send(S2CModelDataPayloadPacket(result.data))

                                offset += length
                            }
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                NoirMain.instance.slF4JLogger.error("Failed to send model chunks to ${this.player.name}", e)
            }
        }
    }

    private fun sendCacheList() {
        val garbageLen = 16 + ModelManager.secureRand.nextInt(48)
        val garbage = ByteArray(garbageLen)
        ModelManager.secureRand.nextBytes(garbage)

        try {
            YSMByteBuf(Unpooled.buffer()).use { outBuf ->
                outBuf.writeGarbageHeader(garbageLen, garbage)
                outBuf.writeVarInt(3)
                outBuf.writeVarLong(0L)
                outBuf.rawBuf.writeBytes(ModelManager.cacheKey())
                outBuf.rawBuf.writeBytes(this.computedClientKey)
                outBuf.writeVarInt(this.allowedModels.size)

                for (model in this.allowedModels) {
                    val sha256 = model.loadedModelData.modelHash
                    val hashes = YsmCrypt.calculateModelHashes(sha256, ModelManager.cacheKey())
                    outBuf.writeVarLong(hashes[0])
                    outBuf.writeVarLong(hashes[1])
                    outBuf.writeString(model.modelId)
                    outBuf.writeVarInt(if (model.isAuth) 1 else 0)
                    outBuf.writeVarInt(if (model.isCustomSkinModel) 1 else 0)
                    outBuf.writeVarInt(32)
                }

                val builtinModels = ArrayList(ModelManager.getBuiltinModels())

                outBuf.writeVarInt(builtinModels.size)

                for (pack in builtinModels) {
                    outBuf.writeString(pack.folderPath)
                    if (pack.iconData != null) {
                        outBuf.writeVarInt(1)
                        outBuf.writeByteArray(pack.iconData)
                        outBuf.writeVarInt(pack.iconWidth)
                        outBuf.writeVarInt(pack.iconHeight)
                        outBuf.writeVarInt(pack.iconFormat)
                        outBuf.writeVarInt(1)
                    } else {
                        outBuf.writeVarInt(0)
                    }

                    if (pack.name == null && pack.description == null) {
                        outBuf.writeVarInt(0)
                    } else {
                        outBuf.writeVarInt(1)
                        outBuf.writeString(pack.name ?: "")
                        outBuf.writeString(pack.description ?: "")
                    }

                    if (pack.lang != null && !pack.lang.isEmpty()) {
                        outBuf.writeVarInt(pack.lang.size)

                        for (langEntry in pack.lang.entries) {
                            outBuf.writeString(langEntry.key)
                            outBuf.writeVarInt((langEntry.value as MutableMap<*, *>).size)

                            for (kv in (langEntry.value as MutableMap<*, *>).entries) {
                                outBuf.writeString(kv.key as String?)
                                outBuf.writeString(kv.value as String?)
                            }
                        }
                    } else {
                        outBuf.writeVarInt(0)
                    }
                }

                outBuf.writeVarInt(0)
                val result = YsmCrypt.encrypt(outBuf.toArray(), this.nextKeyInBytes, false)

                this.player.getYsmConnection().send(S2CModelDataPayloadPacket(result.data()))
            }
        } catch (e: java.lang.Exception) {
            throw RuntimeException(e)
        }
    }

    fun handleClientReply(data: ByteArray) {
        if (data.isEmpty()){
            this.cleanup()
            return
        }

        try {
            val decrypted = YsmCrypt.decrypt(data, this.subKeyInBytes)

            if (this.state == 1) {
                if (decrypted != null && decrypted.size >= 56) {
                    this.nextKeyInBytes = Arrays.copyOfRange(decrypted, decrypted.size - 56, decrypted.size)
                    val payload = Arrays.copyOfRange(decrypted, 0, decrypted.size - 56)

                    YSMByteBuf(Unpooled.wrappedBuffer(payload)).use { buf ->
                        buf.skipGarbageHeader()
                        if (buf.rawBuf.readByte().toInt() != 2) {
                            return
                        }
                    }

                    // promote state
                    this.state = 2

                    this.sendCacheList()
                    return
                }

                return
            } else if (this.state == 2) {
                YSMByteBuf(Unpooled.wrappedBuffer(decrypted)).use { buf ->
                    buf.skipGarbageHeader()

                    val ordinal = buf.rawBuf.readByte().toInt()

                    if (ordinal == 4) {
                        val numRequests = buf.readVarInt()
                        val requestedHashes: MutableList<LongArray> = ArrayList()

                        for (i in 0..< numRequests) {
                            requestedHashes.add(longArrayOf(buf.readVarLong(), buf.readVarLong()))
                        }

                        this.state = 3

                        NoirMain.instance.slF4JLogger.info("Sending requested hashes: $requestedHashes")
                        this.sendRequestedCaches(requestedHashes)
                        return
                    }

                    NoirMain.instance.slF4JLogger.warn("Synchronization err, client reported: $ordinal")
                }
                return
            }
        } catch (e: Exception) {
            NoirMain.instance.slF4JLogger.error("Model synchronization fatal error: ${this.player.name}", e)

            // we'll handle it upstream
            throw e
        }
    }
}