package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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

    // HandlerThread for managing photo downloads
    private PhotoDownloaderHandlerThread<PhotoHolder> mPhotoDownloaderHandlerThread;

    // Last page fetched
    private int lastPageFetched = 1;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);            // Retain fragment state on configuration changes

        // Use AsyncTask to get json string of flickr data in the background data.
        // This then adds the data to mItems list.
        // Note that we only have objects containing the String data, NOT THE DOWNLOADED IMAGES.

        // 1) Build URL string of data.
        // 2) Use background thread to download data as a json string.
        // 3) Store the json data into GalleryItem POJOs.
        // 4) Store the GalleryItem objects into mItems list.
        new FetchItemsTask().execute();

        Handler mainThreadHandler = new Handler();
        mPhotoDownloaderHandlerThread = new PhotoDownloaderHandlerThread<>(mainThreadHandler);
        mPhotoDownloaderHandlerThread.setPhotoDownloaderListener(
                new PhotoDownloaderHandlerThread.PhotoDownloaderListener<PhotoHolder>() {

                    /**
                     * Binds thumbnail drawable to PhotoHolder on download
                     *
                     * @param photoHolder
                     * @param bitmap
                     */
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder photoHolder, Bitmap bitmap) {

                        // Create drawable of bitmap.
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);

                        // Bind drawable to photoholder.
                        photoHolder.bindDrawable(drawable);
                    }
                }
        );

        mPhotoDownloaderHandlerThread.start();  // Start HandlerThread.
//        mPhotoDownloaderHandlerThread.getLooper();  // ???
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

                // If user scrolled to bottom of page, fetch another page of items
                if (lastPosition >= totalNumberOfItems - numColumns - loadBufferPosition) {
                    new FetchItemsTask().execute(lastPosition + 1);
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
    public void onDestroyView() {
        super.onDestroyView();
        mPhotoDownloaderHandlerThread.clearMessageQueue();      // Clean out downloader
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mPhotoDownloaderHandlerThread.quit();

        Log.i(TAG, "Background thread destroyed.");
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

            // Default Placeholder Image (displays when no image exists)
            Drawable placeholder = getResources().getDrawable(R.drawable.nothing_to_do_here);
            photoHolder.bindDrawable(placeholder);

            // Queue downloaded thumbnail image
            mPhotoDownloaderHandlerThread.queueThumbnail(photoHolder, galleryItem.getUrl());
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
            return new FlickrFetchr().getItems(lastPageFetched);
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
