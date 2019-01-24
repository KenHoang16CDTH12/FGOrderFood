package it.hueic.kenhoang.orderfoods_app.model;

/**
 * Created by kenhoang on 09/02/2018.
 */

public class NotificationModel {
    public String title;
    public String body;

    public NotificationModel() {
    }

    public NotificationModel(String title, String body) {
        this.body = body;
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
