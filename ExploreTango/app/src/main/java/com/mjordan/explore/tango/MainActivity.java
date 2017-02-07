package com.mjordan.explore.tango;

import com.mjordan.explore.tango.databinding.AMainBinding;
import com.mjordan.explore.tango.viewModels.ITangoViewModel;
import com.mjordan.explore.tango.viewModels.MockTangoViewModel;
import com.mjordan.explore.tango.viewModels.TangoViewModel;

import android.databinding.DataBindingUtil;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.toString();

    private final File mOutputFileDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
    private ITangoViewModel mTangoViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AMainBinding binding =  DataBindingUtil.setContentView(this, R.layout.a_main);

        File sessionTangoFile = new File(mOutputFileDir, "tango_data.json");
        if (!sessionTangoFile.mkdirs()) {
            Log.e(TAG, "directory not created");
        }
        // only keeping track of the most recent session
        if (sessionTangoFile.exists()) {
            sessionTangoFile.delete();
        }

        mTangoViewModel = createViewModel(sessionTangoFile);
        binding.setModel(mTangoViewModel);
        binding.setOutputFile(sessionTangoFile.getAbsolutePath());
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

    private ITangoViewModel createViewModel(File outputFile) {
        if (BuildConfig.MOCK) {
            return new MockTangoViewModel(outputFile);
        }
        return new TangoViewModel(outputFile);
    }
}
