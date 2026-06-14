package moe.meowrealms.noir

object NoirConstants {
    object ModelSyncConstants {
        // defaults, overridden by config.yml at startup
        @Volatile var PER_PLAYER_RATE_LIMIT_MBPS = 8.0
        @Volatile var GLOBAL_RATE_LIMIT_MBPS = 40.0
        const val MAX_CHUNK_BYTES = 30 * 1024
    }

    object PermissionConstants {
        const val RELOAD_MODELS_COMMAND = "noir.commands.reloadmodels"
    }

    object LanguageConstants {
        const val NO_PERMISSION = "noir.command.no_permission"
        const val RELOAD_MODELS_STARTED = "noir.reload_models.started"
        const val RELOAD_MODELS_SUCCESS = "noir.reload_models.success"
        const val RELOAD_MODELS_FAILED = "noir.reload_models.failed"
    }
}
