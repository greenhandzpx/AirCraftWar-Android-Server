package server;

import DAO.userInfo.FileUserInfoDAOImpl;
import DAO.userInfo.UserInfo;

import java.util.List;

/**
 * @author greenhandzpx
 */
public class Login {

    public static boolean validate(String username, String password) {

        FileUserInfoDAOImpl userInfoDAO = new FileUserInfoDAOImpl();

        List<UserInfo> userInfoList = userInfoDAO.getAllUserInfo();

        for (UserInfo userInfo: userInfoList) {
            if (userInfo.getName().equals(username) &&
                    userInfo.getPassword().equals(password)) {
                return true;
            }
        }
        return false;
    }
}


