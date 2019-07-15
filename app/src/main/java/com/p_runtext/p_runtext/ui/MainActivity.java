package com.p_runtext.p_runtext.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import com.p_runtext.p_runtext.R;
import com.p_runtext.p_runtext.core.RootMaMarText;

public class MainActivity extends Activity {

    private RootMaMarText tvMar;
    private RootMaMarText tvMar1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        tvMar = findViewById(R.id.tvmar);
        tvMar1 = findViewById(R.id.tvmar1);
        // tvMar1.setMartext("我我我我我我我我我我我我我我我我我我我我我我我我");
    }
}
