package com.rpham64.android.photogallery.ui.photo;

import com.rpham64.android.photogallery.models.Size;
import com.rpham64.android.photogallery.models.Sizes;
import com.rpham64.android.photogallery.network.response.SizesResponse;
import com.rpham64.android.photogallery.base.BasePresenter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Rudolf on 6/19/2017.
 */

public class PhotoViewPresenter extends BasePresenter<PhotoViewPresenter.View> {

    private String photoId;

    public PhotoViewPresenter(String photoId) {
        this.photoId = photoId;
    }

    public void getPhoto() {
        Call<SizesResponse> call = getApiService().getPhotoBySize(photoId);
        call.enqueue(new Callback<SizesResponse>() {
            @Override
            public void onResponse(Call<SizesResponse> call, Response<SizesResponse> response) {

                if (response.isSuccessful()) {

                    Sizes sizes = response.body().sizes;
                    List<Size> sizeList = sizes.sizesList;

                    // Get largest photo size from sizeList
                    // List has sizes in ascending order, so largest is the last Size in sizeList
                    String url = sizeList.get(sizeList.size() - 1).source;
                    getView().showPhoto(url);

                }
            }

            @Override
            public void onFailure(Call<SizesResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public interface View {
        void showPhoto(String url);
    }
}
