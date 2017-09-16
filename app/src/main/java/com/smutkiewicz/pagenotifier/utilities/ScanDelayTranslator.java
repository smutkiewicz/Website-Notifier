package com.smutkiewicz.pagenotifier.utilities;

import android.content.Context;

import com.smutkiewicz.pagenotifier.R;

import java.util.HashMap;
import java.util.Map;

public class ScanDelayTranslator {
    private int[] stepsValuesInMilliseconds;
    private String[] stepsNameArray;
    private Map<Integer, String> stepAndNameMap;

    public ScanDelayTranslator(Context context) {
        getArraysFromResources(context);
        initStepAndNameMap();
    }

    public String putStepAndReturnItsName(int step) {
        return stepAndNameMap.get(step);
    }

    public int putStepAndReturnItsValueInMilliseconds(int step) {
        return stepsValuesInMilliseconds[step];
    }

    private void getArraysFromResources(Context context) {
        stepsNameArray = context.getResources().getStringArray(R.array.delay_strings_array);
        stepsValuesInMilliseconds = context.getResources().getIntArray(R.array.delay_values_array);
    }

    private void initStepAndNameMap() {
        int numberOfStep = 0;
        stepAndNameMap = new HashMap<>();

        for(String s : stepsNameArray) {
            stepAndNameMap.put(numberOfStep, s);
            numberOfStep++;
        }
    }
}
