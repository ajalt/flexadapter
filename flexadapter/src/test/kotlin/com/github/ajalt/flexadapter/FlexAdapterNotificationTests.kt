package com.github.ajalt.flexadapter

import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v7.widget.RecyclerView
import com.nhaarman.mockito_kotlin.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = intArrayOf(25))
class FlexAdapterNotificationTests {
    val adapter = FlexAdapter<Int>()
    val observer = mock<RecyclerView.AdapterDataObserver>()

    @Before
    fun setup() {
        adapter.register<Int>(0)
        adapter.registerAdapterDataObserver(observer)
    }

    @Test
    fun `adding and removing items causes notifications`() {
        adapter.items.add(1)
        verify(observer).onItemRangeInserted(0, 1)
        adapter.items.add(2)
        verify(observer).onItemRangeInserted(1, 1)
        adapter.items.add(0, 0)
        verify(observer, times(2)).onItemRangeInserted(0, 1)
        adapter.items[0] = 3
        verify(observer).onItemRangeChanged(eq(0), eq(1), anyOrNull())
        adapter.items.removeAt(1)
        verify(observer).onItemRangeRemoved(1, 1)
        adapter.items.remove(3)
        verify(observer).onItemRangeRemoved(0, 1)
        adapter.items.clear()
        verify(observer, times(2)).onItemRangeRemoved(0, 1)
    }

    @Test
    fun `settings items causes notifications`() {
        adapter.items.add(1)
        verify(observer).onItemRangeInserted(0, 1)
        adapter.items[0] = -1
        verify(observer).onItemRangeChanged(eq(0), eq(1), anyOrNull())
        adapter.items.add(1)
        verify(observer).onItemRangeInserted(1, 1)
        adapter.items[1] = -2
        verify(observer).onItemRangeChanged(eq(1), eq(1), anyOrNull())
    }

    @Test
    @RequiresApi(Build.VERSION_CODES.N)
    fun `changing ranges causes notifications`() {
        adapter.items.addAll(listOf(1, 2, 3))
        verify(observer).onItemRangeInserted(0, 3)
        adapter.items.addAll(0, listOf(4, 5))
        verify(observer).onItemRangeInserted(0, 2)
        adapter.items.removeAll(listOf(4, 5, 6))
        verify(observer).onChanged()
        adapter.items.retainAll(listOf(2, 3))
        verify(observer, times(2)).onChanged()
        (adapter.items as ArrayList<Int>).removeIf { it == 3 }
        verify(observer, times(3)).onChanged()
    }

    @Test
    fun `modifying iterator causes notification`() {
        adapter.items.addAll(listOf(1, 2, 3))
        verify(observer).onItemRangeInserted(0, 3)

        val iter = adapter.items.listIterator()
        iter.next()
        iter.set(5)
        verify(observer).onItemRangeChanged(eq(0), eq(1), anyOrNull())
        iter.next()
        iter.set(6)
        verify(observer).onItemRangeChanged(eq(1), eq(1), anyOrNull())
        iter.remove()
        verify(observer).onItemRangeRemoved(1, 1)
    }

    @Test
    fun `automatic notifications can be disabled`() {
        adapter.items.add(1)
        verify(observer).onItemRangeInserted(0, 1)
        adapter.automaticallyNotifyOnItemChanges = false
        adapter.items.add(2)
        adapter.items[0] = 3
        adapter.items.removeAt(0)
        adapter.items.clear()
        verifyNoMoreInteractions(observer)
    }
}


