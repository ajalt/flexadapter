<h1 align="center">
    <img src="web/wordmark.png">
</h1>

Define and Coordinate items in a RecyclerView without boilerplate.

* multple item layouts
* per-item swipe and drag behavior
* Zero typecasting

#### Check it out:

```kotlin
val adapter = FlexAdapter()
recyclerView.adapter = adapter
recyclerView.layoutManager = GridLayoutManager(this, 3).apply {
    // This line is all that's required to enable per-item spans
    spanSizeLookup = adapter.spanSizeLookup
}
// This line is all that's required to enable per-item swpe and drag
adapter.itemTouchHelper.attachToRecyclerView(recyclerView)
```

#### Define item types like this:
``` kotlin
class TextItem(var text: String) :
        FlexAdapterExtensionItem(R.layout.item_text, span = 3) {
    override fun bindItemView(itemView: View, position: Int) {
        itemView.text_view.text = text
    }
}

class PictureItem(@DrawableRes val imageRes: Int) :
        FlexAdapterExtensionItem(R.layout.item_picture) {
    override fun dragDirs(): Int = ALL_DIRS

    override fun bindItemView(itemView: View, position: Int) {
        itemView.image_view.setImageResource(imageRes)
    }
}
```

#### Add some items:
```kotlin
adapter.addItems(listOf(
    TextItem("Look at these pictures"),
    PictureItem(R.drawable.picture_1),
    PictureItem(R.drawable.picture_2)
))
```