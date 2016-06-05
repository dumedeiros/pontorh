package controllers.securesocial;

import notifiers.securesocial.Mails;
import play.Logger;
import play.data.validation.Email;
import play.data.validation.Required;
import play.i18n.Messages;
import play.mvc.Controller;
import securesocial.provider.SocialUser;
import securesocial.provider.UserService;

/**
 * OBS EDUARDO MEDEIROS (ALL CLASS)
 * Controller for handling the activation resend flow, for cases where the user didn' receive the last activation link
 * * password and needs a way to reset it to a known one
 */
public class ActivationResendController extends Controller {

    static final String SECURESOCIAL_SECURE_SOCIAL_NOTICE_PAGE_HTML = "securesocial/SecureSocial/noticePage.html";

    private static final String ACTIVATION_RESEND_MAIL_SENT = "securesocial.activationLinkResendMailSent";
    private static final String ACTIVATION_RESEND_TITLE = "securesocial.activationLinkResendTitle";
    private static final String ACTIVATION_RESEND_EMAIL_NOT_REGISTRED = "securesocial.activationLinkResendEmailNotRegistred";

    //    private static final String INVALID_RESET_TITLE = "securesocial.invalidResetTitle";
//    private static final String INVALID_RESET_LINK = "securesocial.invalidResetLink";
    private static final String SECURESOCIAL_ERROR_PASSWORD_RESET = "securesocial.resetError";


    public static void reactivate() {
        render();
    }

    /**
     * Post endpoint for sending out activation link resend
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
//                validation.addError(SecureSocial.EMAIL, Messages.get(RESET_MAIL_NOT_REGISTRED));
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

            final String uuid = UserService.createActivation(user);
            Mails.sendActivationEmail(user, uuid);
            showEmailSuccessPage(email);
        } catch (Exception e) {
            Logger.error(e, "Error while invoking " + ActivationResendController.class.getSimpleName() + ".sendEmail");
            flash.error(Messages.get(SECURESOCIAL_ERROR_PASSWORD_RESET));
            tryAgainRequestReset(email);
        }
    }

    /**
     * Show a success page for sending out the reset email. This page does double duty as the error page, when
     * a user requests a password reset for an email that we don't know about
     */
    private static void showEmailSuccessPage(final String email) {
        flash.success(Messages.get(ACTIVATION_RESEND_MAIL_SENT, email));
        final String title = Messages.get(ACTIVATION_RESEND_TITLE);
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
        reactivate();
    }

}
