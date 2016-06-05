package controllers;

import controllers.deadbolt.Deadbolt;
import controllers.securesocial.SecureSocial;
import models.Account;
import models.LinkedAccount;
import models.User;
import play.Logger;
import play.mvc.Before;
import play.mvc.With;
import securesocial.provider.IdentityProvider;
import securesocial.provider.ProviderRegistry;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;
import security.LinkedAccountToSocialUser;
import utils.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


@With({SecureSocial.class,
        Deadbolt.class})
public class Application extends RootController {


    @Before(unless = {}, priority = Integer.MAX_VALUE)

    static void globals() {
        if (!request.isAjax()) {
            SocialUser socialUser = SecureSocial.getCurrentUser();

            LinkedAccount linkedAccount = LinkedAccount.findByProviderAndUserId(socialUser.id.provider,
                    socialUser.id.id);
            //Se nao existe um userName nos Sessao (cookies)
            if (session.get("userId") == null) {
                if (linkedAccount != null
                        && linkedAccount.user != null) {
                    setUser(linkedAccount.user.id);
                }
                //Pode existir um linkedAccount sem um usuario
                else {
                    //Entao adiciin
                    createAccount();
                }
            } else {
                //Se userName ta na sessao, obter Usuario - DB
                User user = getUser();
                if (user == null) {
                    createAccount();
                }
            }

            if (linkedAccount != null) {
                //Vincula a um usuario
                if (linkedAccount.user == null) {
                    Account account = getAccount();
                    if (!account.linkedAccounts.containsKey(socialUser.id.provider)) {
                        linkedAccount.user = getUser();
                        linkedAccount.save();

                        account.linkedAccounts.put(linkedAccount.provider,
                                linkedAccount);
                        account.save();
                    }
                }

                renderArgs.put("displayName",
                        linkedAccount.user.displayName);
                renderArgs.put("avatarUrl",
                        linkedAccount.user.avatarUrl);
                renderArgs.put("lAcc",
                        linkedAccount);

            }
        }
    }


    private static void checkSession(LinkedAccount linkedAccount) {
        if (session.get("userId") == null) { //Se nao existe um userName nos Sessao (cookies)
            if (linkedAccount != null
                    && linkedAccount.user != null) { //mas o usuario existe e está vinculado a uma linkedAccount
                setUser(linkedAccount.user.id); //Setar Usuario na sessao
            } else {   //Pode existir um linkedAccount sem um usuario
                // Se fechar o browser, por exemplo?
                createAccount(); //Entao vincule um User ao linkedAccount
                //SecureSocial.login();
            }
        } else {
            User user = getUser();//Se userName ta na sessao, obter Usuario - DB
            if (user == null) {   //verificar se ele existe de verdade
//                    createAccount();
                SecureSocial.login();
            }
        }
    }

    private static void hasNoEmailRedirect(LinkedAccount linkedAccount) {
        if (linkedAccount != null
                && StringUtils.isEmpty(linkedAccount.email)
                && linkedAccount.user == null) {
            //TODO melhorar isso aqui
            flash.error("Não foi possível resgatar o email vinculado à sua conta do " +
                    linkedAccount.provider.valueName +
                    " Tente-se logar com outra conta abaixo ou obtenha mais informações" +
                    " <a href='ww.'>aqui</a> "
            );

            linkedAccount.delete();
            SecureSocial.login();
        }
    }

    public static void index() {


        Account account = getAccount();
        Map<ProviderType, LinkedAccount> linkedAccounts = account.linkedAccounts;

        LinkedAccount linkedAccount = account.linkedAccounts.get(account.linkedAccounts.size() - 1);
        if (linkedAccount != null) {
            if (linkedAccount.user != null) {
                renderArgs.put("displayName",
                        linkedAccount.user.displayName);
                renderArgs.put("avatarUrl",
                        linkedAccount.user.avatarUrl);
                renderArgs.put("lAcc",
                        linkedAccount);
            }
        }


        Collection<IdentityProvider> identityProviders = ProviderRegistry.all();
        List<ProviderType> providerTypes = new ArrayList<ProviderType>();
        for (
                IdentityProvider identityProvider
                : identityProviders)

        {
            if (!linkedAccounts.containsKey(identityProvider.type)) {
                providerTypes.add(identityProvider.type);
            }
        }


        render(linkedAccounts,
                providerTypes);

    }

