package org.crewco.bounty


import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority
import org.crewco.swccore.api.addon.AbstractAddon
import org.crewco.bounty.commands.BountyCommand
import org.crewco.bounty.listeners.BoardListener
import org.crewco.bounty.managers.BountyManager
import org.crewco.bounty.api.BountyAPI
import org.crewco.swccore.Startup
import org.crewco.swccore.api.addon.AddonConfig

/**
 * Bounty addon implementation.
 */
class BountyAddon(plugin: Plugin) : AbstractAddon(plugin) {


    companion object{
        lateinit var addon:BountyAddon
        lateinit var config:AddonConfig
        lateinit var sysMsg:String
        lateinit var bountyManager: BountyManager
        lateinit var bountyAPI: BountyAPI
    }
    override fun onLoad() {
        super.onLoad()
        logInfo("Bounty addon is loading...")

        // Initialize your addon's data here
        // Load configuration, setup data structures, etc.
        // Addon Instance

        addon = this
        config = AddonConfig(this)
        sysMsg = ChatColor.translateAlternateColorCodes('&',"&7[&cBounty&7]>")
        config.load()

        bountyManager = BountyManager(config)
        bountyManager.loadAndScheduleExpiryTasks()
        bountyManager.loadTrackingData()


    }

    override fun onEnable() {
        super.onEnable()

        // Init API
        bountyAPI = BountyAPI()

        // Register commands using the CommandManager (no plugin.yml needed!)
        registerCommand(
            name = "bounty",
            executor = BountyCommand(this),
            description = "bounty command",
            usage = "/bounty <subcommand>",
            aliases = listOf("bty"),
            tabCompleter = BountyCommand(this)
        )


        // Register all listeners in one call
        logInfo("Registering event listeners...")
        registerEvents(BoardListener(this),)
        logInfo("Event listeners registered!")

        try {
            logInfo("Registering API")
            this.plugin.server.servicesManager.register(BountyAPI::class.java, bountyAPI,this.plugin, ServicePriority.Normal)
            logInfo("Registered Bounty API")
        }catch (e:Exception){}

        logInfo("Bounty addon has been enabled!")
    }

    override fun onDisable() {
        super.onDisable()

        // Clean up resources
        // Save data, close connections, etc.

        // Persist tracking state to disk
        bountyManager.saveTrackingData()

        // Cancel all bounty expiry tasks
        bountyManager.expiryTasks.values.forEach { it.cancel() }
        bountyManager.expiryTasks.clear()

        // Cancel all tracking tasks
        bountyManager.trackingTasks.values.forEach { it.cancel() }
        bountyManager.trackingTasks.clear()

        // Close the database connection
        bountyManager.close()

        logInfo("Bounty addon has been disabled!")
    }

    override fun onReload() {
        super.onReload()

        // Reload configuration or data
        logInfo("Bounty addon has been reloaded!")
        config.reload()
        config.save()
    }
}