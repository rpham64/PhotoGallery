package com.rpham64.android.photogallery.ui.gallery;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerView;
import com.rpham64.android.photogallery.R;
import com.rpham64.android.photogallery.models.Photo;
import com.rpham64.android.photogallery.services.PollService;
import com.rpham64.android.photogallery.utils.PagedResult;
import com.rpham64.android.photogallery.utils.QueryPreferences;
import com.rpham64.android.photogallery.utils.VisibleFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;

/**
 * Main Fragment hosted by PhotoGalleryActivity
 *
 * Created by Rudolf on 3/12/2016.
 */
public class PhotoGalleryFragment extends VisibleFragment implements PhotoGalleryPresenter.View {

    private static final String TAG = PhotoGalleryPresenter.class.getName();

    @BindView(R.id.recycler_view_photo_gallery_fragment) UltimateRecyclerView recyclerView;

    private PhotoAdapter mAdapter;

    private PhotoGalleryPresenter mPresenter;

    private String mQuery;
    private int currentPage = 1;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);            // Retain fragment state on configuration changes
        setHasOptionsMenu(true);            // Include toolbar

        mQuery = QueryPreferences.getStoredQuery(getActivity());
        mPresenter = new PhotoGalleryPresenter();
        mPresenter.getPage(Observable.just(currentPage), mQuery);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        ButterKnife.bind(this, view);
        mPresenter.attachView(this);

        // Setup GridLayoutManager as RecyclerView's layout manager
        final GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setDefaultOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        recyclerView.reenableLoadmore();
        recyclerView.setOnLoadMoreListener(new UltimateRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore(int itemsCount, int maxLastVisiblePosition) {

                Log.i(TAG, "Items Count: " + itemsCount);
                Log.i(TAG, "Position: " + maxLastVisiblePosition);

                if (maxLastVisiblePosition >= itemsCount - 3) {
                    Log.i(TAG, "Loading more");
                    mPresenter.getPage(Observable.just(currentPage + 1), mQuery);
                }
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);

        menuInflater.inflate(R.menu.fragment_photo_gallery, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        togglePollingButtonTitle(menu);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                Log.d(TAG, "QueryTextSubmit: " + s);

                QueryPreferences.setStoredQuery(getActivity(), s);
                searchView.clearFocus();            // Hides keyboard on submit
                searchView.setQuery("", false);
                searchView.setIconified(true);      // Collapses SearchView widget

                refresh();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG, "QueryTextChange: " + s);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });
    }

    private void togglePollingButtonTitle(Menu menu) {
        /** Toggle Title for Polling Menu Button */
        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);

        if (PollService.isServiceAlarmOn(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
        } else {
            toggleItem.setTitle(R.string.start_polling);
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
                updateItems();

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
    public void showError() {

    }

    @Override
    public void showPictures(List<Photo> photos, PagedResult pagedResult) {

        if (isAdded() && mAdapter == null) {
            mAdapter = new PhotoAdapter(getContext(), photos);
            recyclerView.setAdapter(mAdapter);
        }

        if (currentPage == 1) {
            mAdapter.setPhotos(photos);
        } else {
            mAdapter.addPhotos(photos);
        }

        currentPage = pagedResult.page;

        Toast.makeText(getContext(), "Page " + currentPage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void refresh() {
        currentPage = 1;
        updateItems();
    }

    @Override
    public void updateItems() {
        mQuery = QueryPreferences.getStoredQuery(getActivity());
        mPresenter.getPage(Observable.just(currentPage), mQuery);
    }
}
