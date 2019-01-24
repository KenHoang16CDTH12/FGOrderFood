package it.hueic.kenhoang.orderfoodsserver_app.model;

/**
 * Created by kenhoang on 09/02/2018.
 */

public class Sender {
    public String to;
    public NotificationModel notification;

    public Sender() {
    }

    public Sender(String to, NotificationModel notification) {
        this.to = to;
        this.notification = notification;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public NotificationModel getNotification() {
        return notification;
    }

    public void setNotification(NotificationModel notification) {
        this.notification = notification;
    }
}
