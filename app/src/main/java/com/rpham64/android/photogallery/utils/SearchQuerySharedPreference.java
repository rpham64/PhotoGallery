package com.rpham64.android.photogallery.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Manages the gallery's submitted search query by storing and retrieving it in a local
 * SharedPreferences file.
 */
public class SearchQuerySharedPreference {

    private static final String PREF_KEY_SEARCH_QUERY = "PREF_KEY_SEARCH_QUERY";

    private final SharedPreferences mSharedPreferences;

    public SearchQuerySharedPreference(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public String getStoredQuery() {
        return mSharedPreferences.getString(PREF_KEY_SEARCH_QUERY, null);
    }

    public void setStoredQuery(String query) {
        mSharedPreferences.edit().putString(PREF_KEY_SEARCH_QUERY, query).apply();
    }
}
