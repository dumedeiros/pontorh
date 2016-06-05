package models;

import javax.persistence.Entity;

@Entity
public class Resetation extends Pendency {


    public Resetation(LinkedAccount linkedAccount, String uuid) {
        super(linkedAccount, uuid);
    }


    public static Resetation findByLinkedAccount(LinkedAccount linkedAccount) {
        return Resetation.find("byLinkedAccount", linkedAccount).first();
    }

}
