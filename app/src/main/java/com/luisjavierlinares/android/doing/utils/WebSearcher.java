package com.luisjavierlinares.android.doing.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Luis on 01/09/2017.
 */

public class WebSearcher {

    public static enum SearchEngine {GOOGLE};

    public static String googleQueryUrl = "http://www.google.com/search?q=";

    private Context mContext;

    private SearchEngine mSearchEngine;

    public WebSearcher(Context context, SearchEngine searchEngine) {
        mContext = context;
        mSearchEngine = searchEngine;
    }

    public void search(String keywords) throws UnsupportedEncodingException {
        String query = URLEncoder.encode(keywords, "utf-8");
        String url = new String();

        switch (mSearchEngine) {
            case GOOGLE:
                url = googleQueryUrl + query;
                break;
            default:
                return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));

        mContext.startActivity(intent);
    }
}
