package com.ismummy.rsaenrollment;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ismummy.rsaenrollment.bases.BaseActivity;

import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @OnClick(R.id.btn_next)
    void nextClicked(){
        startActivity(new Intent(this, CaturingActivity.class));
    }
}
