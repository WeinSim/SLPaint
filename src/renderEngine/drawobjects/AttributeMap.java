package renderEngine.drawobjects;

import java.util.HashMap;

public class AttributeMap {

    private HashMap<Attribute<?, ?>, Object> attributeValues;

    public AttributeMap() {
        attributeValues = new HashMap<>();
    }

    protected <T extends AttributeValue> void set(Attribute<T, ?> attribute, T value) {
        attributeValues.put(attribute, value);
    }

    @SuppressWarnings("unchecked")
    protected <T extends AttributeValue> T get(Attribute<T, ?> attribute) {
        return (T) attributeValues.get(attribute);
    }
}