package server;

import DAO.userInfo.FileUserInfoDAOImpl;
import DAO.userInfo.UserInfo;

import java.util.List;

/**
 * @author greenhandzpx
 */
public class Register {

    public static boolean register(String username, String password) {
        if (username.contains(" ")) {
            // 用户名不能包含空格
            return false;
        }
        FileUserInfoDAOImpl userInfoDAO = new FileUserInfoDAOImpl();

        List<UserInfo> userInfoList = userInfoDAO.getAllUserInfo();
        if (userInfoList != null) {
            for (UserInfo userInfo: userInfoList) {
                if (userInfo.getName().equals(username)) {
                    // 用户名已存在
                    return false;
                }
            }
        }
        userInfoDAO.addUserInfo(new UserInfo(username, password));
        return true;
    }
}
