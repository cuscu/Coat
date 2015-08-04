package coat.model.poirot.databases;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Instance {


    private final Dataset dataset;
    private final Object[] fields;

    public Instance(Dataset dataset, Object[] fields) {
        this.dataset = dataset;
        this.fields = fields;
    }

    public Object getField(int index) {
        return fields[index];
    }

    public Object getField(String column) {
        return fields[dataset.getPositionOf(column)];
    }
}
