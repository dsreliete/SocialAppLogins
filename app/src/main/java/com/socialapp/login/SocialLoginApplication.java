package com.socialapp.login;

import android.app.Application;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import io.fabric.sdk.android.Fabric;

/**
 * Created by eliete on 4/25/16.
 */
public class SocialLoginApplication extends Application {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "6zC60g29TvkxAPYLNEzwSFTwK";
    private static final String TWITTER_SECRET = "Nj7k6371R8RRd4e7hEQo6Zw58hB6i9VsvWMiIBQlfpe9LPeBSO";


    private static SocialLoginApplication socialLoginApplication;

    @Override
    public void onCreate() {
        super.onCreate();

        socialLoginApplication = this;

        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }
}
