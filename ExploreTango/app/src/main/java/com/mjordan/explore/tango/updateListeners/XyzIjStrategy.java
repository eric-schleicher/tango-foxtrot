package com.mjordan.explore.tango.updateListeners;

import com.google.atap.tangoservice.TangoXyzIjData;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by mjordan on 2/4/17.
 */

public class XyzIjStrategy extends TangoUpdateListenerBase {

    public static final String TAG = XyzIjStrategy.class.toString();

    @Override
    public void onXyzIjAvailable(TangoXyzIjData xyzIj) {
        byte[] buffer = new byte[xyzIj.xyzCount * 3 * 4];
        FileInputStream fileStream = new FileInputStream(
            xyzIj.xyzParcelFileDescriptor.getFileDescriptor());
        try {
            fileStream.read(buffer,
                            xyzIj.xyzParcelFileDescriptorOffset, buffer.length);
            fileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Do not process the buffer inside the callback because
        // you will not receive any new data while it processes
    }
}
