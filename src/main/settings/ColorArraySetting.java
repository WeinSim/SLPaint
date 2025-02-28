package main.settings;

import main.ColorArray;
import main.apps.MainApp;
import sutil.json.values.JSONArray;
import sutil.json.values.JSONObject;
import sutil.json.values.JSONValue;
import sutil.math.SVector;

public final class ColorArraySetting extends Setting<ColorArray> {

    public ColorArraySetting(String identifier) {
        super(identifier);
    }

    @Override
    public JSONValue getJSONValue() {
        JSONArray array = new JSONArray();
        for (int i = 0; i < value.getLength(); i++) {
            Integer color = value.getColor(i);
            if (color == null) {
                break;
            }
            SVector colorVec = MainApp.toSVector(color);
            array.add(ColorSetting.getJSONValueFromColor(colorVec));
        }

        JSONObject object = new JSONObject();
        object.put("capacity", value.getLength());
        object.put("values", array);

        return object;
    }

    @Override
    public void setJSONValue(JSONValue value) {
        if (value instanceof JSONObject object) {
            int capacity = object.getInteger("capacity", 10);
            this.value = new ColorArray(capacity);

            JSONArray array = object.getArray("values");
            for (int i = array.size() - 1; i >= 0; i--) {
                SVector colorVec = ColorSetting.getColorFromJSONValue(array.get(i));
                int color = MainApp.toInt(colorVec);
                this.value.addColor(color);
            }
        } else {
            handleIncorrectJSONType("JSONObject", value.getClass().getName());
        }
    }
}