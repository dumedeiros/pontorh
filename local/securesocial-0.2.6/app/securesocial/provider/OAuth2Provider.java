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
package securesocial.provider;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import play.Logger;
import play.Play;
import play.libs.OAuth2;
import play.mvc.Scope;
import play.mvc.results.Redirect;
import securesocial.libs.ExtOAuth2;

import java.util.HashMap;
import java.util.Map;


//OBS alterado por Eduardo Medeiros
/**
 * A provider that handles the OAuth2 authentication flow
 */
public abstract class OAuth2Provider extends IdentityProvider {
    private OAuth2 service;
    public String[] scope;
    private static final String AUTHORIZATION_URL = "authorizationURL";
    private static final String ACCESS_TOKEN_URL = "accessTokenURL";
    private static final String CLIENTID = "clientid";
    private static final String SECRET = "secret";
    private static final String QUESTION_MARK = "?";
    private static final String AMPERSAND = "&";
    private static final String CLIENT_ID = "client_id=";
    private static final String EQUALS_SIGN = "=";
    private static final String REDIRECT_URI = "&redirect_uri=";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String OAUTH_TOKEN = "oauth_token";


    protected static final String ID = "id";
    protected static final String SCOPE = "scope";
    protected static final String ERROR = "error";
    protected static final String METHOD = "method";
    protected static final String MESSAGE = "message";
    protected static final String CODE = "code";
    protected static final String TYPE = "type";
    protected static final String RESPONSE = "response";
    protected static final String RESPONSE_TYPE = "response_type";
    protected static final String ACCESS_TYPE = "access_type";


    protected OAuth2Provider(ProviderType type) {
        super(type, AuthenticationMethod.OAUTH2);
        //OBS Eduardo Medeiros
        final String key = getPropertiesKey(type);//Para gerar a key securesocial.facebook. p.eg
        service = ExtOAuth2Providers.names().contains(type.name()) ? createExtOAuth2(key) : createOAuth2(key);
        scope = getScope(key, SCOPE);

        if (service instanceof ExtOAuth2) {
            addEXTOaut2Params();
        }
    }

    /**
     * Creates an OAuth2 object using the properties specified for the provider.
     *
     * @param key The provider key
     * @return An OAuth2 object
     */
    private OAuth2 createOAuth2(String key) {
        return new OAuth2(
                Play.configuration.getProperty(key + AUTHORIZATION_URL), //securesocial.facebook.authorizationURL , etc...
                Play.configuration.getProperty(key + ACCESS_TOKEN_URL),  //securesocial.facebook.accessTokenURL
                Play.configuration.getProperty(key + CLIENTID),         //securesocial.facebook.clientid
                Play.configuration.getProperty(key + SECRET)            //securesocial.facebook.secret
        );
    }


    /**
     * Creates an ExtOAuth2 object using the properties specified for the provider.
     *
     * @param key The provider key
     * @return An OAuth2 object
     */
    private ExtOAuth2 createExtOAuth2(String key) {

        return new ExtOAuth2(
                Play.configuration.getProperty(key + AUTHORIZATION_URL), //securesocial.facebook.authorizationURL , etc...
                Play.configuration.getProperty(key + ACCESS_TOKEN_URL),  //securesocial.facebook.accessTokenURL
                Play.configuration.getProperty(key + CLIENTID),         //securesocial.facebook.clientid
                Play.configuration.getProperty(key + SECRET)            //securesocial.facebook.secret
        );

    }

    /**
     * Returns the scope specified for this provider in the properties file (eg: facebook.scope)
     *
     * @param providerKey The properties key
     * @param scopeKey    The property name
     * @return The scope
     */
    public static String[] getScope(String providerKey, String scopeKey) {
        final String s = Play.configuration.getProperty(providerKey + SCOPE);//(securesocial.facebook.scope)  email, profile, etc...
        String[] scope = null;
        if (s != null && s.trim().length() > 0) {
            scope = new String[]{scopeKey, s}; //scope = email
        }
        return scope;
    }

    /**
     * @return
     * @see IdentityProvider#doAuth(java.util.Map)
     */
    @Override
    protected SocialUser doAuth(Map<String, Object> authContext) {
        if (service instanceof ExtOAuth2) {
            return doAuthExtOAuth2();
        } else {
            return doAuthOAuth2();
        }

    }

    private SocialUser doAuthExtOAuth2() {
        Scope.Params params = Scope.Params.current();

        if (params.get(ERROR) != null) {
            // todo: improve this.  Get details of the error and include them in the exception.
            throw new AuthenticationException();
        }

        if (!OAuth2.isCodeResponse()) {
            service.retrieveVerificationCode(getFullUrl());
        }

        final String authUrl = getFullUrl();
        OAuth2.Response response = service.retrieveAccessToken(authUrl);
        if (response == null) {
            throw new AuthenticationException();
        }

        String accessToken = ((ExtOAuth2) service).extractAccessToken(response);
        accessToken = accessToken == null ? response.accessToken : accessToken;
        if (accessToken == null) {
            // todo: add error to the exception
            throw new AuthenticationException();
        }

        SocialUser user = createUser();
        user.accessToken = accessToken;
        return user;
    }

