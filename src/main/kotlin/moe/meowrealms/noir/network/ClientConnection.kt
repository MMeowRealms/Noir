package moe.meowrealms.noir.network

import moe.meowrealms.noir.NoirMain
import moe.meowrealms.noir.model.ModelManager
import moe.meowrealms.noir.network.packet.Packet
import moe.meowrealms.noir.network.packet.PacketHandler
import moe.meowrealms.noir.network.packet.c2s.C2SHandshakeConfirmedPacket
import moe.meowrealms.noir.network.packet.c2s.C2SModelDataPayload
import moe.meowrealms.noir.network.packet.s2c.S2CHandshakeRequestPacket
import moe.meowrealms.noir.network.sync.ModelSynchronizationContext
import org.bukkit.entity.Player

class ClientConnection (
    private val player: Player,
): PacketHandler {
    private lateinit var synchronizationContext: ModelSynchronizationContext
    private var handshakeConfirmed: Boolean = false

    fun onConnected() {
        this.synchronizationContext = ModelManager.createNewModelSynchronizationContext(this.player)

        NoirMain.instance.slF4JLogger.info("Sending handshake to player ${this.player.uniqueId}")
        this.send(S2CHandshakeRequestPacket(ClientConnectionManager.VERSION))
    }

    fun onDisconnected() {
        this.synchronizationContext.cleanup()
    }

    override fun receivingDirection(): EnumDirection {
        return EnumDirection.C_T_S
    }

    override fun sendingDirection(): EnumDirection {
        return EnumDirection.S_T_C
    }

    override fun send(packet: Packet) {
        ClientConnectionManager.sendMessage(this.player, packet)
    }

    override fun handleClientModelSyncPayload(packet: C2SModelDataPayload) {
        this.synchronizationContext.handleClientReply(packet.payload)
    }

    override fun handleHandshakeConfirmed(packet: C2SHandshakeConfirmedPacket) {
        this.handshakeConfirmed = true

        NoirMain.instance.slF4JLogger.info("Received handshake confirmed packet from player ${this.player.name}. Begin model synchronization")

        this.synchronizationContext.begin()
    }
}