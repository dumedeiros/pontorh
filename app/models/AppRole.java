package models;

import models.deadbolt.Role;
import play.db.jpa.Model;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Entity
public class AppRole extends Model implements Role {
    @Column(nullable = false)
    public String roleName;

    public AppRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    public static AppRole getByName(String name) {
        return AppRole.find("byName", name).first();
    }

    @Override
    public String toString() {
        return roleName;
    }
}
