package com.p_runtext.p_runtext.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.p_runtext.p_runtext.R;
import com.p_runtext.p_runtext.core.RootMaMarLayout;

public class Main2Activity extends Activity {

    private RootMaMarLayout rootMaMarText2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
    }

    public void clickme2(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
