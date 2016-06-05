package models;

import play.db.jpa.Model;

import javax.persistence.CascadeType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import java.util.Date;

@MappedSuperclass
public abstract class Pendency extends Model {
    public String uuid;

    public Date creation;

    @OneToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    public LinkedAccount linkedAccount;

    public Pendency() {
    }

    public Pendency(LinkedAccount linkedAccount, String uuid) {
        this.uuid = uuid;
        this.linkedAccount = linkedAccount;
        this.creation = new Date();
    }

}
