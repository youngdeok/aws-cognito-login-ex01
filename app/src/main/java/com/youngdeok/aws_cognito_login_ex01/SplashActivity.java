package com.youngdeok.aws_cognito_login_ex01;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.StartupAuthResult;
import com.amazonaws.mobile.auth.core.StartupAuthResultHandler;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.IdentityProvider;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amplifyframework.AmplifyException;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
import com.amplifyframework.core.Amplify;

public class SplashActivity extends AppCompatActivity {

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

        AWSMobileClient mobileClient = (AWSMobileClient) Amplify.Auth.getPlugin("awsCognitoAuthPlugin").getEscapeHatch();
        if (mobileClient != null) {
            mobileClient.initialize(SplashActivity.this, new Callback<UserStateDetails>() {
                @Override
                public void onResult(UserStateDetails result) {
                    IdentityManager identityManager = IdentityManager.getDefaultIdentityManager();
                    identityManager.resumeSession(SplashActivity.this, new StartupAuthResultHandler() {
                        @Override
                        public void onComplete(StartupAuthResult authResults) {
                            if (authResults.isUserSignedIn()) {
                                startActivity(new Intent(SplashActivity.this, MainActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            } else {
                                startActivity(new Intent(SplashActivity.this, LoginActivity.class)
                                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                            }
                        }
                    });
                }

                @Override
                public void onError(Exception e) {
                    Log.e("onError", e.toString());
                }
            });
        }

    }
}