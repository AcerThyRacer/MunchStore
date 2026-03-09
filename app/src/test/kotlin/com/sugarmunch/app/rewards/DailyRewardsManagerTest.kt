package com.sugarmunch.app.rewards

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for Daily Rewards System
 */
class DailyRewardsManagerTest {

    @Test
    fun `REWARD_TIERS should contain 30 days of rewards`() {
        // Then
        assertThat(DailyRewardsManager.REWARD_TIERS).hasSize(30)
    }

    @Test
    fun `day 7 reward should be LEGENDARY`() {
        // When
        val day7Reward = DailyRewardsManager.REWARD_TIERS.find { it.day == 7 }

        // Then
        assertThat(day7Reward).isNotNull()
        assertThat(day7Reward?.type).isEqualTo(RewardType.LEGENDARY)
        assertThat(day7Reward?.isMilestone).isTrue()
    }

    @Test
    fun `day 14 reward should be LEGENDARY`() {
        // When
        val day14Reward = DailyRewardsManager.REWARD_TIERS.find { it.day == 14 }

        // Then
        assertThat(day14Reward).isNotNull()
        assertThat(day14Reward?.type).isEqualTo(RewardType.LEGENDARY)
        assertThat(day14Reward?.isMilestone).isTrue()
    }

    @Test
    fun `day 30 reward should be ULTIMATE`() {
        // When
        val day30Reward = DailyRewardsManager.REWARD_TIERS.find { it.day == 30 }

        // Then
        assertThat(day30Reward).isNotNull()
        assertThat(day30Reward?.type).isEqualTo(RewardType.ULTIMATE)
        assertThat(day30Reward?.isMilestone).isTrue()
    }

    @Test
    fun `rewards should escalate in value`() {
        // Given
        val rewards = DailyRewardsManager.REWARD_TIERS

        // Then - Day 30 should have higher rewards than day 1
        assertThat(rewards.find { it.day == 30 }?.sugarPoints)
            .isGreaterThan(rewards.find { it.day == 1 }?.sugarPoints!!)
        assertThat(rewards.find { it.day == 30 }?.xp)
            .isGreaterThan(rewards.find { it.day == 1 }?.xp!!)
    }

    @Test
    fun `STREAK_RESET_HOURS should be 48 hours`() {
        // Then
        assertThat(DailyRewardsManager.STREAK_RESET_HOURS).isEqualTo(48)
    }

    @Test
    fun `BONUS_DAY_INTERVAL should be 7 days`() {
        // Then
        assertThat(DailyRewardsManager.BONUS_DAY_INTERVAL).isEqualTo(7)
    }

    @Test
    fun `DayReward should have correct data`() {
        // Given
        val reward = DayReward(
            day = 1,
            sugarPoints = 50,
            xp = 10,
            type = RewardType.COMMON,
            title = "Welcome Back"
        )

        // Then
        assertThat(reward.day).isEqualTo(1)
        assertThat(reward.sugarPoints).isEqualTo(50)
        assertThat(reward.xp).isEqualTo(10)
        assertThat(reward.type).isEqualTo(RewardType.COMMON)
        assertThat(reward.title).isEqualTo("Welcome Back")
    }

    @Test
    fun `RewardType enum should have all tiers`() {
        // Then
        val types = RewardType.values()
        assertThat(types).hasSize(6)
        assertThat(types.map { it.name }).containsExactly(
            "COMMON", "UNCOMMON", "RARE", "EPIC", "LEGENDARY", "ULTIMATE"
        )
    }

    @Test
    fun `StreakStatus sealed class should have all types`() {
        // Then
        val claimAvailable = StreakStatus.CLAIM_AVAILABLE(5)
        val alreadyClaimed = StreakStatus.ALREADY_CLAIMED(5)
        val atRisk = StreakStatus.AT_RISK(5, 24)

        // Verify types
        assertThat(claimAvailable).isInstanceOf(StreakStatus.CLAIM_AVAILABLE::class.java)
        assertThat(alreadyClaimed).isInstanceOf(StreakStatus.ALREADY_CLAIMED::class.java)
        assertThat(atRisk).isInstanceOf(StreakStatus.AT_RISK::class.java)
    }

    @Test
    fun `ClaimResult sealed class should have all types`() {
        // Then
        val success = ClaimResult.SUCCESS(1, DayReward(1, 50, 10, RewardType.COMMON, "Test"), 50, 10, 1.0f)
        val alreadyClaimed = ClaimResult.ALREADY_CLAIMED
        val streakBroken = ClaimResult.STREAK_BROKEN

        // Verify types
        assertThat(success).isInstanceOf(ClaimResult.SUCCESS::class.java)
        assertThat(alreadyClaimed).isInstanceOf(ClaimResult.ALREADY_CLAIMED::class.java)
        assertThat(streakBroken).isInstanceOf(ClaimResult.STREAK_BROKEN::class.java)
    }

    @Test
    fun `CalendarDay should have correct data`() {
        // Given
        val calendarDay = CalendarDay(
            day = 15,
            date = System.currentTimeMillis(),
            isToday = true,
            isClaimed = false,
            isMissed = false
        )

        // Then
        assertThat(calendarDay.day).isEqualTo(15)
        assertThat(calendarDay.isToday).isTrue()
        assertThat(calendarDay.isClaimed).isFalse()
        assertThat(calendarDay.isMissed).isFalse()
    }

    @Test
    fun `ClaimHistoryEntry should have correct data`() {
        // Given
        val entry = ClaimHistoryEntry(
            date = System.currentTimeMillis(),
            dayNumber = 7,
            sugarPoints = 500,
            xp = 100
        )

        // Then
        assertThat(entry.dayNumber).isEqualTo(7)
        assertThat(entry.sugarPoints).isEqualTo(500)
        assertThat(entry.xp).isEqualTo(100)
    }

    @Test
    fun `milestone days should have correct sugar points`() {
        // Then
        assertThat(DailyRewardsManager.REWARD_TIERS.find { it.day == 7 }?.sugarPoints).isEqualTo(500)
        assertThat(DailyRewardsManager.REWARD_TIERS.find { it.day == 14 }?.sugarPoints).isEqualTo(750)
        assertThat(DailyRewardsManager.REWARD_TIERS.find { it.day == 30 }?.sugarPoints).isEqualTo(1000)
    }

    @Test
    fun `all reward days should be sequential from 1 to 30`() {
        // Given
        val days = DailyRewardsManager.REWARD_TIERS.map { it.day }

        // Then
        assertThat(days).containsExactlyElementsIn(1..30)
    }
}
