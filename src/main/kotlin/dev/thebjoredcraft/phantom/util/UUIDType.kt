package dev.thebjoredcraft.phantom.util

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import java.util.*

object UUIDType : PersistentDataType<String, UUID> {
    override fun getPrimitiveType(): Class<String> = String::class.java
    override fun getComplexType(): Class<UUID> = UUID::class.java

    override fun toPrimitive(
        complex: UUID,
        context: PersistentDataAdapterContext
    ): String = complex.toString()

    override fun fromPrimitive(
        primitive: String,
        context: PersistentDataAdapterContext
    ): UUID = UUID.fromString(primitive)
}