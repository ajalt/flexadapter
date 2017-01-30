package com.github.ajalt.flexadapter

import android.support.v7.util.DiffUtil
import android.support.v7.util.ListUpdateCallback
import android.support.v7.widget.RecyclerView
import android.view.View
import com.nhaarman.mockito_kotlin.*
import org.junit.Test

private class VH(v: View) : RecyclerView.ViewHolder(v)

private class TestItem1(val x: Int) : FlexAdapterItem<VH>() {
    override fun toString() = "TestItem1($x)"
    override fun viewHolderFactory() = ::VH
    override fun bindViewHolder(holder: VH, position: Int) = Unit
    override fun equals(other: Any?) = this === other || other is TestItem1 && other.x == x
    override fun hashCode() = x
}

private class TestItem2(val x: String) : FlexAdapterItem<VH>() {
    override fun toString() = "TestItem2($x)"
    override fun viewHolderFactory() = ::VH
    override fun bindViewHolder(holder: VH, position: Int) = Unit
    override fun equals(other: Any?) = this === other || other is TestItem2 && other.x == x
    override fun hashCode() = x.hashCode()
}

class FlexAdapterDiffUtilCallbackTest {
    @Test
    fun `empty lists produce no changes`() {
        val result = calcDiff(emptyList(), emptyList(), true)
        verifyZeroInteractions(result)
    }

    @Test
    fun `identical lists produce no changes`() {
        val items = listOf(TestItem1(1), TestItem2("a"))
        val result = calcDiff(items, items, true)
        verifyZeroInteractions(result)
    }

    @Test
    fun `additions are tracked`() {
        val oldItems = listOf(TestItem1(1))
        val newItems = listOf(oldItems[0], TestItem2("a"))
        val result = calcDiff(oldItems, newItems, true)
        verify(result).onInserted(1, 1)
    }

    @Test
    fun `removals are tracked`() {
        val oldItems = listOf(TestItem1(1), TestItem2("a"))
        val newItems = listOf(oldItems[0])
        val result = calcDiff(oldItems, newItems, true)
        verify(result).onRemoved(1, 1)
    }

    @Test
    fun `moves are tracked`() {
        val oldItems = listOf(TestItem1(1), TestItem2("a"), TestItem1(2))
        val newItems = listOf(oldItems[1], oldItems[0], oldItems[2])
        val result = calcDiff(oldItems, newItems, true)
        verify(result).onMoved(any(), any())
    }

    @Test
    fun `changes are tracked`() {
        val oldItems = listOf(TestItem1(1), TestItem2("a"), TestItem1(2))
        val newItems = listOf(TestItem1(3), oldItems[1], oldItems[2])
        val result = calcDiff(oldItems, newItems, true)
        verify(result).onChanged(eq(0), eq(1), anyOrNull())
    }

    private fun calcDiff(oldItems: List<FlexAdapterItem<*>>,
                         newItems: List<FlexAdapterItem<*>>,
                         detectMoves: Boolean)
            = DiffUtil.calculateDiff(FlexAdapterDiffUtilCallback(oldItems, newItems), detectMoves).run {
        mock<ListUpdateCallback>().apply { dispatchUpdatesTo(this) }
    }
}