    private SocialUser doAuthOAuth2() {
        Scope.Params params = Scope.Params.current();

        if (params.get(ERROR) != null) {
            // todo: improve this.  Get details of the error and include them in the exception.
            throw new AuthenticationException();
        }

        if (!OAuth2.isCodeResponse()) {
            StringBuilder authUrl = new StringBuilder(service.authorizationURL);
            String delimiter = service.authorizationURL.indexOf(QUESTION_MARK) == -1 ? QUESTION_MARK : AMPERSAND;
            authUrl.append(delimiter).append(CLIENT_ID).append(service.clientid);
            if (scope != null) {
                authUrl.append(AMPERSAND).append(scope[0]).append(EQUALS_SIGN).append(scope[1]);
            }
            authUrl.append(REDIRECT_URI).append(getFullUrl());
            throw new Redirect(authUrl.toString());
        }

        final String authUrl = getFullUrl();
        OAuth2.Response response = service.retrieveAccessToken(authUrl);
        if (response == null) {
            throw new AuthenticationException();
        }

        String accessTokenFromJson = null;
        if (response.error != null) {
            if (response.error.type == OAuth2.Error.Type.UNKNOWN) {
                // the OAuth2 class is expecting the access token in the query string.
                // this is not what the OAuth2 spec says.  Facebook works, but Foursquare fails for example.
                // So I'm going to check if the token is there before throwing the exception.
                // todo: fix the OAuth2 class.
                JsonElement asJson = response.httpResponse.getJson();

                if (asJson != null) {
                    JsonObject body = asJson.getAsJsonObject();
                    if (body != null) {
                        // this is what many libraries expect (probably because Facebook returns it)
                        JsonElement token = body.get(ACCESS_TOKEN);
                        if (token != null) {
                            accessTokenFromJson = token.getAsString();
                        } else {
                            // this is what should be returned as defined in the OAuth2 spec
                            token = body.get(OAUTH_TOKEN);
                            if (token != null) {
                                accessTokenFromJson = token.getAsString();
                            }
                        }
                    }
                }
            }

            // if the workaround did not find the token then we really have an error,
            // so I need to throw the exception
            if (accessTokenFromJson == null) {
                // todo: add error to the exception
                throw new AuthenticationException();
            }
        }
        SocialUser user = createUser();
        user.accessToken = accessTokenFromJson == null ? response.accessToken : accessTokenFromJson;
        return user;
    }


    /**
     * MethodHolder -> to not be abstract, obliyng the other to implement
     * <p/>
     * <p>Get the particular parameters to execute the Oauth2 call.</p>
     * Each provider has it's own specifics parameters.
     * <p/>
     * e.g.:
     * GoogleOauth2Provider:
     * <ul>
     * <li>
     * (SCOPE, https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/userinfo.email)
     * </li>
     * <li>
     * (response_type, code)
     * </li>
     * </ul>
     *
     * @return A map with all the particular parametrs
     * @author Eduardo Medeiros
     */
    private Map<String, String> getParticularEXTOauth2Parameters() {
        return null;
    }

    ;


    public void addEXTOaut2Params() {
        Map<String, String> p = new HashMap<String, String>();
        String scope = Play.configuration.getProperty(getPropertiesKey(type) + SCOPE);
        Logger.debug("### Scope is %s", scope);
        if (scope != null && scope.trim().length() > 0) {
            ((ExtOAuth2) this.service).addAuthorizationURLExtraParam(SCOPE, scope);
        }
        String accessType = Play.configuration.getProperty(getPropertiesKey(type) + ACCESS_TYPE);
        if (accessType != null && accessType.trim().length() > 0) {
            ((ExtOAuth2) this.service).addAuthorizationURLExtraParam(ACCESS_TYPE, accessType);

        }
        ((ExtOAuth2) this.service).addAuthorizationURLExtraParam(RESPONSE_TYPE, CODE);
        ((ExtOAuth2) service).accessTokenURLMethod = ExtOAuth2.POST_METHOD;
    }


    //TODO remover a parte que se refere a twitteroaht2
    public void addConfigParameters(Map<String, String> map) {
        //loop a Map
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey().equals(METHOD)) {
                ((ExtOAuth2) service).accessTokenURLMethod = entry.getValue();
            } else {
                ((ExtOAuth2) this.service).addAuthorizationURLExtraParam(entry.getKey(), entry.getValue());
            }
        }
    }
}
