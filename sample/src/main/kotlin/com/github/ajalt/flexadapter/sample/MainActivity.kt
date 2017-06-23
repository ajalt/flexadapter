package com.github.ajalt.flexadapter.sample

import android.content.Intent
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.github.ajalt.flexadapter.FlexAdapter
import com.github.ajalt.flexadapter.FlexAdapterExtensionItem
import com.github.ajalt.flexadapter.register
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_picture.view.*
import kotlinx.android.synthetic.main.item_text.view.*

private const val COLUMNS = 3
private const val HORIZONTAL = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
private const val VERTICAL = ItemTouchHelper.UP or ItemTouchHelper.DOWN
private const val ALL_DIRS = HORIZONTAL or VERTICAL

// You can have your data models inherit from FlexAdapterItem if you want to configure drag, swipe,
// or span per-item, or if you need more control over the ViewHolder creation.

/** A regular text item */
private class TextItem(@StringRes var text: Int, dragDirs: Int = 0) :
        FlexAdapterExtensionItem(R.layout.item_text, dragDirs = dragDirs, span = COLUMNS) {
    override fun bindItemView(itemView: View, position: Int) {
        itemView.text_view.setText(text)
    }
}


/** An image that spans all three columns */
private class WidePictureItem(@DrawableRes val image: Int, dragDirs: Int = 0, swipeDirs: Int = 0, span: Int = COLUMNS) :
        FlexAdapterExtensionItem(R.layout.item_picture, dragDirs = dragDirs, swipeDirs = swipeDirs, span = span) {
    override fun bindItemView(itemView: View, position: Int) {
        itemView.image_view.setImageResource(image)
    }
}

// Or you can use any data model class (including primitives) if you use `FlexAdapter.register`.

data class HeaderItem(@StringRes var text: Int)
data class SquarePictureItem(@DrawableRes val image: Int)
object DIVIDER // It's fine to add the same item to the list more than once.

class MainActivity : AppCompatActivity() {
    // If you are only going to add a single type to the adapter, or if you have a common base type,
    // you can use that as the type parameter instead of needing to cast from Any.
    val adapter = FlexAdapter<Any>()
    var extraBurtsAdded = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler_view.adapter = adapter
        recycler_view.layoutManager = GridLayoutManager(this, COLUMNS).apply {
            spanSizeLookup = adapter.spanSizeLookup
        }

        with(adapter) {
            register<HeaderItem>(R.layout.item_header, span = COLUMNS) { it, v, _ ->
                v.text_view.setText(it.text)
            }
            register<SquarePictureItem>(R.layout.item_picture_square, dragDirs = ALL_DIRS) { it, v, _ ->
                v.image_view.setImageResource(it.image)
            }
            register<DIVIDER>(R.layout.item_divider, span = COLUMNS)
        }

        val header1 = HeaderItem(R.string.title_drag_all)
        val header2 = HeaderItem(R.string.title_swipe)

        adapter.items.addAll(arrayOf(header1,
                SquarePictureItem(R.drawable.burt_square_1),
                SquarePictureItem(R.drawable.burt_square_2),
                SquarePictureItem(R.drawable.burt_square_3),
                SquarePictureItem(R.drawable.burt_square_4),
                SquarePictureItem(R.drawable.burt_square_5),
                SquarePictureItem(R.drawable.burt_square_6),
                SquarePictureItem(R.drawable.burt_square_7),
                SquarePictureItem(R.drawable.burt_square_8),
                SquarePictureItem(R.drawable.burt_square_9),
                WidePictureItem(R.drawable.burt_wide_3, span = 2, dragDirs = ALL_DIRS),
                SquarePictureItem(R.drawable.burt_square_10),
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
                WidePictureItem(R.drawable.burt_wide_1, swipeDirs = HORIZONTAL),
                HeaderItem(R.string.title_no_swipe),
                WidePictureItem(R.drawable.burt_wide_2)
        ))

        // Change the header text when the car picture is swiped away
        adapter.itemSwipedListener = {
            header2.text = R.string.title_post_swipe
            adapter.notifyItemObjectChanged(header2)
        }

        // These will get added when the fab is pressed
        val extraBurts = listOf(
                SquarePictureItem(R.drawable.burt_square_11),
                SquarePictureItem(R.drawable.burt_square_12)
        )

        fab.setOnClickListener {
            if (extraBurtsAdded >= extraBurts.size) {
                Snackbar.make(root_layout, R.string.snackbar_add_failure, Snackbar.LENGTH_SHORT).show()
            } else {
                val item = extraBurts[extraBurtsAdded++]
                adapter.items.add(adapter.items.indexOf(header1) + 1, item)
                Snackbar.make(root_layout, R.string.snackbar_add_success, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.action_undo, { adapter.items.remove(item); extraBurtsAdded-- })
                        .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 0, 0, "Switch to java sample")
        menu.add(1, 1, 1, "Switch to view pager sample")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            0 -> startActivity(Intent(this, JavaMainActivity::class.java))
            1 -> startActivity(Intent(this, ViewPagerActivity::class.java))
        }

        finish()
        return true
    }
}
