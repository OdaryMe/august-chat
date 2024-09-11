package ru.geekbrains.august_chat.chat_server.server;

import ru.geekbrains.august_chat.chat_server.error.WrongCredentialsException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

public class ClientHandler {

    private Socket socket;
    private DataOutputStream out;
    private DataInputStream in;
    private Thread handlerThread;
    private Server server;
    private String user;

    int isAutorized = 0;
    long authTimeout = 30000;

    public ClientHandler(Socket socket, Server server) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            System.out.println("Handler created");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handle() {
        handlerThread = new Thread(() -> {
            authorize();
            while (!Thread.currentThread().isInterrupted() && socket.isConnected()) {
                try {
                    var message = in.readUTF();
                    handleMessage(message);  //
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        handlerThread.start();
    }

    private void handleMessage(String message) { //после второго кейса списано с урока, но методы не создавала
        var splitMessage = message.split(Server.REGEX);
        try {
            switch (splitMessage[0]) {
                case "/broadcast" :
                    server.broadcastMessage(user, splitMessage[1]);
                    break;
                case "/private" :
                    server.privateMessage(user, splitMessage[1], splitMessage[2]);
                    break;
                case "/change_nick" :
                    String nick = server.getAuthService().changeNick(this.user, splitMessage[1]);
                    server.removeAuthorizedClientFromList(this);
                    this.user = nick;
                    server.addAuthorizedClientToList(this);
                    send("/change_nick_ok");
                    break;
                case "/change_pass" :
                    server.getAuthService().changePassword(this.user, splitMessage[1], splitMessage[2]);
                    send("/change_pass_ok");
                    break;
                case "/remove" :
                    server.getAuthService().deleteUser(splitMessage[1], splitMessage[2]);
                    this.socket.close();
                    break;
                case "/register" :
                    server.getAuthService().createNewUser(splitMessage[1], splitMessage[2], splitMessage[3]);
                    send("/register_ok");
                    break;
            }
        } catch (IOException e) {
            send("/error" + server.REGEX + e.getMessage());
        }
    }


    private void authTimer() {
        var timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isAutorized == 0) {
                    var response = "Too long time for connection";
                    send("/error" + Server.REGEX + response);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, authTimeout);
    }


    private void authorize() {
        authTimer();
        while (true) {
            try {
                var message = in.readUTF();
                if (message.startsWith("/auth")) {
                    var parsedAuthMessage = message.split(Server.REGEX);
                    var response = "";
                    String nickname = null;
                    try {
                        nickname = server.getAuthService().authorizeUserByLoginAndPassword(parsedAuthMessage[1], parsedAuthMessage[2]);
                    } catch (WrongCredentialsException e) {
                        response = "/error" + Server.REGEX + e.getMessage();
                        System.out.println("Wrong credentials, nick " + parsedAuthMessage[1]);
                    }

                    if (server.isNickBuzy(nickname)) {
                        response = "/error" + Server.REGEX + "this client is already connected";
                        System.out.println("Nick is busy " + nickname);
                    }
                    if (!response.equals("")) {
                        send(response);
                    } else {
                        this.user = nickname;
                        server.addAuthorizedClientToList(this);
                        send("/auth_ok" + Server.REGEX + nickname);
                        isAutorized = 1;
                        break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void send(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Thread getHandlerThread() {
        return handlerThread;
    }

    public String getUserNick() {
        return this.user;
    }
}
