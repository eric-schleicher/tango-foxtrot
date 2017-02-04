package com.mjordan.explore.tango.updateListeners;

import com.google.atap.tangoservice.Tango;
import com.google.atap.tangoservice.TangoEvent;
import com.google.atap.tangoservice.TangoPointCloudData;
import com.google.atap.tangoservice.TangoPoseData;
import com.google.atap.tangoservice.TangoXyzIjData;

/**
 * Created by mjordan on 2/4/17.
 */

public class TangoUpdateListenerBase implements Tango.OnTangoUpdateListener {

    @Override
    public void onPoseAvailable(TangoPoseData tangoPoseData) {
        // no op
    }

    @Override
    public void onXyzIjAvailable(TangoXyzIjData tangoXyzIjData) {
        // no op
    }

    @Override
    public void onFrameAvailable(int i) {
        // no op
    }

    @Override
    public void onTangoEvent(TangoEvent tangoEvent) {
        // no op
    }

    @Override
    public void onPointCloudAvailable(TangoPointCloudData tangoPointCloudData) {
        // no op
    }
}
