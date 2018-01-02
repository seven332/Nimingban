/*
 * Copyright 2018 Hippo Seven
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

package com.hippo.nimingban.ui;

/*
 * Created by Hippo on 2018/1/1.
 */

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.hippo.app.ProgressDialogBuilder;
import com.hippo.io.UniFileInputStreamPipe;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.ac.ACUrl;
import com.hippo.nimingban.network.SimpleCookieStore;
import com.hippo.nimingban.util.BitmapUtils;
import com.hippo.nimingban.util.Settings;
import com.hippo.unifile.UniFile;
import java.net.HttpCookie;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;

public class QRCodeScanActivity extends TranslucentActivity implements QRCodeReaderView.OnQRCodeReadListener {

  private static final int REQUEST_CODE_PICK_IMAGE = 0;

  private QRCodeReaderView qrCodeReaderView;

  private AlertDialog progressDialog;

  @Override
  protected int getLightThemeResId() {
    return Settings.getColorStatusBar() ? R.style.NormalActivity : R.style.NormalActivity_NoStatus;
  }

  @Override
  protected int getDarkThemeResId() {
    return Settings.getColorStatusBar() ? R.style.NormalActivity_Dark : R.style.NormalActivity_Dark_NoStatus;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_qr_code_scan);

    qrCodeReaderView = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
    qrCodeReaderView.setOnQRCodeReadListener(this);
    qrCodeReaderView.setBackCamera();

    findViewById(R.id.choose_pic).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        try {
          startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        } catch (Exception e) {
          // Ignore
        }
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
    qrCodeReaderView.startCamera();
  }

  @Override
  protected void onPause() {
    super.onPause();
    qrCodeReaderView.stopCamera();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_PICK_IMAGE) {
      if (resultCode == RESULT_OK && data.getData() != null) {
        scanImage(data.getData());
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  @SuppressLint("StaticFieldLeak")
  private void scanImage(final Uri uri) {
    if (progressDialog != null) return;
    progressDialog = new ProgressDialogBuilder(this)
        .setTitle(R.string.please_wait)
        .setMessage(R.string.qr_scan_processing)
        .setCancelable(false)
        .show();

    new AsyncTask<Void, Void, String>() {
      @Override
      protected String doInBackground(Void... voids) {
        UniFile file = UniFile.fromUri(QRCodeScanActivity.this, uri);
        if (file == null) return null;

        try {
          // ZXing can't process large image
          Bitmap bitmap = BitmapUtils.decodeStream(new UniFileInputStreamPipe(file), 1024, 1024);
          if (bitmap == null) return null;

          int width = bitmap.getWidth();
          int height = bitmap.getHeight();
          int[] pixels = new int[width * height];
          bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
          bitmap.recycle();

          RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);
          final HybridBinarizer hybBin = new HybridBinarizer(source);
          final BinaryBitmap bBitmap = new BinaryBitmap(hybBin);

          Map<DecodeHintType, Boolean> hints = new HashMap<>();
          hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
          QRCodeReader reader = new QRCodeReader();
          Result result = reader.decode(bBitmap, hints);

          return result.getText();
        } catch (Exception e) {
          e.printStackTrace();
          return null;
        }
      }

      @Override
      protected void onPostExecute(String text) {
        if (progressDialog != null) {
          progressDialog.dismiss();
          progressDialog = null;
        }

        if (text != null) {
          processCookieText(text);
        } else {
          Toast.makeText(QRCodeScanActivity.this, R.string.qr_scan_invalid, Toast.LENGTH_SHORT).show();
        }
      }
    }.execute();
  }

  @Override
  public void onQRCodeRead(String text, PointF[] points) {
    if (!isFinishing()) {
      processCookieText(text);
    }
  }

  private void processCookieText(String text) {
    try {
      JSONObject jo = new JSONObject(text);
      String userhash = jo.getString("cookie");
      if (userhash == null) {
        throw new NullPointerException("cookie is null");
      }

      HttpCookie cookie = new HttpCookie("userhash", userhash);
      cookie.setDomain(ACUrl.DOMAIN);
      cookie.setPath("/");
      cookie.setMaxAge(-1);

      SimpleCookieStore store = NMBApplication.getSimpleCookieStore(this);
      store.add(new URL(ACUrl.HOST), cookie);

      Toast.makeText(this, R.string.qr_scan_succeed, Toast.LENGTH_SHORT).show();
      finish();
    } catch (Exception e) {
      e.printStackTrace();

      Toast.makeText(this, R.string.qr_scan_invalid, Toast.LENGTH_SHORT).show();
    }
  }
}
