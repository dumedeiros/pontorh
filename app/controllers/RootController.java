package controllers;


import controllers.securesocial.SecureSocial;
import models.pontorh.Avatar;
import models.pontorh.User;
import models.securesocial.Account;
import models.securesocial.LinkedAccount;
import play.Logger;
import play.data.validation.Required;
import play.mvc.Controller;
import play.mvc.Util;
import play.mvc.With;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;
import utils.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

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
                .fullName(socialUser.displayName)
                .avatarUrl(socialUser.avatarUrl)
                .email(socialUser.email)
                .build()
                .save();


        if (user.avatarUrl != null) {
            try {
                URL url = new URL(user.avatarUrl);

                final BufferedImage image = ImageIO.read(url);

                URLConnection conn = url.openConnection();
                String contentType = conn.getContentType();

                Avatar avatar = new Avatar();

                avatar.name = "Avatar".concat(user.id.toString()).concat(contentType.replace("image/", "."));
                avatar.contentType = contentType;
                avatar.imageBytes = Avatar.getScaledInstance(image, 200, 200, RenderingHints.VALUE_INTERPOLATION_BICUBIC, true);
                avatar.size = avatar.imageBytes.length;
                avatar.user = user;
                avatar.save();

            } catch (IOException e) {
                Logger.error("Erro ao atribuir avatar da rede social");
                e.printStackTrace();
            }

        }

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
