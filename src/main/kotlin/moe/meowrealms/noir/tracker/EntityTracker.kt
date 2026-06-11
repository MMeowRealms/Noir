package moe.meowrealms.noir.tracker

import io.papermc.paper.event.player.PlayerTrackEntityEvent
import io.papermc.paper.event.player.PlayerUntrackEntityEvent
import moe.meowrealms.noir.NoirMain
import moe.meowrealms.noir.network.ClientConnectionManager.getYsmConnection
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object EntityTracker : Listener{
    private val trackerVisibleMap: MutableMap<UUID, MutableSet<UUID>> = ConcurrentHashMap<UUID, MutableSet<UUID>>()

    fun init() {
        Bukkit.getPluginManager().registerEvents(this, NoirMain.instance)
    }

    @EventHandler
    fun onPlayerTrackEntity(trackEvent: PlayerTrackEntityEvent) {
        val tracker = trackEvent.getPlayer()
        val tracked = trackEvent.entity

        if (tracked is Player) {
            val visibleList = this.trackerVisibleMap.computeIfAbsent(tracker.uniqueId) { HashSet() }

            if (visibleList.add(tracked.uniqueId)) {
                this.handlePairingAdd(tracker, tracked)
            }
        }
    }

    fun getVisible(of: Player): Set<Player> {
        val visibleList = this.trackerVisibleMap[of.uniqueId]

        if (visibleList != null) {
            val result = mutableSetOf<Player>()

            for (uuid in visibleList) {
                val target = Bukkit.getPlayer(uuid)

                if (target != null) {
                    result.add(target)
                }
            }

            return result
        }

        return emptySet()
    }

    fun handlePairingAdd(owner: Player, watched: Player) {
        watched.getYsmConnection().syncModelFullDataTo(owner)
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        this.trackerVisibleMap.remove(event.player.uniqueId)
    }

    @EventHandler
    fun onPlayerUntrackEntity(untrackEvent: PlayerUntrackEntityEvent) {
        val watcher = untrackEvent.getPlayer()
        val untracked = untrackEvent.entity

        if (untracked is Player) {
            val visibleList = this.trackerVisibleMap[watcher.uniqueId] ?: return

            visibleList.remove(untracked.uniqueId)
        }
    }

}