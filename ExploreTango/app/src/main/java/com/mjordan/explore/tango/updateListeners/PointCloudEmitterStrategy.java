package com.mjordan.explore.tango.updateListeners;

import com.google.atap.tangoservice.TangoPointCloudData;

import com.mjordan.explore.tango.PointCloudData;
import com.mjordan.explore.tango.viewModels.TangoViewModel;

import android.util.Log;

import rx.Emitter;

/**
 * Created by mjordan on 2/4/17.
 */

public class PointCloudEmitterStrategy extends TangoUpdateListenerBase {

    private final Emitter<PointCloudData> mEmitter;

    public PointCloudEmitterStrategy(Emitter<PointCloudData> emitter) {
        mEmitter = emitter;
    }

    @Override
    public void onPointCloudAvailable(TangoPointCloudData tangoPointCloudData) {
        float[] points = new float[tangoPointCloudData.numPoints];
        // Tango uses ByteBuffer to have fast native memory allocations. Want to read that buffer
        // into our own allocated array before passing off to logger
        tangoPointCloudData.points.get(points);
        Log.d(TangoViewModel.TAG, "emit point cloud data");
        mEmitter.onNext(PointCloudData.create(tangoPointCloudData.timestamp,
                                              tangoPointCloudData.numPoints,
                                              points));
    }
}
