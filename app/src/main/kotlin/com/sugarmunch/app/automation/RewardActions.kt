package com.sugarmunch.app.automation

import com.sugarmunch.app.rewards.DailyRewardsManager
import com.sugarmunch.app.shop.ShopManager

/**
 * Reward-related automation actions execution logic.
 * Handles claiming rewards and adding Sugar Points.
 */

/**
 * Execute ClaimReward action
 * Claims the daily reward for the user
 */
suspend fun ActionExecutor.executeClaimReward(action: AutomationAction.ClaimRewardAction): ActionResult {
    return try {
        val rewardsManager = DailyRewardsManager.getInstance(context)
        val result = rewardsManager.claimDailyReward()

        when (result) {
            is DailyRewardsManager.ClaimResult.SUCCESS -> {
                AutomationEventBus.emitRewardClaimed("daily")
                ActionResult.Success("Claimed daily reward: ${result.sugarPoints} points")
            }
            DailyRewardsManager.ClaimResult.ALREADY_CLAIMED -> {
                ActionResult.Success("Reward already claimed today")
            }
            else -> ActionResult.Failure("Failed to claim reward")
        }
    } catch (e: Exception) {
        ActionResult.Failure(e.message ?: "Failed to claim reward")
    }
}

/**
 * Execute AddSugarPoints action
 * Adds the specified number of Sugar Points to the user's balance
 */
suspend fun ActionExecutor.executeAddSugarPoints(action: AutomationAction.AddSugarPointsAction): ActionResult {
    return try {
        val shopManager = ShopManager.getInstance(context)
        shopManager.addSugarPoints(action.points, action.reason)
        ActionResult.Success("Added ${action.points} Sugar Points")
    } catch (e: Exception) {
        ActionResult.Failure(e.message ?: "Failed to add points")
    }
}
