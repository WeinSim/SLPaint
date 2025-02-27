package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import sutil.json.JSONParser;
import sutil.json.JSONSerializer;
import sutil.json.values.JSONArray;
import sutil.json.values.JSONObject;

/**
 * This class manages global (user) settings, such as the color theme (base
 * color + dark mode) and the shape of the HueSatField.
 */
public class Settings {

    private static final String SETTINGS_FILE = "res/settings.json";

    static {
        loadSettings();
    }

    private Settings() {
    }

    private static void loadSettings() {
        File file = new File(SETTINGS_FILE);
        if (file.exists()) {
            String buffer = "";
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    buffer += line + "\n";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            JSONObject json = JSONParser.parseObject(buffer);

            System.out.println(JSONSerializer.serialize(json));

            boolean darkMode = json.getBoolean("darkMode", true);
            boolean hueSatCircle = json.getBoolean("hueSatCircle", false);
            JSONArray baseColorArray = json.getArray("baseColor");
            int red = baseColorArray.getInteger(0),
                    green = baseColorArray.getInteger(1),
                    blue = baseColorArray.getInteger(2);
        } else {

        }
    }

    private static class Setting<T> {

        private String name;
        private T value;
    }
}