package com.mjordan.explore.tango;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoInvalidException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;

import com.mjordan.explore.tango.updateListeners.PointCloudStrategy;
import com.projecttango.tangosupport.TangoSupport;

import android.content.Context;
import android.databinding.ObservableField;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by mjordan on 2/3/17.
 */

public class TangoViewModel {

    public static final String TAG = TangoViewModel.class.toString();

    private Tango mTango;

    private final ObservableField<String> mPointCount = new ObservableField<>();
    private final ObservableField<String> mAverageDepth = new ObservableField<>();

    public ObservableField<String> getPointCount() {
        return mPointCount;
    }

    public ObservableField<String> getAverageDepth() {
        return mAverageDepth;
    }

    public void startObservingTango(Context context) {
        mTango = new Tango(context, new Runnable() {

            @Override
            public void run() {
                try {
                    TangoSupport.initialize();
                    mTango.connect(setupTangoConfig(mTango));
                    startupTango();
                }
                catch (TangoOutOfDateException e) {
                    Log.e(TAG, "Tango Service outdated!", e);
                }
                catch (TangoErrorException e) {
                    Log.e(TAG, "Tango Exception, try again!", e);
                }
                catch (TangoInvalidException e) {
                    Log.e(TAG, "Tango Invalid Exception, try again!", e);
                }
            }
        });
    }

    public void stopObservingTango() {
        // all these synchronized calls were in the tango example, but may not be needed when not using openGl
        synchronized (this) {
            try {
                mTango.disconnect();
            }
            catch (TangoErrorException e) {
                Log.e(TAG, "Tango Exception, try again!", e);
            }
        }
    }

    /**
     * Set up the callback listeners for the Tango service and obtain other parameters required
     * after Tango connection.
     * Listen to updates from the Point Cloud and Tango Events and Pose.
     */
    private void startupTango() {
        ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<>();

        framePairs.add(new TangoCoordinateFramePair(TangoPoseData.COORDINATE_FRAME_START_OF_SERVICE,
                                                    TangoPoseData.COORDINATE_FRAME_DEVICE));

        mTango.connectListener(framePairs,
                               new PointCloudStrategy(this,
                                                      mPointCount,
                                                      mAverageDepth));
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
