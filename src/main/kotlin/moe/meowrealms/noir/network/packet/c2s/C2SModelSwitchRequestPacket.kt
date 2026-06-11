package moe.meowrealms.noir.network.packet.c2s

import moe.meowrealms.noir.network.EnumDirection
import moe.meowrealms.noir.network.packet.Packet
import moe.meowrealms.noir.network.packet.PacketHandler
import moe.meowrealms.noir.utils.SimpleFriendlyByteBuf

class C2SModelSwitchRequestPacket(
    var modelId: String,
    var textureId: String
) : Packet{
    override fun direction(): EnumDirection {
        return EnumDirection.C_T_S
    }

    override fun handle(handler: PacketHandler) {
        handler.handleModelSwitchRequest(this)
    }

    override fun read(buffer: SimpleFriendlyByteBuf) {
        this.modelId = buffer.readUtf()
        this.textureId = buffer.readUtf()
    }

    override fun write(buffer: SimpleFriendlyByteBuf) {
        buffer.writeUtf(this.modelId)
        buffer.writeUtf(this.textureId)
    }
}