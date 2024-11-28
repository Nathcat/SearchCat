package com.nathcat.searchcat.crawler;

import com.nathcat.searchcat.crawler.exceptions.InvalidConfigException;
import com.nathcat.searchcat.index.SearchIndex;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Main crawler application entry point
 */
public class Main {
    /**
     * The path at which the configuration file should be stored
     */
    public static final String CONFIG_PATH = "Assets/Crawler_Config.json";

    /**
     * Get the configuration file for the crawler application. The config file should have the following format:
     * <pre>
     *     {
     *         "start-url": String
     *     }
     * </pre>
     * @return The JSONObject contained within the config file
     * @throws IOException Thrown if the file cannot be found at <code>CONFIG_PATH</code>, or some other I/O error
     *                     occurs.
     */
    public static JSONObject getConfig() throws IOException {
        try {
            FileInputStream fis = new FileInputStream(CONFIG_PATH);
            String s = new String(fis.readAllBytes());
            fis.close();

            return (JSONObject) new JSONParser().parse(s);
        }
        catch (ParseException e) {
            System.err.println("Failed to read config!");
            e.printStackTrace();
            System.exit(-1);
        }
        catch (IOException e) {
            throw e;
        }

        return null;
    }

    public static void main(String[] args) throws IOException, InvalidConfigException {
        JSONObject config = getConfig();

        String startURL = (String) config.get("start-url");
        if (startURL == null) {
            throw new InvalidConfigException("Required config param \"start-url\" was not found.");
        }

        SearchIndex index = new SearchIndex();
        Crawler crawler = new Crawler(startURL, index);

        // Don't want to run this as a thread right now
        crawler.run();
    }
}
