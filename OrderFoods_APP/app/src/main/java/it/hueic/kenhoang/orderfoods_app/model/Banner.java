package it.hueic.kenhoang.orderfoods_app.model;

/**
 * Created by kenhoang on 13/02/2018.
 */

public class Banner {
    private String id, name, image, menuId;

    public Banner() {
    }

    public Banner(String id, String name, String image, String menuId) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.menuId = menuId;
    }

    public String getMenuId() {
        return menuId;
    }

    public void setMenuId(String menuId) {
        this.menuId = menuId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
