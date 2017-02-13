package com.mjordan.explore.tango;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by mjordan on 2/4/17.
 */

public class JsonFileWriter {

    public static final String TAG = JsonFileWriter.class.toString();

    private final File mOutputFile;

    public JsonFileWriter(File outputFile) {
        mOutputFile = outputFile;
    }

    public void writeToFile(List<?> pointCloudDatas) {
        try {
            Writer writer = new FileWriter(mOutputFile);
            Gson gson = new GsonBuilder().create();
            Type typeOfSrc = new TypeToken<List<PointCloudData>>() {
            }.getType();
            gson.toJson(pointCloudDatas,
                        typeOfSrc,
                        writer);
            writer.close();
        }
        catch (IOException e) {
            Log.e(TAG, "couldn't write to file", e);
        }
    }
}
