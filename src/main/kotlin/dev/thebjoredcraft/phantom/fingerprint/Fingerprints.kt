package dev.thebjoredcraft.phantom.fingerprint

import dev.thebjoredcraft.phantom.PhantomConstants
import dev.thebjoredcraft.phantom.util.UUIDType
import org.bukkit.inventory.ItemStack
import java.util.*

object Fingerprints {
    private val key get() = PhantomConstants.fingerprintKey

    fun isTracked(item: ItemStack?): Boolean =
        item != null && !item.type.isAir

    fun get(item: ItemStack?): UUID? {
        if (item == null || item.type.isAir || !item.hasItemMeta()) {
            return null
        }

        val raw =
            item.itemMeta.persistentDataContainer.get(key, UUIDType) ?: return null

        return runCatching { UUID.fromString(raw) }.getOrNull()
    }

    fun assign(item: ItemStack?): UUID {
        val id = UUID.randomUUID()
        item?.editMeta {
            it.persistentDataContainer.set(
                key,
                UUIDType,
                id
            )
        }

        return id
    }

    fun ensure(item: ItemStack?): UUID? {
        if (!isTracked(item)) {
            return null
        }

        return get(item) ?: assign(item)
    }

    private fun stripped(item: ItemStack) = item.asOne().apply {
        editMeta { it.persistentDataContainer.remove(key) }
    }

    fun similarIgnoringFingerprint(a: ItemStack, b: ItemStack) =
        a.type == b.type && stripped(a).isSimilar(stripped(b))
}