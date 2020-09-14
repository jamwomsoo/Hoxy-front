package org.techtown.hoxy.community;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class CommentItem implements Serializable {
    private int resId;
    private String comment;
    private String userId;
    private int comment_no;
    private String reg_date;
    //String time;


    public CommentItem(int resId, String comment, String userId, int comment_no, String reg_date) {
        this.resId = resId;
        this.comment = comment;
        this.userId=userId;
        this.comment_no=comment_no;
        this.reg_date = reg_date;
       // this.time=time;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getComment_no() {
        return comment_no;
    }

    public void setComment_no(int comment_no) {
        this.comment_no = comment_no;
    }

    public String getReg_date() {
        return reg_date;
    }

    public void setReg_date(String reg_date) {
        this.reg_date = reg_date;
    }

    public JSONObject CommentToJSON(){
        JSONObject jsonobj=new JSONObject();
        try {
            jsonobj.put("id",this.getUserId());
            jsonobj.put("comment",this.getComment());
            jsonobj.put("resId",this.getResId());
            jsonobj.put("postNum",this.getComment_no());
            jsonobj.put("teg_date",this.getReg_date());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonobj;
    }
}
