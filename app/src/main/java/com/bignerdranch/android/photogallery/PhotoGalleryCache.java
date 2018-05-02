package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;

public class PhotoGalleryCache extends LruCache<String, Bitmap> {

    private static final String TAG = PhotoGalleryCache.class.getSimpleName();

    /**
     * @param maxSize for caches that do not override {@link #sizeOf}, this is
     *                the maximum number of entries in the cache. For all other caches,
     *                this is the maximum sum of the sizes of the entries in this cache.
     */
    public PhotoGalleryCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, Bitmap value) {
        return value.getByteCount() / 1024;
    }

    public void addBitmapToCache(String url) throws IOException {
        // Retrieve data from url in bytes and convert it into a bitmap
        byte[] bitmapBytes = new FlickrFetchr().downloadData(url);
        Bitmap bitmap = BitmapFactory
                .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

        Log.i(TAG, "Bitmap created");

        // Add url and bitmap to cache.
        put(url, bitmap);

        Log.i(TAG, "Bitmap added to cache");
    }

    public Bitmap getBitmapFromCache(String url) {
        Bitmap bitmap = get(url);
        Log.i(TAG, "Retrieved bitmap from cache");
        return bitmap;
    }
}
