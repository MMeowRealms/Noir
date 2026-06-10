package moe.meowrealms.noir.network.packet

import moe.meowrealms.noir.network.EnumDirection
import moe.meowrealms.noir.utils.SimpleFriendlyByteBuf

interface Packet {
    fun direction() : EnumDirection

    fun handle(handler: PacketHandler)

    fun read(buffer: SimpleFriendlyByteBuf)

    fun write(buffer: SimpleFriendlyByteBuf)
}