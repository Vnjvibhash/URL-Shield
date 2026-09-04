/*
 *
 *   Created by VnjVibhash on 2/21/24, 10:32 AM
 *   Copyright Ⓒ 2026. All rights reserved Ⓒ 2026 http://vivekajee.in/
 *   Last modified: 04/09/26, 9:47 am
 *
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 *   except in compliance with the License. You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENS... Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 *    either express or implied. See the License for the specific language governing permissions and
 *    limitations under the License.
 * /
 */

package com.asvk.urlshield.modules.companions;

import com.asvk.urlshield.utilities.methods.StreamUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Unshorten logic extracted and supporting API Token authentication.
 */
public class UnshortenUtility {

    public static class UnshortenResult {
        private final boolean success;
        private final String error;
        private final String finalUrl;
        private final int remainingCalls;
        private final int usageLimit;

        public UnshortenResult(boolean success, String error, String finalUrl, int remainingCalls, int usageLimit) {
            this.success = success;
            this.error = error;
            this.finalUrl = finalUrl;
            this.remainingCalls = remainingCalls;
            this.usageLimit = usageLimit;
        }

        public boolean success() { return success; }
        public String error() { return error; }
        public String finalUrl() { return finalUrl; }
        public int remainingCalls() { return remainingCalls; }
        public int usageLimit() { return usageLimit; }
    }

    /** Calls the unshorten api to unshorten [url]. Can be authenticated ([token]!=null) or not */
    public static UnshortenResult unshort(String url, String token) throws IOException, JSONException {
        boolean hasToken = token != null && !token.trim().isEmpty();
        String encodedUrl = URLEncoder.encode(url, "UTF-8");

        var responseString = hasToken
                ? StreamUtils.readFromUrl("https://unshorten.me/api/v2/unshorten?url=" + encodedUrl, Map.of(
                        "Content-Type", "application/json",
                        "Authorization", "Token " + token.trim()))
                : StreamUtils.readFromUrl("https://unshorten.me/json/" + encodedUrl);

        var response = new JSONObject(responseString);

        var finalUrl = response.optString(hasToken ? "unshortened_url" : "resolved_url", url);
        var usageCount = Integer.parseInt(response.optString("usage_count", "0"));
        int usageLimit = 10; // documented but hardcoded
        int remainingCalls = usageLimit - usageCount;
        var error = response.optString("error", response.toString());
        var success = response.optBoolean("success", true);

        // remaining_calls is not documented, but if it's present use it and replace the hardcoded usage_limit
        try {
            remainingCalls = Integer.parseInt(response.optString("remaining_calls", ""));
            usageLimit = usageCount + remainingCalls;
        } catch (NumberFormatException ignore) {
            // not present, ignore
        }

        return new UnshortenResult(
                success,
                error,
                finalUrl,
                remainingCalls,
                usageLimit
        );
    }
}
