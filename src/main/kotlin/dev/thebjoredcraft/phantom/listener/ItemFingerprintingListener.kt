package dev.thebjoredcraft.phantom.listener

import dev.thebjoredcraft.phantom.fingerprint.Fingerprints
import dev.thebjoredcraft.phantom.plugin
import dev.thebjoredcraft.phantom.stacking.StackMerger
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.*

object ItemFingerprintingListener : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val current = event.currentItem
        val cursor = event.cursor
        val action = event.action

        if (Fingerprints.isTracked(current)) {
            Fingerprints.ensure(current)
        }

        if (Fingerprints.isTracked(cursor)) {
            Fingerprints.ensure(cursor)
        }

        if (action == InventoryAction.COLLECT_TO_CURSOR && !cursor.type.isAir) {
            event.isCancelled = true

            val topInv = event.view.topInventory
            val allInventories = listOfNotNull(topInv, player.inventory)

            for (targetInv in allInventories) {
                for (slot in 0 until targetInv.size) {
                    if (cursor.amount >= cursor.maxStackSize) break
                    val item = targetInv.getItem(slot) ?: continue
                    if (item == cursor) continue

                    if (StackMerger.canMerge(cursor, item)) {
                        StackMerger.merge(cursor, item)
                        if (item.amount <= 0) {
                            targetInv.setItem(slot, null)
                        } else {
                            targetInv.setItem(slot, item)
                        }
                    }
                }
            }

            player.setItemOnCursor(cursor)
            player.scheduler.run(plugin, {
                player.updateInventory()
            }, {})

            return
        }

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

        if (action == InventoryAction.PICKUP_HALF && current != null && !current.type.isAir) {
            event.isCancelled = true

            val halfAmount = (current.amount + 1) / 2
            val remainingAmount = current.amount - halfAmount

            if (remainingAmount > 0) {
                current.amount = remainingAmount
            } else {
                event.clickedInventory?.setItem(event.slot, null)
            }

            val splitStack = current.clone().apply {
                amount = halfAmount
            }
            Fingerprints.assign(splitStack)

            player.setItemOnCursor(splitStack)
            event.clickedInventory?.setItem(event.slot, if (current.amount > 0) current else null)

            player.scheduler.run(plugin, {
                player.updateInventory()
            }, {})

            return
        }

        player.scheduler.run(plugin, {
            reconcile(player, action)
        }, {})
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPrepareCraft(event: PrepareItemCraftEvent) {
        val result = event.recipe?.result ?: return
        if (Fingerprints.isTracked(result)) {
            Fingerprints.assign(result)
            event.inventory.result = result
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onCraft(event: CraftItemEvent) {
        val result = event.inventory.result
        if (result != null && Fingerprints.isTracked(result)) {
            Fingerprints.assign(result)
            event.inventory.result = result
        }
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
        InventoryAction.PLACE_ONE,
        InventoryAction.PLACE_SOME,
        InventoryAction.DROP_ONE_SLOT,
        InventoryAction.DROP_ONE_CURSOR,
        InventoryAction.MOVE_TO_OTHER_INVENTORY
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