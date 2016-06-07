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
package controllers.securesocial;

import play.Logger;
import play.Play;
import play.i18n.Messages;
import play.libs.OAuth;
import play.mvc.Before;
import play.mvc.Controller;
import play.mvc.Util;
import securesocial.provider.*;

import java.util.Collection;

/**
 * This is the main controller for the SecureSocial module.
 */
public class SecureSocial extends Controller {

    public static boolean getOnlyUser = false;
    public static SocialUser savedUser;
    public static String redirectToIfFail;
    public static String redirectToIfGetIt;

    private static final String USER_COOKIE = "securesocial.user";
    private static final String NETWORK_COOKIE = "securesocial.network";
    private static final String ORIGINAL_URL = "originalUrl";
    private static final String GET = "GET";
    private static final String ROOT = "/";
    static final String USER = "user";
    private static final String ERROR = "error";
    private static final String SECURESOCIAL_LOAD_ERROR = "securesocial.loadError";
    private static final String SECURESOCIAL_AUTH_ERROR = "securesocial.authError";
    private static final String SECURESOCIAL_LOGIN_REDIRECT = "securesocial.login.redirect";
    private static final String SECURESOCIAL_LOGOUT_REDIRECT = "securesocial.logout.redirect";
    private static final String SECURESOCIAL_SECURE_SOCIAL_LOGIN = "securesocial.SecureSocial.login";

    // Strings used from multiple places
    public static final String USERNAME = "username";
    public static final String EMAIL = "email";
    public static final String UUID = "uuid";
    public static final String NEW_PASSWORD = "newPassword";
    public static final String CONFIRM_PASSWORD = "confirmPassword";
    public static final String CURRENT_PASSWORD = "currentPassword";


    /**
     * Checks if there is a user logged in and redirects to the login page if not.
     */
    @Before(unless = {"login", "authenticate", "authByPost", "logout"})
    static void checkAccess() throws Throwable {

        final UserId userId = getUserId();

        if (userId == null) {
            final String originalUrl = request.method.equals(GET) ? request.url : ROOT;
            flash.put(ORIGINAL_URL, originalUrl);
            login();
        } else {
            final SocialUser user = loadCurrentUser(userId);
            if (user == null) {
                // the user had the cookies but the UserService can't find it ...
                // it must have been erased, redirect to login again.
                clearUserId();
                login();
            }
        }
    }


    public static SocialUser getSavedUser() {
        SocialUser su = savedUser;
        savedUser = null;
        return su;
    }

    static SocialUser loadCurrentUser() {
        UserId id = getUserId();
        final SocialUser user = id != null ? loadCurrentUser(id) : null;
        return user;
    }


    /**
     * -> Look for user user (in database?) e put him on renderArgs
     */
    private static SocialUser loadCurrentUser(UserId userId) {
        SocialUser user = UserService.find(userId);

        if (user != null) {
            // if the user is using OAUTH1 or OPENID HYBRID OAUTH set the ServiceInfo
            // so the app using this module can access it easily to invoke the APIs.
            if (user.authMethod == AuthenticationMethod.OAUTH1 || user.authMethod == AuthenticationMethod.OPENID_OAUTH_HYBRID) {
                final OAuth.ServiceInfo sinfo;
                IdentityProvider provider = ProviderRegistry.get(user.id.provider);
                if (user.authMethod == AuthenticationMethod.OAUTH1) {
                    sinfo = ((OAuth1Provider) provider).getServiceInfo();
                } else {
                    sinfo = ((OpenIDOAuthHybridProvider) provider).getServiceInfo();
                }
                user.serviceInfo = sinfo;
            }

            /**
             * TODO comparar com o modulo original e talvez remover
             *@OBS Eduardo Medeiros Ja que o meu find (AppUserService) nao consegue atribuir um userId ao SocialUser,
             * fazê-lo aqui agora
             */
            user.id = userId;

            // make the user available in templates
            renderArgs.put(USER, user);
        }
        return user;
    }

    /**
     * Returns the current user. This method can be called from secured and non-secured controllers giving you the
     * chance to retrieve the logged in user if there is one.
     *
     * @return SocialUser the current user or null if no user is logged in.
     */
    public static SocialUser getCurrentUser() {
        // first, try to get it from the renderArgs since it should be there on secured controllers.
        SocialUser currentUser = (SocialUser) renderArgs.get(USER);

        if (currentUser == null) {
            // the call is being made from an unsecured controller
            // try to provide a current user if there is one in the session
            currentUser = loadCurrentUser();
        }
        return currentUser;
    }

