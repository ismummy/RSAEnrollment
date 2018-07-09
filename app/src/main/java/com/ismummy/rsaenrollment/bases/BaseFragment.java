package com.ismummy.rsaenrollment.bases;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ismummy.rsaenrollment.R;
import com.ismummy.rsaenrollment.listeners.FragmentLifeCycle;

import butterknife.ButterKnife;

/**
 * Created by root on 9/24/17.
 */

@SuppressWarnings("ALL")
public abstract class BaseFragment extends Fragment implements FragmentLifeCycle {

    private volatile boolean isOn = false;
    private Snackbar snackbar;
    private View view;

    @Override
    public void onPause() {
        super.onPause();
        isOn = false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        view = getMyView();
        if (view != null) {
            snackbar = Snackbar.make(view, "Check your internet connection.", Snackbar.LENGTH_INDEFINITE);
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.red));
            TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setGravity(View.TEXT_ALIGNMENT_CENTER);
            textView.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isOn = true;
    }

    protected void toast(String message) {

        if (!isOn || isDetached())
            return;

        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    protected void toastNetwork() {
        if (!isOn || isDetached())
            return;
        toast("Check your internet connection and Try again!!!");
    }

    protected void toastConnectionFailure() {
        if (!isOn || isDetached())
            return;
        toast("Error response from remote server. Please retry!!!");
    }


    @Override
    public void onResumeFragment() {

    }

    protected void snackBar(String message) {
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
        }
    }

    private View getMyView() {
        final ViewGroup vg = getActivity().findViewById(android.R.id.content);
        View rv = null;

        if (vg != null)
            rv = vg.getChildAt(0);
        if (rv == null)
            rv = getActivity().getWindow().getDecorView().getRootView();
        return rv;
    }
}
