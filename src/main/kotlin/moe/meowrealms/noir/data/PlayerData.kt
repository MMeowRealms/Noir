package moe.meowrealms.noir.data

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2FloatMap
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
import moe.meowrealms.noir.model.ModelManager

class PlayerData (
    public var selectedModelId: String = ModelManager.getDefaultModelConfig("default", "default").left,
    public var selectedModelTexture: String = ModelManager.getDefaultModelConfig("default", "default").right,
    public var mandatory: Boolean = false,
    public var disabled: Boolean = false,
    public var molangVariables: Int2ObjectMap<Object2FloatMap<String>> = Int2ObjectOpenHashMap()
){
}