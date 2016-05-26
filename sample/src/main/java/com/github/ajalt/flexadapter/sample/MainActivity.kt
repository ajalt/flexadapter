package com.github.ajalt.flexadapter.sample

import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.View
import com.github.ajalt.flexadapter.FlexAdapter
import com.github.ajalt.flexadapter.FlexAdapterExtensionItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_picture.view.*
import kotlinx.android.synthetic.main.item_text.view.*

val HORIZONTAL = ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
val VERTICAL = ItemTouchHelper.UP or ItemTouchHelper.DOWN
val ALL_DIRS = HORIZONTAL or VERTICAL

/** An text item or header */
class TextItem(var text: String, dragDirs: Int = 0) :
        FlexAdapterExtensionItem(R.layout.item_text, dragDirs = dragDirs, span = 3) {
    override fun bindItemView(itemView: View, position: Int) {
        itemView.text_view.text = text
    }
}

/** An image that spans all three rows */
class WidePictureItem(@DrawableRes val imageRes: Int, swipe: Boolean = false) :
        FlexAdapterExtensionItem(R.layout.item_picture, span = 3, swipeDirs = if (swipe) HORIZONTAL else 0) {
    override fun bindItemView(itemView: View, position: Int) {
        itemView.image_view.setImageResource(imageRes)
    }
}

/** A picture in a square frame layout */
class SquarePictureItem(@DrawableRes val imageRes: Int) :
        FlexAdapterExtensionItem(R.layout.item_picture_square, dragDirs = ALL_DIRS) {
    override fun bindItemView(itemView: View, position: Int) {
        itemView.image_view.setImageResource(imageRes)
    }
}

class MainActivity : AppCompatActivity() {
    val adapter = FlexAdapter()
    var extraBurtsAdded = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView.adapter = adapter
        recyclerView.layoutManager = GridLayoutManager(this, 3).apply {
            spanSizeLookup = adapter.spanSizeLookup
        }
        adapter.itemTouchHelper.attachToRecyclerView(recyclerView)

        val header1 = TextItem("Move these Burts")
        val header2 = TextItem("This Burt is going for a drive")
        val items = listOf(
                TextItem("Rank your favorite movie stars:"),
                TextItem("• Burt Reynolds"),
                TextItem("• Robert Duvall", dragDirs = VERTICAL),
                TextItem("• Al Pacino", dragDirs = VERTICAL),
                TextItem("• Robert De Niro", dragDirs = VERTICAL),
                TextItem("• Harrison Ford", dragDirs = VERTICAL),
                TextItem("• Jack Nicholson", dragDirs = VERTICAL),
                header1,
                SquarePictureItem(R.drawable.burt_square_1),
                SquarePictureItem(R.drawable.burt_square_2),
                SquarePictureItem(R.drawable.burt_square_3),
                SquarePictureItem(R.drawable.burt_square_4),
                SquarePictureItem(R.drawable.burt_square_5),
                SquarePictureItem(R.drawable.burt_square_6),
                SquarePictureItem(R.drawable.burt_square_7),
                SquarePictureItem(R.drawable.burt_square_8),
                SquarePictureItem(R.drawable.burt_square_9),
                header2,
                WidePictureItem(R.drawable.burt_wide_1, swipe = true),
                TextItem("This Burt is staying right where he is"),
                WidePictureItem(R.drawable.burt_wide_2)
        )

        adapter.addItems(items)

        // Change the header text when the car picture is swiped away
        adapter.itemSwipedListener = {
            header2.text = "Vroom vroom"
            adapter.notifyItemChanged(0)
        }

        // These will get added when the fab is pressed
        val extraBurts = listOf(
                SquarePictureItem(R.drawable.burt_square_10),
                SquarePictureItem(R.drawable.burt_square_11),
                SquarePictureItem(R.drawable.burt_square_12)
        )

        fab.setOnClickListener {
            if (extraBurtsAdded >= extraBurts.size) {
                Snackbar.make(root_layout, "You can't handle any more Burts", Snackbar.LENGTH_SHORT).show()
            } else {
                val item = extraBurts[extraBurtsAdded++]
                adapter.insertItem(adapter.indexOf(header1) + 1, item)
                Snackbar.make(root_layout, "Here's a Burt", Snackbar.LENGTH_SHORT)
                        .setAction("undo", { adapter.removeItem(item); extraBurtsAdded-- })
                        .show()
            }
        }
    }
}
