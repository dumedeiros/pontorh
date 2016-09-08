package controllers;

import play.Play;
import play.mvc.Controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class Test extends Controller {

    public static void test() throws IOException {
        InputStream iasndsadsa = Play.classloader.getResourceAsStream("resources/avatar-null.png");
        Play.class.getClassLoader().getResources("play.static");
        Play.classloader.getResourceAsStream("public/images/avatar-null.png");

        File asdsadasdadqw = Play.getFile("/public/images/avatar-null.png");
        renderBinary(iasndsadsa);
    }
}