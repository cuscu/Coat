package coat.model.poirot;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public class Path {
    private String gene;
    private String destiny;

    public Path(String gene, String destiny) {
        this.gene = gene;
        this.destiny = destiny;
    }

    @Override
    public String toString() {
        return gene + "-" + destiny;
    }
}
