package moe.meowrealms.noir.data

import moe.meowrealms.noir.NoirMain
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

object PlayerDataStorage : Listener {
    private val loaded: MutableMap<UUID, PlayerData> = ConcurrentHashMap()

    fun init() {
        Bukkit.getPluginManager().registerEvents(this, NoirMain.instance)
    }

    @EventHandler
    fun onPlayerPreJoin(event: AsyncPlayerPreLoginEvent) {

    }

    @EventHandler
    fun onPlayerQuited(event: PlayerQuitEvent) {
        val removed = this.loaded.remove(event.player.uniqueId)

        if (removed != null) {
            // TODO
        }
    }

    private fun initializePlayerData(owner: Player, data: PlayerData) {
        data.validateAndCorrectModelSelection("default", "default")
        data.initSubComponents(owner)
    }

    @EventHandler
    fun onPlayerJoined(event: PlayerJoinEvent) {
        val shared = PlayerData()

       this.initializePlayerData(event.player, shared)

        this.loaded[event.player.uniqueId] = shared
    }

    fun Player.getNoirData() : PlayerData? {
        return loaded[this.uniqueId]
    }

    fun Player.saveNoirData() {
        TODO()
    }
}