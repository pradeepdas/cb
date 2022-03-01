package org.ob.test;

import org.ob.Config;
import org.ob.service.KVClient;
import org.ob.service.KVServer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class IntegrationTest {
    public static void seqIntTest1() throws IOException, InterruptedException {
        Config.LOGGER.setLevel(Level.SEVERE);
        int portNumber = Config.PORT_NUMBER;
        final File kvDataFile = new File(Config.KV_DATA_FILE_PATH);
        final RandomAccessFile randomAccessKVDataFile = new RandomAccessFile(kvDataFile, "r");
        long fileLength = randomAccessKVDataFile.length();
        new Thread(() -> new KVServer(portNumber)).start();
        Thread.sleep(2000);
        int testCount = 1000;
        int successCount = 0;
        for (int i = 0; i < testCount; i++) {
            // Randomly read a line from KV data
            try {
                String randomLine = null;
                do {
                    final long randomLocation = (long) (Math.random() * fileLength);
                    randomAccessKVDataFile.seek(randomLocation);
                    randomAccessKVDataFile.readLine();
                    randomLine = randomAccessKVDataFile.readLine();
                } while (randomLine == null);
                String[] kv = randomLine.split(" ", 2);
                // Verify
                if (!KVClient.getValue(9000, kv[0]).equals(kv[1])) {
                    Config.LOGGER.log(Level.SEVERE, "Test: Integration test failed for data: " + randomLine);
                    break;
                } else {
                    if (i % 100 == 0) System.out.print("⌛");
                    successCount++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println();
        System.out.println(successCount == testCount ? "\uD83E\uDD42" : "\uD83D\uDE3F");
        randomAccessKVDataFile.close();
        KVClient.getValue(portNumber, "QUIT");
    }

    public static void conIntTest1() throws InterruptedException {
        Config.LOGGER.setLevel(Level.SEVERE);
        int portNumber = Config.PORT_NUMBER;
        new Thread(() -> new KVServer(portNumber)).start();
        Thread.sleep(2000);
        int testCount = 100;
        AtomicInteger successCount = new AtomicInteger();
        Thread[] testThreads = new Thread[testCount];
        for (int i = 0; i < testCount; i++) {
            testThreads[i] = new Thread(() -> {
                // Randomly read a line from KV data
                try {
                    final File kvDataFile = new File(Config.KV_DATA_FILE_PATH);
                    final RandomAccessFile randomAccessKVDataFile = new RandomAccessFile(kvDataFile, "r");
                    long fileLength = randomAccessKVDataFile.length();
                    String randomLine = null;
                    do {
                        final long randomLocation = (long) (Math.random() * fileLength);
                        randomAccessKVDataFile.seek(randomLocation);
                        randomAccessKVDataFile.readLine();
                        randomLine = randomAccessKVDataFile.readLine();
                    } while (randomLine == null);
                    String[] kv = randomLine.split(" ", 2);
                    // Verify
                    if (!KVClient.getValue(9000, kv[0]).equals(kv[1])) {
                        Config.LOGGER.log(Level.SEVERE, "Integration test failed for data: " + randomLine);
                    } else {
                        successCount.getAndIncrement();
                    }
                    randomAccessKVDataFile.close();
//                    Thread.sleep(10);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        for (Thread testThread : testThreads) {
            testThread.start();
            testThread.join();
        }
        System.out.println(successCount.get() == testCount ? "\uD83E\uDD42" : "\uD83D\uDE3F");
        KVClient.getValue(portNumber, "QUIT");
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        seqIntTest1();
        conIntTest1();
    }
}
