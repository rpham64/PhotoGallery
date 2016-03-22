package com.bignerdranch.android.photogallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * WebView for image
 *
 * Created by Rudolf on 3/22/2016.
 */
public class PhotoPageFragment extends VisibleFragment {

    private static final String TAG = "PhotoPageFragment";

    private static final String ARG_URI = "photo_page_url";

    private Uri mUri;       // Photo URL

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

        // Enable JavaScript
        mWebView.getSettings().setJavaScriptEnabled(true);

        // Implement chrome handler view
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

        // Load URL into WebView
        mWebView.loadUrl(mUri.toString());

        Log.i(TAG, "Loaded Uri: " + mUri.toString());

        return view;
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
}
