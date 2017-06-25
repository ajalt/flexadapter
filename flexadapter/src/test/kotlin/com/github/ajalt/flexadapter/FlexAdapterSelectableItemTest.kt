package com.github.ajalt.flexadapter

import android.view.View
import org.assertj.core.api.Java6Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

class TestItem(val tag: String = "") : FlexAdapterExtensionItem(0) {
    override fun toString() = super.toString() + ":$tag"
    override fun bindItemView(itemView: View, position: Int) = Unit
}

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FlexAdapterSelectableItemTest {
    val adapter = FlexAdapter<Any>().apply {
        register<Int>(0)
        register<String>(0)
    }
    val tracker = adapter.selectionTracker()
    val stringItem = "selectable"
    val intItem = 3
    val intItem2 = 7
    val testItem = TestItem("regularItem")

    fun assertTrackedValues(vararg items: Any) {
        with(SoftAssertions()) {
            assertThat(tracker.selectedItemCount).isEqualTo(items.size)
            if (items.isEmpty()) {
                assertThat(tracker.selectedItems()).isEmpty()
            } else {
                assertThat(tracker.selectedItems()).containsOnly(*items)
                for (item in items) {
                    assertThat(tracker.isSelected(item))
                }
            }
            assertAll()
        }
    }

    @Test
    fun `selection count is zero when empty`() {
        assertTrackedValues()
    }

    @Test
    fun `selection count is zero when only unselected items are present`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        assertTrackedValues()
    }

    @Test
    fun `FlexAdapterItems can be selected`() {
        adapter.items.addAll(listOf(intItem, testItem))
        assertTrackedValues()

        tracker.selectItem(intItem)
        assertTrackedValues(intItem)
    }

    @Test
    fun `registered objects can be selected`() {
        adapter.items.addAll(listOf(stringItem, intItem))
        assertTrackedValues()

        tracker.selectItem(stringItem)
        assertTrackedValues(stringItem)
    }

    @Test
    fun `deselecting item updates count`() {
        adapter.items.addAll(listOf(intItem2, testItem))
        tracker.selectItem(intItem2)
        assertTrackedValues(intItem2)

        tracker.deselectItem(intItem2)
        assertTrackedValues()

        adapter.items.addAll(listOf(stringItem, intItem))
        tracker.selectItem(stringItem)
        assertTrackedValues(stringItem)

        tracker.deselectItem(stringItem)
        assertTrackedValues()
    }

    @Test
    fun `removing selected item updates count`() {
        adapter.items.add(intItem)
        tracker.selectItem(intItem)
        assertTrackedValues(intItem)
        tracker.deselectItem(intItem)
        assertTrackedValues()
    }

    @Test
    fun `deselectItem ignores removed items`() {
        adapter.items.addAll(listOf(intItem, intItem2))
        tracker.selectItem(intItem)
        tracker.selectItem(intItem2)
        adapter.items.removeAt(0)
        tracker.deselectItem(intItem)

        assertTrackedValues(intItem2)
    }

    @Test
    fun `selectAllItems works with no selected items`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        assertTrackedValues()
        tracker.selectAllItems()
        assertTrackedValues(testItem, intItem, stringItem)
    }

    @Test
    fun `selectAllItems works with mixed selected items`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        tracker.selectItem(testItem)
        assertTrackedValues(testItem)
        tracker.selectAllItems()
        assertTrackedValues(testItem, intItem, stringItem)
    }

    @Test
    fun `selectAllItems works with all selected items`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        tracker.selectItem(stringItem)
        assertTrackedValues(testItem, intItem, stringItem)
        tracker.selectAllItems()
        assertTrackedValues(testItem, intItem, stringItem)
    }

    @Test
    fun `selectItem works after selectAllItems`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        tracker.selectAllItems()
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        tracker.selectItem(stringItem)
        assertTrackedValues(testItem, intItem, stringItem)
        tracker.selectAllItems()
        assertTrackedValues(testItem, intItem, stringItem)
    }

    @Test
    fun `deselectItem works after selectAllItems`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        tracker.selectAllItems()
        tracker.deselectItem(testItem)
        tracker.deselectItem(intItem)
        assertTrackedValues(stringItem)
        tracker.deselectItem(intItem)
        assertTrackedValues(stringItem)
    }

    @Test
    fun `deselectAllItems works with no selected items`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        assertTrackedValues()

        tracker.deselectAllItems()
        assertTrackedValues()
    }

    @Test
    fun `deselectAllItems works with mixed selected items`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        tracker.selectItem(testItem)
        assertTrackedValues(testItem)
        tracker.deselectAllItems()
        assertTrackedValues()
    }

    @Test
    fun `deselectAllItems works with all selected items`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        tracker.selectItem(stringItem)
        assertTrackedValues(testItem, intItem, stringItem)
        tracker.deselectAllItems()
        assertTrackedValues()
    }

    @Test
    fun `selectItem works after deselectAllItems`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        tracker.selectItem(stringItem)
        tracker.deselectAllItems()
        tracker.selectItem(intItem)

        assertTrackedValues(intItem)
        tracker.selectItem(stringItem)
        assertTrackedValues(intItem, stringItem)
    }

    @Test
    fun `deselectItem works after deselectAllItems`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        tracker.selectItem(stringItem)
        tracker.deselectAllItems()
        tracker.deselectItem(testItem)
        tracker.deselectItem(intItem)
        tracker.deselectItem(stringItem)
        tracker.selectItem(stringItem)
        assertTrackedValues(stringItem)
        tracker.deselectItem(stringItem)
        assertTrackedValues()
    }

    @Test
    fun `toggling selection repeatedly does not affect adapter`() {
        adapter.items.addAll(listOf(testItem, intItem))
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        assertTrackedValues(testItem, intItem)
        tracker.selectItem(testItem)
        tracker.selectItem(testItem)
        assertTrackedValues(testItem, intItem)
        tracker.deselectItem(testItem)
        tracker.deselectItem(intItem)
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        assertTrackedValues(testItem, intItem)
        tracker.deselectItem(testItem)
        tracker.deselectItem(intItem)
        assertTrackedValues()
        tracker.selectAllItems()
        assertTrackedValues(testItem, intItem)
        tracker.deselectAllItems()
        assertTrackedValues()
        tracker.selectAllItems()
        assertTrackedValues(testItem, intItem)
    }

    @Test
    fun `adding items after selectItem does not affect selection`() {
        adapter.items.addAll(listOf(testItem, intItem))
        tracker.selectItem(testItem)
        adapter.items.add(0, stringItem)
        assertTrackedValues(testItem)
    }

    @Test
    fun `adding items after selectAll does not affect selection`() {
        adapter.items.addAll(listOf(testItem, intItem))
        tracker.selectAllItems()
        tracker.deselectItem(intItem)
        assertTrackedValues(testItem)
        adapter.items.add(0, stringItem)
        assertTrackedValues(testItem)
    }

    @Test
    fun `adding items after deselectAll does not affect selection`() {
        adapter.items.addAll(listOf(testItem, intItem))
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        tracker.deselectAllItems()
        tracker.selectItem(intItem)
        adapter.items.add(0, stringItem)
        assertTrackedValues(intItem)
    }

    @Test
    fun `removing item after selectItem deselects it`() {
        adapter.items.addAll(listOf(testItem, intItem))
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        adapter.items.removeAt(0)
        assertTrackedValues(intItem)
    }

    @Test
    fun `removing multiple items after selectItem deselects them`() {
        adapter.items.addAll(listOf(testItem, intItem, intItem2, stringItem))
        tracker.selectItem(testItem)
        tracker.selectItem(stringItem)
        tracker.selectItem(intItem)
        tracker.selectItem(intItem2)
        tracker.deselectItem(intItem)
        adapter.items.removeAll(listOf(intItem, intItem2))
        assertTrackedValues(testItem, stringItem)
    }

    @Test
    fun `removing all items after selectItem deselects them`() {
        adapter.items.addAll(listOf(testItem, intItem))
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        adapter.items.clear()
        assertTrackedValues()
    }

    @Test
    fun `removing item after selectAll deselects it`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        tracker.selectAllItems()
        tracker.deselectItem(intItem)
        adapter.items.removeAt(0)
        assertTrackedValues(stringItem)
    }

    @Test
    fun `removing multiple items after selectAll deselects them`() {
        adapter.items.addAll(listOf(testItem, intItem, intItem2, stringItem))
        tracker.selectAllItems()
        tracker.deselectItem(intItem)
        adapter.items.removeAll(listOf(intItem, intItem2))
        assertTrackedValues(testItem, stringItem)
    }

    @Test
    fun `removing all items after selectAll deselects them`() {
        adapter.items.addAll(listOf(testItem, intItem))
        tracker.selectAllItems()
        adapter.items.clear()
        assertTrackedValues()
    }

    @Test
    fun `removing item after deselectItem deselects it`() {
        adapter.items.addAll(listOf(testItem, intItem))
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        tracker.deselectItem(testItem)
        adapter.items.removeAt(0)
        assertTrackedValues(intItem)
    }

    @Test
    fun `removing all items after deselectItem deselects them`() {
        adapter.items.addAll(listOf(testItem, intItem))
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        tracker.deselectItem(intItem)
        adapter.items.clear()
        assertTrackedValues()
    }

    @Test
    fun `removing item after deselectAll deselects it`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        tracker.deselectAllItems()
        tracker.deselectItem(intItem)
        adapter.items.removeAt(0)
        assertTrackedValues()
    }
    @Test
    fun `removing multiple items after deselectAll deselects them`() {
        adapter.items.addAll(listOf(testItem, intItem, intItem2, stringItem))
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        tracker.selectItem(intItem2)
        tracker.deselectAllItems()
        tracker.selectItem(testItem)
        tracker.deselectItem(intItem)
        tracker.selectItem(intItem2)
        adapter.items.removeAll(listOf(intItem, intItem2))
        assertTrackedValues(testItem)
    }
    @Test
    fun `removing all items after deselectAll deselects them`() {
        adapter.items.addAll(listOf(testItem, intItem))
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        adapter.items.clear()
        assertTrackedValues()
    }
}
