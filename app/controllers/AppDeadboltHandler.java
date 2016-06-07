/**
 * Copyright 2011 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package controllers;

import controllers.deadbolt.DeadboltHandler;
import controllers.deadbolt.ExternalizedRestrictionsAccessor;
import controllers.deadbolt.RestrictedResourcesHandler;
import controllers.securesocial.SecureSocial;
import models.deadbolt.RoleHolder;
import play.i18n.Messages;
import play.mvc.results.Forbidden;

/**
 * A sample handler to show SecureSocial and Deadbolt integration
 */
public class AppDeadboltHandler extends RootController implements DeadboltHandler {

    public final String FORBIDDEN = "forbidden";

    public void beforeRoleCheck() {
        try {
            SecureSocial.DeadboltHelper.beforeRoleCheck();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }

    public RoleHolder getRoleHolder() {
        return RootController.getLoggedUser();
    }

    public void onAccessFailure(String controllerClassName) {
        throw new Forbidden(Messages.get(FORBIDDEN));
    }

    public ExternalizedRestrictionsAccessor getExternalizedRestrictionsAccessor() {
        return null;
    }

    public RestrictedResourcesHandler getRestrictedResourcesHandler() {
        return null;
    }
}
