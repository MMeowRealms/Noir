package moe.meowrealms.noir.network.packet.c2s

import moe.meowrealms.noir.network.EnumDirection
import moe.meowrealms.noir.network.packet.Packet
import moe.meowrealms.noir.network.packet.PacketHandler
import moe.meowrealms.noir.utils.SimpleFriendlyByteBuf

class C2SStarModelPacket(
    var modelId: String,
    var add: Boolean
) : Packet{
    override fun direction(): EnumDirection {
        return EnumDirection.C_T_S
    }

    override fun handle(handler: PacketHandler) {
        handler.handleStarModel(this)
    }

    override fun read(buffer: SimpleFriendlyByteBuf) {
        this.modelId = buffer.readUtf()
        this.add = buffer.readBoolean()
    }

    override fun write(buffer: SimpleFriendlyByteBuf) {
        buffer.writeUtf(this.modelId)
        buffer.writeBoolean(this.add)
    }
}