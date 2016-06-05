package controllers.securesocial;

import notifiers.securesocial.Mails;
import play.Logger;
import play.data.validation.Email;
import play.data.validation.Equals;
import play.data.validation.Required;
import play.i18n.Messages;
import play.mvc.Controller;
import securesocial.provider.SocialUser;
import securesocial.provider.UserService;
import securesocial.utils.SecureSocialPasswordHasher;

/**
 * Controller for handling the password reset flow, for cases where the user has lost his/her
 * password and needs a way to reset it to a known one
 */
public class PasswordResetController extends Controller {

    private static final String PASSWORD_IS_RESET = "securesocial.resetSuccess";
    private static final String PASSWORD_RESET_TITLE = "securesocial.resetSuccessTitle";
    private static final String RESET_MAIL_SENT = "securesocial.resetEmailSent";
    private static final String RESET_MAIL_SENT_TITLE = "securesocial.resetEmailSentTitle";
    private static final String RESET_MAIL_NOT_REGISTRED = "securesocial.resetEmailNotRegistred";

    private static final String INVALID_RESET_TITLE = "securesocial.invalidResetTitle";
    private static final String INVALID_RESET_LINK = "securesocial.invalidResetLink";
    private static final String SECURESOCIAL_ERROR_PASSWORD_RESET = "securesocial.resetError";


    public static void resetPassword() {
        render();
    }

    /**
     * Post endpoint for sending out password reset emails
     *
     * @param email
     */
    public static void sendEmail(@Required @Email(message = "securesocial.invalidEmail") String email) {
        checkAuthenticity();
        if (validation.hasErrors()) {
            tryAgainRequestReset(email);
        }

        try {
            // Check that email exists in the database
            SocialUser user = UserService.find(email);

            if (user == null) {
                validation.addError(SecureSocial.EMAIL, Messages.get(RESET_MAIL_NOT_REGISTRED));
                tryAgainRequestReset(email);
                /**
                 OBS tb mostar msg dizendo que tb pode ser que nao tenha esse email cadastrado
                 pq pode ser que o usuario seja realmente dono do email digitado
                 e ao aparecer a msg de erro pode pensar que realmente o email foi enviado fazendopo procurar no emailTodo (spam, inbox, etc)
                 OUTRA ALTERNATIVA seria ja informar que no sistema nao tem esse usuario cadastrado
                 */
                // Show "email sent" page even if the user does not exist, to prevent figuring out emails this way
//                showEmailSuccessPage(email);
            }

            final String uuid = UserService.createPasswordReset(user);
            Mails.sendPasswordResetEmail(user, uuid);
            showEmailSuccessPage(email);
        } catch (Exception e) {
            Logger.error(e, "Error while invoking " + PasswordResetController.class.getSimpleName() + ".sendEmail");
            flash.error(Messages.get(SECURESOCIAL_ERROR_PASSWORD_RESET));
            tryAgainRequestReset(email);
        }
    }

    /**
     * Show a success page for sending out the reset email. This page does double duty as the error page, when
     * a user requests a password reset for an email that we don't know about
     */
    private static void showEmailSuccessPage(final String email) {
        flash.success(Messages.get(RESET_MAIL_SENT, email));
        final String title = Messages.get(RESET_MAIL_SENT_TITLE);
        render(UsernamePasswordController.SECURESOCIAL_SECURE_SOCIAL_NOTICE_PAGE_HTML, title);
    }


    /**
     * The provided email
     *
     * @param email
     */
    private static void tryAgainRequestReset(String email) {
        flash.put(SecureSocial.EMAIL, email);
        validation.keep();
        resetPassword();
    }


    /**
     * Controller for rendering the reset my password page
     *
     * @param username
     * @param uuid
     */
    public static void changePassword(@Required String username, @Required String uuid) {

        try {

            SocialUser user = UserService.fetchForPasswordReset(username, uuid);
            if (user == null) {
                showInvalidLinkFollowedPage();
            }

            render(username, uuid);
        } catch (Exception e) {
            Logger.error(e, "Error while invoking " + PasswordResetController.class.getSimpleName() + ".changePassword");
            flash.error(Messages.get(SECURESOCIAL_ERROR_PASSWORD_RESET));
            showInvalidLinkFollowedPage();
        }
    }

    /**
     * Post endpoint for the new password. Requires the username, uuid and authenticity token to be present, in order
     * to allow the password change to continue
     *
     * @param username
     * @param uuid
     * @param newPassword
     * @param confirmPassword
     */
    public static void doChange(@Required String username,
                                @Required String uuid,
                                @Required String newPassword,
                                @Required @Equals(message = "securesocial.passwordsMustMatch", value = "newPassword") String confirmPassword) {

        checkAuthenticity();
        if (validation.hasErrors()) {
            validation.keep();
            changePassword(username, uuid);
        }

        SocialUser user = UserService.fetchForPasswordReset(username, uuid);
        if (user == null) {
            showInvalidLinkFollowedPage();
        }

        try {
            user.password = SecureSocialPasswordHasher.passwordHash(newPassword);
            UserService.disableResetCode(username, uuid);
            UserService.save(user);

            flash.success(Messages.get(PASSWORD_IS_RESET));
            final String title = Messages.get(PASSWORD_RESET_TITLE);
            render(UsernamePasswordController.SECURESOCIAL_SECURE_SOCIAL_NOTICE_PAGE_HTML, title);
        } catch (Exception e) {
            Logger.error(e, "Error while invoking " + PasswordResetController.class.getSimpleName() + ".doChange");
            flash.error(Messages.get(SECURESOCIAL_ERROR_PASSWORD_RESET));
            changePassword(username, uuid);
        }
    }

    /**
     * Show the notification page, with an "invalid link followed"-message
     */
    private static void showInvalidLinkFollowedPage() {
        flash.error(Messages.get(INVALID_RESET_LINK));
        final String title = Messages.get(INVALID_RESET_TITLE);
        render(UsernamePasswordController.SECURESOCIAL_SECURE_SOCIAL_NOTICE_PAGE_HTML, title);
    }


}
