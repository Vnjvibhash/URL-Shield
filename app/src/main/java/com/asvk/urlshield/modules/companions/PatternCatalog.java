/*
 *
 *   Created by VnjVibhash on 2/21/24, 10:32 AM
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

package com.asvk.urlshield.modules.companions;

import android.app.Activity;
import android.content.Context;

import com.asvk.urlshield.R;
import com.asvk.urlshield.utilities.generics.JsonCatalog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents the catalog of the Pattern module
 */
public class PatternCatalog extends JsonCatalog {

    public PatternCatalog(Activity cntx) {
        super(cntx, "patterns", R.string.mPttrn_editor);
    }

    @Override
    public JSONObject buildBuiltIn(Context cntx) throws JSONException {
        return new JSONObject()

                // built from the translated strings
                .put(cntx.getString(R.string.mPttrn_ascii), new JSONObject()
                        .put("regex", "[^\\p{ASCII}]")
                )
                .put(cntx.getString(R.string.mPttrn_upperDomain), new JSONObject()
                        .put("regex", "^.*?://[^/?#]*[A-Z]")
                )
                .put("Rickroll", new JSONObject()
                        .put("regex", "youtu\\.?be.*dQw4w9WgXcQ")
                )
                .put(cntx.getString(R.string.mPttrn_http), new JSONObject()
                        .put("regex", "^http://")
                        .put("replacement", "https://")
                )
                .put(cntx.getString(R.string.mPttrn_noSchemeHttp), new JSONObject()
                        .put("regex", "^(?!.*:)")
                        .put("replacement", "http://$0")
                )
                .put(cntx.getString(R.string.mPttrn_noSchemeHttps), new JSONObject()
                        .put("regex", "^(?!.*:)")
                        .put("replacement", "https://$0")
                )
                .put(cntx.getString(R.string.mPttrn_wrongSchemaHttp), new JSONObject()
                        .put("regex", "^(?!http:)[hH][tT]{2}[pP]:(.*)")
                        .put("replacement", "http:$1")
                        .put("automatic", true)
                )
                .put(cntx.getString(R.string.mPttrn_wrongSchemaHttps), new JSONObject()
                        .put("regex", "^(?!https:)[hH][tT]{2}[pP][sS]:(.*)")
                        .put("replacement", "https:$1")
                        .put("automatic", true)
                )

                // privacy redirections samples (see https://github.com/vnjvibhash/URL-Shield/discussions/122)
                .put("Reddit ➔ Eddrit", new JSONObject()
                        .put("regex", "^https?://(?:[a-z0-9-]+\\.)*?reddit\\.com/(.*)")
                        .put("replacement", "https://eddrit.com/$1")
                        .put("enabled", false)
                )
                .put("Twitter/X ➔ Nitter", new JSONObject()
                        .put("regex", "^https?://(?:[a-z0-9-]+\\.)*?(?:twitter|x)\\.com/(.*)")
                        .put("replacement", "https://nitter.net/$1")
                        .put("enabled", false)
                )
                .put("Youtube ➔ Invidious", new JSONObject()
                        .put("regex", "^https?://(?:[a-z0-9-]+\\.)*?youtube\\.com/(.*)")
                        .put("replacement", new JSONArray()
                                .put("https://yewtu.be/$1")
                                .put("https://farside.link/invidious/$1")
                        )
                        .put("enabled", false)
                )
                .put("Farside.link", new JSONObject()
                        .put("replacement", new JSONArray()
                                .put("https://farside.link/$0")
                        )
                        .put("enabled", false)
                )
                .put("URL ➔ Songlink", new JSONObject()
                        .put("regex", "^https?://(?:www\\.)?(open\\.spotify\\.com|music\\.apple\\.com|(?:music\\.)?youtube\\.com/watch|youtu\\.be|soundcloud\\.com|tidal\\.com|deezer\\.com|[^/]*\\.bandcamp\\.com|music\\.amazon\\.com)")
                        .put("excludeRegex", "^https://song\\.link/")
                        .put("replacement", "https://song.link/$0")
                        .put("enabled", false)
                );
    }

}
