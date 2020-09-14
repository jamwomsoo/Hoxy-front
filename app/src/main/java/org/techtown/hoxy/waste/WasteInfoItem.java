package org.techtown.hoxy.waste;



import java.io.Serializable;

public class WasteInfoItem  implements Serializable {

    private String waste_name;
    private String waste_size;
    private int waste_fee;
    private int waste_No;
    private String img_Url;

    public WasteInfoItem(String waste_name, String waste_size, int waste_fee, int waste_No) {
        this.waste_name = waste_name;
        this.waste_size = waste_size;
        this.waste_fee = waste_fee;
        this.waste_No = waste_No;
    }

    public String getImg_Url() {
        return img_Url;
    }
    public void setImg_Url(String img_Url) {
        this.img_Url = img_Url;
    }

    public String getWaste_name() {
        return waste_name;
    }

    public String getWaste_size() {
        return waste_size;
    }

    public int getWaste_fee() {
        return waste_fee;
    }

    public int getWaste_No() {
        return waste_No;
    }

    public void setWaste_name(String waste_name) {
        this.waste_name = waste_name;
    }

    public void setWaste_size(String waste_size) {
        this.waste_size = waste_size;
    }

    public void setWaste_fee(int waste_fee) {
        this.waste_fee = waste_fee;
    }

    public void setWaste_No(int waste_No) {
        this.waste_No = waste_No;
    }

}
