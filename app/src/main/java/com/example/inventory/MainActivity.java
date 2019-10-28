package com.example.inventory;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.example.inventory.utils.GenericDialogFragment;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity {
    private static final int AUTH_ID = 5524;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login();
    }

    private void login() {
        List<AuthUI.IdpConfig> providers = Collections.singletonList(
                new AuthUI.IdpConfig.GoogleBuilder().build());
        startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                AUTH_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AUTH_ID) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    showAuthFailed();
                    return;
                }
                onAuthComplete(user);
            } else {
                Log.i("bleah", "onActivityResult: " + response.getError().getMessage());
                showAuthFailed();
            }
        }
    }

    private void onAuthComplete(FirebaseUser user) {
        Log.i("bleah", "onActivityResult: " + user.getDisplayName()+ " " + user.getPhotoUrl());
        LinearLayout authLayout = findViewById(R.id.authWait);
        authLayout.setVisibility(GONE);
    }

    private void showAuthFailed() {
        GenericDialogFragment dialogFragment = GenericDialogFragment.newInstance()
                .setDialogCancelable(false)
                .setMessage(getString(R.string.auth_failed_desc))
                .setTitle(getString(R.string.auth_failed_title))
                .setOnPositiveButtonTappedListener(this::login);

        dialogFragment.show(getSupportFragmentManager(), null);
    }

}
