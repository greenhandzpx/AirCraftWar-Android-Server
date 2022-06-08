package DAO.userInfo;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUserInfoDAOImpl implements UserInfoDAO {

    private final String fileName = "userInfo";

    public FileUserInfoDAOImpl() {
//        this.fileName = fileName;
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    System.out.println("成功创建一份游戏记录档案");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void addUserInfo(UserInfo userInfo) {
        try {
            List<UserInfo> userInfoList = getAllUserInfo();
            userInfoList.add(userInfo);
            writeToFile(userInfoList);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void deleteUserInfo(int id) {

    }
    @Override
    public List<UserInfo> getAllUserInfo() {
        try {
            return readFromFile();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void writeToFile(List<UserInfo> userInfoList) throws IOException {
        FileOutputStream fout = null;
        {
            try {
                fout = new FileOutputStream(fileName);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        for (UserInfo userInfo: userInfoList) {
            oos.writeObject(userInfo);
            System.out.println("write an object");
            oos.flush();
        }
        // 结束标志，辅助读取
        oos.writeObject(null);
        assert fout != null;
        fout.close();
        oos.close();
    }

    public List<UserInfo> readFromFile() throws IOException, ClassNotFoundException {
        List<UserInfo> res = new ArrayList<>();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ObjectInputStream ois = new ObjectInputStream(fis);

        UserInfo userInfo;
        while ((userInfo = (UserInfo) ois.readObject()) != null){
            res.add(userInfo);
        }
        assert fis != null;
        fis.close();
        ois.close();
        return res;
    }

}
