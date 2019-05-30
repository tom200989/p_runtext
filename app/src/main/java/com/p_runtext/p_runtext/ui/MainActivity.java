package com.p_runtext.p_runtext.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.p_runtext.p_runtext.R;
import com.martext.martext.core.RootMaMarText;

public class MainActivity extends AppCompatActivity {

    private RootMaMarText rootMaMarText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        rootMaMarText = findViewById(R.id.tv_marquee);
    }
}
