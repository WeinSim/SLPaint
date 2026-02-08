package main.settings;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import sutil.json.JSONParser;
import sutil.json.JSONSerializer;
import sutil.json.tokens.JSONParseException;
import sutil.json.values.JSONObject;
import sutil.json.values.JSONValue;

/**
 * This class manages global (user) settings, such as the color theme (base
 * color + dark mode) and the shape of the HueSatField.
 * 
 * Potential futre change: allow settings to not only exist at top-level, but
 * nested inside of JSON objects and arrays (see ArraySetting and
 * ObjectSetting). For the time being though, this is not neccessary.
 */
public class Settings {

    private static final String SETTINGS_FILE = "res/settings/settings.json";
    private static final String DEFAULT_SETTINGS_FILE = "res/settings/defaultSettings.json";

    private static JSONObject defaultSettings;
    private static JSONObject currentSettings;

    private static HashMap<String, Setting<?>> allSettings;

    private static SettingsSaveThread saveThread;

    static {
        defaultSettings = loadSettingsFromFile(DEFAULT_SETTINGS_FILE);
        if (defaultSettings == null) {
            System.err.format("Unable to load default settings file (%s)!\n", DEFAULT_SETTINGS_FILE);
            System.exit(1);
        }

        currentSettings = loadSettingsFromFile(SETTINGS_FILE);
        if (currentSettings == null) {
            System.err.println("Unable to load settings. Using default settings instead.");
            currentSettings = new JSONObject(defaultSettings);
        }

        allSettings = new HashMap<>();

        if (saveThread == null) {
            saveThread = new SettingsSaveThread();
            saveThread.start();
        }
    }

    private Settings() {
    }

    public static void addSetting(String identifier, Setting<?> setting) {
        if (allSettings.get(identifier) != null) {
            throw new RuntimeException(String.format("Duplicate setting %s!", identifier));
        }
        allSettings.put(identifier, setting);

        updateSetting(identifier, setting);
    }

    private static void updateSetting(String identifier, Setting<?> setting) {
        JSONValue jsonValue = currentSettings.get(identifier);
        if (jsonValue == null) {
            jsonValue = defaultSettings.get(identifier);
            if (jsonValue == null) {
                throw new RuntimeException(String.format("The setting named %s does not exist!", identifier));
            }
        }
        setting.setJSONValue(jsonValue);
    }

    public static void loadDefaultSettings() {
        defaultSettings = loadSettingsFromFile(DEFAULT_SETTINGS_FILE);
        currentSettings = new JSONObject(defaultSettings);

        for (Entry<String, Setting<?>> entry : allSettings.entrySet()) {
            updateSetting(entry.getKey(), entry.getValue());
        }
    }

    private static JSONObject loadSettingsFromFile(String path) {
        File file = new File(path);
        if (!file.exists())
            return null;

        String buffer = "";
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                buffer += line + "\n";
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject json = null;
        try {
            json = JSONParser.parseObject(buffer);
        } catch (JSONParseException e) {
            e.printStackTrace();
        }

        return json;
    }

    public static void finish() {
        if (saveThread != null) {
            saveThread.shouldStop = true;
            saveThread.interrupt();
        }
    }

    private static String generateJSONString() {
        LinkedHashMap<String, JSONValue> jsonValues = new LinkedHashMap<>();
        for (String identifier : allSettings.keySet()) {
            Setting<?> setting = allSettings.get(identifier);
            jsonValues.put(identifier, setting.getJSONValue());
        }

        JSONObject json = new JSONObject(jsonValues);
        String jsonString = JSONSerializer.serialize(json);
        return jsonString;
    }

    private static class SettingsSaveThread extends Thread {

        private static final long SLEEP_TIME = 5000;

        private String lastJSONString = null;

        private boolean shouldStop = false;

        @Override
        public void run() {
            while (!shouldStop) {
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    // e.printStackTrace();
                }

                String newJSONString = generateJSONString();

                if (newJSONString.equals(lastJSONString)) {
                    continue;
                }

                save(newJSONString);

                lastJSONString = newJSONString;
            }
        }

        synchronized void save(String json) {
            File file = new File(SETTINGS_FILE);
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(json);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}