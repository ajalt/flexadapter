package com.github.ajalt.flexadapter.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.ajalt.flexadapter.FlexAdapter;
import com.github.ajalt.flexadapter.FlexAdapterItem;

import org.jetbrains.annotations.NotNull;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import kotlin.collections.CollectionsKt;
import kotlin.jvm.functions.Function1;

public class JavaSampleActivity extends AppCompatActivity {
    public static final int HORIZONTAL = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
    public static final int VERTICAL = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
    public static final int ALL_DIRS = HORIZONTAL | VERTICAL;
    public static final int COLUMNS = 3;

    private static final Random rand = new Random();

    private static int randomColor() {
        return Color.HSVToColor(new float[]{rand.nextFloat() * 360, .75f, .8f});
    }

    static class TextItem extends FlexAdapterItem<TextItem.ViewHolder> {
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

        public static class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.text_view) TextView textView;

            public ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

    static class HeaderItem extends FlexAdapterItem<HeaderItem.ViewHolder> {
        public int text;

        public HeaderItem(@StringRes int text) { this.text = text; }

        @Override public int getSpan() { return COLUMNS; }

        @NotNull @Override public Function1<ViewGroup, ViewHolder> viewHolderFactory() {
            return parent -> new ViewHolder(inflate(parent, R.layout.item_header));
        }

        @Override public void bindViewHolder(@NotNull ViewHolder holder, int position) {
            holder.textView.setText(this.text);
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.text_view) TextView textView;

            public ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

    static class WidePictureItem extends FlexAdapterItem<WidePictureItem.ViewHolder> {
        private final int swipeDirs;
        private final int color = randomColor();

        public WidePictureItem(int swipeDirs) {
            this.swipeDirs = swipeDirs;
        }

        @Override public int getSpan() { return COLUMNS; }

        @Override public int getSwipeDirs() { return swipeDirs; }

        @NotNull @Override public Function1<ViewGroup, ViewHolder> viewHolderFactory() {
            return parent -> new ViewHolder(inflate(parent, R.layout.item_color_square_wide));
        }

        @Override public void bindViewHolder(@NotNull ViewHolder holder, int position) {
            holder.card.setCardBackgroundColor(color);
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.card) CardView card;

            public ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

    static class SquarePictureItem extends FlexAdapterItem<SquarePictureItem.ViewHolder> {
        private final int color = randomColor();

        @Override public int getDragDirs() { return ALL_DIRS; }

        @NotNull @Override public Function1<ViewGroup, ViewHolder> viewHolderFactory() {
            return parent -> new ViewHolder(inflate(parent, R.layout.item_color_square_large));
        }

        @Override public void bindViewHolder(@NotNull ViewHolder holder, int position) {
            holder.card.setCardBackgroundColor(color);
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.card) CardView card;

            public ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

    static class DividerItem extends FlexAdapterItem<DividerItem.ViewHolder> {
        @Override public int getSpan() {
            return COLUMNS;
        }

        @NotNull @Override public Function1<ViewGroup, ViewHolder> viewHolderFactory() {
            return parent -> new ViewHolder(inflate(parent, R.layout.item_divider));
        }

        @Override public void bindViewHolder(@NotNull ViewHolder holder, int position) {
            // Nothing to bind for this item
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {
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

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);
        ButterKnife.bind(this);

        GridLayoutManager layoutManager = new GridLayoutManager(this, COLUMNS);
        layoutManager.setSpanSizeLookup(adapter.getSpanSizeLookup());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);

        final HeaderItem header1 = new HeaderItem(R.string.title_drag_all);
        final HeaderItem header2 = new HeaderItem(R.string.title_swipe);

        adapter.items().addAll(CollectionsKt.listOf(
                header1,
                new SquarePictureItem(),
                new SquarePictureItem(),
                new SquarePictureItem(),
                new SquarePictureItem(),
                new SquarePictureItem(),
                new SquarePictureItem(),
                new SquarePictureItem(),
                new SquarePictureItem(),
                new SquarePictureItem(),
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
                new WidePictureItem(HORIZONTAL),
                new HeaderItem(R.string.title_no_swipe),
                new WidePictureItem(0)
        ));

        // Change the header text when the car picture is swiped away
        adapter.setItemSwipedListener(item -> {
            header2.text = R.string.title_post_swipe;
            adapter.notifyItemChanged(adapter.items().indexOf(header2));
        });

        fab.setOnClickListener(v -> {
                final SquarePictureItem item = new SquarePictureItem();
                adapter.items().add(adapter.items().indexOf(header1) + 1, item);
                Snackbar.make(rootView, R.string.snackbar_add_success, Snackbar.LENGTH_SHORT)
                        .setAction(R.string.action_undo, v1 -> adapter.items().remove(item)).show();
        });
    }
}
