package org.ob;

import java.io.File;
import java.util.logging.Logger;

public class Config {
    public final static int PORT_NUMBER = 9000;
    public final static String basePath = new File("").getAbsolutePath();
    public final static String KV_DATA_FILE_PATH = basePath + "/../kv-data-1000.dat";
    public final static String KV_INDEX_FILE_PATH = basePath + "/../kv-index-1000.dat";

    public static final Logger LOGGER = Logger.getLogger(Config.class.getName());
}
