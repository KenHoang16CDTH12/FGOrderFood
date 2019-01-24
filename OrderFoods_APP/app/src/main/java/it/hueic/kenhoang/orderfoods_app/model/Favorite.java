package it.hueic.kenhoang.orderfoods_app.model;

/**
 * Created by kenhoang on 04/02/2018.
 */

public class Favorite {
    private String idFood;
    private String nameFood;

    public Favorite() {
    }

    public Favorite(String idFood, String nameFood) {
        this.idFood = idFood;
        this.nameFood = nameFood;
    }

    public String getIdFood() {
        return idFood;
    }

    public void setIdFood(String idFood) {
        this.idFood = idFood;
    }

    public String getNameFood() {
        return nameFood;
    }

    public void setNameFood(String nameFood) {
        this.nameFood = nameFood;
    }
}
