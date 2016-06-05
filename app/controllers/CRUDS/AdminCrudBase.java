package controllers.CRUDS;

import controllers.CRUD;
import controllers.deadbolt.Deadbolt;
import controllers.deadbolt.Restrict;
import controllers.securesocial.SecureSocial;
import play.mvc.With;

@With({Deadbolt.class, SecureSocial.class})
@Restrict({"admin"})
public abstract class AdminCrudBase extends CRUD {
}
