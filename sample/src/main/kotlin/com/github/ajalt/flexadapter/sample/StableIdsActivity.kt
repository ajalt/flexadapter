package com.github.ajalt.flexadapter.sample

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.ajalt.flexadapter.FlexAdapter
import com.github.ajalt.flexadapter.FlexAdapterExtensionItem
import kotlinx.android.synthetic.main.activity_stable_ids.*
import kotlinx.android.synthetic.main.item_carousel.view.*
import kotlinx.android.synthetic.main.item_color_square.view.*
import java.util.*

private class ColorSquareItem(var color: Int = randomColor()) :
        FlexAdapterExtensionItem(R.layout.item_color_square) {
    override fun bindItemView(itemView: View, position: Int) = itemView.card.setCardBackgroundColor(color)
}

private class CarouselItem(var adapter: FlexAdapter<ColorSquareItem> = FlexAdapter()) :
        FlexAdapterExtensionItem(R.layout.item_carousel) {
    init {
        adapter.setHasStableIds(true)
        adapter.items.add(ColorSquareItem())
    }

    override fun viewHolderFactory(): (ViewGroup) -> ViewHolder = {
        ViewHolder(LayoutInflater.from(it.context).inflate(layoutRes, it, false).apply {
            carousel_rv.layoutManager = GridLayoutManager(it.context, 2, GridLayoutManager.HORIZONTAL, false)
        })
    }

    override fun bindItemView(itemView: View, position: Int) = itemView.run {
        carousel_rv.adapter = adapter

        carousel_add.setOnClickListener {
            adapter.items.add(0, ColorSquareItem())
            updateButtons(this)
        }

        carousel_clear.setOnClickListener {
            adapter.items.clear()
            updateButtons(this)
        }

        carousel_change.setOnClickListener {
            adapter.items.forEach { it.color = randomColor() }
            adapter.notifyDataSetChanged()
        }

        carousel_shuffle.setOnClickListener { Collections.shuffle(adapter.items) }

        updateButtons(this)
    }

    private fun updateButtons(itemView: View) = itemView.run {
        carousel_clear.setVisible(adapter.items.size > 0)
        carousel_change.setVisible(adapter.items.size > 0)
        carousel_shuffle.setVisible(adapter.items.size > 1)
    }
}

fun randomColor(): Int {
    return Random().run { Color.rgb(nextInt(256), nextInt(256), nextInt(256)) }
}

private fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

class StableIdsActivity : AppCompatActivity() {
    private val adapter = FlexAdapter<CarouselItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stable_ids)
        adapter.setHasStableIds(true)

        recycler_view.adapter = adapter
        recycler_view.layoutManager = LinearLayoutManager(this)

        add.setOnClickListener {
            adapter.items.add(0, CarouselItem())
            updateButtons()
            recycler_view.scrollToPosition(0)
        }

        clear.setOnClickListener {
            adapter.items.clear()
            updateButtons()
        }


        change.setOnClickListener {
            adapter.items.forEach {
                it.adapter.items.forEach { it.color = randomColor() }
                it.adapter.notifyDataSetChanged()
            }
            adapter.items.flatMap { it.adapter.items }.forEach { it.color = randomColor() }
        }

        shuffle.setOnClickListener { Collections.shuffle(adapter.items) }

        adapter.items.add(CarouselItem())
        updateButtons()
    }

    private fun updateButtons() {
        clear.setVisible(adapter.items.size > 0)
        change.setVisible(adapter.items.size > 0)
        shuffle.setVisible(adapter.items.size > 1)
    }
}
