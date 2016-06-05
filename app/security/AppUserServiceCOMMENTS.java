//
//package security;
//
//import models.Activation;
//import models.Resetation;
//import models.UserOld;
//import play.libs.Codec;
//import securesocial.provider.SocialUser;
//import securesocial.provider.UserId;
//import securesocial.provider.UserServiceDelegate;
//
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//
///**
// * The default user service provided with SecureSocial.
// * If users need to find/save users in a custom backing store they only
// * need to provide an implementation of the UserService.Service interface in their app. It will be picked up automatically.
// * <p/>
// * This class it not suitable for a production environment.  It is only meant to be used in development.  For production use
// * you need to provide your own implementation.
// *
// * @see UserServiceDelegate
// * @see securesocial.plugin.SecureSocialPlugin
// */
//public class AppUserService implements UserServiceDelegate {
//
//    /**
//     * Busca um usuário com um determinado id (social ou username)
//     *
//     * @param userId Id a ser procurado
//     * @return Um SocialUser com os dados do usuário encontrado
//     */
//    public SocialUser find(UserId userId) {
//        UserOld u = UserOld.findByUserId(userId);
//        if (u == null) {
//            return null;
//        }
//        return u.toSocialUser();
//    }
//
//    /**
//     * Busca um usuário pelo email
//     *
//     * @param email email address to search with
//     * @return Um SocialUser com os dados do usuário encontrado
//     */
//    public SocialUser find(String email) {
//        UserOld u = UserOld.find("byEmail", email).first();
//        if (u != null) {
//            return u.toSocialUser();
//        }
//        return null;
//    }
//
//    //Todo verificar os diferentes tipos de providers saber se salva ou nao o usuario
//    //Num switch talvez
//    //Checar quao o provider do user e buscar pelos provider_id no user
//
//    public void save(SocialUser su) {
//        UserOld u = UserOld.findByUserId(su.id);
//        if (u == null) {
//            u = new UserOld().fromSocial(su);
//            u.save();
//        } else {
//            u.lastAccess = su.lastAccess;
//            u.save();
//        }
//
//    }
//
//    /**
//     * Cria um "Activation" com um usuário e um uuid para ativação por email
//     *
//     * @param user The user that needs to be activated
//     * @return
//     */
//    public String createActivation(SocialUser user) {
//        UserOld u = UserOld.find("byUserName", user.id.id).first();
//        final String uuid = Codec.UUID();
//        if (u != null) {
//            new Activation(u, uuid).save();
//        }
//
//        return uuid;
//    }
//
//    public boolean activate(String uuid) {
//        Activation act = Activation.find("byUuid", uuid).first();
//        boolean result = false;
//        if (act != null) {
//            act.user.isEmailVerified = true;
//            act.save()._delete();
//            result = true;
//        }
//        return result;
//    }
//
//    @Override
//    public String createPasswordReset(SocialUser user) {
//        UserOld u = UserOld.find("byUserName", user.id.id).first();
//        final String uuid = Codec.UUID();
//        if (u != null) {
//            new Resetation(u, uuid).save();
//        }
//
//        return uuid;
//    }
//
//    @Override
//    public SocialUser fetchForPasswordReset(String username, String uuid) {
//        Resetation reset = Resetation.find("byUuid").first();
//
//        if (reset == null && reset.user.userName.equals(username)) {
//            return reset.user.toSocialUser();
//        }
//
//        return null;
//    }
//
//    @Override
//    public void disableResetCode(String username, String uuid) {
//        Resetation reset = Resetation.find("byUuid").first();
//        if (reset != null) {
//            reset.delete();
//        }
//    }
//
//    /**
//     * Remove todas as Ativaçoes pendentes, seja elas de Activations ou de Resetations
//     */
//    public void deletePendingActivations() {
//
//        //OBS Eduardo Medeiros alterar para os ativadores se apaguem apos 48h
//        for (Activation actv : Activation.<Activation>findAll()) {
//            if (actv.creation != null) {
//                LocalDateTime expiration = LocalDateTime.ofInstant(actv.creation.toInstant(), ZoneId.systemDefault()).plusHours(48);
//
//                if (expiration.isBefore(LocalDateTime.now())) {
//                    actv.delete();
//                }
//            }
//        }
//
//        for (Resetation reset : Resetation.<Resetation>findAll()) {
//            if (reset.creation != null) {
//                LocalDateTime expiration = LocalDateTime.ofInstant(reset.creation.toInstant(), ZoneId.systemDefault()).plusHours(48);
//
//                if (expiration.isBefore(LocalDateTime.now())) {
//                    reset.delete();
//                }
//            }
//        }
//    }
//
//}
