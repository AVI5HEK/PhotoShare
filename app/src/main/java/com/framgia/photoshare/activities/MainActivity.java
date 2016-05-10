package com.framgia.photoshare.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.framgia.photoshare.R;
import com.framgia.photoshare.utils.SessionManager;

import java.util.Arrays;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity implements Button.OnClickListener {

    @InjectView(R.id.button_login_fb)
    Button mButtonLogin;
    private CallbackManager mCallbackManager;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(getApplicationContext());
        if (session.isLoggedIn()) {
            goToSharePhotoActivity();

        } else {
            session.deleteSessionData();
        }
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        FacebookSdk.sdkInitialize(getApplicationContext());
        mCallbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        session.createLoginSession(true);
                        goToSharePhotoActivity();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(MainActivity.this, "Login Cancel", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(MainActivity.this, exception.getMessage(), Toast
                                .LENGTH_LONG).show();
                    }
                });
        mButtonLogin.setOnClickListener(this);
    }

    private void goToSharePhotoActivity() {
        startActivity(new Intent(MainActivity.this, SharePhotoActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_login_fb:
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList
                        ("email, publish_actions"));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
