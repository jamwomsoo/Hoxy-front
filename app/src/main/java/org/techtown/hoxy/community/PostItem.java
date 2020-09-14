
package org.techtown.hoxy.community;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class PostItem  implements Serializable {
    private int resId;
    private String content;
    private String userId;
    private String image;
    private int post_no;
    private String title;
    private String reg_date;

    public PostItem(int resId, String title, String userId, int post_no, String reg_date) {
        this.resId = resId;
        this.content = content;
        this.userId = userId;
        this.post_no = post_no;
        this.title = title;
        this.reg_date = reg_date;
    }
    public PostItem() {
        this.resId = 1;
        this.content = "";
        this.userId = "";
        this.post_no = 1;
        this.title = "";
        this.reg_date = "";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getPost_no() {
        return post_no;
    }

    public void setPost_no(int post_no) {
        this.post_no = post_no;
    }

    public String getReg_date() {
        return reg_date;
    }

    public void setReg_date(String reg_date) {
        this.reg_date = reg_date;
    }

    public JSONObject PostToJSON(){
        JSONObject jsonobj=new JSONObject();
        try {
            jsonobj.put("id",this.getUserId());
            jsonobj.put("content",this.getContent());
            jsonobj.put("resId",this.getResId());
            jsonobj.put("image",this.getImage());
            jsonobj.put("post_no",this.getPost_no());
            jsonobj.put("title",this.getTitle());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonobj;
    }
}