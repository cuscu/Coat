/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package coat.model.vep;

import coat.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Web {

    public static String httpRequest(URL url, Map<String, String> properties, JSONObject message) {
//        System.out.println(url);
        try {
            // Create connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            //connection.addRequestProperty("Authorization", API_KEY);
            // Set properties if needed
            if (properties != null && !properties.isEmpty()) {
                properties.forEach(connection::setRequestProperty);
            }
            // Post message
            if (message != null) {
                // Maybe somewhere
                connection.setDoOutput(true);
                connection.setRequestProperty("Accept", "application/json");
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
                    writer.write(message.toString());
                }
            }
            // Establish connection
            connection.connect();
            int responseCode = connection.getResponseCode();

            if (responseCode != 200) {
                System.err.println("Error " + responseCode + ":" + url);
                return null;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                String content = "";
                while ((line = reader.readLine()) != null) {
                    content += line;
                }
                return content;
            }

        } catch (IOException ex) {
            Logger.getLogger(Web.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static JSONObject httpsRequest(URL url, Map<String, String> properties, JSONObject message) {
//        System.out.println(url);
        try {
            // Create connection
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            //connection.addRequestProperty("Authorization", API_KEY);
            // Set properties if needed
            if (properties != null && !properties.isEmpty()) {
                properties.forEach(connection::setRequestProperty);
            }
            // Post message
            if (message != null) {
                // Maybe somewhere
                connection.setDoOutput(true);
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()))) {
                    writer.write(message.toString());
                }
            }
            // Establish connection
            connection.connect();
            int responseCode = connection.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("Response code was not 200. Detected response was " + responseCode);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String line;
                String content = "";
                while ((line = reader.readLine()) != null) {
                    content += line;
                }
                return new JSONObject(content);
            }

        } catch (IOException ex) {
            Logger.getLogger(Web.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

}
