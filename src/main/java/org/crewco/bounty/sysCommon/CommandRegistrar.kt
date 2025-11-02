package org.crewco.bounty.sysCommon

import org.bukkit.command.CommandExecutor
import org.bukkit.plugin.java.JavaPlugin
import kotlin.reflect.KClass

object CommandRegistrar {

    private lateinit var plugin: JavaPlugin

    fun initialize(plugin: JavaPlugin) {
        this.plugin = plugin
    }

    fun registerCommands(vararg commandClasses: KClass<out CommandExecutor>) {
        for (commandClass in commandClasses) {
            try {
                val instance = commandClass.java.newInstance()
                if (instance is CommandExecutor) {
                    val commandName = getCommandName(instance)
                    plugin.getCommand(commandName)?.setExecutor(instance)
                } else {
                    plugin.logger.warning("Class $commandClass does not implement CommandExecutor.")
                }
            } catch (e: Exception) {
                plugin.logger.warning("Error while registering command: ${e.message}")
            }
        }
    }

    private fun getCommandName(commandExecutor: CommandExecutor): String {
        // You can implement a logic to derive the command name from the executor class
        // For simplicity, let's assume the class name is the command name
        return commandExecutor.javaClass.simpleName
    }
}