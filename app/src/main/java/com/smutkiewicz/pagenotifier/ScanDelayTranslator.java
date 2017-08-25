package com.smutkiewicz.pagenotifier;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class ScanDelayTranslator {
    /*5 min = 300 000 ms
    15 min = 900 000 ms
    30 min = 1 800 000 ms
    60 min = 3 600 000 ms
    6 h = 21 600 000 ms
    24 h = 86 400 000 ms
    7 dni = 604 800 000 ms
    Nigdy*/

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
