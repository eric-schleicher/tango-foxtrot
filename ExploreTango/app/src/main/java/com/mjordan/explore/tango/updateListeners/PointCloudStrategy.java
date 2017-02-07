package com.mjordan.explore.tango.updateListeners;

import com.google.atap.tangoservice.TangoPointCloudData;

import android.databinding.ObservableField;

import java.nio.FloatBuffer;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 * This should just directly mimic the behavior of the tango sample project
 * Created by mjordan on 2/4/17.
 */
public class PointCloudStrategy extends TangoUpdateListenerBase {

    private static final String FORMAT_POINT_DISPLAY = "Point Count: %d";
    private static final String FORMAT_DEPTH_DISPLAY = "Average Depth: %s";
    private static final DecimalFormat FORMAT_THREE_DECIMAL = new DecimalFormat("0.000");
    private static final double UPDATE_INTERVAL_MS = 100.0;
    private static final int SECS_TO_MILLISECS = 1000;

    public double mPointCloudPreviousTimeStamp;
    private double mPointCloudTimeToNextUpdate = UPDATE_INTERVAL_MS;

    private final Object mLock;
    private final ObservableField<String> mPointCount;
    private final ObservableField<String> mAverageDepth;


    public PointCloudStrategy(Object lock, ObservableField<String> pointCount, ObservableField<String> averageDepth) {
        mLock = lock;
        mPointCount = pointCount;
        mAverageDepth = averageDepth;
    }

    @Override
    public void onPointCloudAvailable(TangoPointCloudData pointCloud) {

        // don't think this is needed w/o openGL, just maintaining sample app logic
        synchronized (mLock) {
            final double currentTimeStamp = pointCloud.timestamp;
            final double pointCloudFrameDelta =
                (currentTimeStamp - mPointCloudPreviousTimeStamp) * SECS_TO_MILLISECS;
            mPointCloudPreviousTimeStamp = currentTimeStamp;
            final double averageDepth = getAveragedDepth(pointCloud.points,
                                                         pointCloud.numPoints);

            mPointCloudTimeToNextUpdate -= pointCloudFrameDelta;

            if (mPointCloudTimeToNextUpdate < 0.0) {
                mPointCloudTimeToNextUpdate = UPDATE_INTERVAL_MS;

                String pointDisplayString = String.format(Locale.US,
                                                          FORMAT_POINT_DISPLAY,
                                                          pointCloud.numPoints);
                mPointCount.set(pointDisplayString);

                String depthDisplayString = String.format(Locale.US,
                                              FORMAT_DEPTH_DISPLAY,
                                              FORMAT_THREE_DECIMAL.format(averageDepth));
                mAverageDepth.set(depthDisplayString);
            }
        }
    }

    /**
     * Calculates the average depth from a point cloud buffer.
     *
     * @return Average depth.
     */
    private float getAveragedDepth(FloatBuffer pointCloudBuffer, int numPoints) {
        float totalZ = 0;
        float averageZ = 0;
        if (numPoints != 0) {
            int numFloats = 4 * numPoints;
            for (int i = 2; i < numFloats; i = i + 4) {
                totalZ = totalZ + pointCloudBuffer.get(i);
            }
            averageZ = totalZ / numPoints;
        }
        return averageZ;
    }
}
