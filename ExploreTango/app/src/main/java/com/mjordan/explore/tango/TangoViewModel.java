package com.mjordan.explore.tango;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoInvalidException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;

import com.mjordan.explore.tango.updateListeners.PointCloudEmitterStrategy;
import com.projecttango.tangosupport.TangoSupport;

import android.content.Context;
import android.databinding.ObservableField;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import rx.Emitter;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by mjordan on 2/3/17.
 */

public class TangoViewModel implements ITangoViewModel {

    public static final String TAG = TangoViewModel.class.toString();

    private Tango mTango;

    private final JsonFileWriter mFileWriter;
    private final ObservableField<String> mPointCount = new ObservableField<>();
    private final ObservableField<String> mAverageDepth = new ObservableField<>();
    private Subscription mFileWritingSubscription;

    public TangoViewModel(File outputFile) {
        mFileWriter = new JsonFileWriter(outputFile);
    }

    @Override
    public ObservableField<String> getPointCount() {
        return mPointCount;
    }

    @Override
    public ObservableField<String> getAverageDepth() {
        return mAverageDepth;
    }

    @Override
    public void startObservingTango(Context context) {
        mTango = new Tango(context, new Runnable() {

            @Override
            public void run() {
                try {
                    TangoSupport.initialize();
                    mTango.connect(setupTangoConfig(mTango));
                    startupTango(mTango);
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

    @Override
    public void stopObservingTango() {
        // all these synchronized calls were in the tango example, but may not be needed when not using openGl
        synchronized (this) {
            try {
                mTango.disconnect();
                if (mFileWritingSubscription != null) {
                    mFileWritingSubscription.unsubscribe();
                }
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
    private void startupTango(final Tango tango) {

        mFileWritingSubscription =
            Observable.fromEmitter(new Action1<Emitter<PointCloudData>>() {

                                       @Override
                                       public void call(Emitter<PointCloudData> pointCloudDataEmitter) {
                                           ArrayList<TangoCoordinateFramePair> framePairs = new ArrayList<>();

                                           framePairs.add(new TangoCoordinateFramePair(TangoPoseData
                                                                                           .COORDINATE_FRAME_START_OF_SERVICE,
                                                                                       TangoPoseData.COORDINATE_FRAME_DEVICE));

                                           // Currently not using
//                                       PointCloudStrategy fromTangoExamples = new PointCloudStrategy(this,
//                                                                                                     mPointCount,
//                                                                                                     mAverageDepth);

                                           tango.connectListener(framePairs,
                                                                 new PointCloudEmitterStrategy(pointCloudDataEmitter));
                                       }
                                   },
                                   Emitter.BackpressureMode.BUFFER)
                      .buffer(10)
                      .subscribeOn(Schedulers.computation())
                      .observeOn(Schedulers.io())
                      .subscribe(new Subscriber<List<PointCloudData>>() {
                          @Override
                          public void onCompleted() {
                            // do nothing
                          }

                          @Override
                          public void onError(Throwable e) {
                              Log.e(TAG, "error while observing Tango", e);
                          }

                          @Override
                          public void onNext(List<PointCloudData> pointCloudDatas) {
                            mFileWriter.writeToFile(pointCloudDatas);
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