    /**
     * Returns true if there is a user logged in or false otherwise.
     *
     * @return a boolean
     */
    public static boolean isUserLoggedIn() {
        return getUserId() != null;
    }

    /*
     * Removes the SecureSocial cookies from the session.
     */
    private static void clearUserId() {
        session.remove(USER_COOKIE);
        session.remove(NETWORK_COOKIE);
    }


    /*
     * Sets the SecureSocial cookies in the session.
     */

    @Util
    //OBS alterado para public com @Util
    public static void setUserId(SocialUser user) {
        session.put(USER_COOKIE, user.id.id);
        session.put(NETWORK_COOKIE, user.id.provider.toString());
    }

    /*
     * Creates a UserId object from the values stored in the session. (Cookies)
     *
     * @see UserId
     * @returns  UserId the user id
     */
    private static UserId getUserId() {
        final String userId = session.get(USER_COOKIE);
        final String networkId = session.get(NETWORK_COOKIE);

        UserId id = null;

        if (userId != null && networkId != null) {
            id = new UserId();
            id.id = userId;
            id.provider = ProviderType.valueOf(networkId);
        }
        return id;
    }

    /**
     * The action for the login page.
     */
    public static void login() {

        String asdasd = Play.configuration.getProperty("application.name");
//        debug();

        final Collection providers = ProviderRegistry.all();
        flash.keep(ORIGINAL_URL);
        boolean userPassEnabled = ProviderRegistry.get(ProviderType.userpass) != null;
        render(providers, userPassEnabled);
    }


    /**
     * The logout action.
     */
    public static void logout() {
        clearUserId();
        final String redirectTo = Play.configuration.getProperty(SECURESOCIAL_LOGOUT_REDIRECT, SECURESOCIAL_SECURE_SOCIAL_LOGIN);
        redirect(redirectTo);
    }


//    //OBS Eduardo Medeiros
//
//    /**
//     * Seta as variaveis necessarias para executar o metodo de autenticacao da rede social
//     * Geralmente o metodo de autenticacao retorna para a pagina de login e seta nos cookies o usuario carregado da rede social
//     * A outra forma adicionada foi de executar o metodo de autenticacao e savar localmente o usuario carregado, a fim de recupera-lo posteriormente
//     *
//     * @param type           o tipo de provedor da rede social
//     * @param redirectTo     pagina de redirecionamento, após sucesso no login da rede social
//     * @param redirectIfFail (Opicional) página de redirecionamento, caso houver falha.
//     *                       (caso não seja especificado, redirecionar para o redirectTo)
//     */
//    public static void getOnlyUser(ProviderType type, String redirectTo, String... redirectIfFail) {
//        SecureSocial.getOnlyUser = true;
//        SecureSocial.redirectToIfGetIt = redirectTo;
//        SecureSocial.redirectToIfFail = redirectIfFail == null ? redirectTo : redirectIfFail[0];
//        authenticate(type, true);
//
//    }

//
//    /**
//     * This is the entry point for all authentication requests from the login page.
//     * The type is used to invoke the right provider.
//     *
//     * @param type             The provider type as selected by the user in the login page
//     * @param forceGetOnlyUser Forçar para apenas obter o usuario autenticado (sem salva-lo no bd nem colocá-lo no contexto)\
//     * @OBS Opcoes (Colocar o parametro forceGetOnlyUser como opcional foi a forma que deu certo de se fazer pra contornar a situação de retorno da requisicao da rede social)
//     * forceGetOnlyUser  = TRUE :  Pegar apenas o usuario apos logar na rede social
//     * forceGetOnlyUser  = FALSE : Logar no sistema
//     * forceGetOnlyUser  = NULL : Quando a rede redireciona a requisicao de volta ela vem NULL, mas como o forceGetOnlyUser foi setado antes
//     * o sistema sabe como deve se comportar
//     * @see ProviderType
//     * @see IdentityProvider
//     */
//    public static void authenticate(ProviderType type, boolean... forceGetOnlyUser) {
//
//        if (forceGetOnlyUser != null) {
//            getOnlyUser = forceGetOnlyUser[0];
//        }
//
//        doAuthenticate(type);
//    }


    public static void authenticate(ProviderType type) {

        doAuthenticate(type);
    }


    /**
     * TODO Alterar se for colocar para adicionar logins das redes OpenId/WordPress, por exemplo.
     * Ver: authenticate como exemplo
     */
    public static void authByPost(ProviderType type) {
        type = ProviderType.userpass;
        doAuthenticate(type);
    }


