package com.bignerdranch.android.photogallery;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Fragment hosted by PhotoGalleryActivity
 *
 * Created by Rudolf on 3/12/2016.
 */
public class PhotoGalleryFragment extends VisibleFragment {

    // TAG for filtering log messages
    private static final String TAG = "PhotoGalleryFragment";

    // RecyclerView
    private RecyclerView mPhotoRecyclerView;

    // List of Photos (GalleryItem)
    private List<GalleryItem> mItems = new ArrayList<>();

    // Last page fetched
    private int lastPageFetched = 1;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);            // Retain fragment state on configuration changes
        setHasOptionsMenu(true);            // Include toolbar
        updateItems();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mPhotoRecyclerView =
                (RecyclerView) view.findViewById(R.id.fragment_photo_gallery_recycler_view);

        // Setup GridLayoutManager as RecyclerView's layout manager
        final GridLayoutManager mLayoutManager = new GridLayoutManager(getActivity(), 3);

        mPhotoRecyclerView.setLayoutManager(mLayoutManager);

        setupAdapter();

        setEndlessPageScrolling(mLayoutManager);

        return view;
    }

    private void setEndlessPageScrolling(final GridLayoutManager mLayoutManager) {
        /** Endless Page Scrolling */
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                PhotoAdapter adapter = (PhotoAdapter) recyclerView.getAdapter();
                int lastPosition = adapter.getLastBoundPosition();
                int totalNumberOfItems = adapter.getItemCount();
                int numColumns = mLayoutManager.getSpanCount();
                int loadBufferPosition = 1;

                String query = QueryPreferences.getStoredQuery(getActivity());

                // If user scrolled to bottom of page, fetch another page of items
                if (lastPosition >= totalNumberOfItems - numColumns - loadBufferPosition) {
                    new FetchItemsTask(query).execute(lastPosition + 1);
                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);

        menuInflater.inflate(R.menu.fragment_photo_gallery, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

        setSearchViewListeners(searchView);

        togglePollingButtonTitle(menu);

    }

    private void setSearchViewListeners(final SearchView searchView) {
        setQueryTextListener(searchView);
        setIconClickListener(searchView);
    }

    private void setQueryTextListener(final SearchView searchView) {
        /** SearchView Query Text Listener */
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                Log.d(TAG, "QueryTextSubmit: " + s);

                QueryPreferences.setStoredQuery(getActivity(), s);
                searchView.clearFocus();            // Hides keyboard on submit
                searchView.setQuery("", false);
                searchView.setIconified(true);      // Collapses SearchView widget

                updateItems();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG, "QueryTextChange: " + s);
                return false;
            }
        });
    }

    private void setIconClickListener(final SearchView searchView) {
        /** SearchView Icon Click Listener */
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
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_item_clear:

                return clearSearchQuery();

            case R.id.menu_item_toggle_polling:

                return setPollingOnOrOff();

            default:

                return super.onOptionsItemSelected(item);

        }

    }

    private boolean clearSearchQuery() {
        QueryPreferences.setStoredQuery(getActivity(), null);
        updateItems();

        return true;
    }

    private boolean setPollingOnOrOff() {
        boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
        PollService.setServiceAlarm(getActivity(), lastPageFetched, shouldStartAlarm);
        getActivity().invalidateOptionsMenu();

        return true;
    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        lastPageFetched = 1;

        new FetchItemsTask(query).execute(lastPageFetched);
    }

    /**
     * Helper method that binds RecyclerView to Adapter
     */
    private void setupAdapter() {

        // Check: PhotoGalleryFragment is attached to activity (so getActivity() is not null)
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }

    }

    /**
     * Adapter class that connects RecyclerView layout to PhotoHolder
     */
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        // List of Pictures
        private List<GalleryItem> mGalleryItems;

        // Position of last GalleryItem
        private int lastBoundPosition;

        public PhotoAdapter(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new PhotoHolder(view);
        }

        @SuppressWarnings("deprecation")
        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            lastBoundPosition = position;

            photoHolder.bindGalleryItem(galleryItem);
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }

        public int getLastBoundPosition() {
            return lastBoundPosition;
        }
    }

    /**
     * ViewHolder class that binds GalleryItem to an ImageView (ie. adds picture to UI)
     */
    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView mItemImageView;
        private GalleryItem mGalleryItem;

        public PhotoHolder(View itemView) {
            super(itemView);

            mItemImageView =
                    (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);

            itemView.setOnClickListener(this);
        }

        public void bindGalleryItem(GalleryItem galleryItem) {

            mGalleryItem = galleryItem;

            Picasso.with(getActivity())
                    .load(galleryItem.getUrl())
                    .placeholder(R.drawable.gray)
                    .into(mItemImageView);
        }

        /**
         * On image click, sends out an implicit intent that opens the image's flickr page
         * using its page URL
         *
         * @param v
         */
        @Override
        public void onClick(View v) {
            Intent intent = PhotoPageActivity.newIntent(getActivity(),
                    mGalleryItem.getPhotoPageUri());
            startActivity(intent);
        }
    }

    /**
     * Fetches data from URL String using background and main threads
     */
    private class FetchItemsTask extends AsyncTask<Integer, Void, List<GalleryItem>> {

        private String mQuery;

        public FetchItemsTask(String query) {
            mQuery = query;
        }

        /**
         * Retrieves data from a website using background thread
         *
         * @param params
         * @return
         */
        @Override
        protected List<GalleryItem> doInBackground(Integer... params) {

            if (mQuery == null) {
                return new FlickrFetchr().fetchRecentPhotos(lastPageFetched);
            } else {
                return new FlickrFetchr().searchPhotos(mQuery, lastPageFetched);
            }

        }

        /**
         * Sets up Adapter in main thread after background thread
         *
         * @param galleryItems fetched in doInBackground
         */
        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {

            if (lastPageFetched > 1) {
                mItems.addAll(galleryItems);
                mPhotoRecyclerView.getAdapter().notifyDataSetChanged();
            }
            else {
                mItems = galleryItems;

                setupAdapter();
            }

            lastPageFetched++;
        }
    }

}
