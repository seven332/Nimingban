package com.hippo.gif;

import java.io.InputStream;
import java.io.OutputStream;

public class GifDownloadSize {

    public static boolean compress(InputStream is, OutputStream os, int sampleSize) {
        return nativeCompress(is, os, sampleSize);
    }

    static {
        System.loadLibrary("gif_downsize");
    }

    private static native boolean nativeCompress(InputStream is, OutputStream os, int sampleSize);
}
