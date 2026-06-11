package moe.meowrealms.noir.data

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2FloatMap
import moe.meowrealms.noir.model.ModelManager
import moe.meowrealms.noir.network.data.DispatchServerDrivenProperty
import org.bukkit.entity.Player

class PlayerData (
    public var selectedModelId: String = ModelManager.getDefaultModelConfig("default", "default").left,
    public var selectedModelTexture: String = ModelManager.getDefaultModelConfig("default", "default").right,
    public var mandatory: Boolean = false,
    public var disabled: Boolean = false,
    public var molangVariables: Int2ObjectMap<Object2FloatMap<String>> = Int2ObjectOpenHashMap()
){
    fun validateModelSelection(defaultModelIdFallback: String, defaultTextureFallback: String) {
        val validateResult = ModelManager.validateSelectedModel(this.selectedModelId, this.selectedModelTexture)
        val defaultModelConfig = ModelManager.getDefaultModelConfig(defaultModelIdFallback, defaultTextureFallback)

        // model not found
        if (!validateResult.left) {
            this.selectedModelId = defaultModelConfig.left
            this.selectedModelTexture = defaultModelConfig.right

        // else: texture not found
        } else if (!validateResult.right) {
            this.selectedModelTexture = defaultModelConfig.right
        }
    }

    fun createModelState(player: Player): DispatchServerDrivenProperty {
        val instanced = DispatchServerDrivenProperty(player.entityId)

        instanced.clear(player.entityId)

        return instanced
    }
}