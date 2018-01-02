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

import android.graphics.PointF;
import android.os.Bundle;
import android.widget.Toast;
import com.dlazaro66.qrcodereaderview.QRCodeReaderView;
import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.ac.ACUrl;
import com.hippo.nimingban.network.SimpleCookieStore;
import com.hippo.nimingban.util.Settings;
import java.net.HttpCookie;
import java.net.URL;
import org.json.JSONObject;

public class QRCodeScanActivity extends TranslucentActivity implements QRCodeReaderView.OnQRCodeReadListener {

  private QRCodeReaderView qrCodeReaderView;

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
  public void onQRCodeRead(String text, PointF[] points) {
    if (isFinishing()) {
      return;
    }

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
