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
    lateinit var owner: Player
    lateinit var animationData: DispatchServerDrivenProperty

    @Volatile
    private var dirty: Boolean = false

    fun initSubComponents(owner: Player) {
        this.owner = owner
        this.animationData = this.createModelState(this.owner)
    }

    fun markDirty() {
        this.dirty = true
    }

    fun validateAndCorrectModelSelection(defaultModelIdFallback: String, defaultTextureFallback: String) {
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

    fun validateModelSelection(): Boolean {
        val validateResult = ModelManager.validateSelectedModel(this.selectedModelId, this.selectedModelTexture)

        return validateResult.left && validateResult.right
    }


    fun createModelState(player: Player): DispatchServerDrivenProperty {
        val instanced = DispatchServerDrivenProperty(player.entityId)

        instanced.clear(player.entityId)

        return instanced
    }
}