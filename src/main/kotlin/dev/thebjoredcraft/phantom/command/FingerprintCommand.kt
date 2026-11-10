package dev.thebjoredcraft.phantom.command

import com.mojang.brigadier.Command
import dev.thebjoredcraft.phantom.PhantomConstants
import dev.thebjoredcraft.phantom.fingerprint.Fingerprints
import dev.thebjoredcraft.phantom.plugin
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.entity.Player

object FingerprintCommand {
    fun register() =
        plugin.lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            event.registrar().register(
                Commands.literal("fingerprint")
                    .requires { it.sender.hasPermission("phantom.fingerprint") }
                    .then(Commands.literal("dump").executes { context ->
                        val player = context.source.sender as? Player
                            ?: return@executes Command.SINGLE_SUCCESS

                        val fingerprint = Fingerprints.get(player.inventory.itemInMainHand)

                        player.sendRichMessage("${PhantomConstants.PREFIX} Item Fingerprint: <red>${fingerprint ?: "NONE"}")
                        Command.SINGLE_SUCCESS
                    })
                    .build()
            )
        }
}