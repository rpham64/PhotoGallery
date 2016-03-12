package com.bignerdranch.android.photogallery;

/**
 * Photo contents
 *
 * Created by Rudolf on 3/12/2016.
 */
public class GalleryItem {

    private String mCaption;            // Photo Caption/Title
    private String mId;                 // Photo ID
    private String mUrl;                // Photo Link (flickr)

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public String toString() {
        return mCaption;
    }
}
