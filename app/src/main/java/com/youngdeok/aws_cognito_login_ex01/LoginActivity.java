package com.youngdeok.aws_cognito_login_ex01;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.youngdeok.aws_cognito_login_ex01.api.AWSLoginHandler;
import com.youngdeok.aws_cognito_login_ex01.api.AWSLoginModel;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, AWSLoginHandler {

    AWSLoginModel awsLoginModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public void onRegisterSuccess(boolean mustConfirmToComplete) {

    }

    @Override
    public void onRegisterConfirmed() {

    }

    @Override
    public void onSignInSuccess() {

    }

    @Override
    public void onResendConfirmationCodeSuccess(String medium) {

    }

    @Override
    public void onRequestResetUserPasswordSuccess(String medium) {

    }

    @Override
    public void onResetUserPasswordSuccess() {

    }

    @Override
    public void onFailure(int process, Exception exception, int cause, String message) {

    }
}