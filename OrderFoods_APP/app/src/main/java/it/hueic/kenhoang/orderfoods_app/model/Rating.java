package it.hueic.kenhoang.orderfoods_app.model;

/**
 * Created by kenhoang on 07/02/2018.
 */

public class Rating {
    private String userPhone;//both key and value
    private String foodId;
    private String rateValue;
    private String comment;
    private String timestamp;
    public Rating() {
    }

    public Rating(String userPhone, String foodId, String rateValue, String comment, String timestamp) {
        this.userPhone = userPhone;
        this.foodId = foodId;
        this.rateValue = rateValue;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getFoodId() {
        return foodId;
    }

    public void setFoodId(String foodId) {
        this.foodId = foodId;
    }

    public String getRateValue() {
        return rateValue;
    }

    public void setRateValue(String rateValue) {
        this.rateValue = rateValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
