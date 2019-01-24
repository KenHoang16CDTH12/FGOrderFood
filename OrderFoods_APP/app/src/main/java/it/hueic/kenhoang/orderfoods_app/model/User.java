package it.hueic.kenhoang.orderfoods_app.model;

/**
 * Created by kenhoang on 25/01/2018.
 */

public class User {
    private String name;
    private String password;
    private String phone;
    private String isStaff;
    private String secureCode;
    private String homeAddress;
    private String balance;
    public User() {
    }

    public User(String name, String password, String secureCode) {
        this.name = name;
        this.password = password;
        this.secureCode = secureCode;
        isStaff = "false";
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getSecureCode() {
        return secureCode;
    }

    public void setSecureCode(String secureCode) {
        this.secureCode = secureCode;
    }

    public String getIsStaff() {
        return isStaff;
    }

    public void setIsStaff(String isStaff) {
        this.isStaff = isStaff;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        this.homeAddress = homeAddress;
    }
}
