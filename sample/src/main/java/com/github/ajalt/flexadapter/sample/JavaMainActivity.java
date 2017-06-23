package com.github.ajalt.flexadapter.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ajalt.flexadapter.FlexAdapter;
import com.github.ajalt.flexadapter.FlexAdapterItem;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function1;

public class JavaMainActivity extends AppCompatActivity {
    public static final int HORIZONTAL = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
    public static final int VERTICAL = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
    public static final int ALL_DIRS = HORIZONTAL | VERTICAL;
    public static final int COLUMNS = 3;

    class TextItem extends FlexAdapterItem<TextItem.ViewHolder> {
        public int text;
        private final int dragDirs;

        public TextItem(@StringRes int text, int dragDirs) {
            this.text = text;
            this.dragDirs = dragDirs;
        }

        @Override public int getDragDirs() { return dragDirs; }

        @Override public int getSpan() { return COLUMNS; }

        @NotNull @Override public Function1<ViewGroup, ViewHolder> viewHolderFactory() {
            return parent -> new ViewHolder(inflate(parent, R.layout.item_text));
        }

        @Override public void bindViewHolder(@NotNull ViewHolder holder, int position) {
            holder.textView.setText(this.text);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.text_view) TextView textView;

            public ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

    class HeaderItem extends FlexAdapterItem<HeaderItem.ViewHolder> {
        public int text;

        public HeaderItem(@StringRes int text) { this.text = text; }

        @Override public int getSpan() { return COLUMNS; }

        @NotNull @Override public Function1<ViewGroup, ViewHolder> viewHolderFactory() {
            return parent -> new ViewHolder(inflate(parent, R.layout.item_header));
        }

        @Override public void bindViewHolder(@NotNull ViewHolder holder, int position) {
            holder.textView.setText(this.text);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.text_view) TextView textView;

            public ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

    class WidePictureItem extends FlexAdapterItem<WidePictureItem.ViewHolder> {
        public int image;
        private final int swipeDirs;

        public WidePictureItem(@DrawableRes int image, int swipeDirs) {
            this.image = image;
            this.swipeDirs = swipeDirs;
        }

        @Override public int getSpan() { return COLUMNS; }

        @Override public int getSwipeDirs() { return swipeDirs; }

        @NotNull @Override public Function1<ViewGroup, ViewHolder> viewHolderFactory() {
            return parent -> new ViewHolder(inflate(parent, R.layout.item_picture));
        }

        @Override public void bindViewHolder(@NotNull ViewHolder holder, int position) {
            holder.imageView.setImageResource(this.image);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.image_view) ImageView imageView;

            public ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

    class SquarePictureItem extends FlexAdapterItem<SquarePictureItem.ViewHolder> {
        public int image;

        public SquarePictureItem(@DrawableRes int image) {
            this.image = image;
        }

        @Override public int getDragDirs() { return ALL_DIRS; }

        @NotNull @Override public Function1<ViewGroup, ViewHolder> viewHolderFactory() {
            return parent -> new ViewHolder(inflate(parent, R.layout.item_picture_square));
        }

        @Override public void bindViewHolder(@NotNull ViewHolder holder, int position) {
            holder.imageView.setImageResource(this.image);
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.image_view) ImageView imageView;

            public ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

    class DividerItem extends FlexAdapterItem<DividerItem.ViewHolder> {
        @Override public int getSpan() {
            return COLUMNS;
        }

        @NotNull @Override public Function1<ViewGroup, ViewHolder> viewHolderFactory() {
            return parent -> new ViewHolder(inflate(parent, R.layout.item_divider));
        }

        @Override public void bindViewHolder(@NotNull ViewHolder holder, int position) {
            // Nothing to bind for this item
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public ViewHolder(View view) { super(view); }
        }
    }

    private static View inflate(ViewGroup parent, @LayoutRes int res) {
        return LayoutInflater.from(parent.getContext()).inflate(res, parent, false);
    }

    @BindView(R.id.root_layout)
    View rootView;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.fab)
    FloatingActionButton fab;


    private final FlexAdapter<FlexAdapterItem> adapter = new FlexAdapter<>();
    private int extraBurtsAdded = 0;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        GridLayoutManager layoutManager = new GridLayoutManager(this, COLUMNS);
        layoutManager.setSpanSizeLookup(adapter.getSpanSizeLookup());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        final HeaderItem header1 = new HeaderItem(R.string.title_drag_all);
        final HeaderItem header2 = new HeaderItem(R.string.title_swipe);

        adapter.items().addAll(CollectionsKt.listOf(
                header1,
                new SquarePictureItem(R.drawable.burt_square_1),
                new SquarePictureItem(R.drawable.burt_square_2),
                new SquarePictureItem(R.drawable.burt_square_3),
                new SquarePictureItem(R.drawable.burt_square_4),
                new SquarePictureItem(R.drawable.burt_square_5),
                new SquarePictureItem(R.drawable.burt_square_6),
                new SquarePictureItem(R.drawable.burt_square_7),
                new SquarePictureItem(R.drawable.burt_square_8),
                new SquarePictureItem(R.drawable.burt_square_9),
                new DividerItem(),
                new HeaderItem(R.string.title_drag_vertical),
                new TextItem(R.string.list_drag_01, 0),
                new TextItem(R.string.list_drag_02, VERTICAL),
                new TextItem(R.string.list_drag_03, VERTICAL),
                new TextItem(R.string.list_drag_04, VERTICAL),
                new TextItem(R.string.list_drag_05, VERTICAL),
                new TextItem(R.string.list_drag_06, VERTICAL),
                new DividerItem(),
                header2,
                new WidePictureItem(R.drawable.burt_wide_1, HORIZONTAL),
                new HeaderItem(R.string.title_no_swipe),
                new WidePictureItem(R.drawable.burt_wide_2, 0)
        ));

        // Change the header text when the car picture is swiped away
        adapter.setItemSwipedListener(item -> {
            header2.text = R.string.title_post_swipe;
            adapter.notifyItemChanged(adapter.items().indexOf(header2));
        });

        final FlexAdapterItem<?>[] extraBurts = {
                new SquarePictureItem(R.drawable.burt_square_10),
                new SquarePictureItem(R.drawable.burt_square_11),
                new SquarePictureItem(R.drawable.burt_square_12),
        };

        fab.setOnClickListener(v -> {
            if (extraBurtsAdded >= extraBurts.length) {
                Snackbar.make(rootView, R.string.snackbar_add_failure, Snackbar.LENGTH_SHORT).show();
            } else {
                final FlexAdapterItem<?> item = extraBurts[extraBurtsAdded++];
                adapter.items().add(adapter.items().indexOf(header1) + 1, item);
                Snackbar.make(rootView, R.string.snackbar_add_success, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.action_undo, v1 -> {
                            adapter.items().remove(item);
                            extraBurtsAdded--;
                        }).show();
            }
        });
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "Switch to Kotlin sample");
        menu.add(1, 1, 1, "Switch to view pager sample");
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 0) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (item.getItemId() == 1) {
            startActivity(new Intent(this, ViewPagerActivity.class));
        }
        return true;
    }
}
