package org.techtown.hoxy.login;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    private String user_Nickname;
    private String user_Id;

    public User(String id, String nickname) {
        this.user_Nickname=nickname;
        this.user_Id=id;
    }

    public void setUserNickname(String nickname){
        this.user_Nickname=nickname;
    }
    public void setUserId(String id){
        this.user_Id=id;
    }
    public String getUserNickname(){
        return this.user_Nickname;
    }
    public String getUserId(){
        return this.user_Id;
    }

    public JSONObject userToJSON(){
        JSONObject jsonobj=new JSONObject();
        try {
            jsonobj.put("user_info_id",this.getUserId());
            jsonobj.put("user_info_name",this.getUserNickname());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonobj;
    }

}
