package com.hippo.nimingban.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.hippo.nimingban.NMBApplication;
import com.hippo.nimingban.R;
import com.hippo.nimingban.client.ac.ACUrl;
import com.hippo.nimingban.network.SimpleCookieStore;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONArray;

import java.net.HttpCookie;
import java.net.URL;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private IWXAPI api = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String appId = "wxe59db8095c5f16de";
        api = WXAPIFactory.createWXAPI(this, appId, false);

        Intent intent = getIntent();
        if (intent != null) {
            api.handleIntent(intent, this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            api.handleIntent(intent, this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (api != null) {
            api.detach();
            api = null;
        }
    }

    @Override
    public void onReq(BaseReq baseReq) { }

    @Override
    public void onResp(BaseResp baseResp) {
        if (baseResp.getType() == ConstantsAPI.COMMAND_LAUNCH_WX_MINIPROGRAM) {
            WXLaunchMiniProgram.Resp launchMiniProResp = (WXLaunchMiniProgram.Resp) baseResp;
            String extMsg =launchMiniProResp.extMsg;

            int messageId = processCookie(extMsg);
            Toast.makeText(this, messageId, Toast.LENGTH_LONG).show();

            finish();
            ((NMBApplication) getApplication()).bringActivitiesToForeground();
        }
    }

    private int processCookie(String extMsg) {
        try {
            JSONArray json = new JSONArray(extMsg);

            if (json.length() == 0) {
                return R.string.wx_add_cookie_empty;
            }

            HttpCookie cookie = new HttpCookie("userhash", json.getString(0));
            cookie.setDomain(ACUrl.DOMAIN);
            cookie.setPath("/");
            cookie.setMaxAge(-1);

            SimpleCookieStore store = NMBApplication.getSimpleCookieStore(this);
            store.add(new URL(ACUrl.getHost()), cookie);

            return json.length() > 1 ? R.string.wx_add_cookie_succeed_multi : R.string.wx_add_cookie_succeed;
        } catch (Exception e) {
            return R.string.wx_add_cookie_invalid;
        }
    }
}
