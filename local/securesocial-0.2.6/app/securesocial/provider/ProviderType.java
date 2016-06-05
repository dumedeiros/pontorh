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
package securesocial.provider;

/**
 * An enum with the authentication providers supported by SecureSocial
 */
//
//public enum ProviderType {
//    twitter,
//    facebook,
//    google,
//    googleopenid,
//    yahoo,
//    linkedin,
//    foursquare,
//    userpass,
//    wordpress,
//    myopenid,
//    github,
//    googleoauth2,
//    yahoooauth2,
//    liveoauth2
//
//}

public enum ProviderType {
    twitter("Twitter"),
    facebook("Facebook"),
    google("Google"),
    googleopenid("Google(ID)"),
    yahoo("Yahoo"),
    linkedin("Linkedin"),
    foursquare("Foursquare"),
    userpass("UserPass"),
    wordpress("WordPress"),
    myopenid("OpenId"),
    github("Github"),
    googleoauth2("Google"),
    yahoooauth2("Yahoo"),
    liveoauth2("Microsoft Live");

    public final String valueName;

    ProviderType(String n) {
        this.valueName = n;
    }


}
