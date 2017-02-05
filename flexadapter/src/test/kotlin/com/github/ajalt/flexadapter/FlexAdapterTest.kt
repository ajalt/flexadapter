package com.github.ajalt.flexadapter

import android.view.View
import android.widget.Space
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

private class C : FlexAdapterExtensionItem(0) {
    override fun bindItemView(itemView: View, position: Int) = Unit
}

abstract class I
open class O : I()
class O2 : O()

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
        adapter.getItemViewType(0)
        adapter.getItemViewType(1)
    }

    @Test
    fun `registered subclasses can be added`() {
        val adapter = FlexAdapter<Any>()
        adapter.register<I>(123) { value, view, i -> }

        adapter.items.addAll(listOf(O2(), O()))
        adapter.getItemViewType(0)
        adapter.getItemViewType(1)
    }

    @Test
    fun `unregistered items cannot be added`() {
        val adapter = FlexAdapter<Any>()
        exception.expect(IllegalArgumentException::class.java)
        adapter.items.add(1)
    }

    @Test
    fun `registering FlexAdapterItems raises an exception`() {
        val adapter = FlexAdapter<C>()
        exception.expect(IllegalArgumentException::class.java)
        adapter.register<C>(0) { it, v, i -> }
        adapter.items.addAll(arrayOf(C(), C()))
    }

    @Test
    fun `multiple base class registration updated subclasses`() {
        val adapter = FlexAdapter<Any>()
        val cb1: Runnable = mock()
        val cb2: Runnable = mock()
        adapter.register<I>(0) { it, v, i -> cb1.run() }

        adapter.items.addAll(listOf(O2(), O()))
        bindViewAt(adapter, 0)
        verify(cb1).run()

        adapter.register<I>(0) { it, v, i -> cb2.run() }

        bindViewAt(adapter, 0)
        verifyNoMoreInteractions(cb1)
        verify(cb2).run()
    }

    private fun bindViewAt(adapter: FlexAdapter<Any>, i: Int) {
        val viewHolder = FlexAdapterExtensionItem.ViewHolder(Space(RuntimeEnvironment.application))
        adapter.bindViewHolder(viewHolder, i)
    }
}


