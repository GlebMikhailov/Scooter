package com.development.sota.scooter.api.client;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserClass {
    public void UserClass() {

    }

    @SerializedName("client_name")
    @Expose
   private transient String  name;
   String phone;
  String balance;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

// public UserClass(String name, String phone, String balance) {
//        this.name = name;
//        this.phone = phone;
//        this.balance = balance;
//    }
}
