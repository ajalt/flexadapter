package com.github.ajalt.flexadapter.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.CardView
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import com.github.ajalt.flexadapter.CachingAdapterItem
import com.github.ajalt.flexadapter.CachingViewHolder
import com.github.ajalt.flexadapter.FlexAdapter
import kotlinx.android.synthetic.main.activity_stable_ids.*

private class ColorSquareItem(var color: Int = randomColor()) : CachingAdapterItem(R.layout.item_color_square_small) {
    override fun CachingViewHolder.bindItemView(position: Int) {
        view<CardView>(R.id.card).setCardBackgroundColor(color)
    }

    fun changeColor() {
        color = randomColor()
    }
}

private class CarouselItem(val carousel: FlexAdapter<ColorSquareItem> = FlexAdapter()) :
        CachingAdapterItem(R.layout.item_carousel) {
    init {
        carousel.setHasStableIds(true)
        carousel.items.add(ColorSquareItem())
    }

    override fun CachingViewHolder.initializeItemView() {
        view<RecyclerView>(R.id.carousel_rv).layoutManager =
                GridLayoutManager(itemView.context, 2, GridLayoutManager.HORIZONTAL, false)
    }

    override fun CachingViewHolder.bindItemView(position: Int) {
        fun updateButtons() {
            view<Button>(R.id.carousel_clear).setVisible(carousel.items.size > 0)
            view<Button>(R.id.carousel_change).setVisible(carousel.items.size > 0)
            view<Button>(R.id.carousel_shuffle).setVisible(carousel.items.size > 1)
        }

        view<RecyclerView>(R.id.carousel_rv).adapter = carousel

        view<View>(R.id.carousel_add).setOnClickListener {
            carousel.items.add(0, ColorSquareItem())
            updateButtons()
        }

        view<View>(R.id.carousel_clear).setOnClickListener {
            carousel.items.clear()
            updateButtons()
        }

        view<View>(R.id.carousel_change).setOnClickListener { changeColors() }

        view<View>(R.id.carousel_shuffle).setOnClickListener { carousel.items.shuffle() }
        updateButtons()
    }

    fun changeColors() {
        carousel.items.forEach { it.changeColor() }
        carousel.notifyDataSetChanged()
    }
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
            adapter.items.forEach { it.changeColors() }
        }

        shuffle.setOnClickListener { adapter.items.shuffle() }

        adapter.items.add(CarouselItem())
        updateButtons()
    }

    private fun updateButtons() {
        clear.setVisible(adapter.items.size > 0)
        change.setVisible(adapter.items.size > 0)
        shuffle.setVisible(adapter.items.size > 1)
    }
}
