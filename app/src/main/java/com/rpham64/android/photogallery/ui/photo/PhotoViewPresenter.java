package com.rpham64.android.photogallery.ui.photo;

import android.support.annotation.NonNull;

import com.rpham64.android.photogallery.models.Size;
import com.rpham64.android.photogallery.models.Sizes;
import com.rpham64.android.photogallery.network.ApiService;
import com.rpham64.android.photogallery.network.RestClient;
import com.rpham64.android.photogallery.network.response.SizesResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Rudolf on 6/19/2017.
 */

public class PhotoViewPresenter implements PhotoViewContract.Presenter {

    private String mPhotoId;

    private PhotoViewContract.View mPhotoView;

    public PhotoViewPresenter(@NonNull PhotoViewContract.View photoView,
                              @NonNull String photoId) {
        mPhotoView = photoView;
        mPhotoId = photoId;
    }

    @Override
    public void getPhoto() {
        Call<SizesResponse> call = getApiService().getPhotoBySize(mPhotoId);
        call.enqueue(new Callback<SizesResponse>() {
            @Override
            public void onResponse(Call<SizesResponse> call, Response<SizesResponse> response) {

                if (response.isSuccessful()) {

                    Sizes sizes = response.body().sizes;
                    List<Size> sizeList = sizes.sizesList;

                    // Get largest photo size from sizeList
                    // List has sizes in ascending order, so largest is the last Size in sizeList
                    String url = sizeList.get(sizeList.size() - 1).source;
                    mPhotoView.showPhoto(url);
                }
            }

            @Override
            public void onFailure(Call<SizesResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public ApiService getApiService() {
        return RestClient.getInstance().getApiService();
    }
}
