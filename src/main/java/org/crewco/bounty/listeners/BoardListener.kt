package org.crewco.bounty.listeners

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.crewco.bounty.BountyAddon
import org.crewco.bounty.BountyAddon.Companion.bountyManager
import org.crewco.swccore.Startup

class BoardListener(private val addon: BountyAddon) : Listener {

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        val player = e.whoClicked as? Player ?: return
        val view = e.view
        val title = view.title

        if (!title.startsWith("§6Bounties Page")) return
        e.isCancelled = true

        val clicked = e.currentItem ?: return
        val name = clicked.itemMeta?.displayName ?: return

        when {
            name.contains("Previous Page") -> {
                val currentPage = title.removePrefix("Bounties Page ").toIntOrNull() ?: 1
                bountyManager.openBountyBoard(player, currentPage - 1)
            }

            name.contains("Next Page") -> {
                val currentPage = title.removePrefix("Bounties Page ").toIntOrNull() ?: 1
                bountyManager.openBountyBoard(player, currentPage + 1)
            }

            name.contains("→") && player.hasPermission("swcb.admin.bounty.claim") -> {
                val lore = clicked.itemMeta?.lore ?: return
                val idLine = lore.find { it.contains("ID:") } ?: return
                val id = idLine.removePrefix("§8ID: ").toIntOrNull() ?: return

                val targetBounty = bountyManager.getActiveBounties().find { it.id == id } ?: return
                val target = Bukkit.getPlayer(targetBounty.target) ?: return

                if (bountyManager.completeBounty(player, target)) {
                    Bukkit.getOnlinePlayers().forEach {
                        it.sendMessage("§a[Admin] ${player.name} has forcefully claimed the bounty on ${target.name}.")
                    }
                    player.closeInventory()
                } else {
                    player.sendMessage("§cBounty no longer available.")
                }
            }
        }
    }
}