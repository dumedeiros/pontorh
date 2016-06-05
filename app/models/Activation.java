package models;

import javax.persistence.Entity;

@Entity
public class Activation extends Pendency {


    public Activation(LinkedAccount linkedAccount, String uuid) {
        super(linkedAccount, uuid);
    }

    public static Activation findByLinkedAccount(LinkedAccount linkedAccount) {
        return Activation.find("byLinkedAccount", linkedAccount).first();
    }
}
