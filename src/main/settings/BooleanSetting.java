package main.settings;

import sutil.json.values.JSONBoolean;
import sutil.json.values.JSONValue;

public final class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String identifier) {
        super(identifier);
    }

    @Override
    public JSONValue getJSONValue() {
        return new JSONBoolean(value);
    }

    @Override
    public void setJSONValue(JSONValue value) {
        if (value instanceof JSONBoolean b) {
            this.value = b.getValue();
        } else {
            handleIncorrectJSONType("JSONBoolean", value.getClass().getName());
        }
    }
}