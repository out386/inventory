package com.example.inventory.utils;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.example.inventory.R;

public class Toolbar extends RelativeLayout {
    ImageView image;

    public Toolbar(Context context) {
        this(context, null);
    }

    public Toolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Toolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Toolbar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.view_toolbar, this);
        image = findViewById(R.id.toolbarImage);
    }

    public void setImage(Uri uri) {
        Glide.with(getContext()).load(uri).into(image);
    }

    public void setImageListener(View.OnClickListener listener) {
        image.setOnClickListener(listener);
    }
}
