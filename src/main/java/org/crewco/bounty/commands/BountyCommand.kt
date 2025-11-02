package org.crewco.bounty.commands

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.crewco.bounty.BountyAddon
import org.crewco.bounty.BountyAddon.Companion.bountyManager
import org.crewco.bounty.BountyAddon.Companion.config
import org.crewco.bounty.BountyAddon.Companion.sysMsg
import org.crewco.swccore.Startup.Companion.economy
import java.time.Duration
import java.time.Instant
import java.util.*

class BountyCommand(private val addon: BountyAddon) : CommandExecutor, TabCompleter {

    override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): Boolean {
        if (args.isEmpty()) {
            showHelp(sender)
            return true
        }

        when (args[0].lowercase()) {
            "help" -> showHelp(sender)
            "info" -> showInfo(sender)
            "version" -> showVersion(sender)
            "reload" -> reload(sender)
            "place" -> placeBounty(sender,args)
            "remove" -> removeBounty(sender,args)
            "track" -> trackBounty(sender,args)
            "board" -> boardHelper(sender, args)
            "claim" -> claimHelper(sender, args)
            else -> {
                sender.sendMessage("§cUnknown subcommand: ${args[0]}")
                sender.sendMessage("§7Use §e/bounty help")
            }
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String>? {
        if (args.size == 1) {
            return listOf("help", "info", "version", "reload","place","remove","track","board","claim")
                .filter { it.startsWith(args[0].lowercase()) }
        }
        return null
    }


    // Place bounty command
    private fun placeBounty(sender:CommandSender,args:Array<String>){

        // Constants
        val placeCooldowns = mutableMapOf<UUID, Long>()
        val PLACE_COOLDOWN_SECONDS = 60L // 60 seconds cooldown

        if (sender is Player) {
            // Checks permission
            if (!sender.hasPermission("swcb.bounty.place")) {sender.sendMessage("$sysMsg You don't have permission to run this command"); return}
            val player = sender

            // Base System setup
            val uuid = player.uniqueId
            val now = System.currentTimeMillis()
            val lastUsed = placeCooldowns[uuid]?: 0L

            // Cool down Check
            if ((now - lastUsed) < PLACE_COOLDOWN_SECONDS * 1000){
                val secondsLeft = ((PLACE_COOLDOWN_SECONDS * 1000 - (now - lastUsed)) / 1000).toInt()
                player.sendMessage("$sysMsg You must wait $secondsLeft seconds before placing another bounty.")
                return
            }

            // Target and Rewards Constant
            val target = Bukkit.getPlayer(args.getOrNull(1) ?: return) ?: return
            val reward = args.getOrNull(2)?.toDouble() ?: return

            if (economy.getBalance(player) < reward) {
                player.sendMessage("$sysMsg You cannot place this bounty because you don't have enough money.")
                return
            }

            if (reward < config.getInt("bounty-amount").toDouble()) {
                player.sendMessage("$sysMsg The amount you set is less than the default amount of ${config.getInt("bounty-amount").toDouble()}")
                return
            }

            val durationStr = args.getOrNull(3)
            val defaultExpiryStr = config.getString("default-expiry", "1d")!!
            val maxExpiryStr = config.getString("max-expiry", "3d")!!

            val defaultDuration = parseTimeToDuration(defaultExpiryStr)
            val maxDuration = parseTimeToDuration(maxExpiryStr)
            val requestedDuration = durationStr?.let { parseTimeToDuration(it) } ?: defaultDuration

            if (requestedDuration == null || maxDuration == null) {
                player.sendMessage("$sysMsg Invalid duration format. Use formats like 1d, 2h30m, 45m, etc.")
                return
            }

            val safeDuration = requestedDuration.coerceAtMost(maxDuration)

            bountyManager.placeBounty(player, target, reward, safeDuration)
            economy.withdrawPlayer(player, reward)
            placeCooldowns[uuid] = now // ✅ set cooldown

            val formattedDuration = formatDuration(safeDuration)
            player.sendMessage("$sysMsg Placed bounty on §e${target.name} §afor §6$$reward§a expiring in §b$formattedDuration")

            for (onlinePlayer in Bukkit.getOnlinePlayers()) {
                if (onlinePlayer != player) {
                    onlinePlayer.sendMessage("$sysMsg ${player.name} §7has placed a bounty on §c${target.name} §7for §6$$reward §7expiring in §b$formattedDuration")
                }
            }

        }
    }

    // Remove bounty
    private fun removeBounty(sender: CommandSender,args: Array<String>){
        if (!sender.hasPermission("swcb.bounty.remove")) {sender.sendMessage("$sysMsg You don't have permission to run this command"); return}
        if (sender is Player){
            val target = Bukkit.getPlayer(args.getOrNull(1) ?: return ) ?: return
            val success = bountyManager.removeBounty(sender, target)
            if (success) {
                sender.sendMessage("$sysMsg Removed bounty on ${target.name}.")
                Bukkit.broadcastMessage("$sysMsg The bounty on §c${target.name}§7 has been removed.")
            } else {
                sender.sendMessage("$sysMsg You do not have permission to remove this bounty or it does not exist.")
            }
        }
    }

    private fun trackBounty(sender: CommandSender, args: Array<String>) {
        if (!sender.hasPermission("swcb.bounty.track")) {sender.sendMessage("$sysMsg You don't have permission to run this command"); return}
        if (sender is Player) {
            val target = Bukkit.getPlayer(args.getOrNull(1) ?: return) ?: return
            if (bountyManager.hasBounty(target)) {
                sender.inventory.addItem(bountyManager.getTargetPuck(target))
                bountyManager.startTracking(sender, target)
                sender.sendMessage("${sysMsg} Tracking puck added for ${target.name} and started tracking.")
            }
        }
    }

    private fun randomBounty(sender:CommandSender,args: Array<String>){
        if (sender is Player){
            if (!sender.isOp || !sender.hasPermission("swcb.bounty.admin.random")) {sender.sendMessage("$sysMsg You don't have permission to run this command"); return}
            val reward = args.getOrNull(1)?.toDouble() ?: 100.0
            val defaultDuration = parseTimeToDuration(addon.plugin.config.getString("default-expiry", "1d")!!) ?: Duration.ofDays(1)
            val bounty = bountyManager.generateRandomBounty(sender, reward, defaultDuration)
            if (bounty != null) {
                val formatted = formatDuration(Duration.between(Instant.now(), bounty.expires))
                Bukkit.broadcastMessage("$sysMsg Random bounty placed on §c${Bukkit.getOfflinePlayer(bounty.target).name} §afor §6$$reward §aexpiring in §b$formatted")
            } else {
                sender.sendMessage("$sysMsg No valid targets online.")
            }
        }
    }

    private fun boardHelper(sender:CommandSender,args: Array<String>){
        if (sender is Player){
            if (!sender.hasPermission("swcb.bounty.board")) {sender.sendMessage("$sysMsg You don't have permission to run this command"); return}
            val page = args.getOrNull(1)?.toIntOrNull()?.coerceAtLeast(1) ?: 1
            bountyManager.openBountyBoard(sender, page)
        }
    }


    private fun claimHelper(sender: CommandSender,args: Array<String>){
        if (sender is Player){
            if (!sender.hasPermission("swcb.admin.bounty.claim")) return
            val target = Bukkit.getPlayer(args.getOrNull(1) ?: return ) ?: return
            val success = bountyManager.completeBounty(sender, target)
            if (success) {
                Bukkit.broadcastMessage("$sysMsg an Admin §7${sender.name} has force-claimed the bounty on §c${target.name}§7.")
            } else {
                sender.sendMessage("$sysMsg No active bounty found for ${target.name}, or it already expired.")
            }
        }
    }






    // =================== Addon Information Commands ============= \\
    private fun showHelp(sender: CommandSender) {
        sender.sendMessage("§e=== Bounty Addon Help ===")
        sender.sendMessage("§7/bounty help §f- Show this help")
        sender.sendMessage("§7/bounty info §f- Show addon info")
        sender.sendMessage("§7/bounty version §f- Show version")
        sender.sendMessage("§7/bounty reload §f- Reload addon")
        sender.sendMessage("§6=== Bounty Commands ===")
        sender.sendMessage("§6/bounty place <player> <amount> [duration]")
        sender.sendMessage("§6/bounty remove <player>")
        sender.sendMessage("§6/bounty track <player>")
        sender.sendMessage("§6/bounty board")
        sender.sendMessage("§6/bounty claim <player>")
        sender.sendMessage("§6Duration format: §f1d2h, 30m, 1h15m, etc.")
    }

    private fun showInfo(sender: CommandSender) {
        sender.sendMessage("§e=== ${addon.name} ===")
        sender.sendMessage("§7${addon.description}")
        sender.sendMessage("§7By: ${addon.authors.joinToString(", ")}")
    }

    private fun showVersion(sender: CommandSender) {
        sender.sendMessage("§e${addon.name} §7v${addon.version}")
    }

    private fun reload(sender: CommandSender) {
        if (!sender.hasPermission("bountyaddon.reload")) {
            sender.sendMessage("§cYou don't have permission!")
            return
        }

        addon.onReload()
        sender.sendMessage("§aAddon reloaded!")
    }


    // Helper functions

    private fun parseTimeToDuration(input: String): Duration? {
        val regex = Regex("""(?:(\d+)d)?\s*(?:(\d+)h)?\s*(?:(\d+)m)?\s*(?:(\d+)s)?""", RegexOption.IGNORE_CASE)
        val match = regex.matchEntire(input.trim()) ?: return null

        val (d, h, m, s) = match.destructured
        return Duration.ofDays(d.toLongOrNull() ?: 0) +
                Duration.ofHours(h.toLongOrNull() ?: 0) +
                Duration.ofMinutes(m.toLongOrNull() ?: 0) +
                Duration.ofSeconds(s.toLongOrNull() ?: 0)
    }

    private fun formatDuration(duration: Duration): String {
        val days = duration.toDays()
        val hours = duration.toHours() % 24
        val minutes = duration.toMinutes() % 60
        val seconds = duration.seconds % 60

        return buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes}m ")
            if (seconds > 0 || isBlank()) append("${seconds}s")
        }.trim()
    }
}