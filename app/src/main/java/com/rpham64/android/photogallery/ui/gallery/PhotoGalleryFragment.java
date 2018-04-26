package com.rpham64.android.photogallery.ui.gallery;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.malinskiy.superrecyclerview.OnMoreListener;
import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.rpham64.android.photogallery.R;
import com.rpham64.android.photogallery.models.Photo;
import com.rpham64.android.photogallery.ui.adapters.PhotoAdapter;
import com.rpham64.android.photogallery.utils.PreCachingLayoutManager;
import com.rpham64.android.photogallery.utils.SearchQuerySharedPreference;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import tr.xip.errorview.ErrorView;

/**
 * Main UI for the Photo Gallery screen. Displays a grid of photos that users can see and interact
 * with.
 */
public class PhotoGalleryFragment extends Fragment implements PhotoGalleryContract.View,
        SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener, OnMoreListener,
        ErrorView.RetryListener {

    private static final String TAG = PhotoGalleryFragment.class.getName();

    // Offset subtracted from total number of photos to call the "load more" function".
    private static final int LOAD_MORE_OFFSET = 30;

    @BindView(R.id.toolbar_fragment_photo_gallery) Toolbar mToolbar;
    @BindView(R.id.recycler_view_photo_gallery_fragment) SuperRecyclerView mRecyclerViewPhotos;
    @BindView(R.id.error) ErrorView mViewError;

    @OnClick
    public void onSearchViewClicked() {
        mPresenter.onSearchViewClicked();
    }

    private Unbinder mUnbinder;

    private PhotoGalleryContract.Presenter mPresenter;

    private PhotoAdapter mAdapter;
    private SearchView mViewSearch;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);  // Retain fragment state on configuration changes
        setHasOptionsMenu(true);  // Include toolbar
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);

        setupRecyclerView();

        mViewError.setRetryListener(this);

        // Retrieve first list of photos.
        mPresenter.getPhotos();

        return view;
    }

    private void setupRecyclerView() {
        final PreCachingLayoutManager mLayoutManager = buildPreCachingLayoutManager();
        mRecyclerViewPhotos.setLayoutManager(mLayoutManager);
        mRecyclerViewPhotos.getRecyclerView().setItemAnimator(new FadeInAnimator());
        mRecyclerViewPhotos.setRefreshListener(this);
        mRecyclerViewPhotos.setOnMoreListener(this);
        setupAdapter(new ArrayList<>());
        setupItemAnimator(1000);
    }

    @NonNull
    private PreCachingLayoutManager buildPreCachingLayoutManager() {
        // Set layout manager to precache a full screen of contents
        DisplayMetrics displayMetrics = new DisplayMetrics();
        int heightPixels = displayMetrics.heightPixels;

        return new PreCachingLayoutManager(getActivity(), 3, heightPixels);
    }

    private void setupAdapter(List<Photo> photos) {
        if (isAdded()) {
            mAdapter = new PhotoAdapter(getContext(), photos);
            SlideInBottomAnimationAdapter animationAdapter = new SlideInBottomAnimationAdapter(mAdapter);
            animationAdapter.setDuration(500);
            mRecyclerViewPhotos.setAdapter(animationAdapter);
        }
    }

    private void setupItemAnimator(int duration) {
        final RecyclerView.ItemAnimator animator = mRecyclerViewPhotos.getRecyclerView().getItemAnimator();
        animator.setAddDuration(duration);
        animator.setRemoveDuration(duration);
        animator.setMoveDuration(duration);
        animator.setChangeDuration(duration);
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.menu_fragment_photo_gallery, menu);

        final MenuItem itemSearch = menu.findItem(R.id.menu_item_search);

        mViewSearch = (SearchView) MenuItemCompat.getActionView(itemSearch);
        mViewSearch.setOnQueryTextListener(this);
        mViewSearch.setMaxWidth(Integer.MAX_VALUE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_item_refresh:
                mPresenter.refresh();
                return true;

            case R.id.menu_item_clear:
                mPresenter.clearSearchQuery();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Displays the list of photos.
     *
     * @param photos List of photos to display in the gallery.
     * @param currentPage Current page number of results
     */
    @Override
    public void showPhotos(List<Photo> photos, int currentPage) {
        hideError();

        Toast.makeText(getContext(), "Loading page: " + currentPage, Toast.LENGTH_SHORT).show();

        if (currentPage == 1) {
            // Remove all old photos from adapter and set to the new photo list.
            mAdapter.setPhotos(photos);
        } else {
            // Add the new photos to the adapter.
            mAdapter.addPhotos(photos);
        }
    }

    @Override
    public void onMoreAsked(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
        boolean canLoadMore =
                itemsBeforeMore + maxLastVisiblePosition + LOAD_MORE_OFFSET >= overallItemsCount;

        if (canLoadMore) {
            mPresenter.loadMorePhotos();
        } else {
            showCannotLoadMoreToast();
        }
    }

    @Override
    public void showCannotLoadMoreToast() {
        // No more photos to load, so display an error toast.
        Toast.makeText(getContext(), R.string.toast_no_more_photos_to_load,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setSearchViewQuery(String query) {
        mViewSearch.setQuery(query, false);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.i(TAG, "Query submitted: " + query);

        mPresenter.storeSearchQuery(query);
        mPresenter.getPhotosBySearch(query, 1);  // Clear old results and display page 1 of search results.

        // Clear query in SearchView.
        mViewSearch.setQuery("", false);

        // Hide keyboard on submit.
        mViewSearch.clearFocus();

        // Collapse SearchView widget.
        mViewSearch.setIconified(true);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d(TAG, "Query: " + newText);
        return false;
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    @Override
    public void refresh() {
        mRecyclerViewPhotos.getRecyclerView().getLayoutManager().scrollToPosition(0);
        mPresenter.refresh();
    }

    @Override
    public void onRetry() {
        Toast.makeText(getContext(), "Retrying...", Toast.LENGTH_SHORT).show();
        mPresenter.refresh();
    }

    @Override
    public void showError() {
        mRecyclerViewPhotos.setVisibility(View.GONE);
        mViewError.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideError() {
        mRecyclerViewPhotos.setVisibility(View.VISIBLE);
        mViewError.setVisibility(View.GONE);
    }

    @Override
    public void setPresenter(PhotoGalleryContract.Presenter presenter) {
        mPresenter = presenter;
    }
}