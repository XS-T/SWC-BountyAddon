package org.crewco.bounty.api

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.crewco.bounty.BountyAddon.Companion.bountyManager
import org.crewco.bounty.managers.BountyManager

import java.time.Duration

class BountyAPI:BountyInt {
    override fun getAllBounties(): List<BountyManager.Bounty> {
        return bountyManager.getActiveBounties()

    }

    override fun placeBounty(placer: Player, target: Player, reward: Double, duration: Duration): Boolean {
        return bountyManager.placeBounty(placer,target,reward,duration)
    }

    override fun completeBounty(hunter: Player, target: Player): Boolean {
        return bountyManager.completeBounty(hunter,target)
    }

    override fun getTrackingItem(target: Player): ItemStack {
        return bountyManager.getTargetPuck(target)
    }
}