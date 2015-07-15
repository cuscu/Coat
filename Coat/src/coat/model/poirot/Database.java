package coat.model.poirot;

import java.util.Collection;

/**
 * This interface can be used to access data
 * @author Lorente Arencibia, Pascual (pasculorente@gmail.com)
 */
public interface Database {

    Collection<String> getUnmodifiableHeaders();

    Collection<DatabaseEntry> getUnmodifiableEntries();

}
