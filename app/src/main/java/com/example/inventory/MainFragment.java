package com.example.inventory;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventory.inventory.InventoryItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.util.List;
import java.util.Map;


public class MainFragment extends Fragment {

    private RecyclerView recycler;
    private LinearLayout loadingLayout;
    private TextView errorTv;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recycler = view.findViewById(R.id.inventoryRecycler);
        loadingLayout = view.findViewById(R.id.inventoryLoading);
        errorTv = view.findViewById(R.id.inventoryErrorTv);
        setViewVisibility(1, null);

        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            initFirestore(user);
        } else {
            ((MainActivity) requireActivity()).login();
        }
    }

    private void initFirestore(FirebaseUser user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("stock").document(user.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            onInventory(document);
                        } else {
                            blankInventory();
                        }
                    } else {
                        if (task.getException() != null)
                            onError(task.getException().getMessage());
                        else
                            onError(null);
                    }
                });
    }

    private void onInventory(DocumentSnapshot doc) {
        setViewVisibility(2, null);

        Log.i("MainFragment", "initFirestore: " + doc.getData());
        ItemAdapter<InventoryItem> itemAdapter = new ItemAdapter<>();
        FastAdapter<InventoryItem> fastAdapter = FastAdapter.with(itemAdapter);
        recycler.setAdapter(fastAdapter);
        itemAdapter.add(InventoryItem.getItems(
                (List<Map<String, Object>>) doc.get("items"), requireContext()));
    }

    private void blankInventory() {
        setViewVisibility(3, "No items. Add new items to get started.");
        Log.i("MainFragment", "blankInventory: ");
    }

    private void onError(String err) {
        String message = "%s Please try again.";
        if (err != null)
            message = String.format(message, err);
        else
            message = String.format(message, "An error occurred.");

        setViewVisibility(3, message);
        Log.i("MainFragment", "onError: " + err);
    }

    private void setViewVisibility(int type, String message) {
        switch (type) {
            case 1:
                recycler.setVisibility(View.GONE);
                loadingLayout.setVisibility(View.VISIBLE);
                errorTv.setVisibility(View.GONE);
                break;
            case 2:
                recycler.setVisibility(View.VISIBLE);
                loadingLayout.setVisibility(View.GONE);
                errorTv.setVisibility(View.GONE);
                break;
            case 3:
                recycler.setVisibility(View.GONE);
                loadingLayout.setVisibility(View.GONE);
                errorTv.setVisibility(View.VISIBLE);
                errorTv.setText(message);
                break;
        }
    }
}
