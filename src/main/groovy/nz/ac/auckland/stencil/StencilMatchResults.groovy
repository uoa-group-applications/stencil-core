package nz.ac.auckland.stencil

/**
 * User: marnix
 * Date: 8/04/13
 * Time: 5:06 PM
 *
 *
 */
class StencilMatchResults {

    /**
     * Page instance that has been matched
     */
    Stencil matchedPage;

    /**
     * A map of URL elements that have been matched
     */
    Map<String, String> pathParameters;
}
