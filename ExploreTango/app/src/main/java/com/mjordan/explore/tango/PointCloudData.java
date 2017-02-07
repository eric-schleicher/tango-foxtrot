package com.mjordan.explore.tango;

import com.google.auto.value.AutoValue;

/**
 * Created by mjordan on 2/4/17.
 */
@AutoValue
public abstract class PointCloudData {

    public static PointCloudData create(double timestamp, int numberOfPoints, float[] points){
        return new AutoValue_PointCloudData(timestamp, numberOfPoints, points);
    }

    abstract double timestamp();
    abstract int numberOfPoints();
    abstract float[] points();
}
