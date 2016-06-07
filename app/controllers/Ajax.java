package controllers;


import models.pontorh.WorkDay;
import play.mvc.Controller;

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
    public static void editComment(Long wdid, String commentMsg) {
        WorkDay p = WorkDay.find("byId", wdid).first();
        p.message = commentMsg.trim();
        p.save();
        flash.put("comment", p.message);
        //Send flash to be read in view
        renderJSON(flash);
    }
}