    public static void linkedAccounts() {
        Account account = getAccount();
        Map<ProviderType, LinkedAccount> linkedAccounts = account.linkedAccounts;
        LinkedAccount twitter = null;
        if (linkedAccounts.containsKey(ProviderType.twitter)) {
            twitter = linkedAccounts.get(ProviderType.twitter);
        }

        Collection<IdentityProvider> identityProviders = ProviderRegistry.all();
        List<ProviderType> providerTypes = new ArrayList<ProviderType>();
        for (IdentityProvider identityProvider : identityProviders) {
            if (!linkedAccounts.containsKey(identityProvider.type)) {
                providerTypes.add(identityProvider.type);
            }
        }

        render(linkedAccounts,
                providerTypes,
                twitter);
    }

    public static void newLinkedAccount(ProviderType provider) {
        User user = getUser();

        if (provider != null) {

            //OBS colocar se a pagina para adicao for diferente de "/" (index)
//            flash.put("originalUrl", "http://localhost:9000/sociallysecure/friends");
            IdentityProvider identityProvider = ProviderRegistry.get(provider);
            SocialUser socialUser = identityProvider.authenticate();
        } else {
            Logger.error("Provider is null!");
        }

        linkedAccounts();
    }

    private static void doNewLinkAccount(LinkedAccount linkedAccount, SocialUser socialUser) {

    }

    public static void unLinkAccount(Long id) {
//
        User user = getUser();
        List<LinkedAccount> linkedAccounts = LinkedAccount.findByUser(user);

        //Tem pelo menos duas linkeds accounts vinculadas
        if (linkedAccounts.size() > 1) {
            LinkedAccount linkedAccount = LinkedAccount.findById(id);
            Account account = getAccount();
            account.linkedAccounts.remove(linkedAccount.provider);
            linkedAccounts.remove(linkedAccount);
            account.save();
            linkedAccount.delete();

            linkedAccount = linkedAccounts.get(0);
            SocialUser su = new LinkedAccountToSocialUser().transform(linkedAccount);
            SecureSocial.setUserId(su);

        } else {
            //TODO colocar msg
            flash.error("Não é possível remover a unica forma de se realizar login");
            index();
        }

        index();

    }

}



    /* static void globals() {
        if (!request.isAjax()) {
            SocialUser socialUser = SecureSocial.getCurrentUser();

            LinkedAccount linkedAccount =
                    LinkedAccount.findByProviderAndUserId(
                            socialUser.id.provider,
                            socialUser.id.id
                    );

            //Se nao existe um userName nos Sessao (cookies)
            if (session.get("userName") == null) {
                if (linkedAccount != null
                        && linkedAccount.user != null) {
                    setUser(linkedAccount.user.userName);
                }
                //Pode existir um linkedAccount sem um usuario
                else {
                    //Entao adiciin
                    createAccount();
                }
            } else {
                //Se userName ta na sessao, obter Usuario - DB
                User user = getUser();
                if (user == null) {
                    createAccount();
                }
            }

            if (linkedAccount != null) {
                if (linkedAccount.user != null) {
                    renderArgs.put("user", linkedAccount.user);
                    renderArgs.put("lAcc", linkedAccount);
                    renderArgs.put("displayName",
                            linkedAccount.user.displayName);
                    renderArgs.put("avatarUrl",
                            linkedAccount.user.avatarUrl);
                } else {
                    Account account = getAccount();
                    if (!account.linkedAccounts.containsKey(socialUser.id.provider)) {
                        linkedAccount.user = getUser();
                        linkedAccount.save();
                        account.linkedAccounts.put(linkedAccount.provider,
                                linkedAccount);
                        account.save();
                    }

                }
            }
        }
    }*/

//@Before(priority = Integer.MAX_VALUE)
//    static void globals() {
//        if (!request.isAjax()) {
//            SocialUser socialUser = SecureSocial.getCurrentUser();
//
//            LinkedAccount linkedAccount =
//                    LinkedAccount.findByProviderAndUserId(
//                            socialUser.id.provider,
//                            socialUser.id.id
//                    );
//
//            hasNoEmailRedirect(linkedAccount); //primeira vinculacao de conta DEVE existir email
//
//            checkSession(linkedAccount);
//
//            if (linkedAccount != null) {
//                if (linkedAccount.user != null) {
//                    renderArgs.put("user", linkedAccount.user);
//                    renderArgs.put("lAcc", linkedAccount);
//                    renderArgs.put("displayName",
//                            linkedAccount.user.displayName);
//                    renderArgs.put("avatarUrl",
//                            linkedAccount.user.avatarUrl);
//                } else {
//                    doNewLinkAccount(linkedAccount, socialUser);
//                }
//            }
//        }
//    }