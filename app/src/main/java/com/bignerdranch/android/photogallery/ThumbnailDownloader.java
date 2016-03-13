package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
public class ThumbnailDownloader<T> extends HandlerThread {

    // TAG for filtering logcat messages
    private static final String TAG = "ThumbnailDownloader";

    // Download request identifier (also the "what" of a message)
    private static final int MESSAGE_DOWNLOAD = 0;


    private Handler mRequestHandler;                // From background thread
    private ConcurrentMap<T, String> mRequestMap    // Used to store and retrieve URL from request
            = new ConcurrentHashMap<>();

    private Handler mResponseHandler;               // From main thread
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;   // Handles downloaded image

    /**
     * Listener called when thumbnail is downloaded from url
     *
     * @param <T>
     */
    public interface ThumbnailDownloadListener<T> {
        void onThumbnailDownloadeded(T target, Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }



    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);

        mResponseHandler = responseHandler;
    }

    /**
     * Looper class that handles messages and sends them to their target (Handler)
     *
     * Called before the Looper checks the queue for the first time
     */
    @Override
    protected void onLooperPrepared() {

        mRequestHandler = new Handler() {

            /**
             * Called when a download message is pulled off queue and
             * ready to be processed
             *
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == MESSAGE_DOWNLOAD) {

                    T target = (T) msg.obj;

                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));

                    handleRequest(target);
                }

            }
        };

    }

    public void queueThumbnail(T target, String url) {
        Log.i(TAG, "Got a URL: " + url);

        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }

    }

    /**
     * Clears message queue
     *
     * Required to avoid error where ThumbnailDownloader holds onto invalid
     * PhotoHolders on configuration change (ie. rotation)
     */
    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    /**
     * Uses FlickrFetchr to download bytes from URL and turn them into a bitmap
     *
     * "Downloading" process
     *
     * @param target
     */
    private void handleRequest(final T target) {

        try {

            final String url = mRequestMap.get(target);

            // Check: url exists
            if (url == null) return;

            // Retrieve data from url in bytes and convert it into a bitmap
            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory
                    .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

            Log.i(TAG, "Bitmap created");

            /**
             * Posts downloaded image to UI
             */
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {

                    // Check: Correct image (since RecyclerView recycles PhotoHolder and
                    // requests a different URL
                    if (mRequestMap.get(target) != url) return;

                    // Remove PhotoHolder-URL mapping from requestMap and
                    // set the bitmap on the target PhotoHolder
                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloadeded(target, bitmap);
                }
            });

        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
        }

    }

}
