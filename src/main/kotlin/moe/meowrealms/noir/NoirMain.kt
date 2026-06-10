package moe.meowrealms.noir

import moe.meowrealms.noir.model.ModelManager
import moe.meowrealms.noir.network.packet.PacketRegistry
import moe.meowrealms.noir.network.ClientConnectionManager
import moe.meowrealms.noir.network.packet.c2s.C2SHandshakeConfirmedPacket
import moe.meowrealms.noir.network.packet.c2s.C2SModelDataPayload
import moe.meowrealms.noir.network.packet.s2c.S2CHandshakeRequestPacket
import moe.meowrealms.noir.network.packet.s2c.S2CModelDataPayloadPacket
import org.bukkit.plugin.java.JavaPlugin

class NoirMain : JavaPlugin() {

    companion object {
        public lateinit var instance: NoirMain
        public lateinit var packetRegistry: PacketRegistry
    }

    fun registerPackets() {
        packetRegistry.register(51, S2CHandshakeRequestPacket::class, {S2CHandshakeRequestPacket()})
        packetRegistry.register(52, C2SHandshakeConfirmedPacket::class, { C2SHandshakeConfirmedPacket()})

        packetRegistry.register(1, S2CModelDataPayloadPacket::class, { S2CModelDataPayloadPacket(ByteArray(0)) })
        packetRegistry.register(2, C2SModelDataPayload::class, { C2SModelDataPayload(ByteArray(0)) })
    }

    fun initNetworking() {
        packetRegistry = PacketRegistry()

        this.registerPackets()

        ClientConnectionManager.init()
    }

    fun initAndLoadModels(){
        this.slF4JLogger.info("Begin loading models")

        ModelManager.setWorkingDir(this.dataPath)

        ModelManager.initEnv()

        val loadBegin = System.nanoTime()

        ModelManager.loadModels()

        val timeElapsed = System.nanoTime() - loadBegin

        this.slF4JLogger.info("Model loading took ${timeElapsed / 1000_000L} ms")
        this.slF4JLogger.info("All models has been loaded! Currently has ${ModelManager.getLoadedModelCount()} models loaded.")
    }

    override fun onEnable() {
        instance = this

        this.initNetworking()
        this.initAndLoadModels()
    }

    override fun onDisable() {
    }
}
