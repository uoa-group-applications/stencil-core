package nz.ac.auckland.stencil

import nz.ac.auckland.common.stereotypes.UniversityComponent
import org.apache.jasper.runtime.TldScanner

import javax.annotation.PostConstruct
import java.lang.reflect.Field

/**
 * Author: *restrictedinformation*
 *
 * This class actually breaks open a class to fix its logic. I'm not proud of this.
 */
@UniversityComponent
class TldScannerFix {

    private static final String FIELD_SYSTEM_URIS = "systemUris"

    /**
     * Post construct will break open tld scanner and remove the jstl/jsp/core URI
     * from the ones that are supposed to be skipped, 'cause it shouldn't be skipped.
     */
    @PostConstruct
    public void breakOpenTldScannerAndQuoteUnquoteFixIt() {
        Field systemUrisField = TldScanner.class.getDeclaredField(FIELD_SYSTEM_URIS)

        // temporarily make "public"
        systemUrisField.setAccessible(true);

        // set class field to empty set
        systemUrisField.set(null, new HashSet<String>());

        // set back to "private"
        systemUrisField.setAccessible(false);

    }
}
