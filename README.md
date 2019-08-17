<h1 align="center">
	<img src=".web/wordmark.png">
</h1>

### Define and coordinate multiple layouts in a RecyclerView  without boilerplate.

- Multiple item layouts in a single adapter with no typecasting.
- Per-item span, swipe, and drag behavior in a RecyclerView in just a few lines of code.
- Define all your view bindings without inheritance or custom ViewHolders

# Usage

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

#### Register your view bindings:

```kotlin
// This item will be a text header with a span of three that can be swiped horizontally to dismiss.
adapter.register<String>(R.layout.text_item, span = 3, swipeDirs = HORIZONTAL) {
    // View lookup is cached. You could also Android data binding or Kotlin Android Extensions instead. 
    view<TextView>(R.id.text_view).text = it
}

// This will be a picture loaded from a resource the can be reordered by dragging in any direction.
data class Picture(val resId: Int, var caption: Text)
adapter.register<Picture>(R.layout.picture_item, dragDirs = ALL_DIRS) {
    view<ImageView>(R.id.image_view).setImageResource(it.resId)
    view<TextView>(R.id.caption).setImageResource(it.caption)
}
```

Each type of item you want is registered before you add it to the to add to the adapter. 

#### Then add some items:

```kotlin
adapter.items.addAll(listOf(
    "Look at these pictures",
    Picture(R.drawable.picture_1, "First Picture"),
    Picture(R.drawable.picture_2, "Second Picture")
))
```

All structural changes to the `items` list automatically update the `RecyclerView`. No need to call
`noifyItemRangeAdded`.

#### Update an existing item:

```kotlin
// If we added this Picture earlier
picture.text = "Look at this new text"
adapter.notifyItemChanged(picture)
```

When you want to update an item, just change its data and notify the adapter that it changed.

#### Per-item spans and drag directions

If you want different items of the same class to have different spans or drag directions, you can have your
item classes inherit from `AdapterItem` or `CachedAdapterItem`. Implementations of those classes don't need to
be `register`ed, and can each instance can have difference `dragDir`s and `span`s.

### Stable item ids

You can optionally tun on stable ids with `adapter.setHasStableIds(true)`. By default, `AdapterItem`
subclasses will each get a unique id per-instance, and item hash codes will be used for other types. You can
customize the stable ids in subclasses of `AdapterItem`.

### Item selection

FlexAdapter can manage a set of selected items and keep it in sync with changes to the adapter items.

```kotlin
val adapter = FlexAdapter<String>()
val selection = adapter.selectionTracker()
adapter.register<String>(R.layout.text_item) {
    view<TextView>(R.id.text_view).text = it
    view<Switch>(R.id.switch).selected = selection.isSelected(it)
}

adapter.add("Example")
selection.selectItem("Example")
```

# API Documentation

API documentation is hosted online [here](https://jitpack.io/com/github/ajalt/flexadapter/2.2.0/javadoc/flexadapter/com.github.ajalt.flexadapter/index.html).

# Sample project

To see more of the features of FlexAdapter in use, check out the Kotlin sample app 
[here](sample/src/main/kotlin/com/github/ajalt/flexadapter/sample/MainActivity.kt),
 or the Java sample app 
[here](sample/src/main/java/com/github/ajalt/flexadapter/sample/JavaMainActivity.kt)

# Download

FlexAdapter is distributed with [JitPack](https://jitpack.io/#ajalt/flexadapter/2.2.0)

```groovy
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
   compile 'com.github.ajalt:flexadapter:2.2.0'
}
```

# License
```
Copyright 2018 AJ Alt

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
