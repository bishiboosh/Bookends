package com.tumblr.bookends;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A RecyclerView.Adapter that allows for headers and footers as well.
 *
 * This class wraps a base adapter that's passed into the constructor. It works by creating extra
 * view items types that are returned in {@link #getItemViewType(int)}, and mapping these to the
 * header and footer views provided via {@link #addHeader(android.view.View)} and {@link
 * #addFooter(android.view.View)}.
 *
 * Created by mlapadula on 12/15/14.
 */
public class Bookends<T extends RecyclerView.Adapter>
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final T mBase;

    private final List<View> mHeaders = new ArrayList<View>();

    private final IntArrayList mHeaderIds = new IntArrayList();

    private final List<View> mFooters = new ArrayList<View>();

    private final IntArrayList mFooterIds = new IntArrayList();

    /**
     * Constructor.
     *
     * @param base the adapter to wrap
     */
    public Bookends(@NonNull T base) {
        super();
        mBase = base;
    }

    /**
     * Gets the base adapter that this is wrapping.
     */
    public T getWrappedAdapter() {
        return mBase;
    }

    /**
     * Adds a header view.
     */
    public void addHeader(@NonNull View view) {
        mHeaders.add(view);
        mHeaderIds.add(generateId());
    }

    /**
     * Adds a footer view.
     */
    public void addFooter(@NonNull View view) {
        mFooters.add(view);
        mFooterIds.add(generateId());
    }

    /**
     * Toggles the visibility of the header views.
     */
    public void setHeaderVisibility(boolean shouldShow) {
        for (View header : mHeaders) {
            header.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Toggles the visibility of the footer views.
     */
    public void setFooterVisibility(boolean shouldShow) {
        for (View footer : mFooters) {
            footer.setVisibility(shouldShow ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * @return the number of headers.
     */
    public int getHeaderCount() {
        return mHeaders.size();
    }

    /**
     * @return the number of footers.
     */
    public int getFooterCount() {
        return mFooters.size();
    }

    /**
     * Gets the indicated header, or null if it doesn't exist.
     */
    public View getHeader(int i) {
        return i < mHeaders.size() ? mHeaders.get(i) : null;
    }

    public int getHeaderId(int i) {
        return i < mHeaderIds.size() ? mHeaderIds.get(i) : RecyclerView.INVALID_TYPE;
    }

    /**
     * Gets the indicated footer, or null if it doesn't exist.
     */
    public View getFooter(int i) {
        return i < mFooters.size() ? mFooters.get(i) : null;
    }

    public int getFooterId(int i) {
        return i < mFooterIds.size() ? mFooterIds.get(i) :  RecyclerView.INVALID_TYPE;
    }

    private boolean isHeader(int viewType) {
        return mHeaderIds.contains(viewType);
    }

    private boolean isFooter(int viewType) {
        return mFooterIds.contains(viewType);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (isHeader(viewType)) {
            int whichHeader = mHeaderIds.indexOf(viewType);
            View headerView = mHeaders.get(whichHeader);
            return new RecyclerView.ViewHolder(headerView) {
            };
        } else if (isFooter(viewType)) {
            int whichFooter = mFooterIds.indexOf(viewType);
            View footerView = mFooters.get(whichFooter);
            return new RecyclerView.ViewHolder(footerView) {
            };

        } else {
            return mBase.onCreateViewHolder(viewGroup, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (position < mHeaders.size()) {
            // Headers don't need anything special

        } else if (position < mHeaders.size() + mBase.getItemCount()) {
            // This is a real position, not a header or footer. Bind it.
            mBase.onBindViewHolder(viewHolder, position - mHeaders.size());

        } else {
            // Footers don't need anything special
        }
    }

    @Override
    public int getItemCount() {
        return mHeaders.size() + mBase.getItemCount() + mFooters.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < mHeaders.size()) {
            return mHeaderIds.get(position);

        } else if (position < (mHeaders.size() + mBase.getItemCount())) {
            return mBase.getItemViewType(position - mHeaders.size());

        } else {
            return mFooterIds.get(position - mBase.getItemCount() - mHeaders.size());
        }
    }

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * Generate an id, used for the headers and footers view id.
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    private static int generateId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) {
                newValue = 1; // Roll over to 1, not 0.
            }
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }
}
