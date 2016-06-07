package controllers;


import controllers.securesocial.SecureSocial;
import models.pontorh.User;
import models.securesocial.Account;
import models.securesocial.LinkedAccount;
import play.data.validation.Required;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;
import utils.StringUtils;

import java.util.HashMap;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@With(SecureSocial.class)
public class RootController extends Controller {

    public static User getLoggedUser() {
        String userId = session.get("userId");
        User user = null;
        if (!StringUtils.isEmpty(userId)) {
            user = User.findByUserId(userId);
        }
        return user;
    }

    public static Account getAccount() {
        User user = getLoggedUser();
        return Account.findByUser(user);
    }

    @Util
    //OBS alterei de publico para protected para nao ser possivel acessar pela barra de enderecos
    protected static void setLoggedUser(Long userId) {
        session.put("userId", userId);
    }


    //TODO remover?
    public static void createUsername() {
        render();
    }

    //TODO remover?
    public static void doCreateUsername(@Required String userName) {
//        String a = "";
//        if (validation.hasErrors()) {
//            params.flash();
//            validation.keep();
//            createAccount();
//        }
//
//        if (User.userExists(userName)) {
//            flash.error("That user name is already taken");
//            params.flash();
//            validation.keep();
//            createAccount();
//        }
//
//        SocialUser socialUser = SecureSocial.getCurrentUser();
//        User user = new User.Builder()
//                .userName(userName)
//                .displayName(socialUser.displayName)
//                .avatarUrl(socialUser.avatarUrl)
//                .build()
//                .save();
//
//        Account account = new Account.Builder()
//                .user(user)
//                .linkedAccounts(new HashMap<ProviderType, LinkedAccount>())
//                .build();
//
//        LinkedAccount linkedAccount = LinkedAccount.findByProviderAndUserId(socialUser.id.provider, socialUser.id.id);
//        linkedAccount.user = user;
//        linkedAccount.save();
//
//        account.linkedAccounts.put(linkedAccount.provider,
//                linkedAccount);
//        account.save();
//
//        setLoggedUser(userName);
//
//        Application.index();
    }

    public static void logout() {
        session.clear();
        SecureSocial.logout();
    }

    public static void createAccount() {

        SocialUser socialUser = SecureSocial.getCurrentUser();
        User user = new User.Builder()
                .displayName(socialUser.displayName)
                .avatarUrl(socialUser.avatarUrl)
                .email(socialUser.email)
                .build()
                .save();

        Account account = new Account.Builder()
                .user(user)
                .linkedAccounts(new HashMap<ProviderType, LinkedAccount>())
                .build();

        LinkedAccount linkedAccount = LinkedAccount.findByProviderAndUserId(socialUser.id.provider,
                socialUser.id.id);
        linkedAccount.user = user;
        linkedAccount.save();

        account.linkedAccounts.put(linkedAccount.provider,
                linkedAccount);
        account.save();
        setLoggedUser(user.id);

        Application.index();
    }


}
