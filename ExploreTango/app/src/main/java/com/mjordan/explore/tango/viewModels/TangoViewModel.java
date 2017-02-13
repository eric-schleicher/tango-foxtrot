package com.mjordan.explore.tango.viewModels;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoConfig;
import com.google.atap.tangoservice.TangoCoordinateFramePair;
import com.google.atap.tangoservice.TangoErrorException;
import com.google.atap.tangoservice.TangoInvalidException;
import com.google.atap.tangoservice.TangoOutOfDateException;
import com.google.atap.tangoservice.TangoPoseData;

import com.mjordan.explore.tango.JsonFileWriter;
import com.mjordan.explore.tango.PointCloudData;
import com.mjordan.explore.tango.PoseData;
import com.mjordan.explore.tango.TangoData;
import com.mjordan.explore.tango.updateListeners.TangoDataEmitterStrategy;
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
import rx.functions.Cancellable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by mjordan on 2/3/17.
 */

public class TangoViewModel implements ITangoViewModel {

    public static final String TAG = TangoViewModel.class.toString();
    private final String mOutputPointCloudPath;
    private final String mOutputPosePath;
    private final JsonFileWriter mPoseFileWriter;

    private Tango mTango;

    private final JsonFileWriter mPointCloudWriter;
    private final ObservableField<String> mPointCount = new ObservableField<>();
    private final ObservableField<String> mAverageDepth = new ObservableField<>();
    private Subscription mPointCloudSubscription;
    private Subscription mPoseSubscription;

    public TangoViewModel(File pointCloudFile, File poseFile) {
        mOutputPointCloudPath = pointCloudFile.getAbsolutePath();
        mPointCloudWriter = new JsonFileWriter(pointCloudFile);

        mOutputPosePath = poseFile.getAbsolutePath();
        mPoseFileWriter = new JsonFileWriter(poseFile);
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
        if (mPointCloudSubscription != null) {
            mPointCloudSubscription.unsubscribe();
            mPoseSubscription.unsubscribe();
        }
        Log.d(TAG, "retreive file with:\nadb pull " + mOutputPointCloudPath + " tango_point_cloud_data.json"
        + "\nadb pull " + mOutputPosePath + " tango_pose_data.json");
    }

    /**
     * Set up the callback listeners for the Tango service and obtain other parameters required
     * after Tango connection.
     * Listen to updates from the Point Cloud and Tango Events and Pose.
     */
    private void startupTango(final Tango tango) {

        Observable<TangoData> tangoObservable =
            Observable.fromEmitter(new Action1<Emitter<TangoData>>() {

                                       @Override
                                       public void call(Emitter<TangoData>
                                           pointCloudDataEmitter) {
                                           ArrayList<TangoCoordinateFramePair> framePairs = new
                                               ArrayList<>();

                                           framePairs.add(new TangoCoordinateFramePair(TangoPoseData
                                                                                           .COORDINATE_FRAME_START_OF_SERVICE,
                                                                                       TangoPoseData.COORDINATE_FRAME_DEVICE));

                                           tango.connectListener(framePairs,
                                                                 new TangoDataEmitterStrategy
                                                                     (pointCloudDataEmitter));

                                           pointCloudDataEmitter.setCancellation(new Cancellable() {
                                               @Override
                                               public void cancel() throws Exception {
                                                   Log.d(TAG, "Disconnecting Tango!");
                                                   mTango.disconnect();
                                               }
                                           });
                                       }
                                   },
                                   Emitter.BackpressureMode.BUFFER);

        Observable<TangoData> hotTangoObservable = tangoObservable.share();
        mPointCloudSubscription =
            hotTangoObservable
                .filter(new Func1<TangoData, Boolean>() {
                    @Override
                    public Boolean call(TangoData tangoData) {
                        return tangoData.dataType() == TangoData.TYPE_POINTCLOUD;
                    }
                })
                .map(new Func1<TangoData, PointCloudData>() {
                    @Override
                    public PointCloudData call(TangoData tangoData) {
                        return tangoData.pointCloudData();
                    }
                })
                .buffer(20)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .subscribe(new Subscriber<List<PointCloudData>>() {
                    @Override
                    public void onCompleted() {
                        // do nothing
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "error while observing PointCloudData", e);
                    }

                    @Override
                    public void onNext(List<PointCloudData> pointCloudDatas) {
                        Log.d(TAG, "dump point cloud records to json. count: " + pointCloudDatas.size());
                        mPointCloudWriter.writeToFile(pointCloudDatas);
                    }
                });

        mPoseSubscription = hotTangoObservable
            .filter(new Func1<TangoData, Boolean>() {
                @Override
                public Boolean call(TangoData tangoData) {
                    return tangoData.dataType() == TangoData.TYPE_POSE;
                }
            })
            .map(new Func1<TangoData, PoseData>() {
                @Override
                public PoseData call(TangoData tangoData) {
                    return tangoData.poseData();
                }
            })
            .buffer(20)
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.io())
            .subscribe(new Subscriber<List<PoseData>>() {
                @Override
                public void onCompleted() {
                    // nothing
                }

                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "error while observing PoseData", e);
                }

                @Override
                public void onNext(List<PoseData> poseDatas) {
                    Log.d(TAG, "dump pose records to json. count: " + poseDatas.size());
                    mPoseFileWriter.writeToFile(poseDatas);
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
