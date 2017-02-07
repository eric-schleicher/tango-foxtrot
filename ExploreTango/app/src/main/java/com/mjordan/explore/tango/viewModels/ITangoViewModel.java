package com.mjordan.explore.tango.viewModels;

import android.content.Context;
import android.databinding.ObservableField;

/**
 * Created by mjordan on 2/5/17.
 */
public interface ITangoViewModel {

    ObservableField<String> getPointCount();

    ObservableField<String> getAverageDepth();

    void startObservingTango(Context context);

    void stopObservingTango();
}
