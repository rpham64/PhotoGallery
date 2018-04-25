package com.rpham64.android.photogallery.ui.web;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.rpham64.android.photogallery.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import tr.xip.errorview.ErrorView;

/**
 * WebView for image
 *
 * Created by Rudolf on 3/22/2016.
 */
public class PhotoPageFragment extends Fragment {

    private static final String TAG = PhotoPageFragment.class.getName();

    interface Arguments {
        String ARG_URI = "PhotoPageFragment.uri";
    }

    @BindView(R.id.toolbar_photo_page_fragment) Toolbar toolbar;
    @BindView(R.id.fragment_photo_page_progress_bar) ProgressBar barProgress;
    @BindView(R.id.fragment_photo_page_web_view) WebView viewWeb;
    @BindView(R.id.error) ErrorView viewError;

    private Unbinder mUnbinder;

    private Uri mWebUri;

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
            mWebUri = getArguments().getParcelable(Arguments.ARG_URI);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_page, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

        barProgress.setMax(100);

        viewWeb.getSettings().setJavaScriptEnabled(true);
        viewWeb.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {

                if (barProgress == null) return;

                if (newProgress == 100) {
                    barProgress.setVisibility(View.GONE);
                } else {
                    barProgress.setVisibility(View.VISIBLE);
                    barProgress.setProgress(newProgress);
                }

            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                if (activity != null) {
                    activity.getSupportActionBar().setSubtitle(title);
                }
            }
        });

        // Override WebViewClient method to return false
        // This lets the app open the clicked image's webview in
        // the task and not in the phone's default browser
        viewWeb.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Log.i(TAG, "Url : " + url);

                if (!URLUtil.isHttpUrl(url) && !URLUtil.isHttpsUrl(url)) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);

                    return true;
                }

                return false;
            }
        });

        // Load URL into WebView
        viewWeb.loadUrl(mWebUri.toString());

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_photo_web_view, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        setShareIntent(item);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // TODO: Figure out why android.R.id.home doesn't exist.
//            case android.R.id.home:
//                NavUtils.navigateUpFromSameTask(getActivity());
//                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    private void setShareIntent(MenuItem item) {
        // Fetch and store ShareActionProvider
        ShareActionProvider shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (shareActionProvider != null) {
            shareActionProvider.setShareIntent(createShareIntent());
        }
    }

    @NonNull
    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, viewWeb.getUrl());
        return shareIntent;
    }

    /**
     * Back press moves WebView back through its items in its browsing history
     *
     * @return
     */
    public boolean canGoBackThroughWebView() {

        if (viewWeb != null && viewWeb.canGoBack()) {
            viewWeb.goBack();
            return true;
        }

        return false;
    }
}
