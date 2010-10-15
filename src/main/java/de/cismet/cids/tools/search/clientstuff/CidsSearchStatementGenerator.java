package de.cismet.cids.tools.search.clientstuff;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.search.SearchOption;
import java.util.Collection;
import java.util.Map;

/**
 *
 * @author stefan
 */
public interface CidsSearchStatementGenerator {

    Collection<MetaClass> getPossibleResultClasses();

    Collection<SearchOption> getParameterizedSearchStatement(Map<String, Object> parameterMap);
}
