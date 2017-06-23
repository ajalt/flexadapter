package com.github.ajalt.flexadapter.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.github.ajalt.flexadapter.FlexAdapterExtensionItem
import com.github.ajalt.flexadapter.FlexPagerAdapter
import kotlinx.android.synthetic.main.activity_view_pager.*
import kotlinx.android.synthetic.main.item_pager_picture.view.*

class PagerImageItem(@StringRes var text: Int, @DrawableRes var image: Int) :
        FlexAdapterExtensionItem(R.layout.item_pager_picture) {
    override fun bindItemView(itemView: View, position: Int) {
        itemView.image.setImageResource(image)
        itemView.text.setText(text)
    }
}

class ViewPagerActivity : AppCompatActivity() {
    val adapter = FlexPagerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pager)

        adapter.addItems(
                PagerImageItem(R.string.pager_title_1, R.drawable.burt_square_1),
                PagerImageItem(R.string.pager_title_2, R.drawable.burt_square_9),
                PagerImageItem(R.string.pager_title_3, R.drawable.burt_square_6),
                PagerImageItem(R.string.pager_title_4, R.drawable.burt_square_12)
        )

        view_pager.adapter = adapter

        // Apply negative margin to show the edges of adjacent pages
        view_pager.pageMargin = dp(-36f).toInt()

        // Keep the adjacent pages loaded since they're visible
        view_pager.offscreenPageLimit = 3
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add("Switch to main sample")
        menu.add("Switch to Java sample")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.order) {
            0 -> startActivity(Intent(this, SampleActivity::class.java))
            1 -> startActivity(Intent(this, JavaSampleActivity::class.java))
        }

        finish()
        return true
    }
}

/** Convert dp to px */
fun Context.dp(dp: Float) = TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp, resources.displayMetrics)
