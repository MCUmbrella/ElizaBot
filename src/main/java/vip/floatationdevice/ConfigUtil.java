package vip.floatationdevice;

import java.io.*;
import java.util.Properties;
import java.util.UUID;

/**
 * Manages 'eliza.properties'. The file is stored and read under the working directory.
 */
public class ConfigUtil
{
    static String token, channelId;

    public static void loadConfig() throws InvalidConfigException, ConfigCreationException
    {
        File configFile = new File("." + File.separator + "eliza.properties");

        // create config file if it doesn't exist
        if(!configFile.exists())
        {
            System.err.println("'eliza.properties' not found and a new one will be created.\nFill it with your token and channel ID, then restart the program.");
            try
            {
                // copy the example config to ./config.properties
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
                InputStream is = Main.class.getResourceAsStream("/eliza.properties");
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                BufferedWriter bw = new BufferedWriter(new FileWriter(configFile));
                for(String line; (line = br.readLine()) != null; ) bw.write(line + "\n");
                bw.flush();
                bw.close();
                br.close();
                isr.close();
                is.close();
                System.exit(1);
            }
            catch(Exception e)
            {
                ConfigCreationException cce = new ConfigCreationException();
                cce.initCause(e);
                throw cce;
            }
        }

        // load and check config file
        try
        {
            Properties config = new Properties();
            config.load(new FileInputStream(configFile));
            token = config.getProperty("token");
            channelId = config.getProperty("channelId");
            // check if all required properties are set
            if(token == null || channelId == null || token.isEmpty() || channelId.isEmpty())
                throw new IllegalArgumentException("Null value detected");
            // test if channelId is a valid UUID
            UUID.fromString(channelId);
        }
        catch(Exception e)
        {
            InvalidConfigException ice = new InvalidConfigException();
            ice.initCause(e);
            throw ice;
        }
    }

    public static class ConfigCreationException extends RuntimeException
    {
        public ConfigCreationException(){super("Failed to create config file");}
    }

    public static class InvalidConfigException extends RuntimeException
    {
        public InvalidConfigException(){super("Config file check failed");}
    }
}
