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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InventoryItem extends AbstractItem<InventoryItem, InventoryItem.ViewHolder> {

    private String name;
    private long quantity;
    private double price;
    @ColorInt
    private static int colourBlue;
    @ColorInt
    private static int colourEmpty;
    @ColorInt
    private static int colourNotEmpty;
    private static NumberFormat numberFormat;

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public long getQuantity() {
        return quantity;
    }

    public InventoryItem() {

    }

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

    @SuppressWarnings("ConstantConditions")
    public static List<InventoryItem> getItems(Map<String, Map<String, Double>> items, Context context) {
        setColours(context);

        ArrayList<InventoryItem> inventoryItems = new ArrayList<>(items.size());
        for (String key : items.keySet()) {
            InventoryItem inventoryItem = new InventoryItem();
            inventoryItem.name = key;
            inventoryItem.price = items.get(key).get("price");
            inventoryItem.quantity = (long) ((double) items.get(key).get("quantity"));

            inventoryItems.add(inventoryItem);
        }
        return inventoryItems;
    }

    private static void setColours(Context context) {
        Resources r = context.getResources();
        Resources.Theme t = context.getTheme();
        colourBlue = r.getColor(R.color.blue, t);
        colourEmpty = r.getColor(R.color.inventoryEmpty, t);
        colourNotEmpty = r.getColor(R.color.inventoryNotEmpty, t);
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMaximumFractionDigits(2);
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
            price.setText(String.format("%s %s/unit", currency,
                    numberFormat.format(item.price)));
            value.setText(String.format("%s %s", currency,
                    numberFormat.format(item.price * item.quantity)));

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
