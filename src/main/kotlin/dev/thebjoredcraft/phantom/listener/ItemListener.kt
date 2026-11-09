package dev.thebjoredcraft.phantom.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent

object ItemListener : Listener {
    @EventHandler
    fun onItemStack(event: InventoryClickEvent) {
        val click = event.click
        val action = event.action
        val cursor = event.cursor
        val currentItem = event.currentItem

        if (action == InventoryAction.PLACE_ALL ||
            action == InventoryAction.PLACE_SOME ||
            action == InventoryAction.PLACE_ONE
        ) {
            if (cursor.type == currentItem?.type) {
                println("Player tried to place item on existing stack")
            }
        }

        if (click == ClickType.SHIFT_LEFT || click == ClickType.SHIFT_RIGHT) {
            println("Player tried to move item stack")
        }
    }
}