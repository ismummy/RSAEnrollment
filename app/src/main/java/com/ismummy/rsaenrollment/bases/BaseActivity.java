package com.ismummy.rsaenrollment.bases;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ismummy.rsaenrollment.R;

import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/*
 * Base activity for all activities to setup common setting for all activity
 * like network fail
 *
 * */

@SuppressWarnings("ALL")
public abstract class BaseActivity extends AppCompatActivity {


    private volatile boolean isOn = false;
    private Snackbar snackbar;
    private View view;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        ButterKnife.bind(this);
        view = getView();
        if (view != null) {
            snackbar = Snackbar.make(view, "Check your internet connection.", Snackbar.LENGTH_INDEFINITE);
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.red));
            TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextColor(ContextCompat.getColor(this, R.color.white));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onPause() {
        super.onPause();
        isOn = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOn = true;
    }

    protected void toast(String message) {
        if (!isOn || isFinishing())
            return;

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    protected void snackBar(String message) {
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
        }
    }

    private View getView() {
        final ViewGroup vg = findViewById(android.R.id.content);
        View rv = null;

        if (vg != null)
            rv = vg.getChildAt(0);
        if (rv == null)
            rv = getWindow().getDecorView().getRootView();
        return rv;
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }
}
