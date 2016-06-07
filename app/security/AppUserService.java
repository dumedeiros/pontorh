package security;

import models.securesocial.Activation;
import models.securesocial.LinkedAccount;
import models.securesocial.Resetation;
import play.libs.Codec;
import securesocial.provider.ProviderType;
import securesocial.provider.SocialUser;
import securesocial.provider.UserId;
import securesocial.provider.UserServiceDelegate;
import utils.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class AppUserService implements UserServiceDelegate {
    /**
     * {@inheritDoc}
     */
    public SocialUser find(UserId id) {
        SocialUser socialUser = null;
        LinkedAccount linkedAccount = LinkedAccount.findByProviderAndUserId(id.provider,
                id.id);
        if (linkedAccount != null) {
            socialUser = new LinkedAccountToSocialUser().transform(linkedAccount);
        }
        return socialUser;
    }

    @Override
    public SocialUser find(String email) {
        SocialUser socialUser = null;
        LinkedAccount linkedAccount = LinkedAccount.findByEmail(email);
        if (linkedAccount != null) {
            socialUser = new LinkedAccountToSocialUser().transform(linkedAccount);
        }
        return socialUser;
    }

    /**
     * {@inheritDoc}
     */
    public void save(SocialUser user) {


        LinkedAccount linkedAccount = LinkedAccount.findByProviderAndUserId(user.id.provider, user.id.id);
        if (linkedAccount == null) {
            linkedAccount = new SocialUserToLinkedAccount().transform(user);
        } else {
            //TRATAR BEM AQUI, POIS UM USUARIO QUERENDO LOGAR COM UMA REDE SOCIAL JA CADASTRADA NAO ESTA EXIBINDO ERRO
            //O CODIGO AI EM BAIXO NAO EH SO PARA QUANDO FOR PASSWORD
            // TRATAR O CASOp
            //Quando vier do resetar senha, fazer a alteração apropriada da senha do usuário
            if (!StringUtils.isEmpty(user.password) && !user.password.equals(linkedAccount.password)) {
                linkedAccount.password = user.password;
            }
        }
        linkedAccount.save();
    }


    /**
     * {@inheritDoc}
     */
    public boolean activate(String uuid) {

        //TODO verificar se tem que atualizar o linked account de Account (mas ele provavelmente estara vazio o account)
        Activation act = (Activation) Activation.find("byUuid", uuid).first();
        boolean result = false;
        if (act != null) {
            act.linkedAccount.isEmailVerified = true;
            act.save()._delete();
            result = true;
        }
        return result;
    }

    @Override
    public String createPasswordReset(SocialUser user) {
        LinkedAccount linkedAccount = LinkedAccount.findByProviderAndUserId(user.id.provider, user.id.id);
        Resetation res = Resetation.findByLinkedAccount(linkedAccount);
        if (res != null) {
            res.delete();
        }
        final String uuid = Codec.UUID();
        if (linkedAccount != null) {
            //OBS Sempre cria outro resetation, ou seja, se for solicitado para resetar várias vezes, cria-se Vários resetations
            //OBS Apagar os resetations anteriores? Até pq os pending jobs se encarregam de apagá-los
            new Resetation(linkedAccount, uuid).save();
        }

        return uuid;
    }

    @Override
    public SocialUser fetchForPasswordReset(String username, String uuid) {
        Resetation reset = Resetation.find("byUuid", uuid).first();

        if (reset != null && reset.linkedAccount.userId.equals(username)) {
            return new LinkedAccountToSocialUser().transform(reset.linkedAccount);
        }

        return null;
    }

    @Override
    public void disableResetCode(String username, String uuid) {
        Resetation reset = Resetation.find("byUuid", uuid).first();
        if (reset != null) {
            reset.delete();
        }
    }

    /**
     * Cria um "Activation" com um usuário e um uuid para ativação por email
     *
     * @param user The user that needs to be activated
     * @return
     */
    public String createActivation(SocialUser user) {

        LinkedAccount linkedAccount = LinkedAccount.findByProviderAndUserId(
                ProviderType.userpass,
                user.id.id
        );

        Activation actv = Activation.findByLinkedAccount(linkedAccount);
        if (actv != null) {
            actv.delete();
        }

        final String uuid = Codec.UUID();
        if (linkedAccount != null) {
            new Activation(linkedAccount, uuid).save();
        }

        return uuid;
    }


    /**
     * {@inheritDoc}
     */
    /**
     * Remove todas as Ativaçoes pendentes, seja elas de Activations ou de Resetations
     */
    public void deletePendingActivations() {

        //OBS Eduardo Medeiros alterar para os ativadores se apaguem apos 48h
        //Apagar para ativadores para nao ter que fazer a logica de reenciar um link de ativacao
//        for (Activation actv : Activation.<Activation>findAll()) {
//            if (actv.creation != null) {
//                LocalDateTime expiration = LocalDateTime.ofInstant(actv.creation.toInstant(), ZoneId.systemDefault()).plusHours(48);
//
//                if (expiration.isBefore(LocalDateTime.now())) {
//                    actv.delete();
//                }
//            }
//        }

        for (Resetation reset : Resetation.<Resetation>findAll()) {
            if (reset.creation != null) {
                LocalDateTime expiration = LocalDateTime.ofInstant(reset.creation.toInstant(), ZoneId.systemDefault()).plusHours(48);

                if (expiration.isBefore(LocalDateTime.now())) {
                    reset.delete();
                }
            }
        }
    }
}
