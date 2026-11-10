package dev.thebjoredcraft.phantom.stacking

import dev.thebjoredcraft.phantom.fingerprint.Fingerprints
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object StackMerger {
    fun canMerge(a: ItemStack?, b: ItemStack?): Boolean =
        a != null && b != null && !a.type.isAir && a.type == b.type && Fingerprints.similarIgnoringFingerprint(
            a,
            b
        )

    fun merge(target: ItemStack, source: ItemStack): Int {
        if (!canMerge(target, source)) {
            return 0
        }

        val moved = minOf(target.maxStackSize - target.amount, source.amount)
        if (moved <= 0) {
            return 0
        }

        target.amount += moved
        source.amount -= moved

        Fingerprints.assign(target)

        if (source.amount > 0) {
            Fingerprints.assign(source)
        }

        return moved
    }

    fun insert(inv: Inventory, stack: ItemStack): ItemStack? {
        val remaining = stack.clone()

        for (slot in 0 until inv.size) {
            val existing = inv.getItem(slot) ?: continue

            if (merge(existing, remaining) > 0) {
                inv.setItem(slot, existing)
            }

            if (remaining.amount <= 0) {
                return null
            }
        }

        for (slot in 0 until inv.size) {
            if (inv.getItem(slot).let { it == null || it.type.isAir }) {
                val placed = remaining.clone()

                Fingerprints.assign(placed)
                inv.setItem(slot, placed)
                return null
            }
        }
        return remaining
    }
}