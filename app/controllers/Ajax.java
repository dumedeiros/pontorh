package controllers;


import models.pontorh.User;
import models.pontorh.WorkDay;
import play.mvc.Controller;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Ajax extends Controller {
    public static int o = 0;

    public static void teste() {
        System.out.println("cacasc");
        render();
    }


    /**
     * Edit WorkDay Comment via Ajax
     *
     * @param wdid       id of WorkDay
     * @param commentMsg msg
     */
    public static void editComment(String wdid, String commentMsg) {
        //TODO tratar o formato das datas (internalization?)
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date dateObj = null;
        try {
            dateObj = format.parse(wdid);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        User u = RootController.getLoggedUser();
        WorkDay wd = WorkDay.getOfDay(dateObj, u);
        if (wd == null) {
            wd = new WorkDay(dateObj, u);
        }

        wd.message = commentMsg.trim();
        wd.save();
        flash.put("comment", wd.message);
        //Send flash to be read in view
        renderJSON(flash);
    }
}