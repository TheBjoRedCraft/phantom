package dev.thebjoredcraft.phantom

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.thebjoredcraft.phantom.listener.ItemListener
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

val plugin get() = JavaPlugin.getPlugin(PhantomMain::class.java)

class PhantomMain : SuspendingJavaPlugin() {
    override suspend fun onEnableAsync() {
        Bukkit.getPluginManager().registerEvents(ItemListener, this)
    }
}