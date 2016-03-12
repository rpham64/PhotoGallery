package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
     */
    public List<GalleryItem> fetchItems() {

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
                    .build().toString();

            // Fetch contents from request URL
            String jsonString = getUrlString(url);

            Log.i(TAG, "Received JSON: " + jsonString);

            JSONObject jsonObject = new JSONObject(jsonString);

            parseItems(items, jsonObject);

        } catch (JSONException jsonException) {

            Log.e(TAG, "Failed to parse JSON", jsonException);

        } catch (IOException ioe) {

            Log.e(TAG, "Failed to fetch items", ioe);

        }

        return items;
    }

    private void parseItems(List<GalleryItem> items, JSONObject jsonObject)
        throws IOException, JSONException {

        JSONObject photosJSONObject = jsonObject.getJSONObject("photos");
        JSONArray photosJSONArray = photosJSONObject.getJSONArray("photo");

        for (int i = 0; i < photosJSONArray.length(); i++) {

            JSONObject photoJSONObject = photosJSONArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();

            // Add photo Id, caption, and url (if it exists) to GalleryItem item
            item.setId(photoJSONObject.getString("id"));
            item.setCaption(photoJSONObject.getString("title"));

            // Ignore images without an image url
            if (!photoJSONObject.has("url_s")) { continue; }

            item.setUrl(photoJSONObject.getString("url_s"));

            // Add item to list of GalleryItems
            items.add(item);
        }

    }

}
