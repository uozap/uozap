package uozap.server;

import uozap.auth.services.AuthService;
import uozap.auth.services.TokenService;
import uozap.auth.services.UserService;

/**
 * main server class that initializes and starts all required services.
 * handles the startup sequence and service dependencies.
 */
public class ServerMain {

    public static final String RESET = "\033[0m";
    public static final String GREEN = "\033[0;32m";
    public static final String RED = "\033[0;31m";
    public static final String YELLOW = "\033[0;33m";
    public static final String CYAN = "\033[0;36m";

    public static void main(String[] args) {

        System.out.println(CYAN + "Server starting..." + RESET);

        System.out.println(YELLOW + "Creating services:" + RESET);

        System.out.print("- token service: ");
        TokenService ts = new TokenService();
        System.out.println(GREEN + "DONE" + RESET);

        System.out.print("- user service: ");
        UserService us = new UserService();
        System.out.println(GREEN + "DONE" + RESET);

        System.out.print("- auth service: ");
        AuthService as = new AuthService(us, ts);
        System.out.println(GREEN + "DONE" + RESET);

        System.out.print("- socket manager: ");
        SocketManager sm = new SocketManager(as);
        System.out.println(GREEN + "DONE" + RESET);

        new Thread(() -> {
            try {
                sm.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        System.out.println(GREEN + "server is up and running." + RESET);

        try {
        Thread.currentThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

