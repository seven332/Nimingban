package com.hippo.gif;

import java.io.InputStream;
import java.io.OutputStream;

public class GifDownloadSize {

    public static boolean compress(String inPath, String outPath, int sampleSize) {
        return nativeCompress(inPath, outPath, sampleSize);
    }

    public static boolean compress(InputStream is, OutputStream os, int sampleSize) {
        return nativeCompressCustom(is, os, sampleSize);
    }

    static {
        System.loadLibrary("gif_downsize");
    }

    private static native boolean nativeCompress(String inPath, String outPath, int sampleSize);
    private static native boolean nativeCompressCustom(InputStream is, OutputStream os, int sampleSize);
}
