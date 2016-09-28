package com.rpham64.android.photogallery.utils;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by Rudolf on 9/26/2016.
 */

public class Pager {

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int PAGE_START = 1;

    private int pageSize = DEFAULT_PAGE_SIZE;
    private int page;
    private PublishSubject<Pager> publishSubject = PublishSubject.create();
    private boolean canLoadMore = true;

    public Pager() {
        this(DEFAULT_PAGE_SIZE);
    }

    public Pager(int pageSize) {
        this.pageSize = pageSize;
    }


    public int getPageSize() {
        return pageSize;
    }

    public int getPage() {
        return page;
    }

    public void nextPage() {
        page += 1;
        publishSubject.onNext(this);
    }

    public void firstPage() {
        page = PAGE_START;
        publishSubject.onNext(this);
    }

    public void setCanLoadMore(boolean canLoadMore) {
        this.canLoadMore = canLoadMore;
    }

    public boolean canLoadMore() {
        return canLoadMore;
    }


    public Observable<Pager> asObservable() {
        return publishSubject.asObservable();
    }
}