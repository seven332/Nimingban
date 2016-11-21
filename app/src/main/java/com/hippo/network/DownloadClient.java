/*
 * Copyright 2015 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hippo.network;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.hippo.io.UniFileOutputStreamPipe;
import com.hippo.unifile.UniFile;
import com.hippo.yorozuya.FileUtils;
import com.hippo.yorozuya.StringUtils;
import com.hippo.yorozuya.Utilities;
import com.hippo.yorozuya.io.OutputStreamPipe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DownloadClient {

    private static long transferData(InputStream in, OutputStream out, OnDownloadListener listener)
            throws Exception {
        final byte data[] = new byte[1024 * 4];
        long receivedSize = 0;

        while (true) {
            int bytesRead = in.read(data);
            if (bytesRead == -1) {
                break;
            }
            out.write(data, 0, bytesRead);
            receivedSize += bytesRead;
            if (listener != null) {
                listener.onDonwlad(receivedSize, bytesRead);
            }
        }

        out.flush();

        return receivedSize;
    }

    private static String getFilenameFromContentDisposition(String contentDisposition) {
        if (contentDisposition == null) {
            return null;
        }

        String[] pieces = StringUtils.split(contentDisposition, ';');
        for (String piece : pieces) {
            int index = piece.indexOf('=');
            if (index < 0) {
                continue;
            }
            String key = piece.substring(0, index).trim();
            String value = piece.substring(index + 1).trim();
            if ("filename".equals(key) && !TextUtils.isEmpty(value)) {
                return value;
            }
        }

        return null;
    }

    public static boolean execute(DownloadRequest request) {
        OnDownloadListener listener = request.mListener;
        OkHttpClient okHttpClient = request.mOkHttpClient;

        UniFile uniFile = null;
        OutputStreamPipe osPipe = null;
        try {
            Call call = okHttpClient.newCall(new Request.Builder().url(request.mUrl).build());
            request.mCall = call;

            // Listener
            if (listener != null) {
                listener.onStartDownloading();
            }

            Response response = call.execute();
            ResponseBody body = response.body();

            // Check response code
            int responseCode = response.code();
            if (responseCode >= 400) {
                throw new ResponseCodeException(responseCode);
            }

            osPipe = request.mOSPipe;
            if (osPipe == null) {
                String extension;
                String name;

                String dispositionFilename = getFilenameFromContentDisposition(response.header("Content-Disposition"));
                if (dispositionFilename != null) {
                    name = FileUtils.getNameFromFilename(dispositionFilename);
                    extension = FileUtils.getExtensionFromFilename(dispositionFilename);
                } else {
                    name = Utilities.getNameFromUrl(request.mUrl);
                    extension = Utilities.getExtensionFromMimeType(response.header("Content-Type"));
                    if (extension == null) {
                        extension = MimeTypeMap.getFileExtensionFromUrl(request.mUrl);
                    }
                }

                String filename;
                if (listener != null) {
                    filename = listener.onFixname(name, extension, request.mFilename);
                } else {
                    filename = request.mFilename;
                }
                request.mFilename = filename;

                // Use Temp filename
                uniFile = request.mDir.createFile(FileUtils.sanitizeFilename(filename + ".download"));
                if (uniFile == null) {
                    // Listener
                    if (listener != null) {
                        listener.onFailed(new IOException("Can't create file " + filename));
                    }
                    return false;
                }
                osPipe = new UniFileOutputStreamPipe(uniFile);
            }
            osPipe.obtain();

            long contentLength = body.contentLength();

            // Listener
            if (listener != null) {
                listener.onConnect(contentLength);
            }

            long receivedSize = transferData(body.byteStream(), osPipe.open(), listener);

            if (contentLength > 0 && contentLength != receivedSize) {
                throw new IOException("contentLength is " + contentLength + ", but receivedSize is " + receivedSize);
            }

            // Rename
            if (uniFile != null && request.mFilename != null) {
                uniFile.renameTo(request.mFilename);
            }

            // Listener
            if (listener != null) {
                listener.onSucceed();
            }
            return true;
        } catch (Exception e) {
            // remove download failed file
            if (uniFile != null) {
                uniFile.delete();
            }

            if (listener != null) {
                listener.onFailed(e);
            }
            return false;
        } finally {
            if (osPipe != null) {
                osPipe.close();
                osPipe.release();
            }
        }
    }

    public static class SimpleDownloadListener implements OnDownloadListener {

        @Override
        public void onStartDownloading() {
        }

        @Override
        public String onFixname(String newFilename, String newExtension, String oldFilename) {
            return oldFilename;
        }

        @Override
        public void onConnect(long totalSize) {
        }

        @Override
        public void onDonwlad(long receivedSize, long singleReceivedSize) {
        }

        @Override
        public void onFailed(Exception e) {
        }

        @Override
        public void onSucceed() {
        }
    }

    public interface OnDownloadListener {

        void onStartDownloading();

        String onFixname(String fileFirstname, String extension, String oldFilename);

        void onConnect(long totalSize);

        void onDonwlad(long receivedSize, long singleReceivedSize);

        void onFailed(Exception e);

        void onSucceed();
    }
}
