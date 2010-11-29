package de.cismet.cids.tools.search.clientstuff;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.search.CidsServerSearch;
import Sirius.server.search.SearchOption;
import java.util.Collection;
import javax.swing.ImageIcon;

/**
 *
 * @author stefan
 */
public interface CidsSearch {

    //Collection<MetaClass> getPossibleResultClasses();

    CidsServerSearch getServerSearch();

    String getName();

    ImageIcon getIcon();


}
