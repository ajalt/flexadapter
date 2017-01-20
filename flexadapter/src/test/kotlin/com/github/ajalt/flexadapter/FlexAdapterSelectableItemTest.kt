package com.github.ajalt.flexadapter

import android.view.View
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

class TestRegularItem : FlexAdapterExtensionItem(0) {
    override fun bindItemView(itemView: View, position: Int) {
    }
}

class TestSelectableItem : FlexAdapterSelectableExtensionItem(0) {
    override fun bindItemView(itemView: View, position: Int) {
    }
}

@RunWith(RobolectricTestRunner::class)
class FlexAdapterSelectableItemTest {
    val adapter = FlexAdapter()

    @Test
    fun `selectable items can be mixed with regular items`() {
        val regularItem = TestRegularItem()
        val selectableItem = TestSelectableItem()
        adapter.resetItems(listOf(regularItem, selectableItem))
        assertThat(adapter.items()).containsExactly(regularItem, selectableItem)
    }

    @Test
    fun `selection count is zero when empty`() {
        assertThat(adapter.selectedItemCount).isEqualTo(0)
        assertThat(adapter.selectedItems()).isEmpty()
    }


    @Test
    fun `selection count is zero when only regular items are present`() {
        val regularItem = TestRegularItem()
        adapter.addItem(regularItem)
        assertThat(adapter.selectedItemCount).isEqualTo(0)
        assertThat(adapter.selectedItems()).isEmpty()
    }

    @Test
    fun `selection count is zero when only unselected items are present`() {
        val regularItem = TestRegularItem()
        val selectableItem = TestSelectableItem()
        adapter.resetItems(listOf(regularItem, selectableItem))
        assertThat(adapter.selectedItemCount).isEqualTo(0)
        assertThat(adapter.selectedItems()).isEmpty()
    }


    @Test
    fun `adding selected item updates count`() {
        val item = TestSelectableItem()
        item.selected = true
        assertThat(adapter.selectedItemCount).isEqualTo(0)
        assertThat(adapter.selectedItems()).isEmpty()
        adapter.addItem(item)
        assertThat(adapter.selectedItemCount).isEqualTo(1)
        assertThat(adapter.selectedItems()).containsExactly(item)
    }

    @Test
    fun `selecting item updates count`() {
        val item = TestSelectableItem()
        adapter.addItem(item)
        assertThat(adapter.selectedItemCount).isEqualTo(0)
        assertThat(adapter.selectedItems()).isEmpty()
        item.selected = true
        assertThat(adapter.selectedItemCount).isEqualTo(1)
        assertThat(adapter.selectedItems()).containsExactly(item)
    }

    @Test
    fun `deselecting item updates count`() {
        val item = TestSelectableItem()
        adapter.addItem(item)
        item.selected = true
        assertThat(adapter.selectedItemCount).isEqualTo(1)
        assertThat(adapter.selectedItems()).containsExactly(item)
        item.selected = false
        assertThat(adapter.selectedItemCount).isEqualTo(0)
        assertThat(adapter.selectedItems()).isEmpty()
    }

    @Test
    fun `selectAllItems works with no selected items`() {
        val regularItem = TestRegularItem()
        val selectableItem1 = TestSelectableItem()
        val selectableItem2 = TestSelectableItem()
        adapter.resetItems(listOf(regularItem, selectableItem1, selectableItem2))
        assertThat(adapter.selectedItemCount).isEqualTo(0)
        adapter.selectAllItems()
        assertThat(adapter.selectedItemCount).isEqualTo(2)
        assertThat(adapter.selectedItems()).containsExactlyInAnyOrder(selectableItem1, selectableItem2)
    }

    @Test
    fun `selectAllItems works with mixed selected items`() {
        val regularItem = TestRegularItem()
        val selectableItem1 = TestSelectableItem()
        val selectableItem2 = TestSelectableItem()
        selectableItem1.selected = true
        adapter.resetItems(listOf(regularItem, selectableItem1, selectableItem2))
        assertThat(adapter.selectedItemCount).isEqualTo(1)
        adapter.selectAllItems()
        assertThat(adapter.selectedItemCount).isEqualTo(2)
        assertThat(adapter.selectedItems()).containsExactlyInAnyOrder(selectableItem1, selectableItem2)
    }

    @Test
    fun `selectAllItems works with all selected items`() {
        val regularItem = TestRegularItem()
        val selectableItem1 = TestSelectableItem()
        val selectableItem2 = TestSelectableItem()
        selectableItem1.selected = true
        selectableItem2.selected = true
        adapter.resetItems(listOf(regularItem, selectableItem1, selectableItem2))
        assertThat(adapter.selectedItemCount).isEqualTo(2)
        adapter.selectAllItems()
        assertThat(adapter.selectedItemCount).isEqualTo(2)
        assertThat(adapter.selectedItems()).containsExactlyInAnyOrder(selectableItem1, selectableItem2)
    }

    @Test
    fun `deselectAllItems works with no selected items`() {
        val regularItem = TestRegularItem()
        val selectableItem1 = TestSelectableItem()
        val selectableItem2 = TestSelectableItem()
        adapter.resetItems(listOf(regularItem, selectableItem1, selectableItem2))
        assertThat(adapter.selectedItemCount).isEqualTo(0)
        adapter.deselectAllItems()
        assertThat(adapter.selectedItemCount).isEqualTo(0)
        assertThat(adapter.selectedItems()).isEmpty()
    }

    @Test
    fun `deselectAllItems works with mixed selected items`() {
        val regularItem = TestRegularItem()
        val selectableItem1 = TestSelectableItem()
        val selectableItem2 = TestSelectableItem()
        selectableItem1.selected = true
        adapter.resetItems(listOf(regularItem, selectableItem1, selectableItem2))
        assertThat(adapter.selectedItemCount).isEqualTo(1)
        adapter.deselectAllItems()
        assertThat(adapter.selectedItemCount).isEqualTo(0)
        assertThat(adapter.selectedItems()).isEmpty()
    }

    @Test
    fun `deselectAllItems works with all selected items`() {
        val regularItem = TestRegularItem()
        val selectableItem1 = TestSelectableItem()
        val selectableItem2 = TestSelectableItem()
        selectableItem1.selected = true
        selectableItem2.selected = true
        adapter.resetItems(listOf(regularItem, selectableItem1, selectableItem2))
        assertThat(adapter.selectedItemCount).isEqualTo(2)
        adapter.deselectAllItems()
        assertThat(adapter.selectedItemCount).isEqualTo(0)
        assertThat(adapter.selectedItems()).isEmpty()
    }
}
