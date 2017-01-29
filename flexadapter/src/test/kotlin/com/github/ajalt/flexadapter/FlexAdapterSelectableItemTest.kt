package com.github.ajalt.flexadapter

import android.view.View
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

class TestRegularItem(val tag: String = "") : FlexAdapterExtensionItem(0) {
    override fun toString() = super.toString() + ":$tag"
    override fun bindItemView(itemView: View, position: Int) = Unit
}

class TestSelectableItem(val tag: String = "") : FlexAdapterSelectableExtensionItem(0) {
    override fun toString() = super.toString() + ":$tag"
    override fun bindItemView(itemView: View, position: Int) = Unit
}

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
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
    fun `removing selected item updates count`() {
        val item = TestSelectableItem()
        adapter.addItem(item)
        item.selected = true
        assertThat(adapter.selectedItemCount).isEqualTo(1)
        assertThat(adapter.selectedItems()).containsExactly(item)
        adapter.removeItem(0)
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

    @Test
    fun `toggling selection of removed item does not affect adapter`() {
        val selectableItem1 = TestSelectableItem()
        val selectableItem2 = TestSelectableItem()
        adapter.resetItems(listOf(selectableItem1, selectableItem2))
        selectableItem1.selected = true
        selectableItem2.selected = true
        adapter.removeItem(0)
        selectableItem1.selected = false
        selectableItem1.selected = true

        assertThat(adapter.selectedItemCount).isEqualTo(1)
        assertThat(adapter.selectedItems()).containsExactly(selectableItem2)
    }

    @Test
    fun `toggling selection repeatedly does not affect adapter`() {
        val selectableItem1 = TestSelectableItem("selectableItem1")
        val selectableItem2 = TestSelectableItem("selectableItem2")
        adapter.resetItems(listOf(selectableItem1, selectableItem2))
        selectableItem1.selected = true
        selectableItem2.selected = true
        assertThat(adapter.selectedItems()).containsExactlyInAnyOrder(selectableItem1, selectableItem2)
        selectableItem1.selected = true
        selectableItem1.selected = true
        assertThat(adapter.selectedItems()).containsExactlyInAnyOrder(selectableItem1, selectableItem2)
        selectableItem1.selected = false
        selectableItem2.selected = false
        selectableItem1.selected = true
        selectableItem2.selected = true
        assertThat(adapter.selectedItems()).containsExactlyInAnyOrder(selectableItem1, selectableItem2)
        selectableItem1.selected = false
        selectableItem2.selected = false
        assertThat(adapter.selectedItems()).isEmpty()
    }

    @Test
    fun `toggling selection of reset item does not affect adapter`() {
        val selectableItem1 = TestSelectableItem("selectableItem1")
        val selectableItem2 = TestSelectableItem("selectableItem2")
        adapter.resetItems(listOf(selectableItem1, selectableItem2))
        selectableItem1.selected = true
        selectableItem2.selected = true
        adapter.resetItems(listOf(selectableItem2))
        selectableItem1.selected = false
        selectableItem1.selected = true

        assertThat(adapter.selectedItems()).containsExactly(selectableItem2)
    }
}
