package com.github.ajalt.flexadapter

import android.view.View
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
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
    override fun bindItemView(itemView: View, selected: Boolean, position: Int) = Unit
}

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FlexAdapterSelectableItemTest {
    val adapter = FlexAdapter<Any>()
    val selectablePrimitive = "selectable"
    val regularPrimitive = 3
    val regularItem = TestRegularItem("regularItem")
    val selectableItem1 = TestSelectableItem("selectableItem1")
    val selectableItem2 = TestSelectableItem("selectableItem2")

    @Before
    fun setup() {
        adapter.register<Int>(0) { it, v, i -> }
        adapter.register<String>(0) { it, v, selectable, i -> }
    }

    @Test
    fun `selectable items can be mixed with regular items`() {
        adapter.items.addAll(listOf(
                regularItem, selectableItem1, selectablePrimitive, regularPrimitive, selectableItem2))
        assertThat(adapter.items).containsExactly(
                regularItem, selectableItem1, selectablePrimitive, regularPrimitive, selectableItem2)
    }

    @Test
    fun `selection count is zero when empty`() {
        assertThat(adapter.selectedItemCount).isEqualTo(0)
        assertThat(adapter.selectedItems()).isEmpty()
    }

    @Test
    fun `selection count is zero when only regular items are present`() {
        adapter.items.addAll(listOf(regularItem, regularPrimitive))
        assertThat(adapter.selectedItemCount).isEqualTo(0)
        assertThat(adapter.selectedItems()).isEmpty()
    }

    @Test
    fun `selection count is zero when only unselected items are present`() {
        adapter.items.addAll(listOf(regularItem, selectableItem1, regularPrimitive, selectablePrimitive))
        assertThat(adapter.selectedItemCount).isEqualTo(0)
        assertThat(adapter.selectedItems()).isEmpty()
    }

    @Test
    fun `FlexAdapterSelectableItems can be selected`() {
        adapter.items.addAll(listOf(selectableItem1, regularItem))
        assertThat(adapter.selectedItemCount).isEqualTo(0)
        assertThat(adapter.selectedItems()).isEmpty()

        adapter.selectItem(selectableItem1)
        assertThat(adapter.selectedItems()).containsExactly(selectableItem1)
    }

    @Test
    fun `registered objects can be selected`() {
        adapter.items.addAll(listOf(selectablePrimitive, regularPrimitive))
        assertThat(adapter.selectedItems()).isEmpty()

        adapter.selectItem(selectablePrimitive)
        assertThat(adapter.selectedItems()).containsExactly(selectablePrimitive)
    }

    @Test
    fun `deselecting item updates count`() {
        adapter.items.addAll(listOf(selectableItem1, regularItem))
        adapter.selectItem(selectableItem1)
        assertThat(adapter.selectedItemCount).isEqualTo(1)
        assertThat(adapter.selectedItems()).containsExactly(selectableItem1)

        adapter.deselectItem(selectableItem1)
        assertThat(adapter.selectedItemCount).isEqualTo(0)
        assertThat(adapter.selectedItems()).isEmpty()

        adapter.items.addAll(listOf(selectablePrimitive, regularPrimitive))
        adapter.selectItem(selectablePrimitive)
        assertThat(adapter.selectedItems()).containsExactly(selectablePrimitive)

        adapter.deselectItem(selectablePrimitive)
        assertThat(adapter.selectedItemCount).isEqualTo(0)
        assertThat(adapter.selectedItems()).isEmpty()
    }

    @Test
    fun `removing selected item updates count`() {
        adapter.items.add(selectableItem1)
        adapter.selectItem(selectableItem1)
        assertThat(adapter.selectedItems()).containsExactly(selectableItem1)
        adapter.deselectItem(selectableItem1)
        assertThat(adapter.selectedItems()).isEmpty()
    }

    @Test
    fun `selectItem ignores not selectable items`() {
        adapter.items.addAll(listOf(regularItem, regularPrimitive))
        adapter.selectItem(regularItem)
        adapter.selectItem(regularPrimitive)
        assertThat(adapter.selectedItems()).isEmpty()
    }

    @Test
    fun `deselectItem ignores removed items`() {
        adapter.items.addAll(listOf(selectableItem1, selectableItem2))
        adapter.selectItem(selectableItem1)
        adapter.selectItem(selectableItem2)
        adapter.items.removeAt(0)
        adapter.deselectItem(selectableItem1)

        assertThat(adapter.selectedItems()).containsExactly(selectableItem2)
    }

    @Test
    fun `selectAllItems works with no selected items`() {
        adapter.items.addAll(listOf(regularItem, selectableItem1, selectableItem2, regularPrimitive, selectablePrimitive))
        assertThat(adapter.selectedItems()).isEmpty()
        adapter.selectAllItems()
        assertThat(adapter.selectedItems()).containsExactlyInAnyOrder(selectableItem1, selectableItem2, selectablePrimitive)
    }

    @Test
    fun `selectAllItems works with mixed selected items`() {
        adapter.items.addAll(listOf(regularItem, selectableItem1, selectableItem2, regularPrimitive, selectablePrimitive))
        adapter.selectItem(selectableItem1)
        assertThat(adapter.selectedItems()).containsExactly(selectableItem1)
        adapter.selectAllItems()
        assertThat(adapter.selectedItems()).containsExactlyInAnyOrder(selectableItem1, selectableItem2, selectablePrimitive)
    }

    @Test
    fun `selectAllItems works with all selected items`() {
        adapter.items.addAll(listOf(regularItem, selectableItem1, selectableItem2, regularPrimitive, selectablePrimitive))
        adapter.selectItem(selectableItem1)
        adapter.selectItem(selectableItem2)
        adapter.selectItem(selectablePrimitive)
        assertThat(adapter.selectedItems()).containsExactlyInAnyOrder(selectableItem1, selectableItem2, selectablePrimitive)
        adapter.selectAllItems()
        assertThat(adapter.selectedItems()).containsExactlyInAnyOrder(selectableItem1, selectableItem2, selectablePrimitive)
    }

    @Test
    fun `deselectAllItems works with no selected items`() {
        adapter.items.addAll(listOf(regularItem, selectableItem1, selectableItem2))
        assertThat(adapter.selectedItems()).isEmpty()

        adapter.deselectAllItems()
        assertThat(adapter.selectedItems()).isEmpty()
    }

    @Test
    fun `deselectAllItems works with mixed selected items`() {
        adapter.items.addAll(listOf(regularItem, selectableItem1, selectableItem2))
        adapter.selectItem(selectableItem1)
        assertThat(adapter.selectedItems()).containsExactly(selectableItem1)
        adapter.deselectAllItems()
        assertThat(adapter.selectedItems()).isEmpty()
    }

    @Test
    fun `deselectAllItems works with all selected items`() {
        adapter.items.addAll(listOf(regularItem, selectableItem1, selectableItem2, regularPrimitive, selectablePrimitive))
        adapter.selectItem(selectableItem1)
        adapter.selectItem(selectableItem2)
        adapter.selectItem(selectablePrimitive)
        assertThat(adapter.selectedItems()).containsExactlyInAnyOrder(selectableItem1, selectableItem2, selectablePrimitive)
        adapter.deselectAllItems()
        assertThat(adapter.selectedItems()).isEmpty()
    }

    @Test
    fun `toggling selection repeatedly does not affect adapter`() {
        adapter.items.addAll(listOf(selectableItem1, selectableItem2))
        adapter.selectItem(selectableItem1)
        adapter.selectItem(selectableItem2)
        assertThat(adapter.selectedItems()).containsExactlyInAnyOrder(selectableItem1, selectableItem2)
        adapter.selectItem(selectableItem1)
        adapter.selectItem(selectableItem1)
        assertThat(adapter.selectedItems()).containsExactlyInAnyOrder(selectableItem1, selectableItem2)
        adapter.deselectItem(selectableItem1)
        adapter.deselectItem(selectableItem2)
        adapter.selectItem(selectableItem1)
        adapter.selectItem(selectableItem2)
        assertThat(adapter.selectedItems()).containsExactlyInAnyOrder(selectableItem1, selectableItem2)
        adapter.deselectItem(selectableItem1)
        adapter.deselectItem(selectableItem2)
        assertThat(adapter.selectedItems()).isEmpty()
    }
}
