package it.hueic.kenhoang.orderfoods_app.model;

/**
 * Created by kenhoang on 09/02/2018.
 */

public class Sender {
    public String to;
    public NotificationModel notification;

    public Sender(String to, NotificationModel notification) {
        this.to = to;
        this.notification = notification;
    }
}
