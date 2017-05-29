<h1 align="center">
	<img src=".web/wordmark.png">
</h1>

<div align="center">
	<img src=".web/sample.gif">
</div>

### Define and coordinate multiple layouts in a RecyclerView or ViewPager without boilerplate.

- Multiple item layouts in a single adapter with no typecasting.
- Per-item span, swipe, and drag behavior in a RecyclerView in just a few lines of code.
- Add, remove, and update multiple layouts in a ViewPager without touching Fragments.

# Usage

The examples will be in Kotlin, but this library works just as well with Java.

#### Create the adapter:

```kotlin
// Create the adapter
val adapter = FlexAdapter<Any>()
recyclerView.adapter = adapter
val layoutManager = GridLayoutManager(this, 3)
// Add this line to enable per-item spans on GridLayoutManagers
layoutManager.spanSizeLookup = adapter.spanSizeLookup
recyclerView.layoutManager = layoutManager
```

#### Define your item types:

``` kotlin
// This item will be a text header with a span of three that can be swiped horizontally to dismiss.
class TextItem(var text: String) :
        FlexAdapterExtensionItem(R.layout.item_text, span = 3, swipeDirs = HORIZONTAL) {
    override fun bindItemView(itemView: View, position: Int) {
        itemView.text_view.text = text
    }
}

// This will be a picture loaded from a resource the can be reordered by dragging in any direction.
class PictureItem(@DrawableRes val imageRes: Int) :
        FlexAdapterExtensionItem(R.layout.item_picture, dragDirs = ALL_DIRS) {
    override fun bindItemView(itemView: View, position: Int) {
        itemView.image_view.setImageResource(imageRes)
    }
}
```

Each layout in the adapter gets its own item class, which has fields for any
mutable data in its layout. Since the items own the data and know how to bind
it to a view holder, there are no intermediate data holder classes, no
interfaces, and no casting to a base class and back.

##### Using your own data models

If you're using Kotlin, you don't even need your items to inherit from `FlexAdapterItem`. You can register a view binder for each type you'd like to add instead:

```kotlin
val adapter = FlexAdapter<String>()
adapter.register<String>(R.layout.text_layout) { str, view, position ->
    view.text_view.text = str
}
adapter.add("title")
```

You can use `register` in the same adapter that you add `FlexAdapterItem`s, pick whichever method you prefer.

#### Then add some items:

```kotlin
adapter.items.addAll(listOf(
    TextItem("Look at these pictures"),
    PictureItem(R.drawable.picture_1),
    PictureItem(R.drawable.picture_2)
))
```

All structural changes to the `items` list automatically updated the `RecyclerView`. No need to call `noifyItemRangeAdded`.

#### Update an existing item:

```kotlin
// If we added this TextItem earlier
textItem.text = "Look at this new text"
adapter.notifyItemChanged(textItem)
```

When you want to update an item, just change its data and notify the adapter that it changed.

That's it. No managing indices. No casting from interfaces or Object. 
Just fast, simple code that does exactly what you want.

### Item selection

FlexAdapter can manage a set of selected items and pass the selection state to your view binders. Just inherit from `FlexAdapterSelectableItem` or use the `register` overload that takes the boolean selection state.

```kotlin
class PictureItem(@DrawableRes val imageRes: Int) :
        FlexAdapterSelectableExtensionItem(R.layout.item_picture, dragDirs = ALL_DIRS) {
    override fun bindItemView(itemView: View, selected:Boolean, position: Int) {
        itemView.image_view.setImageResource(imageRes)
        itemView.switch.selected = selected
    }
}

adapter.register<String>(R.layout.item_text) { str, view, selected, position ->
    view.text_view.text = str
    view.switch.selected = selected
}

adapter.add(PictureItem(R.drawable.image))
adapter.add("Title")

adapter.selectItem("Title")
```

# API Documentation

API documentation is hosted online [here](https://jitpack.io/com/github/ajalt/flexadapter/2.1.0/javadoc/flexadapter/com.github.ajalt.flexadapter/index.html).

# Sample project

To see more of the features of FlexAdapter in use, check out the Kotlin sample app 
[here](sample/src/main/kotlin/com/github/ajalt/flexadapter/sample/MainActivity.kt),
 or the Java sample app 
[here](sample/src/main/java/com/github/ajalt/flexadapter/sample/JavaMainActivity.kt)

# FlexAdapter for a ViewPager

This library also includes an adapter for a `ViewPager` that provides the same interface as the regular `FlexAdapter`: the [FlexPagerAdapter](https://jitpack.io/com/github/ajalt/flexadapter/2.1.0/javadoc/flexadapter/com.github.ajalt.flexadapter/-flex-pager-adapter/index.html)

```kotlin
val adapter = FlexPagerAdapter()

adapter.addItems(
    TextItem("Look at these pictures"),
    PictureItem(R.drawable.picture_1),
    PictureItem(R.drawable.picture_2)
)

view_pager.adapter = adapter
```

That's all that's required to implement multiple layouts in a ViewPager. No need to create Fragments or manage lifecycles. The same classes can be used in both the `FlexAdapter` and `FlexPagerAdapter`. The `span`, `dragDirs`, and `swipeDirs` values will just be ignored on items added to a `FlexPagerAdapter`.

You can see a sample that uses the `FlexPagerAdapter` [here](sample/src/main/kotlin/com/github/ajalt/flexadapter/sample/ViewPagerActivity.kt).

# Download

FlexAdapter is distributed with [JitPack](https://jitpack.io/#ajalt/flexadapter/2.1.0)

```groovy
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
   compile 'com.github.ajalt:flexadapter:2.1.0'
}
```

# License
```
Copyright 2016 AJ Alt

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
