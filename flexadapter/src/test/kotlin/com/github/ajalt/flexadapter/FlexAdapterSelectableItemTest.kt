package com.github.ajalt.flexadapter

import android.view.View
import org.assertj.core.api.Java6Assertions.assertThat
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

    @Test
    fun `selection count is zero when empty`() {
        assertThat(tracker.selectedItemCount).isEqualTo(0)
        assertThat(tracker.selectedItems()).isEmpty()
    }

    @Test
    fun `selection count is zero when only unselected items are present`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        assertThat(tracker.selectedItemCount).isEqualTo(0)
        assertThat(tracker.selectedItems()).isEmpty()
    }

    @Test
    fun `FlexAdapterSelectableItems can be selected`() {
        adapter.items.addAll(listOf(intItem, testItem))
        assertThat(tracker.selectedItemCount).isEqualTo(0)
        assertThat(tracker.selectedItems()).isEmpty()

        tracker.selectItem(intItem)
        assertThat(tracker.selectedItems()).containsExactly(intItem)
    }

    @Test
    fun `registered objects can be selected`() {
        adapter.items.addAll(listOf(stringItem, intItem))
        assertThat(tracker.selectedItems()).isEmpty()

        tracker.selectItem(stringItem)
        assertThat(tracker.selectedItems()).containsExactly(stringItem)
    }

    @Test
    fun `deselecting item updates count`() {
        adapter.items.addAll(listOf(intItem2, testItem))
        tracker.selectItem(intItem2)
        assertThat(tracker.selectedItemCount).isEqualTo(1)
        assertThat(tracker.selectedItems()).containsExactly(intItem2)

        tracker.deselectItem(intItem2)
        assertThat(tracker.selectedItemCount).isEqualTo(0)
        assertThat(tracker.selectedItems()).isEmpty()

        adapter.items.addAll(listOf(stringItem, intItem))
        tracker.selectItem(stringItem)
        assertThat(tracker.selectedItems()).containsExactly(stringItem)

        tracker.deselectItem(stringItem)
        assertThat(tracker.selectedItemCount).isEqualTo(0)
        assertThat(tracker.selectedItems()).isEmpty()
    }

    @Test
    fun `removing selected item updates count`() {
        adapter.items.add(intItem)
        tracker.selectItem(intItem)
        assertThat(tracker.selectedItems()).containsExactly(intItem)
        tracker.deselectItem(intItem)
        assertThat(tracker.selectedItems()).isEmpty()
    }

    @Test
    fun `deselectItem ignores removed items`() {
        adapter.items.addAll(listOf(intItem, intItem2))
        tracker.selectItem(intItem)
        tracker.selectItem(intItem2)
        adapter.items.removeAt(0)
        tracker.deselectItem(intItem)

        assertThat(tracker.selectedItems()).containsExactly(intItem2)
    }

    @Test
    fun `selectAllItems works with no selected items`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        assertThat(tracker.selectedItems()).isEmpty()
        tracker.selectAllItems()
        assertThat(tracker.selectedItems()).containsExactlyInAnyOrder(testItem, intItem, stringItem)
    }

    @Test
    fun `selectAllItems works with mixed selected items`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        tracker.selectItem(testItem)
        assertThat(tracker.selectedItems()).containsExactly(testItem)
        tracker.selectAllItems()
        assertThat(tracker.selectedItems()).containsExactlyInAnyOrder(testItem, intItem, stringItem)
    }

    @Test
    fun `selectAllItems works with all selected items`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        tracker.selectItem(stringItem)
        assertThat(tracker.selectedItems()).containsExactlyInAnyOrder(testItem, intItem, stringItem)
        tracker.selectAllItems()
        assertThat(tracker.selectedItems()).containsExactlyInAnyOrder(testItem, intItem, stringItem)
    }

    @Test
    fun `deselectAllItems works with no selected items`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        assertThat(tracker.selectedItems()).isEmpty()

        tracker.deselectAllItems()
        assertThat(tracker.selectedItems()).isEmpty()
    }

    @Test
    fun `deselectAllItems works with mixed selected items`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        tracker.selectItem(testItem)
        assertThat(tracker.selectedItems()).containsExactly(testItem)
        tracker.deselectAllItems()
        assertThat(tracker.selectedItems()).isEmpty()
    }

    @Test
    fun `deselectAllItems works with all selected items`() {
        adapter.items.addAll(listOf(testItem, intItem, stringItem))
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        tracker.selectItem(stringItem)
        assertThat(tracker.selectedItems()).containsExactlyInAnyOrder(testItem, intItem, stringItem)
        tracker.deselectAllItems()
        assertThat(tracker.selectedItems()).isEmpty()
    }

    @Test
    fun `toggling selection repeatedly does not affect adapter`() {
        adapter.items.addAll(listOf(testItem, intItem))
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        assertThat(tracker.selectedItems()).containsExactlyInAnyOrder(testItem, intItem)
        tracker.selectItem(testItem)
        tracker.selectItem(testItem)
        assertThat(tracker.selectedItems()).containsExactlyInAnyOrder(testItem, intItem)
        tracker.deselectItem(testItem)
        tracker.deselectItem(intItem)
        tracker.selectItem(testItem)
        tracker.selectItem(intItem)
        assertThat(tracker.selectedItems()).containsExactlyInAnyOrder(testItem, intItem)
        tracker.deselectItem(testItem)
        tracker.deselectItem(intItem)
        assertThat(tracker.selectedItems()).isEmpty()
    }
}
