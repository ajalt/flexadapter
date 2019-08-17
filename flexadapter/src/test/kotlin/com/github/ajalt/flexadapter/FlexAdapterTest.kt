package com.github.ajalt.flexadapter

import android.view.View
import android.widget.FrameLayout
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.*
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.shouldThrow
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

private class C : CachingAdapterItem(0) {
    override fun bindItemView(itemView: View, position: Int) = Unit
}

private abstract class I
private open class O : I()
private class O2 : O()

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FlexAdapterTest {
    private val layout = android.R.layout.test_list_item

    private fun bindViewAt(adapter: FlexAdapter<*>, i: Int) {
        val viewHolder = adapter.onCreateViewHolder(
                FrameLayout(ApplicationProvider.getApplicationContext()),
                adapter.getItemViewType(i))
        viewHolder shouldNotBe null
        adapter.bindViewHolder(viewHolder, i)
    }

    @Test
    fun `registered items can be added`() {
        val adapter = FlexAdapter<Any>()
        adapter.register<Int>(layout)
        adapter.register<String>(layout)

        adapter.items.addAll(arrayOf("asd", 1))
        bindViewAt(adapter, 0)
        bindViewAt(adapter, 1)
    }

    @Test
    fun `multiply registered classes can be added`() {
        val adapter = FlexAdapter<O>()
        adapter.register<O>(layout)
        adapter.register<O>(layout)

        adapter.items.addAll(listOf(O2(), O()))
        bindViewAt(adapter, 0)
        bindViewAt(adapter, 1)
    }

    @Test
    fun `registered subclasses can be added`() {
        val adapter = FlexAdapter<I>()
        adapter.register<I>(layout)

        adapter.items.addAll(listOf(O2(), O()))
        bindViewAt(adapter, 0)
        bindViewAt(adapter, 1)
    }

    @Test
    fun `unregistered items cannot be added`() {
        val adapter = FlexAdapter<Any>()
        shouldThrow<IllegalArgumentException> { adapter.items.add(1) }
    }

    @Test
    fun `registering FlexAdapterItems raises an exception`() {
        val adapter = FlexAdapter<C>()
        adapter.register<C>(layout)
        shouldThrow<IllegalArgumentException> { adapter.items.addAll(arrayOf(C(), C())) }
    }

    @Test
    fun `multiple base class registration updates subclasses`() {
        val adapter = FlexAdapter<I>()
        val cb1: Runnable = mock()
        val cb2: Runnable = mock()
        adapter.register<I>(layout) { cb1.run() }

        adapter.items.addAll(listOf(O2(), O()))
        bindViewAt(adapter, 0)
        verify(cb1).run()

        adapter.register<I>(layout) { cb2.run() }

        bindViewAt(adapter, 0)
        verifyNoMoreInteractions(cb1)
        verify(cb2).run()
    }

    @Test
    fun `custom viewTypes are returned`() {
        val adapter = FlexAdapter<String>()
        adapter.register<String>(layout, viewType = 123)
        adapter.items.add("x")
        adapter.getItemViewType(0) shouldBe 123
    }

    @Test
    fun `multiple registration changes viewTypes`() {
        val adapter = FlexAdapter<String>()
        adapter.register<String>(layout, viewType = 123)
        adapter.items.add("x")
        adapter.register<String>(layout, viewType = 789)
        adapter.getItemViewType(0) shouldBe 789
    }

    @Test
    fun `multiple base class registration changes viewTypes`() {
        val adapter = FlexAdapter<I>()
        adapter.register<I>(layout, viewType = 123)
        adapter.items.add(O())
        adapter.register<I>(layout, viewType = 789)
        adapter.getItemViewType(0) shouldBe 789
    }
}


