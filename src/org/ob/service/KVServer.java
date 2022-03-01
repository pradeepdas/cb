package org.ob.service;

import org.ob.Config;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.stream.Stream;

public class KVServer {
    private volatile boolean serverStopped = false;
    private static ServerSocket serverSocket = null;
    private final static ExecutorService threadPool = Executors.newCachedThreadPool();
    private final static HashMap<String, Integer> kvIndex = new HashMap<>();

    public KVServer(int portNumber) {
        RandomAccessFile raf = null;
        try {
            buildIndex();
            raf = new RandomAccessFile(Config.KV_DATA_FILE_PATH, "r");
        } catch (FileNotFoundException fnfe) {
            Config.LOGGER.log(Level.SEVERE, "Unable to build index: " + fnfe.getMessage());
            return;
        }
        try {
            serverSocket = new ServerSocket(portNumber);
            serverSocket.setSoTimeout(0);
            Config.LOGGER.log(Level.INFO, "Started server at port " + portNumber);
        } catch (IOException ioe) {
            Config.LOGGER.log(Level.SEVERE, "Unable to start the Server at port "
                    + portNumber + " " + ioe.getMessage());
            return;
        }
        while(!serverStopped) {
            try {
                Config.LOGGER.log(Level.INFO, "\nWaiting for client connections...");
                Socket clientSocket = serverSocket.accept();

                RandomAccessFile finalRaf = raf;
                new Thread(() -> {
                    try {
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        String key = bufferedReader.readLine();
                        Config.LOGGER.log(Level.INFO, "Server:: Got key from client " + key);
                        String val = "NOT FOUND";
                        if (key != null) {
                            if (key.equals("QUIT")) {
                                Config.LOGGER.log(Level.INFO, "Got Server Quit request");
                                this.shutDownServer();
                            } else {
                                int dataOffset = kvIndex.getOrDefault(key, -1);
                                if (dataOffset >= 0) {
                                    finalRaf.seek(dataOffset);
                                    String[] data = finalRaf.readLine().split(" ", 2);
                                    if (key.equals(data[0])) {
                                        val = data[1];
                                    }
                                }
                            }

                            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                            bufferedWriter.write(val + "\n");
                            bufferedWriter.flush();
                            Config.LOGGER.log(Level.INFO, "Server: Returned Key=" + key + " Val=" + val);
                        }
                    } catch (Exception e) {
                        Config.LOGGER.log(Level.INFO, e.getMessage());
                    }
                }).start();
            } catch (Exception e) {
                Config.LOGGER.log(Level.SEVERE, e.getMessage());
            }
        }
    }

    public synchronized void shutDownServer() {
        try {
            Config.LOGGER.log(Level.INFO, "Shutting down the Server");
            serverStopped = true;
            if(serverSocket != null)
                serverSocket.close();
            threadPool.shutdown();
            Config.LOGGER.log(Level.INFO, "Successfully shut down the Server");
        } catch (IOException ioe) {
            Config.LOGGER.log(Level.SEVERE, ioe.getMessage());
        }
    }

    private void buildIndex() {
        try {
            File indexFile = new File(Config.KV_INDEX_FILE_PATH);
            if (!indexFile.exists()) {
                Config.LOGGER.log(Level.INFO,"Building index...");
                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(Config.KV_INDEX_FILE_PATH), 'w');
                RandomAccessFile raf = new RandomAccessFile(Config.KV_DATA_FILE_PATH, "r");
                long pointer = 0L;
                raf.seek(pointer);
                String line;
                int count = 0;
                while ((line = raf.readLine()) != null) {
                    count++;
                    if (count % 100000 == 0)
                        Config.LOGGER.log(Level.INFO,"Processed " + count + " of one million lines");
                    int i = line.indexOf(' ');
                    if (i > 0) {
                        String key = line.substring(0, i);
                        bufferedWriter.write(key + " " + pointer);
                        bufferedWriter.newLine();
                    }
                    pointer = raf.getFilePointer();
                }
                bufferedWriter.close();
                raf.close();
            }
            Config.LOGGER.log(Level.INFO, "Reading the Index File...");
            Stream<String> indexLines = Files.lines(Paths.get(Config.KV_INDEX_FILE_PATH));
            indexLines.forEach(indexLine -> {
                String[] kv = indexLine.split(" ", 2);
                kvIndex.put(kv[0], Integer.valueOf(kv[1]));
            });
        } catch (Exception e) {
            Config.LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    public static void main(String[] args) {
//        if (args.length < 1) {
//            System.out.println("USAGE: java KVServer <port-number>");
//        }
//        int portNumber = Integer.parseInt(args[0]);
        new KVServer(Config.PORT_NUMBER);
    }
}
