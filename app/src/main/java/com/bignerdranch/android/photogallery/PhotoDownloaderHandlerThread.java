package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Dedicated background thread that acts as a message loop for fetching
 * flickr photos.
 *
 * Created by Rudolf on 3/13/2016.
 */
public class PhotoDownloaderHandlerThread<T> extends HandlerThread {

    // TAG for filtering logcat messages
    private static final String TAG = "PhotoDownloader";

    // Download request identifier (also the "what" of a message)
    private static final int DOWNLOAD_MESSAGE = 0;

    // LruCache for storing downloaded Bitmap images
    private PhotoGalleryCache mCache;

    // Handlers
    private Handler mMainThreadHandler;  // Bounded to PhotoGalleryFragment's Main Thread.
    private Handler mPhotoDownloaderHandler;  // Bounded to this handler thread.

    // Map with Target Object -> URL String pairs.
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();

    // Callback for passing downloaded image result.
    private PhotoDownloaderListener<T> mPhotoDownloaderListener;

    public interface PhotoDownloaderListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail);
    }

    public void setPhotoDownloaderListener(PhotoDownloaderListener<T> listener) {
        mPhotoDownloaderListener = listener;
    }

    public PhotoDownloaderHandlerThread(Handler mainThreadHandler) {
        super(TAG);
        mMainThreadHandler = mainThreadHandler;
        buildCache();
    }

    private void buildCache() {
        // Get max available VM memory.
        // Exceeding this amount throws an OutOfMemory exception.
        // Stored in kilobytes as LruCache takes an int in its constructor.
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8 of available memory for memory cache
        int cacheSize = maxMemory / 8;

        mCache = new PhotoGalleryCache(cacheSize);
    }

    /**
     * Sends message from url to its target handler
     *
     * @param target
     * @param url
     */
    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);

        if (url == null) {
            mRequestMap.remove(target);
        } else {
            // Store target and url in map.
            mRequestMap.put(target, url);

            // Create Message object with the target object. Note that url is stored in the request map
            // for retrieval later on.
            // Afterwards, send to mPhotoDownloaderHandler.
            mPhotoDownloaderHandler.obtainMessage(DOWNLOAD_MESSAGE, target)
                    .sendToTarget();

            // Message will then be passed to mPhotoDownloaderHandler's looper (which is this thread's)
            // and pass the Message to that looper's MessageQueue.

            // Once there, if the Message is dequeued from the MessageQueue, it's then handled in
            // mPhotoDownloaderHandler's handleMessage() method, seen below.
        }

    }

    /**
     * Looper class that handles messages and sends them to their target (Handler)
     *
     * Called before the Looper checks the queue for the first time
     */
    @Override
    protected void onLooperPrepared() {

        // Initialize Handler.
        mPhotoDownloaderHandler = new Handler(getLooper()) {

            /**
             * Called when a download message is pulled off queue and
             * ready to be processed.
             *
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == DOWNLOAD_MESSAGE) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));

                    processMessage(target);
                }
            }
        };
    }

    /**
     * Uses FlickrFetchr to download bytes from URL and turn them into a bitmap
     *
     * "Downloading Image" process
     *
     * @param target
     */
    private void processMessage(final T target) {

        try {

            // Retrieve stored url from request map.
            final String url = mRequestMap.get(target);
            final Bitmap bitmap;

            // Check: url exists in request map
            if (url == null) return;

            if (mCache.get(url) == null) {
                // Download bitmap from url and add to cache.
                mCache.addBitmapToCache(url);
            }

            // Retrieve bitmap from cache for the given url.
            bitmap = mCache.getBitmapFromCache(url);

            /**
             * Posts downloaded image to Main Thread's looper.
             * A callback listener is then used to send the image to PhotoGalleryFragment.
             */
            mMainThreadHandler.post(new Runnable() {
                @Override
                public void run() {

                    // Check: Correct image (since RecyclerView recycles views and requests a
                    // different URL for PhotoHolder after downloading a bitmap
                    if (!mRequestMap.get(target).equals(url)) return;

                    // Remove Target -> URL entry from request map.
                    mRequestMap.remove(target);

                    // Use callback to send downloaded bitmap and target object back to main thread.
                    // This will then be used to update the UI.
                    mPhotoDownloaderListener.onThumbnailDownloaded(target, bitmap);
                }
            });

        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }

    }

    /**
     * Clears message queue
     *
     * Required to avoid error where PhotoDownloaderHandlerThread holds onto invalid
     * PhotoHolders on configuration change (ie. rotation)
     */
    public void clearMessageQueue() {
        mPhotoDownloaderHandler.removeMessages(DOWNLOAD_MESSAGE);
    }
}
