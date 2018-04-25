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
import com.rpham64.android.photogallery.utils.PagedResult;
import com.rpham64.android.photogallery.utils.PreCachingLayoutManager;
import com.rpham64.android.photogallery.utils.QueryPreferences;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import jp.wasabeef.recyclerview.adapters.SlideInBottomAnimationAdapter;
import jp.wasabeef.recyclerview.animators.FadeInAnimator;
import tr.xip.errorview.ErrorView;

/**
 * Main Fragment hosted by PhotoGalleryActivity
 *
 * Created by Rudolf on 3/12/2016.
 */
public class PhotoGalleryFragment extends Fragment implements View.OnClickListener,
        PhotoGalleryContract.View, SearchView.OnQueryTextListener,
        SwipeRefreshLayout.OnRefreshListener, OnMoreListener, ErrorView.RetryListener {

    private static final String TAG = PhotoGalleryFragment.class.getName();

    // Offset subtracted from total number of photos to call the "load more" function".
    private static final int LOAD_MORE_OFFSET = 30;

    @BindView(R.id.toolbar_fragment_photo_gallery) Toolbar toolbar;
    @BindView(R.id.recycler_view_photo_gallery_fragment) SuperRecyclerView recyclerView;
    @BindView(R.id.error) ErrorView viewError;

    private Unbinder mUnbinder;

    private PhotoAdapter mAdapter;
    private PhotoGalleryPresenter mPresenter;
    private SearchView viewSearch;

    private List<Photo> mPhotos;

    private String mQuery;
    private int mCurrentPage;
    private int mPages;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);            // Retain fragment state on configuration changes
        setHasOptionsMenu(true);            // Include toolbar

        mPresenter = new PhotoGalleryPresenter();
        mCurrentPage = 1;

        // Retrieve last saved search query, if it exists.
        mQuery = QueryPreferences.getStoredQuery(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mPresenter.attachView(this);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        setupRecyclerView();

        viewError.setRetryListener(this);

        // Retrieve first list of photos.
        fetchPhotos();

        return view;
    }

    private void setupRecyclerView() {
        final PreCachingLayoutManager mLayoutManager = buildPreCachingLayoutManager();
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.getRecyclerView().setItemAnimator(new FadeInAnimator());
        recyclerView.setRefreshListener(this);
        recyclerView.setOnMoreListener(this);
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
            recyclerView.setAdapter(animationAdapter);
        }
    }

    private void setupItemAnimator(int duration) {
        final RecyclerView.ItemAnimator animator = recyclerView.getRecyclerView().getItemAnimator();
        animator.setAddDuration(duration);
        animator.setRemoveDuration(duration);
        animator.setMoveDuration(duration);
        animator.setChangeDuration(duration);
    }

    @Override
    public void onDestroyView() {
        mPresenter.detachView();
        mUnbinder.unbind();
        super.onDestroyView();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.menu_fragment_photo_gallery, menu);

        final MenuItem itemSearch = menu.findItem(R.id.menu_item_search);

        viewSearch = (SearchView) MenuItemCompat.getActionView(itemSearch);
        viewSearch.setOnQueryTextListener(this);
        viewSearch.setOnClickListener(this);
        viewSearch.setMaxWidth(Integer.MAX_VALUE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_item_refresh:
                refresh();
                return true;

            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Displays the list of photos.
     *
     * @param photos List of photos to display in the gallery.
     * @param pagedResult Object for keeping track of current page of results.
     */
    @Override
    public void showPhotos(List<Photo> photos, PagedResult pagedResult) {
        if (viewError.getVisibility() == View.VISIBLE) {
            recyclerView.setVisibility(View.VISIBLE);
            viewError.setVisibility(View.GONE);
        }

        mCurrentPage = pagedResult.page;
        mPages = pagedResult.pages;
        Toast.makeText(getContext(), "Loading page: " + mCurrentPage, Toast.LENGTH_SHORT).show();

        if (mCurrentPage == 1) {
            // Remove all old photos and set to the new photo list.
            mPhotos = photos;
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

        if (canLoadMore && mCurrentPage < mPages) {
            // Increment page number to load the next page.
            mCurrentPage++;
            fetchPhotos();
        } else if (mCurrentPage == mPages) {
            // No more pages to load, so display an error toast.
            Toast.makeText(getContext(), R.string.toast_no_more_pages_to_load,
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRefresh() {
        refresh();
    }

    @Override
    public void refresh() {
        mQuery = QueryPreferences.getStoredQuery(getActivity());
        mCurrentPage = 1;
        fetchPhotos();
        recyclerView.getRecyclerView().getLayoutManager().scrollToPosition(0);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Log.i(TAG, "QueryTextSubmit: " + query);

        QueryPreferences.setStoredQuery(getActivity(), query);

        viewSearch.clearFocus();            // Hides keyboard on submit
        viewSearch.setQuery("", false);
        viewSearch.setIconified(true);      // Collapses SearchView widget

        refresh();

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        Log.d(TAG, "QueryTextChange: " + newText);
        return false;
    }

    @Override
    public void showError() {
        recyclerView.setVisibility(View.GONE);
        viewError.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRetry() {
        Toast.makeText(getContext(), "Retrying...", Toast.LENGTH_SHORT).show();
        refresh();
    }

    @Override
    public void onClick(View v) {
        String query = QueryPreferences.getStoredQuery(getActivity());
        viewSearch.setQuery(query, false);
    }

    /**
     * Fetches recent photos (if search query is empty) or by search (if search query exists).
     */
    private void fetchPhotos() {
        if (mQuery == null) {
            mPresenter.getRecentPhotos(mCurrentPage);
        } else {
            mPresenter.searchPhotos(mQuery, mCurrentPage);
        }
    }
}
