package com.rpham64.android.photogallery.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.webkit.WebView;
import android.widget.Toast;

import com.rpham64.android.photogallery.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Rudolf on 4/28/2017.
 */

public class ImageUtils {

    public static void viewFullScreenMode(Context mContext, Uri imageUri) {
        Intent intentFullScreen = new Intent();
        intentFullScreen.setAction(Intent.ACTION_VIEW);
        intentFullScreen.setDataAndType(imageUri, "image/png");
        mContext.startActivity(intentFullScreen);
    }

    public static void copyLink(Context mContext, Uri imageUri) {

        Toast.makeText(mContext, mContext.getString(R.string.copied_link), Toast.LENGTH_SHORT).show();

        ClipboardManager clipboard =
                (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);

        ClipData clip = ClipData.newUri(
                mContext.getContentResolver(),
                "URI",
                imageUri);

        clipboard.setPrimaryClip(clip);
    }

    public static void saveImage(Context mContext, String mImageUrl) {

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

    public static void shareImage(Context mContext, String mImageUrl) {
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

    /**
     * Unable to access flickr image due to owner's permissions
     *
     * @return
     */
    private boolean isNotAllowed(Context mContext, String mImageUrl) {
        if (mImageUrl.endsWith("spaceball.gif")) {
            Toast.makeText(mContext, mContext.getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private boolean isImageType(WebView.HitTestResult result) {
        return result.getType() == WebView.HitTestResult.IMAGE_TYPE;
    }

    private boolean isSrcImageAnchorType(WebView.HitTestResult result) {
        return result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE;
    }
}
