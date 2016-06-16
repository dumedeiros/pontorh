package controllers;

import models.pontorh.User;
import play.mvc.Controller;

import java.util.Date;

public class Test extends Controller {

    public static void test() {
        Date date = new Date();
        User u = new User();
        u.birthDate = new Date();
        validation.valid(u);
        render(date);
    }
}