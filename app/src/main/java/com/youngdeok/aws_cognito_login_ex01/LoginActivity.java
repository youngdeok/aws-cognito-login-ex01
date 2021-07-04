package com.youngdeok.aws_cognito_login_ex01;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.youngdeok.aws_cognito_login_ex01.api.AWSLoginHandler;
import com.youngdeok.aws_cognito_login_ex01.api.AWSLoginModel;
import com.youngdeok.aws_cognito_login_ex01.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity implements AWSLoginHandler {

    AWSLoginModel awsLoginModel;
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        awsLoginModel = new AWSLoginModel(this, this);
        binding = DataBindingUtil.setContentView(LoginActivity.this, R.layout.activity_login);
        binding.setLogin(this);
    }

    public void onClickListener(View view) {
        switch (view.getId()) {
            case R.id.btn_register_user:
                registerAction();
                break;
            case R.id.btn_forgot:
                forgotPasswordAction();
                break;
            case R.id.btn_login:
                loginAction();
                break;
            case R.id.btn_reset_password:
                resetAction();
                break;
            case R.id.btn_confirm:
                confirmAction();
                break;
            case R.id.btn_resend_code:
                resendConfirmationAction();
                break;
            case R.id.btn_show_signup:
                showRegisterAction(true);
                break;
            case R.id.btn_show_signin:
                showLoginAction(true);
                break;

        }
    }

    private void registerAction() {
        // do register and handles on interface
        awsLoginModel.registerUser(
                binding.edtRegisterUsername.getText().toString(),
                binding.edtRegisterEmail.getText().toString(),
                binding.edtRegisterPassword.getText().toString());
    }

    private void confirmAction() {
        // do confirmation and handles on interface
        awsLoginModel.confirmRegistration(
                binding.edtConfirmCode.getText().toString());
    }

    private void resendConfirmationAction() {
        // do resend confirmation code and handles on interface
        awsLoginModel.resendConfirmationCode();
    }

    private void loginAction() {
        // do sign in and handles on interface
        awsLoginModel.signInUser(
                binding.edtUsername.getText().toString(),
                binding.edtPassword.getText().toString());
    }

    private void forgotPasswordAction() {
        if (binding.edtUsername.getText().toString().isEmpty()) {
            Toast.makeText(LoginActivity.this, "Username required.", Toast.LENGTH_LONG).show();
        } else {
            awsLoginModel.requestResetUserPassword(binding.edtUsername.getText().toString());
        }
    }

    private void resetAction() {
        // request reset password and handles on interface
        awsLoginModel.resetUserPasswordWithCode(
                binding.edtResetCode.getText().toString(),
                binding.edtNewPassword.getText().toString());
    }

    private void showLoginAction(boolean show) {
        if (show) {
            showRegisterAction(false);
            showConfirm(false);
            showForgotAction(false);
            binding.loginContainer.setVisibility(View.VISIBLE);
            binding.btnShowSignup.setVisibility(View.VISIBLE);
            binding.btnShowSignin.setVisibility(View.GONE);
        } else {
            binding.loginContainer.setVisibility(View.GONE);
            binding.edtUsername.setText("");
            binding.edtPassword.setText("");
        }
    }

    private void showRegisterAction(boolean show) {
        if (show) {
            showLoginAction(false);
            showConfirm(false);
            showForgotAction(false);
            binding.registerContainer.setVisibility(View.VISIBLE);
            binding.btnShowSignup.setVisibility(View.GONE);
            binding.btnShowSignin.setVisibility(View.VISIBLE);
        } else {
            binding.registerContainer.setVisibility(View.GONE);
            binding.edtRegisterUsername.setText("");
            binding.edtRegisterEmail.setText("");
            binding.edtRegisterPassword.setText("");
        }
    }

    private void showConfirm(boolean show) {
        if (show) {
            showLoginAction(false);
            showRegisterAction(false);
            showForgotAction(false);
            binding.confirmContainer.setVisibility(View.VISIBLE);
            binding.btnShowSignup.setVisibility(View.GONE);
            binding.btnShowSignin.setVisibility(View.VISIBLE);
        } else {
            binding.confirmContainer.setVisibility(View.GONE);
            binding.edtConfirmCode.setText("");
        }
    }

    private void showForgotAction(boolean show) {
        if (show) {
            showLoginAction(false);
            showRegisterAction(false);
            showConfirm(false);
            binding.forgotContainer.setVisibility(View.VISIBLE);
            binding.btnShowSignup.setVisibility(View.GONE);
            binding.btnShowSignin.setVisibility(View.VISIBLE);
        } else {
            binding.forgotContainer.setVisibility(View.GONE);
            binding.edtResetCode.setText("");
            binding.edtNewPassword.setText("");
        }
    }

    @Override
    public void onRegisterSuccess(boolean mustConfirmToComplete) {
        if (mustConfirmToComplete) {
            Toast.makeText(LoginActivity.this, "Almost done! Confirm code to complete registration", Toast.LENGTH_LONG).show();
            showConfirm(true);
        } else {
            Toast.makeText(LoginActivity.this, "Registered! Login Now!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRegisterConfirmed() {
        Toast.makeText(LoginActivity.this, "Registered! Login Now!", Toast.LENGTH_LONG).show();
        showLoginAction(true);
    }

    @Override
    public void onSignInSuccess() {
        LoginActivity.this.startActivity(new Intent(LoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    @Override
    public void onResendConfirmationCodeSuccess(String medium) {
        Toast.makeText(LoginActivity.this, "Confirmation code sent! Destination:" + medium, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestResetUserPasswordSuccess(String medium) {
        Toast.makeText(LoginActivity.this, "Reset code sent! Destination:" + medium, Toast.LENGTH_LONG).show();
        showForgotAction(true);
    }

    @Override
    public void onResetUserPasswordSuccess() {
        Toast.makeText(LoginActivity.this, "Password reset! Login Now!", Toast.LENGTH_LONG).show();
        showLoginAction(true);
    }

    @Override
    public void onFailure(int process, Exception exception, int cause, String message) {
        Toast.makeText(LoginActivity.this,  message, Toast.LENGTH_LONG).show();
        if (cause != AWSLoginModel.CAUSE_MUST_CONFIRM_FIRST) {
            exception.printStackTrace();
        } else {
            showConfirm(true);
        }
    }
}