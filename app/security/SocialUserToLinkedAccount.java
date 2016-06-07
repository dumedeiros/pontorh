package security;

import models.securesocial.LinkedAccount;
import securesocial.provider.SocialUser;
import utils.Transformer;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SocialUserToLinkedAccount implements Transformer<SocialUser, LinkedAccount> {
    public LinkedAccount transform(SocialUser socialUser) {
        return new LinkedAccountMerger().merge(new LinkedAccount.Builder().build(),
                socialUser);
    }
}
