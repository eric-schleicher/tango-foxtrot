package com.mjordan.explore.tango;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoInvalidException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;

import com.projecttango.tangosupport.TangoSupport;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by mjordan on 2/3/17.
 */

public class TangoViewModel {

    public static final String TAG = TangoViewModel.class.toString();

    private Tango mTango;
    private boolean mIsConnected;

    public void startObservingTango(Context context) {
        mTango = new Tango(context, new Runnable() {
            private TangoConfig mConfig;

            @Override
            public void run() {
                try {
                    TangoSupport.initialize();
                    mConfig = setupTangoConfig(mTango);
                    mTango.connect(mConfig);
                    startupTango();
                    mIsConnected = true;
                } catch (TangoOutOfDateException e) {
                    Log.e(TAG, "Tango Service outdated!", e);
                } catch (TangoErrorException e) {
                    Log.e(TAG, "Tango Exception, try again!", e);
                } catch (TangoInvalidException e) {
                    Log.e(TAG, "Tango Invalid Exception, try again!", e);
                }
            }
        });
    }

    /**
     * Set up the callback listeners for the Tango service and obtain other parameters required
     * after Tango connection.
     * Listen to updates from the Point Cloud and Tango Events and Pose.
     */
    private void startupTango() {
        ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<TangoCoordinateFramePair>();

        framePairs.add(new TangoCoordinateFramePair(TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                                                    TangoPoseData.COORDINATE_FRAME_DEVICE));

        mTango.connectListener(framePairs, new Tango.OnTangoUpdateListener() {
            @Override
            public void onPoseAvailable(TangoPoseData pose) {
                // Passing in the pose data to UX library produce exceptions.
            }

            @Override
            public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
                // We are not using onXyzIjAvailable for this app.
            }

            @Override
            public void onPointCloudAvailable(TangoPointCloudData pointCloud) {
//                mPointCloudManager.updatePointCloud(pointCloud);
//
//                final double currentTimeStamp = pointCloud.timestamp;
//                final double pointCloudFrameDelta =
//                    (currentTimeStamp - mPointCloudPreviousTimeStamp) * SECS_TO_MILLISECS;
//                mPointCloudPreviousTimeStamp = currentTimeStamp;
//                final double averageDepth = getAveragedDepth(pointCloud.points,
//                                                             pointCloud.numPoints);
//
//                mPointCloudTimeToNextUpdate -= pointCloudFrameDelta;
//
//                if (mPointCloudTimeToNextUpdate < 0.0) {
//                    mPointCloudTimeToNextUpdate = UPDATE_INTERVAL_MS;
//                    final String pointCountString = Integer.toString(pointCloud.numPoints);
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mPointCountTextView.setText(pointCountString);
//                            mAverageZTextView.setText(FORMAT_THREE_DECIMAL.format(averageDepth));
//                        }
//                    });
//                }
            }

            @Override
            public void onTangoEvent(TangoEvent event) {
            }

            @Override
            public void onFrameAvailable(int cameraId) {
                // We are not using onFrameAvailable for this application.
            }
        });
    }

    /**
     * Sets up the tango configuration object. Make sure mTango object is initialized before
     * making this call.
     */
    private TangoConfig setupTangoConfig(Tango tango) {
        // Use the default configuration plus add depth sensing.
        TangoConfig config = tango.getConfig(TangoConfig.CONFIG_TYPE_DEFAULT);
        config.putBoolean(TangoConfig.KEY_BOOLEAN_DEPTH, true);
        config.putInt(TangoConfig.KEY_INT_DEPTH_MODE, TangoConfig.TANGO_DEPTH_MODE_POINT_CLOUD);
        return config;
    }
}
