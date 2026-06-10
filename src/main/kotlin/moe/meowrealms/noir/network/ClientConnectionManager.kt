package moe.meowrealms.noir.network

import io.netty.buffer.Unpooled
import io.netty.handler.codec.DecoderException
import io.netty.util.ReferenceCountUtil
import moe.meowrealms.noir.NoirMain
import moe.meowrealms.noir.network.packet.Packet
import moe.meowrealms.noir.utils.SimpleFriendlyByteBuf
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.messaging.PluginMessageListener
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object ClientConnectionManager : Listener, PluginMessageListener {
    const val VERSION = "2.6.0"
    private val LISTENING_CHANNEL = NamespacedKey("yes_steve_model", VERSION.replace(".", "_"))
    private val LISTENING_CHANNEL_STR = LISTENING_CHANNEL.toString()

    private val connectedPlayers : MutableMap<UUID, ClientConnection> = ConcurrentHashMap()

    fun init() {
        Bukkit.getPluginManager().registerEvents(this, NoirMain.instance)

        Bukkit.getMessenger().registerIncomingPluginChannel(NoirMain.instance, LISTENING_CHANNEL_STR, this)
        Bukkit.getMessenger().registerOutgoingPluginChannel(NoirMain.instance, LISTENING_CHANNEL_STR)

        NoirMain.instance.slF4JLogger.info("Initialized network manager.")
    }

    // note: packets are coming from netty threads
    override fun onPluginMessageReceived(
        channel: String,
        player: Player,
        message: ByteArray
    ) {
        if (channel != LISTENING_CHANNEL_STR) {
            return
        }

        // push back to main thread
        // so that we could manage the states of a single connection safely and easily
        player.scheduler.execute(NoirMain.instance, {
            val messageAsBuffer = SimpleFriendlyByteBuf(Unpooled.wrappedBuffer(message))

            try {
                this.onClientMessage(player, messageAsBuffer)
            }finally {
                ReferenceCountUtil.safeRelease(messageAsBuffer)
            }
        }, null, 1L)
    }

    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        NoirMain.instance.slF4JLogger.info("Player {} has connected with UUID {}", event.player.name, event.player.uniqueId)

        val newConnection = ClientConnection(event.player)

        this.connectedPlayers[event.player.uniqueId] = newConnection

        // we are already on the target thread, no need to schedule back

        // catch and pass through exceptions
        try {
            newConnection.onConnected()
        }catch (throwable : Throwable){
            this.connectionExceptionCaught(event.player, throwable)
        }
    }

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        val disconnected = this.connectedPlayers.remove(event.player.uniqueId)

        if (disconnected != null) {
            // catch and pass through exceptions
            try {
                disconnected.onDisconnected()
            }catch (throwable : Throwable){
                this.connectionExceptionCaught(event.player, throwable)
            }

            NoirMain.instance.slF4JLogger.info("Disconnected connection for player {}", event.player.name)
        }
    }

    fun connectionExceptionCaught(player: Player, throwable: Throwable) {
        NoirMain.instance.slF4JLogger.error("Error has caught during packet processing! Packet sender is ${player.name}.", throwable)
    }

    fun sendMessage(receiver: Player, packet: Packet) {
        try {
            val packetId = NoirMain.packetRegistry.lookupForId(packet)

            if (packetId == -1) {
                NoirMain.instance.slF4JLogger.warn("Sending unknown packet of id: {}", packetId)
                return
            }

            val tempBuffer = SimpleFriendlyByteBuf(Unpooled.buffer())
            var encodedPacket: ByteArray?

            try {
                tempBuffer.writeByte(packetId)

                packet.write(tempBuffer)
            }finally {
                try {
                    encodedPacket = ByteArray(tempBuffer.readableBytes())
                    tempBuffer.readBytes(encodedPacket)
                }finally {
                    ReferenceCountUtil.safeRelease(tempBuffer)
                }
            }

            receiver.sendPluginMessage(NoirMain.instance, LISTENING_CHANNEL_STR, encodedPacket)
        }catch (throwable : Throwable){
            this.connectionExceptionCaught(receiver, throwable)
        }
    }

    fun onClientMessage(sender: Player, packetData: SimpleFriendlyByteBuf) {
        val connection = this.connectedPlayers[sender.uniqueId]

        if (connection == null) {
            NoirMain.instance.slF4JLogger.warn("Receiving packets for uninitialized connection for player {}!", sender.uniqueId)
            return
        }

        try {
            val packetInt = packetData.readByte().toInt()

            val createdPacket = NoirMain.packetRegistry.lookupAndConstruct(packetInt)
                ?: throw DecoderException("Packet with id $packetInt not found!")

            val direction = createdPacket.direction()
            val receivingDirection = connection.receivingDirection()

            if (direction != receivingDirection) {
                throw DecoderException("Handling packets with mismatched direction $direction! Expected direction is $receivingDirection!")
            }

            createdPacket.read(packetData)

            if (packetData.readableBytes() != 0) {
                throw DecoderException("Packet $packetInt has ${packetData.readableBytes()} unread bytes")
            }

            createdPacket.handle(connection)
        } catch (throwable: Throwable) {
            this.connectionExceptionCaught(sender, throwable)
        }
    }

    fun Player.getYsmConnection(): ClientConnection {
        return connectedPlayers[this.uniqueId]!!
    }
}