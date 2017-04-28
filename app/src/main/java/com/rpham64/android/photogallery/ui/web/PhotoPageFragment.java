package com.rpham64.android.photogallery.ui.web;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.rpham64.android.photogallery.ui.VisibleFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnLongClick;
import butterknife.Unbinder;
import tr.xip.errorview.ErrorView;

/**
 * WebView for image
 *
 * Created by Rudolf on 3/22/2016.
 */
public class PhotoPageFragment extends VisibleFragment implements
        View.OnCreateContextMenuListener, PhotoPagePresenter.View {

    private static final String TAG = PhotoPageFragment.class.getName();

    interface Arguments {
        String ARG_URI = "PhotoPageFragment.uri";
    }

    @BindView(R.id.fragment_photo_page_progress_bar) ProgressBar barProgress;
    @BindView(R.id.fragment_photo_page_web_view) WebView viewWeb;
    @BindView(R.id.error) ErrorView viewError;

    private Unbinder mUnbinder;
    private PhotoPagePresenter mPresenter;

    private Uri mWebUri;
    private String mImageUrl;

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

        mPresenter = new PhotoPagePresenter(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_page, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mPresenter.attachView(this);

        barProgress.setMax(100);

        registerForContextMenu(viewWeb);

        viewWeb.setOnCreateContextMenuListener(this);
        viewWeb.getSettings().setJavaScriptEnabled(true);
        viewWeb.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(WebView view, int newProgress) {

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
                activity.getSupportActionBar().setSubtitle(title);
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
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(0, R.id.enter_full_screen_context_item, 0, "Enter fullscreen");
        menu.add(0, R.id.copy_link_address_context_item, 0, "Copy link address");
        menu.add(0, R.id.save_image_context_item, 0, "Save Image");
        menu.add(0, R.id.share_image_context_item, 0, "Share");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if (isNotAllowed()) return false;

        Uri imageUri = Uri.parse(mImageUrl);

        switch (item.getItemId()) {

            case R.id.enter_full_screen_context_item:

                mPresenter.viewFullScreenMode(imageUri);
                break;

            case R.id.copy_link_address_context_item:

                mPresenter.copyLink(imageUri);
                break;

            case R.id.save_image_context_item:

                mPresenter.saveImage(mImageUrl);
                break;

            case R.id.share_image_context_item:

                mPresenter.shareImage(mImageUrl);
                break;

        }

        return super.onContextItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @OnLongClick(R.id.fragment_photo_page_web_view)
    public boolean onImageLongClick() {
        HitTestResult result = viewWeb.getHitTestResult();

        if (isImageType(result) || isSrcImageAnchorType(result)) {
            mImageUrl = result.getExtra();  // Save Image URL
        }

        return true;
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
     * Unable to access flickr image due to owner's permissions
     *
     * @return
     */
    private boolean isNotAllowed() {
        if (mImageUrl.endsWith("spaceball.gif")) {
            Toast.makeText(getActivity(), getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private boolean isImageType(HitTestResult result) {
        return result.getType() == HitTestResult.IMAGE_TYPE;
    }

    private boolean isSrcImageAnchorType(HitTestResult result) {
        return result.getType() == HitTestResult.SRC_IMAGE_ANCHOR_TYPE;
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
}
