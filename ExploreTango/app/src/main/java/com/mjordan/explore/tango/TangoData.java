package com.mjordan.explore.tango;

import com.google.auto.value.AutoValue;

import android.support.annotation.Nullable;

/**
 * Created by mjordan on 2/12/17.
 */

@AutoValue
public abstract class TangoData {

    public static int TYPE_POSE = 1;
    public static int TYPE_POINTCLOUD = 2;

    public static TangoData createPose(PoseData data) {
        return new AutoValue_TangoData(TYPE_POSE, data, null);
    }

    public static TangoData createPointCloud(PointCloudData data) {
        return new AutoValue_TangoData(TYPE_POINTCLOUD, null, data);
    }

    public abstract int dataType();
    @Nullable
    public abstract PoseData poseData();

    @Nullable
    public abstract PointCloudData pointCloudData();
}
