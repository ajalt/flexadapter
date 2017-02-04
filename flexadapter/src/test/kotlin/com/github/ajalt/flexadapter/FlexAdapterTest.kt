package com.github.ajalt.flexadapter

import android.view.View
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class FlexAdapterTest {
    @Test
    fun `registered items can be added`() {
        val adapter = FlexAdapter<Any>()
        with(adapter) {
            register<Int>(123) { value, view, i ->
                println("Int")
                view.findViewById(233)?.visibility = View.GONE
            }
            register<String>(456) { value, view, i ->
                println("String")
                view.findViewById(233)?.visibility = View.GONE
            }
        }

        adapter.items.addAll(arrayOf("asd", 1))
    }
}


