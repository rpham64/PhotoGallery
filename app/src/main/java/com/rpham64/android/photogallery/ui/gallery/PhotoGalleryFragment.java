package com.rpham64.android.photogallery.ui.gallery;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.rpham64.android.photogallery.ApplicationController;
import com.rpham64.android.photogallery.R;
import com.rpham64.android.photogallery.models.Photo;
import com.rpham64.android.photogallery.services.PollService;
import com.rpham64.android.photogallery.ui.VisibleFragment;
import com.rpham64.android.photogallery.ui.adapters.PhotoAdapter;
import com.rpham64.android.photogallery.utils.PagedResult;
import com.rpham64.android.photogallery.utils.PreCachingLayoutManager;
import com.rpham64.android.photogallery.utils.QueryPreferences;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import tr.xip.errorview.ErrorView;

/**
 * Main Fragment hosted by PhotoGalleryActivity
 *
 * Created by Rudolf on 3/12/2016.
 */
public class PhotoGalleryFragment extends VisibleFragment implements View.OnClickListener,
        PhotoGalleryPresenter.View, UltimateRecyclerView.OnLoadMoreListener,
        SearchView.OnQueryTextListener, ErrorView.RetryListener {

    private static final String TAG = PhotoGalleryFragment.class.getName();

    @BindView(R.id.recycler_view_photo_gallery_fragment) UltimateRecyclerView recyclerView;
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

        mCurrentPage = 1;
        mQuery = QueryPreferences.getStoredQuery(getActivity());
        mPresenter = new PhotoGalleryPresenter();
        mPresenter.getPage(mCurrentPage, mQuery);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mPresenter.attachView(this);

        // Setup GridLayoutManager as RecyclerView's layout manager
        final PreCachingLayoutManager mLayoutManager = new PreCachingLayoutManager(getActivity(), 3);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        int heightPixels = displayMetrics.heightPixels;

        mLayoutManager.setExtraLayoutSpace(heightPixels);

        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(15);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        recyclerView.setDefaultOnRefreshListener(() -> refresh());
        recyclerView.reenableLoadmore();
        recyclerView.setOnLoadMoreListener(this);

        viewError.setOnRetryListener(this);

        setupAdapter(mPhotos);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);

        menuInflater.inflate(R.menu.fragment_photo_gallery, menu);

        final MenuItem itemToggle = menu.findItem(R.id.menu_item_toggle_polling);
        final MenuItem itemSearch = menu.findItem(R.id.menu_item_search);

        viewSearch = (SearchView) itemSearch.getActionView();
        viewSearch.setOnQueryTextListener(this);
        viewSearch.setOnClickListener(this);
        viewSearch.setMaxWidth(Integer.MAX_VALUE);          // Max Width

        if (PollService.isServiceAlarmOn(getActivity())) {
            itemToggle.setTitle(R.string.stop_polling);
        } else {
            itemToggle.setTitle(R.string.start_polling);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
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

            case R.id.menu_item_toggle_polling:

                boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
                PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
                getActivity().invalidateOptionsMenu();

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public void showPictures(List<Photo> photos, PagedResult pagedResult) {

        if (viewError.getVisibility() == View.VISIBLE) {
            recyclerView.setVisibility(View.VISIBLE);
            viewError.setVisibility(View.GONE);
        }

        mCurrentPage = pagedResult.page;
        mPages = pagedResult.pages;
        Toast.makeText(getContext(), "Loading page: " + mCurrentPage, Toast.LENGTH_SHORT).show();

        if (mCurrentPage == 1) {
            mPhotos = photos;
            setupAdapter(photos);
            mAdapter.notifyDataSetChanged();
        } else {
            mAdapter.addPhotos(photos);
        }

    }

    @Override
    public void loadMore(int itemsCount, int maxLastVisiblePosition) {
        if (mCurrentPage < mPages) {
            mPresenter.getPage(mCurrentPage + 1, mQuery);
        } else {
            // Disable load more
            Toast.makeText(getContext(), "No more pictures to show.", Toast.LENGTH_SHORT).show();
            recyclerView.disableLoadmore();
        }
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
        Log.i(TAG, "QueryTextChange: " + newText);
        return false;
    }

    @Override
    public void refresh() {
        mQuery = QueryPreferences.getStoredQuery(getActivity());
        mPresenter.getPage(1, mQuery);
        recyclerView.scrollVerticallyToPosition(0);
    }

    @Override
    public void showError() {
        recyclerView.setVisibility(View.GONE);
        viewError.setVisibility(View.VISIBLE);
        viewError.setConfig(ApplicationController.getInstance().getErrorConfig());
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

    private void setupAdapter(List<Photo> photos) {
        if (isAdded()) {
            mAdapter = new PhotoAdapter(getContext(), photos);
            recyclerView.setAdapter(mAdapter);
        }
    }
}
