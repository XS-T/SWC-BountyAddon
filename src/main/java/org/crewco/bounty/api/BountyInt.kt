package org.crewco.bounty.api

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.crewco.bounty.managers.BountyManager
import java.time.Duration

interface BountyInt {
    fun getAllBounties(): List<BountyManager.Bounty>
    fun placeBounty(placer: Player, target: Player, reward: Double, duration: Duration): Boolean
    fun completeBounty(hunter: Player, target: Player): Boolean
    fun getTrackingItem(target: Player): ItemStack
}