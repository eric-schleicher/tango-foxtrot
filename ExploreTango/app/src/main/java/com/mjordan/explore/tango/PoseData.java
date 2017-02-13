package com.mjordan.explore.tango;

import com.google.auto.value.AutoValue;

/**
 * Created by mjordan on 2/12/17.
 */

@AutoValue
public abstract class PoseData {

    public static PoseData create(double timestamp, int baseFrame, int targetFrame, int confidence, float accuracy,
        double[] rotation, double[] translation){
        return new AutoValue_PoseData(timestamp, baseFrame, targetFrame, confidence, accuracy, rotation, translation);
    }

    abstract double timestamp();
    abstract int baseFrame();
    abstract int targetFrame();
    abstract int confidence();
    abstract float accuracy();
    abstract double[] rotation();
    abstract double[] translation();
}
