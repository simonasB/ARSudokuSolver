package com.puzzleslab.arsudokusolver.Clients;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.puzzleslab.arsudokusolver.Utils.SudokuUtils;

/**
 * Created by Simonas on 2016-05-20.
 */
public class RestClient {
    private static AsyncHttpClient client = new SyncHttpClient();
    private static String baseUrl;

    public static void setBaseUrl(String url) {
        baseUrl = url;
    }

    public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return baseUrl + relativeUrl;
    }
}
