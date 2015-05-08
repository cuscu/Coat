package coat;

import javafx.beans.property.Property;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public abstract class CoatModule {

    private Map<String, Property> properties = new TreeMap<>();

    public void setInput(String name, Property value){
        properties.put(name, value);
        propertyChanged(name, value);
    }

    protected abstract void propertyChanged(String name, Property value);

    public Property getOutput(String name){
        return properties.get(name);
    }
}
