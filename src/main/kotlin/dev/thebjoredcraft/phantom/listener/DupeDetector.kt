package dev.thebjoredcraft.phantom.listener

import dev.thebjoredcraft.phantom.PhantomConstants
import dev.thebjoredcraft.phantom.fingerprint.Fingerprints
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

object DupeDetector {
    fun findDuplicates(items: Iterable<ItemStack?>): Map<UUID, List<ItemStack>> =
        items.filterNotNull()
            .mapNotNull { item -> Fingerprints.get(item)?.let { it to item } }
            .groupBy({ it.first }, { it.second })
            .filterValues { it.size > 1 }

    fun alert(player: Player, id: UUID, stacks: List<ItemStack>) {
        val typeString = if (stacks.isEmpty()) {
            "UNKNOWN"
        } else {
            stacks.distinctBy { it.type }.joinToString { it.type.name.lowercase() }
        }

        Bukkit.getOnlinePlayers()
            .filter { it.hasPermission("phantom.alert") }
            .forEach {
                it.sendRichMessage("${PhantomConstants.PREFIX} Detected invalid <red>${stacks.size}x $typeString <white>on <red>${player.name}")
            }
    }
}