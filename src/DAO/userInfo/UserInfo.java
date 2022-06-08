package DAO.userInfo;

import java.io.Serializable;

public class UserInfo implements Serializable {

    private final String name;
    private final String password;

    public UserInfo(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }
    public String getPassword() {
        return password;
    }
}
