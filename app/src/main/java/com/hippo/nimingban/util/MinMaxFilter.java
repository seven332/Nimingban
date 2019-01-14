package com.hippo.nimingban.util;

import android.text.InputFilter;
import android.text.Spanned;

// https://stackoverflow.com/q/14212518
public class MinMaxFilter implements InputFilter {

    private int mMin;
    private int mMax;

    public MinMaxFilter(int min, int max) {
        mMin = min;
        mMax = max;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            // Remove the string out of destination that is to be replaced
            String newVal = dest.toString().substring(0, dstart) + dest.toString().substring(dend, dest.toString().length());
            // Add the new string in
            newVal = newVal.substring(0, dstart) + source.toString() + newVal.substring(dstart, newVal.length());
            int input = Integer.parseInt(newVal);
            if (isInRange(mMin, mMax, input))
                return null;
        } catch (NumberFormatException nfe) {}
        return "";
    }

    private boolean isInRange(int a, int b, int c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;
    }
}
