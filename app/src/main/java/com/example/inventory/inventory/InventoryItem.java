package com.example.inventory.inventory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.example.inventory.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InventoryItem extends AbstractItem<InventoryItem, InventoryItem.ViewHolder> {

    private String name;
    private long quantity;
    private long price;
    @ColorInt
    private static int colourBlue;
    @ColorInt
    private static int colourEmpty;
    @ColorInt
    private static int colourNotEmpty;

    @NonNull
    @Override
    public ViewHolder getViewHolder(View v) {
        return new ViewHolder(v);
    }

    @Override
    public int getType() {
        return R.id.inventoryRoot;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_inventory;
    }

    public static List<InventoryItem> getItems(List<Map<String, Object>> items, Context context) {
        setColours(context);

        ArrayList<InventoryItem> inventoryItems = new ArrayList<>(items.size());
        for (Map<String, Object> item : items) {
            InventoryItem inventoryItem = new InventoryItem();
            inventoryItem.name = (String) item.get("name");

            inventoryItem.price = ((Long) item.get("price"));
            inventoryItem.quantity = (Long) item.get("quantity");

            inventoryItems.add(inventoryItem);
        }
        return inventoryItems;
    }

    private static void setColours(Context context) {
        Resources r= context.getResources();
        Resources.Theme t = context.getTheme();
        colourBlue = r.getColor(R.color.blue, t);
        colourEmpty = r.getColor(R.color.inventoryEmpty, t);
        colourNotEmpty = r.getColor(R.color.inventoryNotEmpty, t);
    }

    class ViewHolder extends FastAdapter.ViewHolder<InventoryItem> {

        private TextView name;
        private TextView price;
        private TextView quantity;
        private TextView value;
        private String currency;

        ViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.inventoryName);
            price = v.findViewById(R.id.inventoryPrice);
            quantity = v.findViewById(R.id.inventoryQuantity);
            value = v.findViewById(R.id.inventoryValue);
            currency = Currency.getInstance(Locale.getDefault()).getSymbol();
        }

        @SuppressLint("DefaultLocale")
        @Override
        public void bindView(InventoryItem item, List<Object> payloads) {
            name.setText(item.name);
            name.setSelected(true);
            quantity.setText(String.format("%d units", item.quantity));
            price.setText(String.format("%s %d/unit", currency, item.price));
            value.setText(String.format("%s %d", currency, item.price * item.quantity));

            if (item.quantity == 0 || item.price == 0)
                value.setTextColor(colourEmpty);
            else
                value.setTextColor(colourBlue);
            if (item.price == 0)
                price.setTextColor(colourEmpty);
            else
                price.setTextColor(colourNotEmpty);
        }

        @Override
        public void unbindView(InventoryItem item) {
            name.setText(null);
            quantity.setText(null);
            price.setText(null);
            value.setText(null);
        }
    }
}
