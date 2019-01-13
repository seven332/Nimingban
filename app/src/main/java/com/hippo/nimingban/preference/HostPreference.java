/*
 * Copyright 2019 Hippo Seven
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

package com.hippo.nimingban.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.hippo.nimingban.R;
import com.hippo.nimingban.util.Settings;
import com.hippo.preference.DialogPreference;

public class HostPreference extends DialogPreference {

  public HostPreference(Context context) {
    super(context);
  }

  public HostPreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public HostPreference(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @NonNull
  @Override
  protected AlertDialog onCreateDialog(Context context) {
    return new AlertDialog.Builder(context)
        .setTitle(R.string.main_customized_ac_host)
        .setPositiveButton(android.R.string.ok, null)
        .setView(R.layout.dialog_host)
        .create();
  }

  @Override
  protected void onDialogCreated(final AlertDialog dialog) {
    final EditText host = dialog.findViewById(R.id.host);
    host.setText(Settings.getCustomizedAcHost());

    Button button = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String url = host.getText().toString();

        if (url.startsWith("http://") || url.startsWith("https://")) {
          // Remove ending '/'
          while (url.charAt(url.length() - 1) == '/') {
            url = url.substring(0, url.length() - 1);
          }

          if (url.charAt(4) == 's' ? url.length() > 8 : url.length() > 7) {
            Settings.putCustomizedAcHost(url);
            dialog.dismiss();
            return;
          }
        }

        Toast.makeText(dialog.getContext(), R.string.main_customized_ac_host_invalid, Toast.LENGTH_SHORT).show();
      }
    });
  }
}
