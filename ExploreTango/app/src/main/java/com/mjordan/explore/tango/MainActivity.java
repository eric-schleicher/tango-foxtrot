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

        File sessionPointCloudFile = new File(mOutputFileDir, "tango_point_cloud_data.json");
        if (!sessionPointCloudFile.mkdirs()) {
            Log.e(TAG, "directory not created");
        }

        // only keeping track of the most recent session
        if (sessionPointCloudFile.exists()) {
            sessionPointCloudFile.delete();
        }

        File sessionPoseFile = new File(mOutputFileDir, "tango_pose_data.json");
        if (sessionPoseFile.exists()) {
            sessionPoseFile.delete();
        }

        mTangoViewModel = createViewModel(sessionPointCloudFile,
                                          sessionPoseFile);
        binding.setModel(mTangoViewModel);
        binding.setOutputFile(sessionPointCloudFile.getAbsolutePath());
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

    private ITangoViewModel createViewModel(File pointCloudFile, File poseFile) {
        if (BuildConfig.MOCK) {
            return new MockTangoViewModel(pointCloudFile);
        }
        return new TangoViewModel(pointCloudFile,
                                  poseFile);
    }
}
