package com.example.inventory;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.Navigation;

import com.example.inventory.utils.GenericDialogFragment;
import com.example.inventory.utils.Toolbar;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    private static final int AUTH_ID = 5524;
    private static final String PROFILE_IMAGE_URI = "profilePhotoUri";

    private Toolbar toolbar;
    private Uri profileImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        if (savedInstanceState != null) {
            Log.i("blah", "onCreate: ");
            profileImageUri = savedInstanceState.getParcelable(PROFILE_IMAGE_URI);
            if (profileImageUri != null)
                toolbar.setImage(profileImageUri);
        }
        setupTranslucent();
    }

    public void login() {
        List<AuthUI.IdpConfig> providers = Collections.singletonList(
                new AuthUI.IdpConfig.GoogleBuilder().build());
        startActivityForResult(AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
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
                    showAuthFailed(null);
                    return;
                }
                onAuthComplete(user);
            } else {
                String message = null;
                if (response != null && response.getError() != null) {
                    message = response.getError().getMessage();
                }
                showAuthFailed(message);
            }
        }
    }

    private void onAuthComplete(FirebaseUser user) {
        setToolbarImage(user.getPhotoUrl());
        Navigation.findNavController(this, R.id.mainNav)
                .navigate(R.id.action_auth_to_login);
    }

    public void setToolbarImage(Uri uri) {
        profileImageUri = uri;
        toolbar.setImage(uri);
    }

    private void showAuthFailed(String err) {
        String message = getString(R.string.auth_failed_desc);
        if (err != null)
            message += " " + err + ".";

        GenericDialogFragment dialogFragment = GenericDialogFragment.newInstance()
                .setDialogCancelable(false)
                .setMessage(message)
                .setTitle(getString(R.string.auth_failed_title))
                .setOnPositiveButtonTappedListener(this::login);

        dialogFragment.show(getSupportFragmentManager(), null);
    }

    private void setupTranslucent() {
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= 26) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                            View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        } else {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }

        decorView.setOnApplyWindowInsetsListener((view, insets) -> {
            int topInset = insets.getSystemWindowInsetTop();
            int leftInset = insets.getSystemWindowInsetLeft();
            int rightInset = insets.getSystemWindowInsetRight();
            int actionbarHeight = getActionbarHeight(this);
            ViewGroup.LayoutParams toolbarParams = toolbar.getLayoutParams();
            toolbarParams.height = (actionbarHeight > -1 ? actionbarHeight : toolbarParams.height) +
                    topInset;
            toolbar.setPadding(leftInset, topInset, rightInset, 0);

            return insets.consumeSystemWindowInsets();
        });

    }

    public static int getActionbarHeight(Context context) {
        TypedValue tv = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data,
                    context.getResources().getDisplayMetrics());
        }
        return -1;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(PROFILE_IMAGE_URI, profileImageUri);
        super.onSaveInstanceState(outState);
    }
}
