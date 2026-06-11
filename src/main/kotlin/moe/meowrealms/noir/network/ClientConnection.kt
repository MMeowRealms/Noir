package moe.meowrealms.noir.network

import moe.meowrealms.noir.NoirMain
import moe.meowrealms.noir.data.PlayerDataStorage.getNoirData
import moe.meowrealms.noir.model.ModelManager
import moe.meowrealms.noir.network.ClientConnectionManager.getYsmConnection
import moe.meowrealms.noir.network.packet.Packet
import moe.meowrealms.noir.network.packet.PacketHandler
import moe.meowrealms.noir.network.packet.c2s.C2SAnimationRequestPacket
import moe.meowrealms.noir.network.packet.c2s.C2SHandshakeConfirmedPacket
import moe.meowrealms.noir.network.packet.c2s.C2SModelDataPayload
import moe.meowrealms.noir.network.packet.c2s.C2SModelSwitchRequestPacket
import moe.meowrealms.noir.network.packet.s2c.S2CEntityModelAnimationDataPacket
import moe.meowrealms.noir.network.packet.s2c.S2CEntityModelSelectionDataPacket
import moe.meowrealms.noir.network.packet.s2c.S2CHandshakeRequestPacket
import moe.meowrealms.noir.network.sync.ModelSynchronizationContext
import moe.meowrealms.noir.tracker.EntityTracker
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

    fun syncModelSelectionDataTo(player: Player) {
        val playerData = this.player.getNoirData() ?: return

        player.getYsmConnection().send(S2CEntityModelSelectionDataPacket(
            this.player.entityId,
            playerData.selectedModelId,
            playerData.selectedModelTexture,
            playerData.disabled,
            playerData.animationData
        ))
    }

    fun syncModelAnimationDataTo(player: Player) {
        val playerData = this.player.getNoirData() ?: return

        player.getYsmConnection().send(S2CEntityModelAnimationDataPacket(
            playerData.animationData
        ))
    }

    fun syncModelFullDataTo(player: Player) {
        this.syncModelSelectionDataTo(player)
        this.syncModelAnimationDataTo(player)
    }

    fun syncModelFullData() {
        this.syncModelFullDataTo(this.player)

        for (visible in EntityTracker.getVisible(this.player)) {
            this.syncModelFullDataTo(visible)
        }
    }

    fun syncModelSelectionData() {
        this.syncModelSelectionDataTo(this.player)

        for (visible in EntityTracker.getVisible(this.player)) {
            this.syncModelSelectionDataTo(visible)
        }
    }

    fun syncModelAnimationData() {
        this.syncModelAnimationDataTo(this.player)

        for (visible in EntityTracker.getVisible(this.player)) {
            this.syncModelAnimationDataTo(visible)
        }
    }

    fun handleHandshakeCallback() {
        this.synchronizationContext.begin()

        // sync to ourselves
        this.syncModelSelectionDataTo(this.player)
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

        this.handleHandshakeCallback()
    }

    override fun handleModelSwitchRequest(packet: C2SModelSwitchRequestPacket) {
        val validate = ModelManager.validateSelectedModel(packet.modelId, packet.textureId)

        if (!validate.left || !validate.right) {
            NoirMain.instance.slF4JLogger.warn("Player ${this.player.name} is trying switching to an unknown model ${packet.modelId} with texture ${packet.textureId}!")
            return
        }

        val playerData = this.player.getNoirData() ?: return

        playerData.selectedModelId = packet.modelId
        playerData.selectedModelTexture = packet.textureId

        playerData.markDirty()

        this.syncModelSelectionData()
    }

    override fun handleAnimationRequest(packet: C2SAnimationRequestPacket) {
        val playerData = this.player.getNoirData() ?: return

        if (packet.entityId != -1) {
            // TODO - This is for TLS, not ours to process
            return
        }

        if (packet.animationIndex == -1) {
            playerData.animationData.extraAnimation("")

            this.syncModelAnimationData()
            return
        }

        val selectionValidated = playerData.validateModelSelection()
        if (!selectionValidated) {
            NoirMain.instance.slF4JLogger.warn("Player ${this.player.name} is trying playing animations with an invalid model selection ${playerData.selectedModelId} - ${playerData.selectedModelTexture}!")
            return
        }

        val animationLookup = ModelManager.lookupAnimationFromPacket(playerData.selectedModelId, packet.animationIndex, packet.category)
        if (animationLookup == null) {
            NoirMain.instance.slF4JLogger.warn("Player ${this.player.name} is trying playing an invalid animation ${packet.category} - ${packet.animationIndex}!")
            return
        }

        playerData.animationData.extraAnimation(animationLookup)
        this.syncModelAnimationData()
    }
}