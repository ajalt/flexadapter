package com.github.ajalt.flexadapter.sample

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.View
import com.github.ajalt.flexadapter.FlexAdapter
import com.github.ajalt.flexadapter.FlexAdapterExtensionItem
import kotlinx.android.synthetic.main.activity_sample.*
import kotlinx.android.synthetic.main.item_color_square.view.*
import java.util.*

private const val COLUMNS = 4

private class ColorSquareItem(var color: String) :
        FlexAdapterExtensionItem(R.layout.item_color_square) {
    override fun bindItemView(itemView: View, position: Int) {
        itemView.card.setCardBackgroundColor(Color.parseColor(color))
    }
}

class StableIdsActivity : AppCompatActivity() {
    val adapter = FlexAdapter<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        adapter.setHasStableIds(true)

        recycler_view.adapter = adapter
        recycler_view.layoutManager = GridLayoutManager(this, COLUMNS)

        adapter.items.addAll(listOf(
                ColorSquareItem("#440154"),
                ColorSquareItem("#481f70"),
                ColorSquareItem("#443982"),
                ColorSquareItem("#3a518b"),
                ColorSquareItem("#30678d"),
                ColorSquareItem("#287c8e"),
                ColorSquareItem("#20908c"),
                ColorSquareItem("#20a486"),
                ColorSquareItem("#35b778"),
                ColorSquareItem("#5cc862"),
                ColorSquareItem("#8ed643"),
                ColorSquareItem("#c7e020")
        ))

        fab.setImageResource(R.drawable.ic_shuffle_white_24dp)
        fab.setOnClickListener {
            Collections.shuffle(adapter.items)
        }
    }
}
