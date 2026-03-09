package com.sugarmunch.app.bounties

/**
 * Dev-to-user bounty: developers offer rewards for actions (e.g. find bug, write review).
 */
data class Bounty(
    val id: String,
    val appId: String,
    val type: BountyType,
    val rewardAmount: Int,
    val title: String,
    val description: String,
    val creatorId: String? = null,
    val isClaimed: Boolean = false
)

enum class BountyType {
    FIND_BUG,
    REVIEW_5_STAR,
    SHARE_APP,
    COMPLETE_TUTORIAL
}
