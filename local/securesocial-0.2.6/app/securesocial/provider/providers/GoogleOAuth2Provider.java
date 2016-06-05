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
public class GoogleOAuth2Provider extends OAuth2Provider {
    private static final String ME_API = "https://www.googleapis.com/oauth2/v1/userinfo?access_token=%s";
    private static final String NAME = "name";
    private static final String PICTURE = "picture";
    private static final String EMAIL = "email";


    public GoogleOAuth2Provider() {
        super(ProviderType.googleoauth2);
    }


    @Override
    protected void fillProfile(SocialUser user, Map<String, Object> authContext) {
        JsonObject me = WS.url(ME_API, user.accessToken).get().getJson().getAsJsonObject();
        JsonObject error = me.getAsJsonObject(ERROR);

        if (error != null) {
            final String message = error.get(MESSAGE).getAsString();
            final String type = error.get(TYPE).getAsString();
            Logger.error("Error retrieving profile information from Facebook. Error type: %s, message: %s.", type, message);
            throw new AuthenticationException();
        }


        user.id.id = me.get(ID).getAsString();

        JsonElement displayName = me.get(NAME);
        user.displayName = displayName == null ? null : displayName.getAsString();

        JsonElement avatarUrl = me.get(PICTURE);
        user.avatarUrl = avatarUrl == null ? null : avatarUrl.getAsString();

        JsonElement email = me.get(EMAIL);
        user.email = email == null ? null : email.getAsString();
    }
}
