package ru.geekbrains.august_chat.chat_server.server;

import ru.geekbrains.august_chat.chat_server.auth.AuthService;

import javax.lang.model.type.ArrayType;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Server {
    public static final String REGEX = "%!%";
    private static final int PORT = 8189;
    private AuthService authService;
    private List<ClientHandler> clientHandlers;

    public Server(AuthService authService) {
        this.clientHandlers = new ArrayList<>();
        this.authService = authService;
    }

    public void start() {
        try(ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server start");
            while (true) {
                System.out.println("Waiting for connection...");
                Socket socket = serverSocket.accept();
                System.out.println("Client connected");
                ClientHandler clientHandler = new ClientHandler(socket, this);
//                clientHandlers.add(clientHandler); //убрали, потому что сначала надо авторизоваться
                clientHandler.handle();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            authService.stop();
            shutdown();
        }
    }

    public void privateMessage(String from, String recipient, String message) {
        message = "/private" + REGEX + from + REGEX + recipient + REGEX + message;
        for (int i = 0; i < clientHandlers.size(); i++) {
            var handler = clientHandlers.get(i);
            var name = handler.getUserNick();
            if (name.equals(recipient)) {
                handler.send(message);
                break;
            }
        }
    }


    public void broadcastMessage(String from, String message) {
        message = "/broadcast" + REGEX + from + REGEX + message;
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.send(message);
        }
    }

    public synchronized void addAuthorizedClientToList(ClientHandler clientHandler) {
        clientHandlers.add(clientHandler);
        sendOnlineClients();
    }

    public synchronized void removeAuthorizedClientToList(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        sendOnlineClients();
    }

    public void sendOnlineClients() {
        var sb = new StringBuilder("/list");
        sb.append(REGEX);
        for (ClientHandler clientHandler : clientHandlers) {
            sb.append(clientHandler.getUserNick());
            sb.append(REGEX);
        }
        var message = sb.toString();
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.send(message);
        }
    }

    public synchronized boolean isNickBuzy(String nick) {
        for (ClientHandler clientHandler : clientHandlers) {
            if(clientHandler.getUserNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    private void shutdown() {

    }

    public AuthService getAuthService() {
        return authService;
    }
}
