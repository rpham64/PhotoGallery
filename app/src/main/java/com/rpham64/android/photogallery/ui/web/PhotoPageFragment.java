package com.rpham64.android.photogallery.ui.web;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.rpham64.android.photogallery.R;
import com.rpham64.android.photogallery.utils.VisibleFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import tr.xip.errorview.ErrorView;

/**
 * WebView for image
 *
 * Created by Rudolf on 3/22/2016.
 */
public class PhotoPageFragment extends VisibleFragment implements View.OnCreateContextMenuListener, Runnable {

    private static final String TAG = PhotoPageFragment.class.getName();

    interface Arguments {
        String ARG_URI = "PhotoPageFragment.uri";
    }

    interface Methods {
        String FULL_SCREEN = "PhotoPageFragment.fullScreen";
        String COPY_LINK = "PhotoPageFragment.copyLink";
        String SAVE = "PhotoPageFragment.save";
        String SHARE = "PhotoPageFragment.share";
    }

    @BindView(R.id.fragment_photo_page_progress_bar) ProgressBar barProgress;
    @BindView(R.id.fragment_photo_page_web_view) WebView viewWeb;
    @BindView(R.id.error) ErrorView viewError;

    private Unbinder mUnbinder;

    private Uri mUri;
    private String mImageUrl;

    private ShareActionProvider mShareActionProvider;

    public static PhotoPageFragment newInstance(Uri uri) {

        Bundle args = new Bundle();
        args.putParcelable(Arguments.ARG_URI, uri);

        PhotoPageFragment fragment = new PhotoPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mUri = getArguments().getParcelable(Arguments.ARG_URI);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_page, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        barProgress.setMax(100);

        registerForContextMenu(viewWeb);

        viewWeb.getSettings().setJavaScriptEnabled(true);
        viewWeb.setWebChromeClient(new WebChromeClient() {

            /**
             * Displays progress bar
             *
             * @param view
             * @param newProgress
             */
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                if (newProgress == 100) {
                    barProgress.setVisibility(View.GONE);
                } else {
                    barProgress.setVisibility(View.VISIBLE);
                    barProgress.setProgress(newProgress);
                }

            }

            /**
             * Update toolbar's subtitle with title of loaded page
             *
             * @param view
             * @param title
             */
            @Override
            public void onReceivedTitle(WebView view, String title) {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                activity.getSupportActionBar().setSubtitle(title);
            }
        });

        setClientToRightActivity(); // If not WebView

        // Load URL into WebView
        viewWeb.loadUrl(mUri.toString());

        Log.i(TAG, "Loaded Uri: " + mUri.toString());

        viewWeb.setOnCreateContextMenuListener(this);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    private void setClientToRightActivity() {
        // Override WebViewClient method to return false
        // This lets the app open the clicked image's webview in
        // the task and not in the phone's default browser
        viewWeb.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Log.i(TAG, "Url: " + url);

                if (!URLUtil.isHttpUrl(url) && !URLUtil.isHttpsUrl(url)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);

                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_photo_web_view, menu);

        MenuItem item = menu.findItem(R.id.menu_item_share);

        setShareIntent(item);
    }

    private void setShareIntent(MenuItem item) {
        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareIntent());
        }
    }

    @NonNull
    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, viewWeb.getUrl());
        return shareIntent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        HitTestResult result = viewWeb.getHitTestResult();

        if (isImageType(result) || isSrcImageAnchorType(result)) {
            mImageUrl = result.getExtra();  // Save Image URL
            createContextMenu(menu);
        }
    }

    private boolean isImageType(HitTestResult result) {
        return result.getType() == HitTestResult.IMAGE_TYPE;
    }

    private boolean isSrcImageAnchorType(HitTestResult result) {
        return result.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE;
    }

    private void createContextMenu(ContextMenu menu) {
        menu.add(0, R.id.enter_full_screen_context_item, 0, "Enter fullscreen");
        menu.add(0, R.id.copy_link_address_context_item, 0, "Copy link address");
        menu.add(0, R.id.save_image_context_item, 0, "Save Image");
        menu.add(0, R.id.share_image_context_item, 0, "Share");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (isNotAllowed()) return false;

        switch (item.getItemId()) {

            case R.id.enter_full_screen_context_item:

                new RetrieveImageTask(Methods.FULL_SCREEN).execute();
                break;

            case R.id.copy_link_address_context_item:

                Toast.makeText(getActivity(), getString(R.string.copied_link), Toast.LENGTH_SHORT).show();
                new RetrieveImageTask(Methods.COPY_LINK).execute();
                break;

            case R.id.save_image_context_item:

                Toast.makeText(getActivity(), getString(R.string.downloading_image), Toast.LENGTH_SHORT).show();
                new RetrieveImageTask(Methods.SAVE).execute();
                break;

            case R.id.share_image_context_item:

                new RetrieveImageTask(Methods.SHARE).execute();
                break;

        }

        return super.onContextItemSelected(item);
    }

    /**
     * Unable to access flickr image due to owner's permissions
     *
     * @return
     */
    private boolean isNotAllowed() {
        if (mImageUrl.endsWith("spaceball.gif")) {
            showPermissionDeniedToast();
            return true;
        }
        return false;
    }

    private void showPermissionDeniedToast() {
        String permissionDeniedMsg = "Permission denied by the owner. " +
                "Unable to interact with this image.";

        Toast.makeText(getActivity(), permissionDeniedMsg, Toast.LENGTH_SHORT).show();

        return;
    }

    /**
     * Back press moves WebView back through its items in its browsing history
     *
     * @return
     */
    public boolean isBackPressed() {

        if (viewWeb.canGoBack()) {
            viewWeb.goBack();
            return true;
        }

        return false;
    }

    @Override
    public void run() {
        new RetrieveImageTask(null).execute();
    }

    private class RetrieveImageTask extends AsyncTask<String, Void, Bitmap> {

        private String mMethod;

        public RetrieveImageTask(String method) {
            mMethod = method;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = getBitmapFromURL(mImageUrl);
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            Uri imageUri = Uri.parse(mImageUrl);

            switch (mMethod) {

                case Methods.FULL_SCREEN:

                    viewImageInFullScreen(imageUri);
                    break;

                case Methods.COPY_LINK:

                    copyLink(imageUri);
                    break;

                case Methods.SAVE:

                    saveImageToExternalMemory(bitmap);
                    break;

                case Methods.SHARE:

                    shareImage();
                    break;

            }

        }

        public Bitmap getBitmapFromURL(String src) {
            try {
                URL url = new URL(src);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                // Log exception
                return null;
            }
        }

        private void viewImageInFullScreen(Uri imageUri) {

            Intent intentFullScreen = new Intent();
            intentFullScreen.setAction(Intent.ACTION_VIEW);
            intentFullScreen.setDataAndType(imageUri, "image/png");
            startActivity(intentFullScreen);
        }

        private void copyLink(Uri imageUri) {
            ClipboardManager clipboard = (ClipboardManager) getActivity()
                    .getSystemService(Context.CLIPBOARD_SERVICE);

            ClipData clip = ClipData.newUri(
                    getContext().getContentResolver(),
                    "URI",
                    imageUri);

            clipboard.setPrimaryClip(clip);
        }

        private void saveImageToExternalMemory(Bitmap bitmap) {
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

            String message = "Image saved!";

            Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
        }

        private void shareImage() {

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
                    getString(R.string.intent_share_image));

            startActivity(intent);
        }
    }
}
