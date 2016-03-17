package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetches data from Flickr
 *
 * Created by Rudolf on 3/12/2016.
 */
public class FlickrFetchr {

    // TAG for filtering log messages
    private static final String TAG = "FlickrFetchr";

    // flickr API Key
    private static final String API_KEY = "027c43e90b643994b94b559626dc08be";

    /**
     * Fetches raw data from URL and returns it as an array of bytes
     *
     * @param urlSpec
     * @return
     * @throws IOException
     */
    public byte[] getUrlBytes(String urlSpec) throws IOException {

        // Create URL object from String (like a website link with http)
        URL url = new URL(urlSpec);

        // Create connection object pointed at the URL
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {

                throw new IOException(
                        connection.getResponseMessage() + ": with " + urlSpec);

            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];

            // Read in data to outputStream until connection runs out of data
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();

            return outputStream.toByteArray();

        } finally {
            connection.disconnect();
        }
    }

    /**
     * Converts result from getUrlBytes to a String
     *
     * @param urlSpec
     * @return
     * @throws IOException
     */
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    /**
     * Builds an appropriate request URL and fetches its contents
     *
     * Called in background thread
     */
    public List<GalleryItem> fetchItems(int page) {

        List<GalleryItem> items = new ArrayList<>();

        try {

            // Build request URL
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extras", "url_s")
                    .appendQueryParameter("page", Integer.toString(page))
                    .build().toString();

            // Fetch contents from request URL
            String jsonString = getUrlString(url);

            Log.i(TAG, "Received JSON: " + jsonString);

            parseItems(items, jsonString);

        } catch (JSONException jsonException) {

            Log.e(TAG, "Failed to parse JSON", jsonException);

        } catch (IOException ioe) {

            Log.e(TAG, "Failed to fetch items", ioe);

        }

        return items;
    }

    /**
     * Deserializes JSON into Java objects
     *
     * @param items
     * @param jsonString
     * @throws IOException
     * @throws JSONException
     */
    private void parseItems(List<GalleryItem> items, String jsonString)
        throws IOException, JSONException {

        // Deserialize JSON to Java object using Gson
        FlickrResponse flickrResponse = new Gson().fromJson(jsonString, FlickrResponse.class);

        // List of photos
        List<Photo> photos = flickrResponse.getPhotos().getPhoto();

        for (int i = 0; i < photos.size(); i++) {

            Photo photo = photos.get(i);
            GalleryItem galleryItem = new GalleryItem();

            // Ignore image if url_s does not exist
            if (photo.getUrl_s() == null) continue;

            galleryItem.setId(photo.getId());
            galleryItem.setCaption(photo.getTitle());
            galleryItem.setUrl(photo.getUrl_s());

            items.add(galleryItem);
        }

    }

}
