package com.example.inventory.utils;

/*
 * Copyright (C) 2019 Ritayan Chakraborty <ritayanout@gmail.com>
 *
 * This file is part of RapidBr
 *
 * RapidBr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * RapidBr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with RapidBr.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.inventory.R;
import com.google.android.material.textfield.TextInputEditText;

public class InputDialogFragment extends DialogFragment {

    private OnPositiveButtonTappedListener positiveListener;
    private OnNegativeButtonTappedListener negativeListener;
    private String title;
    private String message;
    private String positiveText;
    private String negativeText;
    private String prefilledName;
    private double prefilledPrice;
    private long prefilledQuantity;
    private boolean cancelable = true;
    private View content;

    public static InputDialogFragment newInstance(Context context) {
        View content = LayoutInflater.from(context).inflate(R.layout.dialog_new_item, null);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        content.setLayoutParams(params);
        InputDialogFragment newIDF = new InputDialogFragment();
        newIDF.content = content;
        return newIDF;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState != null)
            dismiss();

        TextInputEditText nameTv = content.findViewById(R.id.newName);
        TextInputEditText priceTv = content.findViewById(R.id.newPrice);
        TextInputEditText quantityTv = content.findViewById(R.id.newQuantity);

        if (prefilledName != null) {
            nameTv.setText(prefilledName);
            priceTv.setText(String.format("%.2f", prefilledPrice));
            quantityTv.setText(Long.toString(prefilledQuantity));
        }

        Log.i("InputDialogFragment", "onCreateDialog: " + content);
        if (positiveText == null)
            positiveText = getString(R.string.ok);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        if (title != null)
            builder.setTitle(title);
        builder.setMessage(message)
                .setPositiveButton(positiveText, (dialog, id) -> {
                    if (positiveListener != null) {
                        Editable nameE = nameTv.getText();
                        Editable priceE = priceTv.getText();
                        Editable quantityE = quantityTv.getText();
                        if (nameE != null && priceE != null && quantityE != null) {
                            String name = nameE.toString();
                            String price = priceE.toString();
                            String quantity = quantityE.toString();
                            if (!"".equals(name) && !"".equals(price) && !"".equals(quantity))
                                positiveListener.onButtonPressed(name.trim(),
                                        Double.parseDouble(price),
                                        Long.parseLong(quantity));
                        }
                    }
                });
        if (negativeText != null) {
            builder.setNegativeButton(negativeText, (dialog, id) -> {
                if (negativeListener != null)
                    negativeListener.onButtonPressed(dialog);
            });
        }
        builder.setCancelable(cancelable);

        builder.setView(content);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(cancelable);
        return dialog;
    }

    public InputDialogFragment setPrefilled(String name, double price, long quantity) {
        prefilledName = name;
        prefilledPrice = price;
        prefilledQuantity = quantity;
        return this;
    }

    public InputDialogFragment setOnPositiveButtonTappedListener(OnPositiveButtonTappedListener positiveListener) {
        this.positiveListener = positiveListener;
        return this;
    }

    public InputDialogFragment setOnNegativeButtonTappedListener(OnNegativeButtonTappedListener negativeListener) {
        this.negativeListener = negativeListener;
        return this;
    }

    public InputDialogFragment setMessage(String message) {
        this.message = message;
        return this;
    }

    public InputDialogFragment setTitle(String title) {
        this.title = title;
        return this;
    }

    public InputDialogFragment setPositiveText(String positiveText) {
        this.positiveText = positiveText;
        return this;
    }

    public InputDialogFragment setNegativeText(String negativeText) {
        this.negativeText = negativeText;
        return this;
    }

    public InputDialogFragment setDialogCancelable(boolean isCancelable) {
        this.cancelable = isCancelable;
        return this;
    }

    public interface OnPositiveButtonTappedListener {
        void onButtonPressed(String name, double price, long quantity);
    }

    public interface OnNegativeButtonTappedListener {
        void onButtonPressed(DialogInterface dialog);
    }
}
