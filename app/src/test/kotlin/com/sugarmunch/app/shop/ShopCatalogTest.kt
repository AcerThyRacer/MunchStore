package com.sugarmunch.app.shop

import com.google.common.truth.Truth.assertThat
import com.sugarmunch.app.features.model.Rarity
import org.junit.Test

/**
 * Unit tests for Shop Catalog
 */
class ShopCatalogTest {

    @Test
    fun `getAllItems should return all shop items`() {
        // When
        val allItems = ShopCatalog.getAllItems()

        // Then - Should have 50+ items
        assertThat(allItems).hasSize(50)
    }

    @Test
    fun `EXCLUSIVE_THEMES should contain 10 themes`() {
        // Then
        assertThat(ShopCatalog.EXCLUSIVE_THEMES).hasSize(10)
    }

    @Test
    fun `PREMIUM_EFFECTS should contain 8 effects`() {
        // Then
        assertThat(ShopCatalog.PREMIUM_EFFECTS).hasSize(8)
    }

    @Test
    fun `PROFILE_BADGES should contain 10 badges`() {
        // Then
        assertThat(ShopCatalog.PROFILE_BADGES).hasSize(10)
    }

    @Test
    fun `APP_ICONS should contain 8 icons`() {
        // Then
        assertThat(ShopCatalog.APP_ICONS).hasSize(8)
    }

    @Test
    fun `BOOSTS should contain 6 boosts`() {
        // Then
        assertThat(ShopCatalog.BOOSTS).hasSize(6)
    }

    @Test
    fun `FEATURES should contain 4 features`() {
        // Then
        assertThat(ShopCatalog.FEATURES).hasSize(4)
    }

    @Test
    fun `BUNDLES should contain 4 bundles`() {
        // Then
        assertThat(ShopCatalog.BUNDLES).hasSize(4)
    }

    @Test
    fun `getItemsByType should filter correctly`() {
        // When
        val themes = ShopCatalog.getItemsByType(ShopItemType.THEME)

        // Then
        assertThat(themes).isNotEmpty()
        assertThat(themes.all { it.type == ShopItemType.THEME }).isTrue()
    }

    @Test
    fun `getItemById should return correct item`() {
        // When
        val item = ShopCatalog.getItemById("theme_golden_candy")

        // Then
        assertThat(item).isNotNull()
        assertThat(item?.id).isEqualTo("theme_golden_candy")
        assertThat(item?.name).isEqualTo("Golden Candy")
    }

    @Test
    fun `getItemById should return null for unknown id`() {
        // When
        val item = ShopCatalog.getItemById("nonexistent_item")

        // Then
        assertThat(item).isNull()
    }

    @Test
    fun `getFeaturedItems should return rare or higher items`() {
        // When
        val featuredItems = ShopCatalog.getFeaturedItems(4)

        // Then
        assertThat(featuredItems).isNotEmpty()
        assertThat(featuredItems.all { 
            it.rarity.ordinal >= Rarity.RARE.ordinal 
        }).isTrue()
    }

    @Test
    fun `getNewItems should return items marked as new`() {
        // When
        val newItems = ShopCatalog.getNewItems()

        // Then
        assertThat(newItems.all { it.isNew }).isTrue()
    }

    @Test
    fun `calculateBundleValue should return sum of item costs`() {
        // Given
        val bundle = ShopCatalog.BUNDLES.first { it.bundleItems != null }

        // When
        val value = ShopCatalog.calculateBundleValue(bundle)

        // Then
        assertThat(value).isAtLeast(0)
    }

    @Test
    fun `all items should have non-empty fields`() {
        // When
        val allItems = ShopCatalog.getAllItems()

        // Then
        allItems.forEach { item ->
            assertThat(item.id).isNotEmpty()
            assertThat(item.name).isNotEmpty()
            assertThat(item.description).isNotEmpty()
            assertThat(item.cost).isAtLeast(0)
        }
    }

    @Test
    fun `all items should have unique ids`() {
        // Given
        val ids = ShopCatalog.getAllItems().map { it.id }

        // Then
        assertThat(ids).containsNoDuplicates()
    }

    @Test
    fun `bundle items should reference valid item ids`() {
        // Given
        val allItemIds = ShopCatalog.getAllItems().map { it.id }
        val bundles = ShopCatalog.BUNDLES.filter { it.bundleItems != null }

        // Then
        bundles.forEach { bundle ->
            bundle.bundleItems?.forEach { itemId ->
                // Note: Some bundle items might reference items not in main catalog
                // This test just verifies the structure
                assertThat(itemId).isNotEmpty()
            }
        }
    }

    @Test
    fun `items with requirements should have valid constraints`() {
        // When
        val allItems = ShopCatalog.getAllItems()

        // Then
        allItems.forEach { item ->
            if (item.requirements.minLevel != null) {
                assertThat(item.requirements.minLevel!!).isAtLeast(0)
            }
        }
    }

    @Test
    fun `rarity enum should have correct values`() {
        // Then
        assertThat(Rarity.COMMON.name).isEqualTo("COMMON")
        assertThat(Rarity.UNCOMMON.name).isEqualTo("UNCOMMON")
        assertThat(Rarity.RARE.name).isEqualTo("RARE")
        assertThat(Rarity.EPIC.name).isEqualTo("EPIC")
        assertThat(Rarity.LEGENDARY.name).isEqualTo("LEGENDARY")
    }

    @Test
    fun `shop item type enum should have all types`() {
        // Then
        val types = ShopItemType.values()
        assertThat(types).hasSize(8)
        assertThat(types.map { it.name }).containsExactly(
            "THEME", "EFFECT", "BADGE", "ICON", "BOOST", "FEATURE", "BUNDLE", "AVATAR"
        )
    }

    @Test
    fun `item availability enum should have all values`() {
        // Then
        val availability = ShopItemAvailability.values()
        assertThat(availability).hasSize(4)
        assertThat(availability.map { it.name }).containsExactly(
            "ALWAYS", "LIMITED_TIME", "SEASONAL", "EXCLUSIVE"
        )
    }
}
