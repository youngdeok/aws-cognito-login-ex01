package com.youngdeok.aws_cognito_login_ex01;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.StartupAuthResult;
import com.amazonaws.mobile.auth.core.StartupAuthResultHandler;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;

public class SplashActivity extends AppCompatActivity {

    SplashActivity splashActivity;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Add this line, to include the Auth plugin.
        try {
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            Amplify.configure(getApplicationContext());
        } catch (AmplifyException e) {
            e.printStackTrace();
        }

        Amplify.Auth.fetchAuthSession(
                result -> Log.i("AmplifyQuickstart", result.toString()),
                error -> Log.e("AmplifyQuickstart", error.toString())
        );

        AWSMobileClient mobileClient = (AWSMobileClient) Amplify.Auth.getPlugin("awsCognitoAuthPlugin").getEscapeHatch();
        if (mobileClient != null) {
            mobileClient.initialize(SplashActivity.this, new Callback<UserStateDetails>() {
                @Override
                public void onResult(UserStateDetails userStateDetails) {

                    switch (userStateDetails.getUserState()){
                        case SIGNED_IN:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    IdentityManager identityManager = IdentityManager.getDefaultIdentityManager();
                                    identityManager.resumeSession(SplashActivity.this, new StartupAuthResultHandler() {
                                        @Override
                                        public void onComplete(StartupAuthResult authResults) {
                                            if (authResults != null && authResults.isUserSignedIn()) {
                                                Log.d("AuthResults", authResults.toString());
                                            } else {
                                                Log.d("AuthResults null", authResults.toString());
                                            }
                                        }
                                    });

                                    startActivity(new Intent(SplashActivity.this, MainActivity.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                }
                            });
                            break;
                        case SIGNED_OUT:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    startActivity(new Intent(SplashActivity.this, LoginActivity.class)
                                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                }
                            });
                            break;
                        default:
                            AWSMobileClient.getInstance().signOut();
                            break;
                    }
                }

                @Override
                public void onError(Exception e) {
                    Log.e("onError", e.toString());
                }
            });
        }
    }
}