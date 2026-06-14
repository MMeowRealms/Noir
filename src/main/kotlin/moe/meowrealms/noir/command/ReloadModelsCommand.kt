package moe.meowrealms.noir.command

import moe.meowrealms.noir.NoirConstants
import moe.meowrealms.noir.NoirMain
import moe.meowrealms.noir.model.ModelManager
import moe.meowrealms.noir.network.ClientConnectionManager
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ReloadModelsCommand : Command(
    "reloadmodels",
    "Reload Noir YSM models",
    "/reloadmodels",
    emptyList()
) {
    init {
        this.permission = NoirConstants.PermissionConstants.RELOAD_MODELS_COMMAND
    }

    override fun execute(
        sender: CommandSender,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (!sender.hasPermission(NoirConstants.PermissionConstants.RELOAD_MODELS_COMMAND)) {
            this.sendI18n(sender, NoirConstants.LanguageConstants.NO_PERMISSION)
            return true
        }

        this.sendI18n(sender, NoirConstants.LanguageConstants.RELOAD_MODELS_STARTED)

        NoirMain.instance.morePaperLib.scheduling().asyncScheduler().run(Runnable {
            val loadBegin = System.nanoTime()

            try {
                ModelManager.loadModels()

                val timeElapsed = (System.nanoTime() - loadBegin) / 1_000_000L
                val loadedCount = ModelManager.getLoadedModelCount()

                ClientConnectionManager.restartModelSynchronizationForConnectedPlayers()

                this.sendI18n(
                    sender,
                    NoirConstants.LanguageConstants.RELOAD_MODELS_SUCCESS,
                    listOf("count", "time_ms"),
                    listOf(loadedCount, timeElapsed)
                )

                NoirMain.instance.slF4JLogger.info("Reloaded models by command. Currently has $loadedCount models loaded.")
            } catch (throwable: Throwable) {
                NoirMain.instance.slF4JLogger.error("Failed to reload models by command!", throwable)

                this.sendI18n(
                    sender,
                    NoirConstants.LanguageConstants.RELOAD_MODELS_FAILED,
                    listOf("reason"),
                    listOf(throwable.message ?: throwable.javaClass.simpleName)
                )
            }
        })

        return true
    }

    override fun tabComplete(
        sender: CommandSender,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        return mutableListOf()
    }

    private fun sendI18n(
        sender: CommandSender,
        key: String,
        subKeys: List<String> = emptyList(),
        args: List<Any> = emptyList()
    ) {
        val message = NoirMain.languageManager.i18n(key, subKeys, args)

        sender.sendMessage(message)
    }
}
