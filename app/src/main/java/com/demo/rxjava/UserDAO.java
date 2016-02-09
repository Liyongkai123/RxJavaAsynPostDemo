package com.demo.rxjava;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kkmike999 on 16/2/9.
 */
public class UserDAO {

    public User parse(JSONObject json) {
        User user = new User();
        user.setUid(json.optInt("userid"));
        user.setName(json.optString("name"));

        return user;
    }

    /**
     * string -> {@linkplain User}
     *
     * @param result
     *
     * @return
     */
    public User parse(String result) {
        try {
            JSONObject userJson = new JSONObject(result).getJSONObject("user");

            return parse(userJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new User();
    }
}
