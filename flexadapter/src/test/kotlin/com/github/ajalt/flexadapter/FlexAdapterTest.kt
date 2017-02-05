package com.github.ajalt.flexadapter

import android.support.v7.widget.RecyclerView
import android.view.View
import com.nhaarman.mockito_kotlin.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private class C : FlexAdapterExtensionItem(0) {
    override fun bindItemView(itemView: View, position: Int) = Unit
}

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FlexAdapterTest {
    @Rule @JvmField
    val exception: ExpectedException = ExpectedException.none()

    @Test
    fun `registered items can be added`() {
        val adapter = FlexAdapter<Any>()
        with(adapter) {
            register<Int>(123) { value, view, i -> }
            register<String>(456) { value, view, i -> }
        }

        adapter.items.addAll(arrayOf("asd", 1))
    }

    @Test
    fun `registering FlexAdapterItems raises an exception`() {
        val adapter = FlexAdapter<C>()
        exception.expect(IllegalArgumentException::class.java)
        adapter.register<C>(0) { it, v, i -> }
        adapter.items.addAll(arrayOf(C(), C()))
    }

    @Test
    fun `all changes to items cause notifications`() {
        val adapter = FlexAdapter<C>()
        val observer = mock<RecyclerView.AdapterDataObserver>()
        adapter.registerAdapterDataObserver(observer)
        adapter.items.add(C())
        verify(observer).onItemRangeInserted(0, 1)
        adapter.items.add(C())
        verify(observer).onItemRangeInserted(1, 1)
        adapter.items[0] = C()
        verify(observer).onItemRangeChanged(eq(0), eq(1), anyOrNull())
        adapter.items.removeAt(0)
        verify(observer).onItemRangeRemoved(0, 1)
    }

    @Test
    fun `disabling automatic notifications`() {
        val adapter = FlexAdapter<C>()
        val observer = mock<RecyclerView.AdapterDataObserver>()
        adapter.registerAdapterDataObserver(observer)
        adapter.items.add(C())
        verify(observer).onItemRangeInserted(0, 1)
        adapter.automaticallyNotifyOnItemChanges = false
        adapter.items.add(C())
        adapter.items[0] = C()
        adapter.items.removeAt(0)
        adapter.items.clear()
        verifyNoMoreInteractions(observer)
    }
}


