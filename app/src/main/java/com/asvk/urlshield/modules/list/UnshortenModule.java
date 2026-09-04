/*
 *
 *   Created by Amitskr & VnjVibhash on 2/21/24, 10:32 AM
 *   Copyright Ⓒ 2024. All rights reserved Ⓒ 2024 http://vivekajee.in/
 *   Last modified: 2/29/24, 1:59 PM
 *
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENS... Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *    either express or implied. See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package com.asvk.urlshield.modules.list;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.asvk.urlshield.R;
import com.asvk.urlshield.activities.ModulesActivity;
import com.asvk.urlshield.dialogs.MainDialog;
import com.asvk.urlshield.modules.AModuleConfig;
import com.asvk.urlshield.modules.AModuleData;
import com.asvk.urlshield.modules.AModuleDialog;
import com.asvk.urlshield.modules.companions.UnshortenUtility;
import com.asvk.urlshield.url.UrlData;
import com.asvk.urlshield.utilities.generics.GenericPref;
import com.asvk.urlshield.utilities.methods.AndroidUtils;

import org.json.JSONException;

import java.io.IOException;
import java.util.Objects;

/**
 * Module to Unshort links by using https://unshorten.me/
 */
public class UnshortenModule extends AModuleData {

    public static final String PREF = "unshorten_token";

    static GenericPref.Str TOKEN_PREF(Context cntx) {
        return new GenericPref.Str(PREF, "", cntx);
    }

    @Override
    public String getId() {
        return "unshorten";
    }

    @Override
    public int getName() {
        return R.string.mUnshort_name;
    }

    @Override
    public AModuleDialog getDialog(MainDialog cntx) {
        return new UnshortenDialog(cntx);
    }

    @Override
    public AModuleConfig getConfig(ModulesActivity cntx) {
        return new UnshortenConfig(cntx);
    }
}

class UnshortenConfig extends AModuleConfig {

    final GenericPref.Str token;

    public UnshortenConfig(ModulesActivity cntx) {
        super(cntx);
        this.token = UnshortenModule.TOKEN_PREF(cntx);
    }

    @Override
    public int getLayoutId() {
        return R.layout.config_unshorten;
    }

    @Override
    public void onInitialize(View views) {
        views.<TextView>findViewById(R.id.label).setText(R.string.mUnshort_desc);
        token.attachToEditText(views.findViewById(R.id.token));
    }
}

class UnshortenDialog extends AModuleDialog {

    private Button unshort;
    private TextView info;
    private final GenericPref.Str token;

    private Thread thread = null;

    public UnshortenDialog(MainDialog dialog) {
        super(dialog);
        token = UnshortenModule.TOKEN_PREF(dialog);
    }

    @Override
    public int getLayoutId() {
        return R.layout.button_text;
    }

    @Override
    public void onInitialize(View views) {
        unshort = views.findViewById(R.id.button);
        unshort.setText(R.string.mUnshort_unshort);
        unshort.setOnClickListener(v -> unshort());

        info = views.findViewById(R.id.text);
    }

    @Override
    public void onPrepareUrl(UrlData urlData) {
        // cancel previous check if pending
        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    @Override
    public void onDisplayUrl(UrlData urlData) {
        // reset all
        unshort.setEnabled(true);
        info.setText("");
        AndroidUtils.clearRoundedColor(info);
    }

    /**
     * Unshorts the current url
     */
    private void unshort() {
        // disable button and run in background
        unshort.setEnabled(false);
        info.setText(R.string.mUnshort_checking);
        AndroidUtils.clearRoundedColor(info);

        thread = new Thread(this::_check);
        thread.start();
    }

    private void _check() {
        try {
            UnshortenUtility.UnshortenResult result = UnshortenUtility.unshort(getUrl(), token.get());

            // exit if was canceled
            if (Thread.currentThread().isInterrupted()) {
                Log.d("THREAD", "Interrupted");
                return;
            }

            if (!result.success()) {
                // server error, maybe too many checks
                getActivity().runOnUiThread(() -> {
                    info.setText(getActivity().getString(R.string.mUnshort_error, result.error()));
                    AndroidUtils.setRoundedColor(R.color.warning, info);
                    // allow retries
                    unshort.setEnabled(true);
                });
            } else if (Objects.equals(result.finalUrl(), getUrl())) {
                // same, nothing to replace
                getActivity().runOnUiThread(() -> {
                    var pending = result.remainingCalls() <= result.usageLimit() / 2
                            ? " (" + getActivity().getString(R.string.mUnshort_pending, result.remainingCalls(), result.usageLimit()) + ")"
                            : "";
                    info.setText(getActivity().getString(R.string.mUnshort_notFound) + pending);
                    AndroidUtils.clearRoundedColor(info);
                });
            } else {
                // correct, replace
                getActivity().runOnUiThread(() -> {
                    setUrl(new UrlData(result.finalUrl()).dontTriggerOwn());

                    var pending = result.remainingCalls() <= result.usageLimit() / 2
                            ? " (" + getActivity().getString(R.string.mUnshort_pending, result.remainingCalls(), result.usageLimit()) + ")"
                            : "";
                    info.setText(getActivity().getString(R.string.mUnshort_ok) + pending);
                    AndroidUtils.setRoundedColor(R.color.good, info);
                    // a short url can redirect to another short url
                    unshort.setEnabled(true);
                });
            }

        } catch (IOException | JSONException e) {
            // internal error
            e.printStackTrace();

            // exit if was canceled
            if (Thread.currentThread().isInterrupted()) {
                Log.d("THREAD", "Interrupted");
                return;
            }

            getActivity().runOnUiThread(() -> {
                info.setText(getActivity().getString(R.string.mUnshort_internal, e.getMessage()));
                AndroidUtils.setRoundedColor(R.color.bad, info);
                unshort.setEnabled(true);
            });
        }
    }
}
