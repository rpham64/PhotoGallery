package com.rpham64.android.photogallery.ui.gallery;

import com.rpham64.android.photogallery.base.BasePresenter;
import com.rpham64.android.photogallery.models.Photo;
import com.rpham64.android.photogallery.models.Photos;
import com.rpham64.android.photogallery.network.ApiService;
import com.rpham64.android.photogallery.network.response.FlickrResponse;
import com.rpham64.android.photogallery.utils.SearchQuerySharedPreference;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Handles business logic of using ({@link ApiService}) to load images from Flickr's servers,
 * listening to events from the View ({@link PhotoGalleryFragment}), and updating the View's UI.
 */
public class PhotoGalleryPresenter extends BasePresenter<PhotoGalleryContract.View>
        implements PhotoGalleryContract.Presenter {

    private static final String TAG = PhotoGalleryPresenter.class.getSimpleName();

    // Sort Order Parameters (for Search function)
    private static final String SORT_RELEVANCE = "relevance";

    // SharedPreference helper class for accessing stored search query.
    private SearchQuerySharedPreference mSearchQuerySharedPreference;

    // Callback object for handling response after retrieving photos from Flickr.
    private Callback<FlickrResponse> mFetchPhotosCallback;

    // Last submitted search query from View's SearchView widget.
    private String mSearchQuery;

    // Page values from Flickr's API network calls.
    private int mCurrentPage;
    private int mMaxNumPages;

    public PhotoGalleryPresenter(SearchQuerySharedPreference searchQuerySharedPreference) {
        mSearchQuerySharedPreference = searchQuerySharedPreference;
        mSearchQuery = mSearchQuerySharedPreference.getStoredQuery();
        mCurrentPage = 1;

        // Initialize callback for fetching recent photos and search.
        mFetchPhotosCallback = new Callback<FlickrResponse>() {
            @Override
            public void onResponse(Call<FlickrResponse> call, Response<FlickrResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    if (getView() != null) {
                        getView().showError();
                    }
                }

                Photos photosResponse = response.body().photosResponse;
                List<Photo> photoList = photosResponse.photos;
                mCurrentPage = photosResponse.page;
                mMaxNumPages = photosResponse.pages;

                // Send photos to the View, if it exists.
                if (getView() != null) {
                    // Pass photo list back to the View.
                    getView().showPhotos(photoList, mCurrentPage);
                }
            }

            @Override
            public void onFailure(Call<FlickrResponse> call, Throwable t) {
                handleError(t);
            }
        };
    }

    @Override
    public void getPhotos() {
        mSearchQuery = mSearchQuerySharedPreference.getStoredQuery();

        if (mSearchQuery == null) {
            getRecentPhotos(mCurrentPage);
        } else {
            getPhotosBySearch(mSearchQuery, mCurrentPage);
        }
    }

    @Override
    public void getRecentPhotos(int page) {
        Call<FlickrResponse> getPhotosCall = getApiService().getRecentPhotos(page);
        getPhotosCall.enqueue(mFetchPhotosCallback);
    }

    @Override
    public void getPhotosBySearch(String query, int page) {
        Call<FlickrResponse> getPhotosBySearchCall =
                getApiService().getPhotosBySearch(query, page, SORT_RELEVANCE);
        getPhotosBySearchCall.enqueue(mFetchPhotosCallback);
    }

    @Override
    public void loadMorePhotos() {
        if (mCurrentPage + 1 <= mMaxNumPages) {
            mCurrentPage++;  // Increment current page to get the next page of photos.
            getPhotos();
        } else {
            if (getView() != null) {
                getView().showCannotLoadMoreToast();
            }
        }
    }

    @Override
    public void onSearchViewClicked() {
        if (getView() != null) {
            String query = mSearchQuerySharedPreference.getStoredQuery();
            getView().setSearchViewQuery(query);
        }
    }

    @Override
    public void storeSearchQuery(String query) {
        mSearchQuerySharedPreference.setStoredQuery(query);
    }

    @Override
    public void clearSearchQuery() {
        mSearchQuerySharedPreference.setStoredQuery(null);
    }

    @Override
    public void refresh() {
        mCurrentPage = 1;  // Reset current page back to 1.
        getPhotos();  // Clear results and retrieve the first page of photos.
    }

    @Override
    public void handleError(Throwable throwable) {
        throwable.printStackTrace();
        if (getView() != null) {
            getView().showError();
        }
    }
}
