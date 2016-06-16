package models.pontorh;

import annotations.Cpf;
import models.deadbolt.AppRole;
import models.deadbolt.Role;
import models.deadbolt.RoleHolder;
import play.data.validation.Email;
import play.data.validation.Phone;
import play.data.validation.Required;
import play.db.jpa.Blob;
import play.db.jpa.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;


@Entity
public class User extends Model implements RoleHolder {

    public User() {
    }

    //    @Transient
    public String avatarUrl;
    //TODO remover? Ja que o username ta sendo salvo no linkedAccount?
    //REMOVIDO
    //public String userName;
    @Required
    public String fullName;

    @Email
    @Required
    public String email;

    public int sex;
    public Date birthDate;
    public String matricula;
    @Phone(message = "Telefone Inválido")
    public String phone;

    public Blob photo;

    @Cpf(message = "Cpf inválido")
    public String cpf;
    public String rg;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = "privilege_id", referencedColumnName = "id", nullable = false)
    @JoinTable(name = "USER_ROLES")
    public List<AppRole> roles;

    private User(Builder builder) {
        avatarUrl = builder.avatarUrl;
        fullName = builder.fullName;
        email = builder.email;
        sex = builder.sex;
        birthDate = builder.birthDate;
        cpf = builder.cpf;
        rg = builder.rg;
        roles = builder.roles;
    }

    public List<? extends Role> getRoles() {
        return roles;
    }

    public static User findByUserName(String userName) {
        return find("byUserName", userName).first();
    }

    public static User findByUserId(String id) {
        return User.findById(new Long(id));
    }

    public static boolean userExists(String userName) {
        return count("byUserName", userName) > 0;
    }

    public static final class Builder {
        private String avatarUrl;
        private String fullName;
        private String email;
        private char sex;
        private Date birthDate;
        private String cpf;
        private String rg;
        private List<AppRole> roles;

        public Builder() {
        }

        public Builder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder sex(char sex) {
            this.sex = sex;
            return this;
        }

        public Builder birthDate(Date birthDate) {
            this.birthDate = birthDate;
            return this;
        }

        public Builder cpf(String cpf) {
            this.cpf = cpf;
            return this;
        }

        public Builder rg(String rg) {
            this.rg = rg;
            return this;
        }

        public Builder roles(List<AppRole> roles) {
            this.roles = roles;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }


}
