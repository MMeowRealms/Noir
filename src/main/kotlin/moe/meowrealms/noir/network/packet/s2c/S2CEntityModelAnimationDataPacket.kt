package moe.meowrealms.noir.network.packet.s2c

import moe.meowrealms.noir.network.EnumDirection
import moe.meowrealms.noir.network.data.DispatchServerDrivenProperty
import moe.meowrealms.noir.network.packet.Packet
import moe.meowrealms.noir.network.packet.PacketHandler
import moe.meowrealms.noir.utils.SimpleFriendlyByteBuf

class S2CEntityModelAnimationDataPacket(
    var modelAnimationData: DispatchServerDrivenProperty
): Packet {
    override fun direction(): EnumDirection {
        return EnumDirection.S_T_C
    }

    override fun handle(handler: PacketHandler) {
        TODO("Not yet implemented on server side!")
    }

    override fun read(buffer: SimpleFriendlyByteBuf) {
        this.modelAnimationData = DispatchServerDrivenProperty.decode(buffer)
    }

    override fun write(buffer: SimpleFriendlyByteBuf) {
        DispatchServerDrivenProperty.encode(this.modelAnimationData, buffer)
    }
}