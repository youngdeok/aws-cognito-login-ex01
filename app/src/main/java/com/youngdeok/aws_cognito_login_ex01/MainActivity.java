package com.youngdeok.aws_cognito_login_ex01;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.youngdeok.aws_cognito_login_ex01.api.AWSLoginModel;
import com.youngdeok.aws_cognito_login_ex01.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    // {XmlName}Binding - Pascal Expression
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setMain(this);  // because the <data>'s <variable> name is 'main' set{Name}
    }

    public void onClickListener(View view) {
        switch (view.getId()) {
            case R.id.btn_logout:
                logoutAction();
                break;
        }
    }

    private void logoutAction() {
        AWSLoginModel.doUserLogout();
        startActivity(new Intent(MainActivity.this, LoginActivity.class)
        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    @Override
    protected void onResume() {
        super.onResume();

        String user = AWSLoginModel.getSavedUserName(MainActivity.this);
        TextView username = findViewById(R.id.tv_username);
        username.setText(user);

    }
}