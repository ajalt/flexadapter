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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_picture.view.*
import kotlinx.android.synthetic.main.item_text.view.*

val COLUMNS = 3
val HORIZONTAL = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
val VERTICAL = ItemTouchHelper.UP or ItemTouchHelper.DOWN
val ALL_DIRS = HORIZONTAL or VERTICAL

/** A regular text item */
class TextItem(@StringRes var text: Int, dragDirs: Int = 0) :
        FlexAdapterExtensionItem(R.layout.item_text, dragDirs = dragDirs, span = COLUMNS) {
    override fun bindItemView(itemView: View, position: Int) {
        itemView.text_view.setText(text)
    }
}

/** A large header text item */
class HeaderItem(@StringRes var text: Int) :
        FlexAdapterExtensionItem(R.layout.item_header, span = COLUMNS) {
    override fun bindItemView(itemView: View, position: Int) {
        itemView.text_view.setText(text)
    }
}

/** An image that spans all three columns */
class WidePictureItem(@DrawableRes val image: Int, swipeDirs: Int = 0) :
        FlexAdapterExtensionItem(R.layout.item_picture, span = COLUMNS, swipeDirs = swipeDirs) {
    override fun bindItemView(itemView: View, position: Int) {
        itemView.image_view.setImageResource(image)
    }
}

/** A picture in a square frame layout */
class SquarePictureItem(@DrawableRes val image: Int) :
        FlexAdapterExtensionItem(R.layout.item_picture_square, dragDirs = ALL_DIRS) {
    override fun bindItemView(itemView: View, position: Int) {
        itemView.image_view.setImageResource(image)
    }
}

/** A divider that spans all columns */
class DividerItem() : FlexAdapterExtensionItem(R.layout.item_divider, span = COLUMNS) {
    override fun bindItemView(itemView: View, position: Int) {
        // Nothing to bind for this item
    }
}

class MainActivity : AppCompatActivity() {
    val adapter = FlexAdapter()
    var extraBurtsAdded = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, COLUMNS).apply {
            spanSizeLookup = adapter.spanSizeLookup
        }

        val header1 = HeaderItem(R.string.title_drag_all)
        val header2 = HeaderItem(R.string.title_swipe)

        adapter.addItems(header1,
                SquarePictureItem(R.drawable.burt_square_1),
                SquarePictureItem(R.drawable.burt_square_2),
                SquarePictureItem(R.drawable.burt_square_3),
                SquarePictureItem(R.drawable.burt_square_4),
                SquarePictureItem(R.drawable.burt_square_5),
                SquarePictureItem(R.drawable.burt_square_6),
                SquarePictureItem(R.drawable.burt_square_7),
                SquarePictureItem(R.drawable.burt_square_8),
                SquarePictureItem(R.drawable.burt_square_9),
                DividerItem(),
                HeaderItem(R.string.title_drag_vertical),
                TextItem(R.string.list_drag_01),
                TextItem(R.string.list_drag_02, dragDirs = VERTICAL),
                TextItem(R.string.list_drag_03, dragDirs = VERTICAL),
                TextItem(R.string.list_drag_04, dragDirs = VERTICAL),
                TextItem(R.string.list_drag_05, dragDirs = VERTICAL),
                TextItem(R.string.list_drag_06, dragDirs = VERTICAL),
                DividerItem(),
                header2,
                WidePictureItem(R.drawable.burt_wide_1, swipeDirs = HORIZONTAL),
                HeaderItem(R.string.title_no_swipe),
                WidePictureItem(R.drawable.burt_wide_2)
        )

        // Change the header text when the car picture is swiped away
        adapter.itemSwipedListener = {
            header2.text = R.string.title_post_swipe
            adapter.notifyItemChanged(adapter.indexOf(header2))
        }

        // These will get added when the fab is pressed
        val extraBurts = listOf(
                SquarePictureItem(R.drawable.burt_square_10),
                SquarePictureItem(R.drawable.burt_square_11),
                SquarePictureItem(R.drawable.burt_square_12)
        )

        fab.setOnClickListener {
            if (extraBurtsAdded >= extraBurts.size) {
                Snackbar.make(root_layout, R.string.snackbar_add_failure, Snackbar.LENGTH_SHORT).show()
            } else {
                val item = extraBurts[extraBurtsAdded++]
                adapter.insertItem(adapter.indexOf(header1) + 1, item)
                Snackbar.make(root_layout, R.string.snackbar_add_success, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.action_undo, { adapter.removeItem(item); extraBurtsAdded-- })
                        .show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menu?.add("Switch to java sample")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        startActivity(Intent(this, JavaMainActivity::class.java))
        finish()
        return true
    }
}
