package com.rpham64.android.photogallery.ui.web;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import com.rpham64.android.photogallery.R;
import com.rpham64.android.photogallery.utils.BasePresenter;
import com.rpham64.android.photogallery.utils.UrlUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Rudolf on 4/27/2017.
 */

public class PhotoPagePresenter extends BasePresenter<PhotoPagePresenter.View> {

    private Context mContext;

    public PhotoPagePresenter(Context context) {
        mContext = context;
    }

    public void viewFullScreenMode(Uri imageUri) {
        Intent intentFullScreen = new Intent();
        intentFullScreen.setAction(Intent.ACTION_VIEW);
        intentFullScreen.setDataAndType(imageUri, "image/png");
        mContext.startActivity(intentFullScreen);
    }

    public void copyLink(Uri imageUri) {

        Toast.makeText(mContext, mContext.getString(R.string.copied_link), Toast.LENGTH_SHORT).show();

        ClipboardManager clipboard =
                (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clip = ClipData.newUri(
                mContext.getContentResolver(),
                "URI",
                imageUri);

        clipboard.setPrimaryClip(clip);
    }

    public void saveImage(String mImageUrl) {

        Bitmap bitmap = UrlUtils.getBitmapFromURL(mImageUrl);

        Toast.makeText(mContext, mContext.getString(R.string.downloading_image), Toast.LENGTH_SHORT).show();

        // Create image file
        File sdCardDirectory = Environment.getExternalStorageDirectory();
        String filename = mImageUrl
                .substring(mImageUrl.lastIndexOf('/') + 1, mImageUrl.length());
        File image = new File(sdCardDirectory, filename);

        // Save image file
        FileOutputStream outputStream;

        try {

            outputStream = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

            outputStream.flush();
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(mContext, R.string.image_saved, Toast.LENGTH_SHORT).show();
    }

    public void shareImage(String mImageUrl) {
        // Create image file
        File sdCardDirectory = Environment.getExternalStorageDirectory();
        String filename = mImageUrl
                .substring(mImageUrl.lastIndexOf('/') + 1, mImageUrl.length());
        File image = new File(sdCardDirectory, filename);

        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(image));
        shareIntent.setType("image/jpeg");
        Intent intent = Intent.createChooser(
                shareIntent,
                mContext.getString(R.string.intent_share_image)
        );

        mContext.startActivity(intent);
    }

    public interface View {

    }
}
