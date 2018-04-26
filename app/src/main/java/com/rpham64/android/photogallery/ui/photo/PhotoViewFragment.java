package com.rpham64.android.photogallery.ui.photo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.rpham64.android.photogallery.R;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import fr.tvbarthel.lib.blurdialogfragment.SupportBlurDialogFragment;

/**
 * Created by Rudolf on 6/19/2017.
 *
 * TODO: Create PhotoViewActivity.
 */

public class PhotoViewFragment extends SupportBlurDialogFragment implements PhotoViewContract.View {

    public interface Arguments {
        String PHOTO_ID = PhotoViewFragment.class.getName() + ".photoId";
    }

    @BindView(R.id.view_progress_spinner) ProgressBar viewSpinner;
    @BindView(R.id.img_photo) ImageView imgPhoto;

    private Unbinder mUnbinder;
    private PhotoViewContract.Presenter mPresenter;

    private String mPhotoId;
    private String mPhotoUrl;

    public static PhotoViewFragment newInstance(String photoId) {

        Bundle args = new Bundle();
        args.putString(Arguments.PHOTO_ID, photoId);

        PhotoViewFragment fragment = new PhotoViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mPhotoId = getArguments().getString(Arguments.PHOTO_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.v_photo, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mPhotoUrl == null) {
            // Call GET request to retrieve photo url
            mPresenter.getPhoto();
        } else {
            showPhoto(mPhotoUrl);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void showPhoto(String url) {

        mPhotoUrl = url;

        viewSpinner.setVisibility(View.GONE);
        Picasso.with(getActivity()).load(url).into(imgPhoto);
    }

    @OnClick(R.id.img_photo)
    public void onViewClicked() {
        dismiss();
    }

    @Override
    public void setPresenter(PhotoViewContract.Presenter presenter) {
        mPresenter = presenter;
    }
}
