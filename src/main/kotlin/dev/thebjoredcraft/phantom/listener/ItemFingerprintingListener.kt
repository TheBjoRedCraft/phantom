package dev.thebjoredcraft.phantom.listener

import dev.thebjoredcraft.phantom.fingerprint.Fingerprints
import dev.thebjoredcraft.phantom.plugin
import dev.thebjoredcraft.phantom.stacking.StackMerger
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent

object ItemFingerprintingListener : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val current = event.currentItem
        val cursor = event.cursor

        if (Fingerprints.isTracked(current)) {
            Fingerprints.ensure(current)
        }

        if (Fingerprints.isTracked(cursor)) {
            Fingerprints.ensure(cursor)
        }

        val action = event.action
        if (action in setOf(
                InventoryAction.PLACE_ALL,
                InventoryAction.PLACE_SOME,
                InventoryAction.PLACE_ONE,
                InventoryAction.SWAP_WITH_CURSOR
            ) && StackMerger.canMerge(current, cursor)
        ) {
            event.isCancelled = true

            val source = if (action == InventoryAction.PLACE_ONE) {
                cursor.asOne()
            } else {
                cursor
            }

            val moved = StackMerger.merge(current!!, source)

            if (action == InventoryAction.PLACE_ONE) {
                cursor.amount -= moved
            }

            event.clickedInventory?.setItem(event.slot, current)
            player.setItemOnCursor(if (cursor.amount > 0) cursor else null)

            player.scheduler.run(plugin, {
                player.updateInventory()
            }, {})

            return
        }

        player.scheduler.run(plugin, {
            reconcile(player, action)
        }, {})
    }

    @EventHandler(ignoreCancelled = true)
    fun onPickup(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return

        event.isCancelled = true

        val entityItem = event.item
        val stack = entityItem.itemStack
        val remaining = StackMerger.insert(player.inventory, stack)

        val pickedUp = stack.amount - (remaining?.amount ?: 0)
        if (pickedUp <= 0) {
            return
        }

        player.playPickupItemAnimation(entityItem, pickedUp)

        if (remaining == null) {
            entityItem.remove()
        } else {
            entityItem.itemStack = remaining
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onHopperMove(event: InventoryMoveItemEvent) {
        Fingerprints.ensure(event.item)
    }

    @EventHandler(ignoreCancelled = true)
    fun onDrag(event: InventoryDragEvent) {
        val cursor = event.oldCursor

        if (Fingerprints.isTracked(cursor)) {
            event.newItems.values.forEach { Fingerprints.assign(it) }
        }
    }

    private val splitActions = setOf(
        InventoryAction.PICKUP_HALF, InventoryAction.PLACE_ONE,
        InventoryAction.PLACE_SOME, InventoryAction.DROP_ONE_SLOT,
        InventoryAction.DROP_ONE_CURSOR
    )

    private fun reconcile(player: Player, action: InventoryAction) {
        DupeDetector.findDuplicates(player.inventory.contents.toList() + player.itemOnCursor)
            .forEach { (id, stacks) ->
                if (action in splitActions) {
                    stacks.drop(1).forEach { Fingerprints.assign(it) }
                    player.updateInventory()
                } else {
                    DupeDetector.alert(player, id, stacks)
                }
            }
    }
}