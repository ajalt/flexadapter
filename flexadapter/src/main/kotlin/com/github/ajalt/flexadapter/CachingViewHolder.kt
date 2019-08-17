package com.github.ajalt.flexadapter

import android.support.annotation.IdRes
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.View

/**
 * A [ViewHolder][RecyclerView.ViewHolder] that provides view lookup functions with a cache.
 */
class CachingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val cache = SparseArray<View>(0)

    /** A shorter alias for [requireViewById]. */
    fun <T : View> view(@IdRes id: Int): T = requireViewById(id)

    /** Like [View.requireViewById], but caches retrieved views. */
    fun <T : View> requireViewById(@IdRes id: Int): T {
        return findViewById<T>(id)
                ?: throw IllegalArgumentException("ID does not reference a View inside this View")
    }

    /** Like [View.findViewById], but caches retrieved views. */
    @Suppress("UNCHECKED_CAST")
    fun <T : View> findViewById(@IdRes id: Int): T? {
        return cache[id]?.let { it as T }
                ?: itemView.findViewById<T>(id)?.also { cache.put(id, it) }
    }
}