    //    private static void doAuthenticate(ProviderType type) {
//        if (type == null) {
//            Logger.error("Provider type was missing in request");
//            // just throw a 404 error
//            notFound();
//        }
//        flash.keep(ORIGINAL_URL);
//
//        IdentityProvider provider = ProviderRegistry.get(type);
//        String originalUrl = null;
//
//        try {
//            SocialUser user;
//            //OBS Eduardo Medeiros
//            //DONE se o usuario voltar a pagina no navegador antes do processo de login se concluir,
//            // na confirmacao de aplicativo, por exemplo, o que acontece? (BUG?)
//            if (getOnlyUser) {
//                savedUser = provider.authenticateNoSaving();
//                getOnlyUser = false;
//                redirect(redirectToIfGetIt);
//            } else {
//                user = provider.authenticate();
//                setUserId(user);
//                originalUrl = flash.get(ORIGINAL_URL);
//                final String redirectTo = Play.configuration.getProperty(SECURESOCIAL_LOGIN_REDIRECT, ROOT);
//                redirect(originalUrl != null ? originalUrl : redirectTo);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            Logger.error(e, "Error authenticating user");
//            //OBS Eduardo Medeiros
//            if (getOnlyUser) {
//                flash.error(Messages.get(SECURESOCIAL_LOAD_ERROR));
//                redirect(redirectToIfFail);
//            } else {
//                if (flash.get(ERROR) == null) {
//                    flash.error(Messages.get(SECURESOCIAL_AUTH_ERROR));
//                }
//                flash.keep(ORIGINAL_URL);
//                login();
//            }
//        }
//    }
    private static void doAuthenticate(ProviderType type) {
        if (type == null) {
            Logger.error("Provider type was missing in request");
            // just throw a 404 error
            notFound();
        }
        flash.keep(ORIGINAL_URL);

        IdentityProvider provider = ProviderRegistry.get(type);
        String originalUrl = null;

        try {
            SocialUser user = provider.authenticate();
            setUserId(user);
            originalUrl = flash.get(ORIGINAL_URL);
            final String redirectTo = Play.configuration.getProperty(SECURESOCIAL_LOGIN_REDIRECT, ROOT);
            redirect(originalUrl != null ? originalUrl : redirectTo);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.error(e, "Error authenticating user");
            if (flash.get(ERROR) == null) {
                flash.error(Messages.get(SECURESOCIAL_AUTH_ERROR));
            }
            flash.keep(ORIGINAL_URL);
            login();
        }
    }

    /**
     * A helper class to integrate SecureSocial with the Deadbolt module.
     * <p/>
     * Basically the integration is done by calling SecureSocial.Deadbolt.beforeRoleCheck()
     * within the DeadboltHandler.beforeRoleCheck implementation.
     * <p/>
     * Eg:
     * <p/>
     * public class MyDeadboltHandler extends Controller implements DeadboltHandler
     * {
     * try {
     * SecureSocial.DeadboltHelper.beforeRoleCheck();
     * } catch ( Throwable t) {
     * // handle the exception in an application specific way
     * }
     * }
     */
    public static class DeadboltHelper {
        public static void beforeRoleCheck() throws Throwable {
            checkAccess();
        }
    }


    //TODO Eduardo Medeiros Remover MEthodo
//    private static void debug() {
//        new Activation(null, "1231231").save();
//
//        UserOld aaa = UserOld.find("byUserName", "marceloa").first();
//        if (aaa == null) {
//            System.out.println("creating user a");
//            aaa = new UserOld("marceloa").save();
//            aaa.save();
//
//            System.out.println("creating user b");
//
//            UserOld b = new UserOld("marceloa").save();
//            b.save();
//
//            //////////
//
//            System.out.println("creating activation1");
//
//            Activation ac = new Activation(aaa, "123123");
//            ac.save();
//            System.out.println("Creation do ativation " + ac.id + " " + ac.creation);
//
//            System.out.println("creating activation2");
//            ac = new Activation(b, "55555");
//            ac.save();
//            System.out.println("Creation do ativation" + ac.id + " " + ac.creation);
//
//
//            Date in = new Date();
//            LocalDateTime ldt = LocalDateTime.ofInstant(in.toInstant(), ZoneId.systemDefault()).plusMinutes(3);
//            Date out = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
//            ac.creation = out;
//            System.out.println("Activation  " + ac.id + " update creation to " + ac.creation);
//
//            ac.save();
//
//
//        }
//
////        for (Activation actv : Activation.<Activation>findAll()) {
////            System.out.println(actv);
////            actv.user.isEmailVerified = true;
////            ((Activation) actv.save()).delete();
//////            actv.delete();
////        }
//
//
////        else {
////            Activation avc = Activation.find("byUser", a).first();
////            avc.delete();
////        }
//    }
}
