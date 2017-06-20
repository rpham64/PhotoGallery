package com.rpham64.android.photogallery.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rpham64.android.photogallery.R;
import com.rpham64.android.photogallery.models.Photo;
import com.rpham64.android.photogallery.ui.photo.PhotoViewFragment;
import com.rpham64.android.photogallery.ui.web.PhotoPageActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by Rudolf on 9/23/2016.
 */

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoHolder> {

    public interface Tags {
        String dialogPhoto = PhotoAdapter.class.getName() + ".dialogPhoto";
    }

    private Context mContext;
    private List<Photo> mPhotos;

    public PhotoAdapter(Context context, List<Photo> photos) {
        setHasStableIds(true);
        this.mContext = context;
        this.mPhotos = photos;
    }

    @Override
    public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.v_gallery_item, parent, false);
        ButterKnife.bind(this, view);

        return new PhotoHolder(view);
    }

    @Override
    public void onBindViewHolder(PhotoHolder holder, int position) {
        Photo photo = mPhotos.get(position);
        holder.bindPhoto(photo);
    }

    @Override
    public long getItemId(int position) {
        return mPhotos.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        if (mPhotos != null) {
            return mPhotos.size();
        }
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

    /**
     * ViewHolder class that binds GalleryItem to an ImageView (ie. adds picture to UI)
     */
    class PhotoHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.fragment_photo_gallery_image_view) ImageView imgPhoto;

        private Photo mPhoto;

        public PhotoHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
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

        @OnLongClick(R.id.fragment_photo_gallery_image_view)
        public boolean onImageLongClicked() {

            // Open photo view dialog (PhotoView classes)
            AppCompatActivity activity = (AppCompatActivity) mContext;
            FragmentManager fragmentManager = activity.getSupportFragmentManager();

            PhotoViewFragment fragment = PhotoViewFragment.newInstance(mPhoto.id);
            fragment.show(fragmentManager, Tags.dialogPhoto);

            return true;
        }
    }
}