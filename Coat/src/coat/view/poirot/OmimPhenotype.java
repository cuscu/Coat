package coat.view.poirot;

/**
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
class OmimPhenotype implements Comparable<OmimPhenotype> {


    private Integer number;
    private String name;
    private Integer mappingKey;

    public OmimPhenotype(Integer number, String name, Integer mappingKey) {
        this.number = number;
        this.name = name;
        this.mappingKey = mappingKey;
    }

    public Integer getNumber() {
        return number;
    }

    public String getName() {
        return name;
    }

    public int getMappingKey() {
        return mappingKey;
    }

    @Override
    public int compareTo(OmimPhenotype other) {
        final int numberCompare = Integer.compare(number, other.number);
        if (numberCompare != 0) return numberCompare;
        final int nameCompare = name.compareTo(other.name);
        if (nameCompare != 0) return nameCompare;
        return Integer.compare(mappingKey, other.mappingKey);
    }

    @Override
    public boolean equals(Object other) {
        return other.getClass() == OmimPhenotype.class
                && (this == other || compareTo((OmimPhenotype) other) == 0);
    }

    public boolean matches(String text) {
        final String lower = text.toLowerCase();
        return name.toLowerCase().contains(lower) || String.valueOf(number).contains(lower);
    }
}
