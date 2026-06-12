package moe.meowrealms.noir

import moe.meowrealms.noir.data.PlayerDataStorage
import moe.meowrealms.noir.model.ModelManager
import moe.meowrealms.noir.network.packet.PacketRegistry
import moe.meowrealms.noir.network.ClientConnectionManager
import moe.meowrealms.noir.network.data.DispatchServerDrivenProperty
import moe.meowrealms.noir.network.packet.c2s.C2SAnimationRequestPacket
import moe.meowrealms.noir.network.packet.c2s.C2SHandshakeConfirmedPacket
import moe.meowrealms.noir.network.packet.c2s.C2SModelDataPayload
import moe.meowrealms.noir.network.packet.c2s.C2SModelSwitchRequestPacket
import moe.meowrealms.noir.network.packet.c2s.C2SMolangExecuteRequestPacket
import moe.meowrealms.noir.network.packet.c2s.C2SStarModelPacket
import moe.meowrealms.noir.network.packet.s2c.S2CAuthModelListPacket
import moe.meowrealms.noir.network.packet.s2c.S2CEntityModelAnimationDataPacket
import moe.meowrealms.noir.network.packet.s2c.S2CEntityModelSelectionDataPacket
import moe.meowrealms.noir.network.packet.s2c.S2CHandshakeRequestPacket
import moe.meowrealms.noir.network.packet.s2c.S2CModelDataPayloadPacket
import moe.meowrealms.noir.network.packet.s2c.S2CMolangExecutePacket
import moe.meowrealms.noir.network.packet.s2c.S2CStarModelListPacket
import moe.meowrealms.noir.tracker.EntityTracker
import org.bukkit.plugin.java.JavaPlugin

class NoirMain : JavaPlugin() {

    companion object {
        lateinit var instance: NoirMain
        lateinit var packetRegistry: PacketRegistry
    }

    fun registerPackets() {
        packetRegistry.register(51, S2CHandshakeRequestPacket::class) { S2CHandshakeRequestPacket() }
        packetRegistry.register(52, C2SHandshakeConfirmedPacket::class) { C2SHandshakeConfirmedPacket() }

        packetRegistry.register(1, S2CModelDataPayloadPacket::class) { S2CModelDataPayloadPacket(ByteArray(0)) }
        packetRegistry.register(2, C2SModelDataPayload::class) { C2SModelDataPayload(ByteArray(0)) }

        packetRegistry.register(4, S2CEntityModelSelectionDataPacket::class) {
            S2CEntityModelSelectionDataPacket(
                0,
                "",
                "",
                false,
                DispatchServerDrivenProperty(0)
            )
        }
        packetRegistry.register(21, S2CEntityModelAnimationDataPacket::class) {
            S2CEntityModelAnimationDataPacket(
                DispatchServerDrivenProperty(0)
            )
        }

        packetRegistry.register(3, S2CMolangExecutePacket::class) { S2CMolangExecutePacket(IntArray(0), "") }
        packetRegistry.register(5, C2SModelSwitchRequestPacket::class) { C2SModelSwitchRequestPacket("", "") }
        packetRegistry.register(7, C2SAnimationRequestPacket::class) { C2SAnimationRequestPacket(0, "", 0) }
        packetRegistry.register(6, S2CAuthModelListPacket::class) { S2CAuthModelListPacket(HashSet()) }
        packetRegistry.register(8, S2CStarModelListPacket::class) { S2CStarModelListPacket(HashSet()) }
        packetRegistry.register(9, C2SStarModelPacket::class) { C2SStarModelPacket("", true) }
        packetRegistry.register(17, C2SMolangExecuteRequestPacket::class) { C2SMolangExecuteRequestPacket("", 0) }
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

    fun initDataStorage() {
        PlayerDataStorage.init()

        this.slF4JLogger.info("Data storage initialized.")
    }

    fun initEntityTracker() {
        EntityTracker.init()

        this.slF4JLogger.info("Entity tracker initialized.")
    }

    override fun onEnable() {
        instance = this

        this.initNetworking()
        this.initAndLoadModels()
        this.initDataStorage()
        this.initEntityTracker()
    }

    override fun onDisable() {
    }
}
