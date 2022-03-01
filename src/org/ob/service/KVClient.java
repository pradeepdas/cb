package org.ob.service;

import org.ob.Config;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;

public class KVClient {
    public static String getValue(int portNumber, String key) {
        try (
                Socket clientSocket = new Socket(InetAddress.getLocalHost(), portNumber);
        ) {
            Config.LOGGER.log(Level.INFO, "Client: Connected to server at port " + portNumber);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            Config.LOGGER.log(Level.INFO, "Client: writing to server, key: " + key);
            writer.write(key + "\n");
            writer.flush();

            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String val = reader.readLine();
            Config.LOGGER.log(Level.INFO, "Client: Key: " + key + " Value: " + val);
            return val;
        } catch (IOException ioe) {
            throw new RuntimeException("Could not connect to Server " + ioe.getMessage());
        }
    }

    public static void main(String[] args) {
//        int portNumber = Integer.parseInt(args[0]);
        Scanner scanner = new Scanner(new InputStreamReader(System.in));
        System.out.println(getValue(Config.PORT_NUMBER, scanner.nextLine()));
    }
}
