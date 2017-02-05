package com.github.ajalt.flexadapter

import android.view.View
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
}


