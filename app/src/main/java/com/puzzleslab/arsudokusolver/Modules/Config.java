package com.puzzleslab.arsudokusolver.modules;

/**
 * Created by Simonas on 2016-05-18.
 */
public class Config {
    private String apiUrl;
    private String authToken;
    private boolean isProduction;

    public Config(String apiUrl, String authToken, boolean isProduction) {
        this.apiUrl = apiUrl;
        this.authToken = authToken;
        this.isProduction = isProduction;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getAuthToken() {
        return authToken;
    }

    public boolean isProduction() {
        return isProduction;
    }
}
