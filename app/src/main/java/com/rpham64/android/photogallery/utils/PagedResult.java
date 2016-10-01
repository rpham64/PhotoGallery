package com.rpham64.android.photogallery.utils;

/**
 * Created by Rudolf on 9/26/2016.
 */

public class PagedResult {

    public final int page;
    public int pages;
    public int perPage;
    public boolean canLoadMore;
    public boolean replace;

    public PagedResult(int page, int pages) {
        this.page = page;
        this.pages = pages;
    }

    public PagedResult(int page, int pages, int perPage, boolean canLoadMore) {
        this.page = page;
        this.pages = pages;
        this.perPage = perPage;
        this.canLoadMore = canLoadMore;
        this.replace = page == Pager.PAGE_START;
    }
}