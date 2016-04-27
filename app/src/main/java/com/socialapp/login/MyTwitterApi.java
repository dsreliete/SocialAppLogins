package com.socialapp.login;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.User;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by eliete on 4/26/16.
 */
public class MyTwitterApi extends TwitterApiClient {
    public MyTwitterApi(TwitterSession session) {
        super(session);
    }

    public CustomService getCustomService() {
        return getService(CustomService.class);
    }

    interface CustomService {
        @GET("/1.1/users/show.json")
        void show(@Query("user_id") long id, Callback<User> cb);
    }
}
