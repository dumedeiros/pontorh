/**
 * Copyright 2011 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package securesocial.provider.providers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import play.Logger;
import play.libs.WS;
import securesocial.provider.AuthenticationException;
import securesocial.provider.OAuth2Provider;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * A Google Provider implementing Oauth2. See https://developers.google.com/accounts/docs/OAuth2
 * In a single flow the user gets authenticated and a token that can be used to invoke
 * Google's APIs is retrieved.
 * see https://developers.google.com/accounts/docs/OAuth2WebServer for details.
 * Available configuration parameters are :
 * <ul>
 * <li>securesocial.googleoauth2.authorizationURL</li>
 * <li>securesocial.googleoauth2.accessTokenURL</li>
 * <li>securesocial.googleoauth2.clientid</li>
 * <li>securesocial.googleoauth2.secret</li>
 * <li>securesocial.googleoauth2.scope</li>
 * <li>securesocial.googleoauth2.access_type : online or offline</li>
 * </ul>
 */


public class LiveOAuth2Provider extends OAuth2Provider {
    private static final String ME_PICTURE = "https://apis.live.net/v5.0/";
    private static final String ME_API = "https://apis.live.net/v5.0/me?access_token=%s";
    private static final String NAME = "name";
    private static final String PICTURE = "picture";
    private static final String EMAILS = "emails";
    private static final String SPACE = " ";
    private static final String GENDER = "gender";
    private static final String ACCOUNT = "account";
    private static final String SLASH = "/";


    public LiveOAuth2Provider() {
        super(ProviderType.liveoauth2);

    }


    @Override
    protected void fillProfile(SocialUser user, Map<String, Object> authContext) {

        Logger.info("!-*-------------------------USER ACESS TOKEN " + user.accessToken);
        Logger.info("!-*-------------------------USER ACESS TOKEN " + user.accessToken);


        JsonObject me = WS.url(ME_API, user.accessToken).get().getJson().getAsJsonObject();
        JsonObject error = me.getAsJsonObject(ERROR);
        Logger.info("!-*-------------------------MEEEE " + me);

        if (error != null) {
            final String message = error.get(MESSAGE).getAsString();
            final String type = error.get(TYPE).getAsString();
            Logger.error("Error retrieving profile information from Yahoo. Error type: %s, message: %s.", type, message);
            throw new AuthenticationException();
        }

        String id = me.get(ID).getAsString();
        user.id.id = id;

        JsonElement name = me.get(NAME);
        user.displayName = name == null ? null : name.getAsString();

        JsonElement gender = me.get(GENDER);
        JsonObject email = me.get(EMAILS).getAsJsonObject();
        user.email = email == null ? null : email.get(ACCOUNT).getAsString();
        user.avatarUrl = getLivePicture(id);
    }

    private String getLivePicture(String id) {
        try {
            HttpURLConnection conn = (HttpURLConnection) (new URL(new StringBuilder(ME_PICTURE).append(id).append(SLASH).append(PICTURE).toString()).openConnection());
            conn.setInstanceFollowRedirects(false);
            conn.connect();
            return conn.getHeaderField("Location");
        } catch (IOException e) {
            throw new AuthenticationException();
        }
    }
}
