package server;


import DAO.userInfo.FileUserInfoDAOImpl;
import DAO.userInfo.UserInfo;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class MyServer {
    public static void main(String[] args){
        new MyServer();
//        FileUserInfoDAOImpl fileUserInfoDAO = new FileUserInfoDAOImpl();
//        fileUserInfoDAO.addUserInfo(new UserInfo("www", "www"));
//        List<UserInfo> userInfoList = fileUserInfoDAO.getAllUserInfo();
//        for (UserInfo userInfo: userInfoList) {
//            System.out.println(userInfo.getName() + userInfo.getPassword());
//        }
    }

    private final int allowUsers = 2;
    /**
     * 当前在线的人数
      */
    private int users = 0;
    /**
     *
     */
    private String difficulty;


    private final Object lockScore1 = "";
    private final Object lockScore2 = "";

    private String score1;
    private String score2;

    private boolean score1Changed = false;
    private boolean score2Changed = false;

    private final Object lockDifficulty = "";

    private final Object lockUsers = "";

    private int chooseDifficultyCount = 0;

    private final int port = 22222;
    public  MyServer(){
        try{
            InetAddress addr = InetAddress.getLocalHost();
            System.out.println("local host:" + addr);

            //创建server socket
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("listen port "+port);

            while(true){
                System.out.println("waiting client connect");
                Socket socket = serverSocket.accept();
                System.out.println("accept client connect" + socket);
                new Thread(new Service(socket)).start();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    enum State {
        Ready,
        Unready,
        Disconnect
    }

    class Service implements Runnable{
        private final Socket socket;
        private BufferedReader in = null;

        private State state = State.Unready;

        private int id;

        public Service(Socket socket){
            this.socket = socket;
            try{
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            }catch (IOException ex){
                ex.printStackTrace();
            }
        }

        @Override
        public void run() {
            System.out.println("wait client message " );
            boolean startGame = false;
            try {
                String content;
                while ((content = in.readLine()) != null) {
                    System.out.println("message from client:"+ content);
                    switch (content) {
                        case "bye":
                            if (state.equals(State.Ready)) {
                                --users;
                            }
                            System.out.println("disconnect from client,close socket");
                            socket.shutdownInput();
                            socket.shutdownOutput();
                            socket.close();
                            break;

                        case "login":
//                            sendMessage("Login Success.");
//                            break;
                            boolean chooseRegister = false;
//                            // 用户发来登录请求，等待用户接下来发送的用户名和密码
                            while ((content = in.readLine()) != null) {
                                // 客户用户名和密码用空格隔开
                                String[] userAndPwd = content.split(" ");
                                if (userAndPwd.length == 1 && "register".equals(userAndPwd[0])) {
                                    chooseRegister = true;
                                    System.out.println("message from client:register");
                                    break;
                                } else {
                                    if (userAndPwd.length != 2) {
                                        System.out.println("username and password's length not valid");
                                        continue;
                                    }
                                    String username = userAndPwd[0];
                                    String password = userAndPwd[1];
                                    System.out.println("username:"+username);
                                    System.out.println("password:"+password);
                                    if (!Login.validate(username, password)) {
                                        // 验证不通过
                                        sendMessage("Login Fail.");
                                    } else {
                                        sendMessage("Login Success.");
                                        break;
                                    }
                                }
                            }
                            if (!chooseRegister) {
                                break;
                            }

                        case "register":
                            // 用户发来注册请求，等待用户接下来发送的用户名和密码
                            while ((content = in.readLine()) != null) {
                                // 客户用户名和密码用空格隔开
                                String[] userAndPwd = content.split(" ");
                                if (userAndPwd.length != 2) {
                                    break;
                                }
                                String username = userAndPwd[0];
                                String password = userAndPwd[1];

                                if (Register.register(username, password)) {
                                    // 验证不通过
                                    sendMessage("Register Success.");
                                    break;
                                } else {
                                    sendMessage("Register Fail.");
                                }
                            }
                            break;

                        case "ready":
                            if (users == 0) {
                                id = 0;
                            } else {
                                id = 1;
                            }
                            ++users;
                            state = State.Ready;
                            startGame = true;
                            ready();
                        default:
                    }
                    if (startGame) {
                        break;
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        /**
         * waiting for another user to join
         */
        private void ready() {
            // check whether there are 2 users online.
            synchronized (lockUsers) {
                while (users != allowUsers) {
                    try {
                        lockUsers.wait();
//                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                lockUsers.notify();
            }
            start();
        }

        /**
         * Two users are online. Start choosing difficulty
          */
        private void start() {

            sendMessage("start");
            try {
                // 等待两个用户发来他们的难度选择信息
                String reply;
                while ((reply = in.readLine()) != null) {
                    // TODO
                    if (!"easy".equals(reply) && !"normal".equals(reply) && !"hard".equals(reply)) {
                        continue;
                    }
                    // 以先选的那个用户的难度为准
                    synchronized (lockDifficulty) {
                        chooseDifficultyCount++;
                        if (difficulty == null) {
                            difficulty = reply;
                        }
                        break;
                    }
                }

                synchronized (lockUsers) {
                    while (chooseDifficultyCount != allowUsers) {
                        try {
                            lockUsers.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    lockUsers.notify();
                }

                sendMessage(difficulty);

            } catch (IOException e) {
                e.printStackTrace();
            }
            // 正式开始游戏
            game();
        }

        /**
         * After choosing difficulty, start playing!
         */
        private void game() {
            System.out.println("start game!");
            new Thread(this::receiveScore).start();
            new Thread(this::forwardScore).start();
        }

        private void receiveScore() {
            try {
                String reply;
                while ((reply = in.readLine()) != null) {
                    if (id == 0) {
                        synchronized (lockScore1) {
                            score1 = reply;
                            score1Changed = true;
                            lockScore1.notify();
                        }
                    } else {
                        synchronized (lockScore2) {
                            score2 = reply;
                            score2Changed = true;
                            lockScore2.notify();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void forwardScore() {
            while (true) {
                if (id == 0) {
                    synchronized (lockScore2) {
                        while (!score2Changed) {
                            try {
                                lockScore2.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        sendMessage(score2);
                        score2Changed = false;
                    }
                } else {
                    synchronized (lockScore1) {
                        while (!score1Changed) {
                            try {
                                lockScore1.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        sendMessage(score1);
                        score1Changed = false;
                    }
                }

            }
        }

        public void sendMessage(String message) {
            PrintWriter pout;
            try{
                System.out.println("message to client:" + message);
                pout = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream())),true);
                pout.println(message);
            }catch (IOException ex){
                ex.printStackTrace();
            }

    }
}