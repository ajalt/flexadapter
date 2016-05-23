package com.github.ajalt.flexadapter.sample

import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
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

class TextItem(val text: String) : FlexAdapterExtensionItem(R.layout.item_text) {
    override fun bindItemView(itemView: View, position: Int) {
        itemView.text_view.text = text
    }
}

class PictureItem(@DrawableRes val imageRes: Int) : FlexAdapterExtensionItem(R.layout.item_picture) {
    override fun dragDirs(): Int = ALL_DIRS

    override fun bindItemView(itemView: View, position: Int) {
        itemView.image_view.setImageResource(imageRes)
    }
}


class MainActivity : AppCompatActivity() {
    val a = FlexAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView.adapter = a
        recyclerView.layoutManager = LinearLayoutManager(this)
    }
}
