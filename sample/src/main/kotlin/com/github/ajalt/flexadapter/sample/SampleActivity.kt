package com.github.ajalt.flexadapter.sample

import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.widget.TextView
import com.github.ajalt.flexadapter.CachingAdapterItem
import com.github.ajalt.flexadapter.CachingViewHolder
import com.github.ajalt.flexadapter.FlexAdapter
import kotlinx.android.synthetic.main.activity_sample.*

private const val COLUMNS = 3
private const val HORIZONTAL = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
private const val VERTICAL = ItemTouchHelper.UP or ItemTouchHelper.DOWN
private const val ALL_DIRS = HORIZONTAL or VERTICAL

// You can have your data models inherit from FlexAdapterItem if you want to configure drag, swipe,
// or span per-item, or if you need more control over the ViewHolder creation.

/** A regular text item */
private class TextItem(
        @StringRes var text: Int,
        dragDirs: Int = 0
) : CachingAdapterItem(R.layout.item_text, dragDirs = dragDirs, span = COLUMNS) {
    override fun CachingViewHolder.bindItemView(position: Int) {
        view<TextView>(R.id.text_view).setText(text)
    }
}

/** An image that spans all three columns */
private class WidePictureItem(
        @ColorInt val color: Int = randomColor(),
        dragDirs: Int = 0,
        swipeDirs: Int = 0,
        span: Int = COLUMNS
) : CachingAdapterItem(
        R.layout.item_color_square_wide,
        dragDirs = dragDirs,
        swipeDirs = swipeDirs,
        span = span
) {
    override fun CachingViewHolder.bindItemView(position: Int) {
        view<CardView>(R.id.card).setCardBackgroundColor(color)
    }
}

// Or you can use any data model class (including primitives) if you use `FlexAdapter.register`.

data class HeaderItem(@StringRes var text: Int)
data class SquarePictureItem(@ColorInt val color: Int = randomColor())
object DIVIDER // It's fine to add the same item to the list more than once.

class SampleActivity : AppCompatActivity() {
    // If you are only going to add a single type to the adapter, or if you have a common base type,
    // you can use that as the type parameter instead of needing to cast from Any.
    private val adapter = FlexAdapter<Any>()
    private var extraImagesAdded = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)

        recycler_view.adapter = adapter
        recycler_view.layoutManager = GridLayoutManager(this, COLUMNS).apply {
            spanSizeLookup = adapter.spanSizeLookup
        }

        with(adapter) {
            register<HeaderItem>(R.layout.item_header, span = COLUMNS) {
                view<TextView>(R.id.text_view).setText(it.text)
            }
            register<SquarePictureItem>(R.layout.item_color_square_large, dragDirs = ALL_DIRS) {
                view<CardView>(R.id.card).setCardBackgroundColor(it.color)
            }
            register<DIVIDER>(R.layout.item_divider, span = COLUMNS)
        }

        val header1 = HeaderItem(R.string.title_drag_all)
        val header2 = HeaderItem(R.string.title_swipe)

        adapter.items.addAll(listOf(header1,
                SquarePictureItem(),
                SquarePictureItem(),
                SquarePictureItem(),
                SquarePictureItem(),
                SquarePictureItem(),
                SquarePictureItem(),
                SquarePictureItem(),
                SquarePictureItem(),
                SquarePictureItem(),
                WidePictureItem(span = 2, dragDirs = ALL_DIRS),
                SquarePictureItem(),
                DIVIDER,
                HeaderItem(R.string.title_drag_vertical),
                TextItem(R.string.list_drag_01),
                TextItem(R.string.list_drag_02, dragDirs = VERTICAL),
                TextItem(R.string.list_drag_03, dragDirs = VERTICAL),
                TextItem(R.string.list_drag_04, dragDirs = VERTICAL),
                TextItem(R.string.list_drag_05, dragDirs = VERTICAL),
                TextItem(R.string.list_drag_06, dragDirs = VERTICAL),
                DIVIDER,
                header2,
                WidePictureItem(swipeDirs = HORIZONTAL),
                HeaderItem(R.string.title_no_swipe),
                WidePictureItem()
        ))

        // Change the header text when the large picture is swiped away
        adapter.itemSwipedListener = {
            header2.text = R.string.title_post_swipe
            adapter.notifyItemObjectChanged(header2)
        }

        fab.setOnClickListener {
            val item = SquarePictureItem()
            adapter.items.add(adapter.items.indexOf(header1) + 1, item)
            Snackbar.make(root_layout, R.string.snackbar_add_success, Snackbar.LENGTH_SHORT)
                    .setAction(R.string.action_undo) { adapter.items.remove(item); extraImagesAdded-- }
                    .show()
        }
    }
}
