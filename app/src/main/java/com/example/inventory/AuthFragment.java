package com.example.inventory;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class AuthFragment extends Fragment {


    public AuthFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            RelativeLayout authExplain = view.findViewById(R.id.authExplain);
            LinearLayout authWait = view.findViewById(R.id.authWait);
            MaterialButton loginButton = view.findViewById(R.id.authButton);

            authExplain.setVisibility(View.VISIBLE);
            authWait.setVisibility(View.GONE);

            loginButton.setOnClickListener(v -> {
                authExplain.setVisibility(View.GONE);
                authWait.setVisibility(View.VISIBLE);
                ((MainActivity) requireActivity()).login();
            });
        } else {
            ((MainActivity) requireActivity()).setToolbarImage(user.getPhotoUrl());
            Navigation.findNavController(view)
                    .navigate(R.id.action_auth_to_login);
        }
    }
}
