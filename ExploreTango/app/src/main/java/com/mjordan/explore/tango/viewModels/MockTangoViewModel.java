package com.mjordan.explore.tango.viewModels;

import com.mjordan.explore.tango.JsonFileWriter;
import com.mjordan.explore.tango.PointCloudData;

import android.content.Context;
import android.databinding.ObservableField;

import java.io.File;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by mjordan on 2/5/17.
 */

public class MockTangoViewModel implements ITangoViewModel {

    public static final String TAG = MockTangoViewModel.class.toString();

    private final ScheduledExecutorService mExecutorService = Executors.newScheduledThreadPool(1);

    private final ObservableField<String> mPointCloud = new ObservableField<>();
    private final ObservableField<String> mAverageDepth = new ObservableField<>();
    private final PublishSubject<PointCloudData> mPointCloudSubject;
    private final JsonFileWriter mFileWriter;
    private Subscription mSubscription;

    public MockTangoViewModel(File outputFile) {
        mFileWriter = new JsonFileWriter(outputFile);
        mPointCloud.set("Point Cloud: Mock");
        mAverageDepth.set("Average Depth: Mock");

        mPointCloudSubject = PublishSubject.create();
    }

    @Override
    public ObservableField<String> getPointCount() {
        return mPointCloud;
    }

    @Override
    public ObservableField<String> getAverageDepth() {
        return mAverageDepth;
    }

    @Override
    public void startObservingTango(Context context) {

        mSubscription = mPointCloudSubject
            // don't want consumer's file I/O to bottleneck the chain, so buffer data in
            // in groups in order to batch write to file.
            .buffer(10)
            .subscribeOn(Schedulers.computation())
            .observeOn(Schedulers.io())
            .subscribe(new Subscriber<List<PointCloudData>>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(List<PointCloudData> pointCloudDatas) {
                    mFileWriter.writeToFile(pointCloudDatas);
                }
            });

        mExecutorService.scheduleWithFixedDelay(mPublishingTask,
                                                0,
                                                100,
                                                TimeUnit.MILLISECONDS);
    }

    @Override
    public void stopObservingTango() {
        mSubscription.unsubscribe();
        mExecutorService.shutdownNow();
    }

    private final Runnable mPublishingTask = new Runnable() {
        @Override
        public void run() {
            float[] points = new float[]{1, 2, 3.5F, 5.5F, 6};
            mPointCloudSubject.onNext(PointCloudData.create(System.currentTimeMillis(),
                                                            5,
                                                            points));
        }
    };
}
