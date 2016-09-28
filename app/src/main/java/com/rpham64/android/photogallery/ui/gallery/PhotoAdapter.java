package com.rpham64.android.photogallery.ui.gallery;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.marshalchen.ultimaterecyclerview.UltimateViewAdapter;
import com.rpham64.android.photogallery.R;
import com.rpham64.android.photogallery.models.Photo;
import com.rpham64.android.photogallery.ui.web.PhotoPageActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Rudolf on 9/23/2016.
 */

public class PhotoAdapter extends UltimateViewAdapter<PhotoHolder> {

    private Context mContext;
    private List<Photo> mPhotos;

    public PhotoAdapter(Context context, List<Photo> photos) {
        this.mContext = context;
        this.mPhotos = photos;
    }

    @Override
    public PhotoHolder onCreateViewHolder(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.v_gallery_item, parent, false);
        ButterKnife.bind(this, view);

        return new PhotoHolder(mContext, view);
    }

    @Override
    public void onBindViewHolder(PhotoHolder holder, int position) {
        Photo photo = mPhotos.get(position);
        holder.bindPhoto(photo);
    }

    @Override
    public PhotoHolder newFooterHolder(View view) {
        return null;
    }

    @Override
    public PhotoHolder newHeaderHolder(View view) {
        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        return null;
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getAdapterItemCount() {
        if (mPhotos != null) {
            return mPhotos.size();
        }
        return 0;
    }

    @Override
    public long generateHeaderId(int position) {
        return 0;
    }

    public void setPhotos(List<Photo> photos) {
        this.mPhotos = photos;
        notifyDataSetChanged();
    }

    public void addPhotos(List<Photo> photos) {
        mPhotos.addAll(photos);
        notifyDataSetChanged();
    }
}

    /**
     * ViewHolder class that binds GalleryItem to an ImageView (ie. adds picture to UI)
     */
    class PhotoHolder extends UltimateRecyclerviewViewHolder {

        @BindView(R.id.fragment_photo_gallery_image_view) ImageView imgPhoto;

        private Context mContext;
        private Photo mPhoto;

        public PhotoHolder(Context context, View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            this.mContext = context;
        }

        public void bindPhoto(Photo photo) {

            this.mPhoto = photo;

            Picasso.with(mContext)
                    .load(photo.url)
                    .placeholder(android.R.color.white)
                    .into(imgPhoto);
        }

        @OnClick(R.id.fragment_photo_gallery_image_view)
        public void onImageViewClicked() {
            Intent intent = PhotoPageActivity.newIntent(mContext, mPhoto.getPhotoPageUri());
            mContext.startActivity(intent);
        }
}