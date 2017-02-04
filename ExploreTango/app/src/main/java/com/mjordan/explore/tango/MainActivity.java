package com.mjordan.explore.tango;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private TangoViewModel mTangoViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTangoViewModel = new TangoViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTangoViewModel.startObservingTango(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTangoViewModel.stopObservingTango();
    }
}
