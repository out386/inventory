package com.example.inventory;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.inventory.inventory.InventoryItem;
import com.example.inventory.inventory.ItemDecoration;
import com.example.inventory.utils.InputDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.fastadapter.utils.ComparableItemListImpl;
import com.mikepenz.fastadapter_extensions.drag.ItemTouchCallback;
import com.mikepenz.fastadapter_extensions.drag.SimpleDragCallback;
import com.mikepenz.fastadapter_extensions.swipe.SimpleSwipeCallback;
import com.mikepenz.fastadapter_extensions.swipe.SimpleSwipeDragCallback;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainFragment extends Fragment implements ItemTouchCallback,
        SimpleSwipeCallback.ItemSwipeCallback {

    private RecyclerView recycler;
    private LinearLayout loadingLayout;
    private LinearLayout inventoryData;
    private TextView errorTv;
    private TextView statusCount;
    private TextView statusValue;
    private ItemAdapter<InventoryItem> itemAdapter;
    private FirebaseUser user;
    private MaterialButton addButton;
    private NumberFormat numberFormat;
    private ItemDecoration itemDecoration;

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
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(2);
        recycler = view.findViewById(R.id.inventoryRecycler);
        loadingLayout = view.findViewById(R.id.inventoryLoading);
        inventoryData = view.findViewById(R.id.inventoryData);
        errorTv = view.findViewById(R.id.inventoryErrorTv);
        statusCount = view.findViewById(R.id.inventoryStatusCount);
        statusValue = view.findViewById(R.id.inventoryStatusValue);
        addButton = view.findViewById(R.id.inventoryAdd);

        addButton.setOnClickListener(v -> addItem());
        setViewVisibility(1, null, true);
        setupTranslucent(view);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            initFirestore();
        } else {
            ((MainActivity) requireActivity()).login();
        }
    }

    private void setupRecycler() {
        ComparableItemListImpl<InventoryItem> comparableItemList =
                new ComparableItemListImpl<>((o1, o2) ->
                        o1.getName().compareToIgnoreCase(o2.getName()));
        itemAdapter = new ItemAdapter<>(comparableItemList);
        FastAdapter<InventoryItem> fastAdapter = FastAdapter
                .with(itemAdapter);
        fastAdapter
                .withSelectable(true)
                .withOnClickListener(new OnInventoryClickedListener());

        Context context = requireContext();
        Drawable deleteDrawable = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        int deleteColour = ContextCompat.getColor(context, R.color.deleteItem);
        SimpleDragCallback touchCallback = new SimpleSwipeDragCallback(
                this,
                this,
                deleteDrawable,
                ItemTouchHelper.LEFT,
                deleteColour
        )
                .withBackgroundSwipeRight(deleteColour)
                .withLeaveBehindSwipeRight(deleteDrawable);
        touchCallback.setIsDragEnabled(false);

        ItemTouchHelper touchHelper = new ItemTouchHelper(touchCallback);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        recycler.setAdapter(fastAdapter);
        touchHelper.attachToRecyclerView(recycler);
    }

    private void setupTranslucent(View root) {
        setupRecycler();
        handleInsets(root.getRootWindowInsets());
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            handleInsets(insets);
            return insets;
        });

    }

    private void handleInsets(WindowInsets insets) {
        if (insets == null)
            return;
        int bottomInset = insets.getSystemWindowInsetTop();
        RelativeLayout.LayoutParams params =
                (RelativeLayout.LayoutParams) addButton.getLayoutParams();

        // Yes, I know that that "20" is not in DP.
        params.setMargins(0, 0, 0, 20 + bottomInset);
        if (itemDecoration != null)
            recycler.removeItemDecoration(itemDecoration);
        itemDecoration = new ItemDecoration(bottomInset);
        recycler.addItemDecoration(itemDecoration);
    }

    private void initFirestore() {
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

    @SuppressWarnings("unchecked")
    private void onInventory(DocumentSnapshot doc) {
        itemAdapter.clear();
        Map<String, Map<String, Double>> inventoryItems =
                (Map<String, Map<String, Double>>) doc.get("items");
        if (inventoryItems == null || inventoryItems.size() == 0) {
            blankInventory();
            return;
        }
        setViewVisibility(2, null, true);

        itemAdapter.add(InventoryItem.getItems(inventoryItems, requireContext()));
        setStatus();
    }

    private void blankInventory() {
        setViewVisibility(3, "No items. Add new items to get started.", true);
    }

    private void onError(String err) {
        String message = "%s Please try again.";
        if (err != null)
            message = String.format(message, err);
        else
            message = String.format(message, "An error occurred.");

        setViewVisibility(3, message, false);
    }

    private void setViewVisibility(int type, String message, boolean addAvailable) {
        switch (type) {
            case 1:
                inventoryData.setVisibility(View.GONE);
                loadingLayout.setVisibility(View.VISIBLE);
                errorTv.setVisibility(View.GONE);
                addButton.setVisibility(View.GONE);
                break;
            case 2:
                inventoryData.setVisibility(View.VISIBLE);
                loadingLayout.setVisibility(View.GONE);
                errorTv.setVisibility(View.GONE);
                addButton.setVisibility(View.VISIBLE);
                break;
            case 3:
                inventoryData.setVisibility(View.GONE);
                loadingLayout.setVisibility(View.GONE);
                errorTv.setVisibility(View.VISIBLE);
                addButton.setVisibility(addAvailable ? View.VISIBLE : View.GONE);
                errorTv.setText(message);
                break;
        }
    }

    private void addItem() {
        InputDialogFragment dialogFragment = InputDialogFragment.newInstance(requireContext());
        dialogFragment.setOnPositiveButtonTappedListener((name, price, quantity) -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference ref = db.collection("stock").document(user.getUid());

            if (checkDuplicate(name)) {
                Toast.makeText(requireContext(),
                        "This item already exists.", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Map<String, Map<String, Double>>> map = new HashMap<>();
            Map<String, Map<String, Double>> items = new HashMap<>();
            Map<String, Double> item = new HashMap<>();
            item.put("price", price);
            item.put("quantity", (double) quantity);
            items.put(name, item);
            map.put("items", items);
            ref.set(map, SetOptions.merge());

            itemAdapter.add(InventoryItem.getItems(items, requireContext()));
            setViewVisibility(2, null, true);
            setStatus();
        });
        dialogFragment.show(getFragmentManager(), null);
    }

    private void deleteItem(String name) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("stock").document(user.getUid());

        Map<String, Map<String, Object>> map = new HashMap<>();
        Map<String, Object> item = new HashMap<>();
        item.put(name, FieldValue.delete());
        map.put("items", item);
        ref.set(map, SetOptions.merge());
    }

    private boolean checkDuplicate(String name) {
        List<InventoryItem> items = itemAdapter.getAdapterItems();
        for (InventoryItem item : items) {
            if (item.getName().equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    @SuppressLint("SetTextI18n")
    private void setStatus() {
        double totalValue = 0;
        List<InventoryItem> items = itemAdapter.getAdapterItems();
        for (InventoryItem item : items) {
            totalValue = totalValue + item.getPrice() * item.getQuantity();
        }
        String currencySymbol = Currency.getInstance(Locale.getDefault()).getSymbol();
        String value = String.format("%s %s", currencySymbol,
                numberFormat.format(totalValue));
        statusValue.setText(value);
        statusCount.setText(Integer.toString(items.size()));
    }

    // FastAdapter's Swipe to delete

    @Override
    public void itemTouchDropped(int oldPosition, int newPosition) {
    }

    @Override
    public void itemSwiped(int position, int direction) {
        deleteItem(itemAdapter.getAdapterItem(position).getName());
        itemAdapter.remove(position);
        setStatus();
        if (itemAdapter.getAdapterItemCount() == 0)
            blankInventory();
    }

    @Override
    public boolean itemTouchOnMove(int oldPosition, int newPosition) {
        return false;
    }

    class OnInventoryClickedListener implements OnClickListener<InventoryItem> {
        @Override
        public boolean onClick(@javax.annotation.Nullable View v, IAdapter<InventoryItem> adapter,
                               InventoryItem originalItem, int position) {

            InputDialogFragment dialogFragment = InputDialogFragment
                    .newInstance(requireContext())
                    .setPrefilled(originalItem.getName(),
                            originalItem.getPrice(), originalItem.getQuantity());

            dialogFragment.setOnPositiveButtonTappedListener((name, price, quantity) -> {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                DocumentReference ref = db.collection("stock").document(user.getUid());

                Map<String, Map<String, Map<String, Double>>> map = new HashMap<>();
                Map<String, Map<String, Double>> items = new HashMap<>();
                Map<String, Double> item = new HashMap<>();
                item.put("price", price);
                item.put("quantity", (double) quantity);
                items.put(name, item);
                if (!originalItem.getName().equals(name))
                    deleteItem(originalItem.getName());
                items.put(name, item);
                map.put("items", items);
                ref.set(map, SetOptions.merge());

                itemAdapter.remove(itemAdapter.getAdapterPosition(originalItem));
                itemAdapter.add(InventoryItem.getItems(items, requireContext()));
                setViewVisibility(2, null, true);
                setStatus();
            });
            dialogFragment.show(getFragmentManager(), null);

            return false;
        }
    }
}
