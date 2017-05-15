package controllers;


import models.pontorh.Avatar;
import models.pontorh.User;
import models.pontorh.WorkDay;
import play.data.Upload;
import play.mvc.Controller;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Ajax extends Controller {
    public static int o = 0;

    public static void teste() {
        System.out.println("cacasc");
        render();
    }

    public static void getFile(File file) {
        System.out.println(request);
        System.out.println("cacasc");
        renderJSON(flash);
    }

    public static void setAvatarFromURL(String url) {

        System.out.println(request);
        Map<String, String> result = new HashMap<String, String>();
        try {
            User user = Application.getLoggedUser();
            Avatar.setUserAvatarFromURL(user, url);
            result.put("isStatusOk", "true");
        } catch (Exception e) {
            result.put("isStatusOk", "false");
        }
        renderJSON(result);
    }

    public static void setUserAvatar(Upload blob) {

        User user = Application.getLoggedUser();

        Avatar avatar = Avatar.findByUser(user);
        if (avatar == null) {
            avatar = new Avatar();
        }

        avatar.contentType = blob.getContentType();
        avatar.name = "Avatar".concat(user.id.toString()).concat(avatar.contentType.replace("image/", "."));
        avatar.size = blob.getSize();


        avatar.imageBytes = blob.asBytes();
        avatar.save();
        renderJSON(flash);
    }


    /**
     * Edit WorkDay Comment via Ajax
     *
     * @param wdid       id of WorkDay
     * @param commentMsg msg
     */
    public static void editComment(String wdid, String commentMsg) throws InterruptedException {
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