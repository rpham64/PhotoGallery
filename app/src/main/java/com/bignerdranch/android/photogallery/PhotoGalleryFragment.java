package com.bignerdranch.android.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
    private int lastPageFetched = 1;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);            // Retain fragment state on configuration changes
        new FetchItemsTask().execute();
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

        // Implement endless page scrolling
        mPhotoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                PhotoAdapter adapter = (PhotoAdapter) recyclerView.getAdapter();
                int lastPosition = adapter.getLastBoundPosition();
                int loadBufferPosition = 1;

                if (lastPosition >= adapter.getItemCount() - mLayoutManager.getSpanCount()
                    - loadBufferPosition) {
                    new FetchItemsTask().execute(lastPosition + 1);
                }

            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        setupAdapter();

        return view;
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
            TextView textView = new TextView(getActivity());
            return new PhotoHolder(textView);
        }

        @Override
        public void onBindViewHolder(PhotoHolder photoHolder, int position) {
            GalleryItem galleryItem = mGalleryItems.get(position);
            lastBoundPosition = position;

            Log.i(TAG, "Last bound position: " + lastBoundPosition);

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
     * ViewHolder class that binds GalleryItem to a TextView
     */
    private class PhotoHolder extends RecyclerView.ViewHolder {

        private TextView mTitleTextView;

        public PhotoHolder(View itemView) {
            super(itemView);

            mTitleTextView = (TextView) itemView;
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            mTitleTextView.setText(galleryItem.toString());
        }
    }

    /**
     * Fetches data from URL String using background and main threads
     */
    private class FetchItemsTask extends AsyncTask<Integer, Void, List<GalleryItem>> {

        /**
         * Retrieves data from a website using background thread
         *
         * @param params
         * @return
         */
        @Override
        protected List<GalleryItem> doInBackground(Integer... params) {
            return new FlickrFetchr().fetchItems(lastPageFetched);
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
