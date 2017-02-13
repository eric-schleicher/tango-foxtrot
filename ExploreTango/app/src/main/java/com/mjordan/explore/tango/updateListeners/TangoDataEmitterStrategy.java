package com.mjordan.explore.tango.updateListeners;

import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;

import com.mjordan.explore.tango.PointCloudData;
import com.mjordan.explore.tango.PoseData;
import com.mjordan.explore.tango.TangoData;
import com.mjordan.explore.tango.viewModels.TangoViewModel;

import junit.framework.Assert;

import android.util.Log;

import java.nio.FloatBuffer;

import rx.Emitter;

/**
 * Created by mjordan on 2/4/17.
 */

public class TangoDataEmitterStrategy extends TangoUpdateListenerBase {

    private final Emitter<TangoData> mEmitter;

    public TangoDataEmitterStrategy(Emitter<TangoData> emitter) {
        mEmitter = emitter;
    }

    @Override
    public void onPoseAvailable(TangoPoseData tangoPoseData) {
        Log.d(TangoViewModel.TAG, "emit pose data"
            + "\naccuracy: " + tangoPoseData.accuracy
            + "\nconfidence: " + tangoPoseData.confidence);

        PoseData poseData = PoseData.create(tangoPoseData.timestamp,
                                            tangoPoseData.baseFrame,
                                            tangoPoseData.targetFrame,
                                            tangoPoseData.confidence,
                                            tangoPoseData.accuracy,
                                            tangoPoseData.rotation,
                                            tangoPoseData.translation);

        mEmitter.onNext(TangoData.createPose(poseData));
    }

    @Override
    public void onPointCloudAvailable(TangoPointCloudData tangoPointCloudData) {
        FloatBuffer floatBuffer = tangoPointCloudData.points;
        Assert.assertTrue("buffer not in read mode!", floatBuffer.isReadOnly());
        float[] points = new float[floatBuffer.limit()];
        // Tango uses ByteBuffer to have fast native memory allocations. Want to read that buffer
        // into our own allocated array before passing off to logger
        floatBuffer.get(points);
        Log.d(TangoViewModel.TAG, "emit point cloud data"
            + "\nnumber of points: " + tangoPointCloudData.numPoints
            + "\nflat floats buffer size: " + points.length);

        PointCloudData pointCloudData = PointCloudData.create(tangoPointCloudData.timestamp,
                                                              tangoPointCloudData.numPoints,
                                                              points);

        mEmitter.onNext(TangoData.createPointCloud(pointCloudData));
    }
}
