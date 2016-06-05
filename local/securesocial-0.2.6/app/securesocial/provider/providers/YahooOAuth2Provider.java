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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import play.Logger;
import play.libs.WS;
import securesocial.provider.AuthenticationException;
import securesocial.provider.OAuth2Provider;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;

import java.util.Map;

public class YahooOAuth2Provider extends OAuth2Provider {
    private static final String ME_API = "https://social.yahooapis.com/v1/user/me/profile?format=json";

    private static final String GUID = "guid";
    private static final String NAME = "name";
    private static final String PICTURE = "picture";
    private static final String EMAIL = "email";
    private static final String PROFILE = "profile";
    private static final String SPACE = " ";

    public YahooOAuth2Provider() {
        super(ProviderType.yahoooauth2);
    }

    @Override
    protected void fillProfile(SocialUser user, Map<String, Object> authContext) {
        Logger.info("!-*-------------------------USER ACESS TOKEN " + user.accessToken);

        //OBS: Eduardo Medeiros quem fez =D - Implementacao diferente da do googleOauth2provider\
        //OBS: O Yahoo requer o token no cabecalho, ja o Google envia o token como parametro


        JsonObject me = WS.url(ME_API).setHeader("Authorization", new StringBuilder("Bearer").append(SPACE).append(user.accessToken).toString()).get().getJson().getAsJsonObject();
        JsonObject error = me.getAsJsonObject(ERROR);
        Logger.info("!-*-------------------------MEEEE " + me);

        if (error != null) {
            final String message = error.get(MESSAGE).getAsString();
            final String type = error.get(TYPE).getAsString();
            Logger.error("Error retrieving profile information from Yahoo. Error type: %s, message: %s.", type, message);
            throw new AuthenticationException();
        }


//        {"profile":
//            {"guid":"QAI7B3PCDJQZ662E7CAJAAECME",
//                    "addresses":[{"city":"","country":"BR","current":true,"id":1,"postalCode":"","state":"","street":"","type":"HOME"},{"city":"","country":"BR","current":true,"id":2,"postalCode":"","state":"","street":"","type":"WORK"}],
//                "ageCategory":"A",
//                    "created":"2015-12-01T23:23:43Z",
//                    "emails":[{"handle":"brnwebbery@yahoo.com","id":1,"primary":true,"type":"HOME"}],
//                "familyName":"Silva",
//                    "gender":"M",
//                    "givenName":"Bruno",
//                    "image":{"height":192,"imageUrl":"https://s.yimg.com/dh/ap/social/profile/profile_b192.png","size":"192x192","width":192},
//                "intl":"br",
//                    "jurisdiction":"br",
//                    "lang":"pt-BR",
//                    "memberSince":"2015-11-29T20:55:29Z",
//                    "migrationSource":1,
//                    "nickname":"Bruno",
//                    "notStored":true,
//                    "nux":"0",
//                    "phones":[{"id":10,"number":"55-8488312000","type":"MOBILE"}],
//                "profileMode":"PUBLIC",
//                    "profileStatus":"ACTIVE",
//                    "profileUrl":"http://profile.yahoo.com/QAI7B3PCDJQZ662E7CAJAAECME",
//                    "timeZone":"America/Buenos_Aires",
//                    "isConnected":true,
//                    "profileHidden":false,
//                    "bdRestricted":true,
//                    "profilePermission":"PRIVATE",
//                    "uri":"https://social.yahooapis.com/v1/user/QAI7B3PCDJQZ662E7CAJAAECME/profile"}}
//        user.id.id = me.get(ID).getAsString();

        JsonObject profile = me.get(PROFILE).getAsJsonObject();

        String id = profile.get("guid").getAsString();

        JsonElement givenName = profile.get("givenName");
        JsonElement nickName = profile.get("givenName");
        String fullName = (givenName != null) ?
                givenName.getAsString() :
                ((nickName != null) ? nickName.getAsString() : null);
        String surName = (profile.get("familyName")) != null ? profile.get("familyName").getAsString() : null;
        String gender = profile.get("gender") != null ? profile.get("gender").getAsString() : null;
        JsonObject image = profile.get("image") != null ? profile.get("image").getAsJsonObject() : null;
        String url = image != null ? image.get("imageUrl").getAsString() : null;
        JsonArray j = profile.get("emails").getAsJsonArray();
        String asdasd = j != null ? j.iterator().next().getAsJsonObject().get("handle").getAsString() : null;

        user.id.id = id;
        user.email = asdasd;
        user.displayName = new StringBuilder(fullName).append(SPACE).append(surName).toString();
        user.avatarUrl = url;
    }
}
