package org.techtown.hoxy.waste;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

public class ApplyInfo implements Serializable {
    private String user_No;
    private int apply_fee;
    private String address;
    private String phone_No;
    private int waste_No=0;
    private String img_Url="";
    private String apply_date;
    private String user_name;
    private WasteInfoItem apply_waste_item;

    private ArrayList<WasteInfoItem> apply_waste_list;
    public ApplyInfo(){
    }
    public ApplyInfo(String user_No, int apply_fee, String address, String phone_No, int waste_No, String img_Url,String apply_date,String user_name) {
        this.user_No = user_No;
        this.apply_fee = apply_fee;
        this.address = address;
        this.phone_No = phone_No;
        this.waste_No = waste_No;
        this.img_Url = img_Url;
        this.apply_date=apply_date;
        this.user_name= user_name;
    }

    public ArrayList<WasteInfoItem> getApply_waste_list() {return apply_waste_list;}

    public void setApply_waste_list(WasteInfoItem waste_item) {apply_waste_list.add(waste_item);}

    public String getUser_No() {
        return user_No;
    }

    public int getApply_fee() {
        return apply_fee;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone_No() {
        return phone_No;
    }

    public int getWaste_No() {
        return waste_No;
    }

    public String getImg_Url() {
        return img_Url;
    }

    public void setUser_No(String user_No) {
        this.user_No = user_No;
    }

    public void setApply_fee(int apply_fee) {
        this.apply_fee = apply_fee;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPhone_No(String phone_No) {
        this.phone_No = phone_No;
    }

    public void setWaste_No(int waste_No) {
        this.waste_No = waste_No;
    }

    public void setImg_Url(String img_Url) {
        this.img_Url = img_Url;
    }

    public String getApply_date() { return apply_date; }

    public void setApply_date(String apply_date) { this.apply_date = apply_date; }
    public JSONObject applyInfoToJSON(){
        JSONObject jsonobj=new JSONObject();

        try {
            jsonobj.put("user_no",this.getUser_No());
            jsonobj.put("apply_fee",this.getApply_fee());
            jsonobj.put("address",this.getAddress());
            jsonobj.put("phone_no",this.getPhone_No());
            jsonobj.put("waste_no",this.getWaste_No());
            jsonobj.put("img_url",this.getImg_Url());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonobj;
    }
    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }
}