package com.bignerdranch.android.photogallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * WebView for image
 *
 * Created by Rudolf on 3/22/2016.
 */
public class PhotoPageFragment extends VisibleFragment implements View.OnCreateContextMenuListener, Runnable {

    private static final String TAG = "PhotoPageFragment";

    private static final String ARG_URI = "photo_page_url";

    private static final String METHOD_FULL_SCREEN = "full_screen";
    private static final String METHOD_SAVE = "save";
    private static final String METHOD_SHARE = "share";

    private Uri mUri;
    private String mImageUrl;

    private WebView mWebView;
    private ProgressBar mProgressBar;

    public static PhotoPageFragment newInstance(Uri uri) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);

        PhotoPageFragment fragment = new PhotoPageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mUri = getArguments().getParcelable(ARG_URI);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_page, container, false);

        mProgressBar = (ProgressBar) view.findViewById(R.id.fragment_photo_page_progress_bar);
        mProgressBar.setMax(100);

        mWebView = (WebView) view.findViewById(R.id.fragment_photo_page_web_view);

        registerForContextMenu(mWebView);

        enableJavascript();
        setWebViewClient();
        setClientToRightActivity(); // If not WebView

        // Load URL into WebView
        mWebView.loadUrl(mUri.toString());

        Log.i(TAG, "Loaded Uri: " + mUri.toString());

        mWebView.setOnCreateContextMenuListener(this);

        return view;
    }

    private void enableJavascript() {
        mWebView.getSettings().setJavaScriptEnabled(true);
    }

    private void setWebViewClient() {
        mWebView.setWebChromeClient(new WebChromeClient() {

            /**
             * Displays progress bar
             *
             * @param view
             * @param newProgress
             */
            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                if (newProgress == 100) {
                    mProgressBar.setVisibility(View.GONE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mProgressBar.setProgress(newProgress);
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
    }

    private void setClientToRightActivity() {
        // Override WebViewClient method to return false
        // This lets the app open the clicked image's webview in
        // the task and not in the phone's default browser
        mWebView.setWebViewClient(new WebViewClient() {
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        HitTestResult result = mWebView.getHitTestResult();

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
        menu.setHeaderTitle("Context Menu");
        menu.add(0, R.id.enter_full_screen_context_item, 0, "Enter fullscreen");
        menu.add(0, R.id.save_image_context_item, 0, "Save Image");
        menu.add(0, R.id.share_image_context_item, 0, "Share");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.enter_full_screen_context_item:

                showFullScreenToast();

                break;

            case R.id.save_image_context_item:

                showDownloadingToast();

                new RetrieveImageTask(METHOD_SAVE).execute();

                break;

            case R.id.share_image_context_item:

                break;

        }

        return super.onContextItemSelected(item);
    }

    private void showFullScreenToast() {

        String fullScreenMsg = "Fullscreen mode ON";

        Toast.makeText(getActivity(), fullScreenMsg, Toast.LENGTH_SHORT).show();
    }

    private void showDownloadingToast() {
        String downloadingMsg = "Downloading image...";

        Toast.makeText(getActivity(), downloadingMsg, Toast.LENGTH_SHORT).show();
    }

    /**
     * Back press moves WebView back through its items in its browsing history
     *
     * @return
     */
    public boolean isBackPressed() {

        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return true;
        }

        return false;
    }

    @Override
    public void run() {
        new RetrieveImageTask(null).execute();
    }

    private class RetrieveImageTask extends AsyncTask<String, Void, Void> {

        private String mMethod;

        public RetrieveImageTask(String method) {
            mMethod = method;
        }

        @Override
        protected Void doInBackground(String... params) {

            byte[] byteArray = null;

            try {
                byteArray = FlickrFetchr.getUrlBytes(mImageUrl);
            } catch(IOException e) {
                e.printStackTrace();
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            File sdCardDirectory = Environment.getExternalStorageDirectory();
            String filename = mImageUrl.substring(mImageUrl.lastIndexOf('/') + 1, mImageUrl.length());
            File image = new File(sdCardDirectory, filename);

            FileOutputStream outputStream;

            try {

                outputStream = new FileOutputStream(image);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                outputStream.flush();
                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            switch (mMethod) {

                case METHOD_FULL_SCREEN:

                    break;

                case METHOD_SAVE:

                    String message = "Image saved!";

                    Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();

                    break;

                case METHOD_SHARE:

                    break;

            }

        }
    }
}
