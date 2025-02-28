package main.settings;

import sutil.json.values.JSONArray;
import sutil.json.values.JSONValue;
import sutil.math.SVector;

public final class ColorSetting extends Setting<SVector> {

    public ColorSetting(String identifier) {
        super(identifier);
    }

    @Override
    public JSONValue getJSONValue() {
        return getJSONValueFromColor(value);
    }

    @Override
    public void setJSONValue(JSONValue value) {
        this.value = getColorFromJSONValue(value);
    }

    public static JSONValue getJSONValueFromColor(SVector color) {
        JSONArray array = new JSONArray();
        array.add((int) Math.round(color.x * 255));
        array.add((int) Math.round(color.y * 255));
        array.add((int) Math.round(color.z * 255));
        return array;
    }

    public static SVector getColorFromJSONValue(JSONValue value) {
        if (value instanceof JSONArray array) {
            int r = array.getInteger(0, 0);
            int g = array.getInteger(1, 0);
            int b = array.getInteger(2, 0);
            return new SVector(r, g, b).div(255);
        } else {
            handleIncorrectJSONType("JSONArray", value.getClass().getName());
            return null;
        }
    }
}