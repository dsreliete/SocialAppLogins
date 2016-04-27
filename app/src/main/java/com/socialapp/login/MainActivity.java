package com.socialapp.login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.LoggingBehavior;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;

import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {

    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.facebook_login_button)
    LoginButton faceLoginButton;
    @Bind(R.id.twitter_login_button)
    TwitterLoginButton twitterLoginButton;
    @Bind(R.id.google_login_button)
    SignInButton googleLoginButton;
    private ProgressDialog mProgressDialog;

    public static final String[] facebookPermissions = {"public_profile", "email", "user_friends"};
    public static final String TAG = MainActivity.class.getName();
    private static final int RC_SIGN_IN = 9001;

    private SocialLoginApplication socialLoginApplication;
    private CallbackManager callbackManager;
    private AccessToken accessToken;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        socialLoginApplication = (SocialLoginApplication) getApplicationContext();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (BuildConfig.DEBUG) {
            FacebookSdk.setIsDebugEnabled(true);
            FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        }

        setSupportActionBar(toolbar);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        googleLoginButton.setSize(SignInButton.SIZE_STANDARD);
        googleLoginButton.setScopes(gso.getScopeArray());

        callbackManager = CallbackManager.Factory.create();

        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                // The TwitterSession is also available through:
                // Twitter.getInstance().core.getSessionManager().getActiveSession()
                TwitterSession session = result.data;
                getDataFromTwitter(session);
            }
            @Override
            public void failure(TwitterException exception) {
                Log.e("TwitterKit", "Login with Twitter failure", exception);
            }
        });
    }

    @OnClick(R.id.facebook_login_button) public void loginByFacebook() {
        faceLoginButton.setReadPermissions(facebookPermissions);
        faceLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {
                accessToken = loginResult.getAccessToken();
                if (accessToken != null)
                    getDataFromFacebook();
            }

            @Override
            public void onCancel() {
                createToast("login was canceled. Try again");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("facebook", "Login with facebook failure", error);
                createToast("Can't login. Try Again");
            }
        });
    }

    @OnClick(R.id.google_login_button) public void loginByGoogle(){
        showProgressDialog();
        signIn();
    }


    public void getDataFromTwitter(TwitterSession twitterSession){
        MyTwitterApi twitterApi = new MyTwitterApi(twitterSession);
        twitterApi.getCustomService().show(twitterSession.getUserId(), new Callback<User>() {
            @Override
            public void success(Result<User> result) {
                String description = result.data.description;
                String name = result.data.name;
                String location = result.data.location;
                String imageUrl = result.data.profileImageUrlHttps;
                Log.d(TAG, "twitter result = " + description + " " + name + " " + location + " " + imageUrl);
            }

            @Override
            public void failure(TwitterException e) {
                Log.e("TwitterKit", "get data from Twitter failure", e);
                createToast("Can't login. Try Again");
            }
        });
    }

    private void getDataFromFacebook() {
        GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                Person person = Person.getPersonFomJson(object);
                Log.d(TAG, "facebook " + person.toString());
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id, name, email");
        request.setParameters(parameters);
        request.executeAsync();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            hideProgressDialog();
            GoogleSignInAccount googleAcount = result.getSignInAccount();
            Log.d(TAG, "google account info: " + googleAcount.getPhotoUrl());
            Person person = new Person(googleAcount.getDisplayName(), googleAcount.getEmail());
            Log.d(TAG, "googleAccount " + person.toString());
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.e(TAG, "Google onConnectionFailed:" + connectionResult);
    }


    private void createToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> googlePendingResult = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (googlePendingResult.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            Log.d(TAG, "Got cached sign-in Google");
            GoogleSignInResult result = googlePendingResult.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            showProgressDialog();
            googlePendingResult.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }
}
