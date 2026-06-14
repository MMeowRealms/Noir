package moe.meowrealms.noir

object NoirConstants {
    object ModelSyncConstants {
        // defaults, overridden by config.yml at startup
        @Volatile var PER_PLAYER_RATE_LIMIT_MBPS = 8.0
        @Volatile var GLOBAL_RATE_LIMIT_MBPS = 40.0
        const val MAX_CHUNK_BYTES = 30 * 1024
    }

    object ModelDefaults {
        // defaults, overridden by config.yml at startup
        @Volatile var DEFAULT_MODEL_ID = "default"
        @Volatile var DEFAULT_MODEL_TEXTURE = "default"
    }

    object PermissionConstants {
        const val NOIR_COMMAND = "noir.command"
        const val RELOAD_MODELS_COMMAND = "noir.command.reload"
        const val SET_MODEL_COMMAND = "noir.command.setmodel"
    }

    object LanguageConstants {
        const val NO_PERMISSION = "noir.command.no_permission"
        const val COMMAND_HELP = "noir.command.help"
        const val COMMAND_UNKNOWN_SUBCOMMAND = "noir.command.unknown_subcommand"
        const val RELOAD_MODELS_STARTED = "noir.reload_models.started"
        const val RELOAD_MODELS_SUCCESS = "noir.reload_models.success"
        const val RELOAD_MODELS_FAILED = "noir.reload_models.failed"
        const val SET_MODEL_USAGE = "noir.set_model.usage"
        const val SET_MODEL_PLAYER_NOT_FOUND = "noir.set_model.player_not_found"
        const val SET_MODEL_NOT_FOUND = "noir.set_model.model_not_found"
        const val SET_MODEL_TEXTURE_NOT_FOUND = "noir.set_model.texture_not_found"
        const val SET_MODEL_PLAYER_NO_DATA = "noir.set_model.player_no_data"
        const val SET_MODEL_SUCCESS = "noir.set_model.success"
    }
}
