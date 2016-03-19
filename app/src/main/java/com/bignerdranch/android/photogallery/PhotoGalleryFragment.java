package com.bignerdranch.android.photogallery;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
public class PhotoGalleryFragment extends Fragment {

    // TAG for filtering log messages
    private static final String TAG = "PhotoGalleryFragment";

    // RecyclerView
    private RecyclerView mPhotoRecyclerView;

    // List of Photos (GalleryItem)
    private List<GalleryItem> mItems = new ArrayList<>();

    // Last page fetched
    private int lastPageFetched = 0;

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

        // Implement endless page scrolling
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

        return view;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);

        menuInflater.inflate(R.menu.fragment_photo_gallery, menu);

        final MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();

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

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            // Removes query from QueryPreferences
            case R.id.menu_item_clear:

                QueryPreferences.setStoredQuery(getActivity(), null);
                updateItems();

                return true;

            default:
                return super.onOptionsItemSelected(item);

        }


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

//            Log.i(TAG, "Last bound position: " + lastBoundPosition);

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
    private class PhotoHolder extends RecyclerView.ViewHolder {

        private ImageView mItemImageView;

        public PhotoHolder(View itemView) {
            super(itemView);

            mItemImageView =
                    (ImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
        }

        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            Picasso.with(getActivity())
                    .load(galleryItem.getUrl())
                    .placeholder(R.drawable.gray)
                    .into(mItemImageView);
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
