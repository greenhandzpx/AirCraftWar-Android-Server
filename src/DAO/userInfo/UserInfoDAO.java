package DAO.userInfo;

import java.util.List;

public interface UserInfoDAO {

    void addUserInfo(UserInfo userInfo);

    void deleteUserInfo(int id);

    List<UserInfo> getAllUserInfo();
}
