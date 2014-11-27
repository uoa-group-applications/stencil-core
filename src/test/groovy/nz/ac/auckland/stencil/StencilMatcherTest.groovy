package nz.ac.auckland.stencil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Author: Marnix
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
public class StencilMatcherTest {


    @Inject private ApplicationContext applicationContext;


    @Test(expected=RuntimeException)
    public void ensureDuplicateDefaultPagesAreDetected() {
        StencilMatcher pm = new StencilMatcher()
        pm.pages = [new P2(), new P2()]
        pm.keepAnnotatedPageInstances()
    }

    /**
     * Test that only the annotated pages are stored
     */
    @Test
    public void testKeepAnnotatedPageInstances() {
        StencilMatcher pageMatcher = applicationContext.getBean(StencilMatcher.class);

        assert pageMatcher.pages.size() == 4
        assert pageMatcher.defaultPageUrl == '/test/test'

        List<Class<?>> pageTypes = pageMatcher.pages.collect { it.class }
        assert P1.class in pageTypes
        assert P2.class in pageTypes
        assert P3.class in pageTypes
        assert !(P4WithoutAnnotation.class in pageTypes)
    }

    /**
     * test the ordering by specificity, most specific gets sorted to top of list
     */
    @Test
    public void testSpecificityOrdering() {
        StencilMatcher pageMatcher = applicationContext.getBean(StencilMatcher.class);

        List<Stencil> ordered = pageMatcher.orderPagesBySpecificity("/test/test")
        assert ordered[0] instanceof P2
        assert ordered[1] instanceof P1
        assert ordered[3] instanceof P3
    }


    @Test
    public void testMatchResultsIfMatches() {
        StencilMatcher pageMatcher = applicationContext.getBean(StencilMatcher.class);

        StencilMatchResults pmResults = null


        pmResults = pageMatcher.provideMatchResultsIfMatches(
                                    pageMatcher.pathMatcherInstance, new P1(), "/test/My+Content+Match"
                                )

        assert pmResults != null
        assert pmResults.matchedPage instanceof P1
        assert pmResults.pathParameters['myurl'] == "My+Content+Match"


        pmResults = pageMatcher.provideMatchResultsIfMatches(
                pageMatcher.pathMatcherInstance, new P1(), "/testing/My+Content+Match"
        )

        assert pmResults == null

    }


    @Test
    public void testGetPagePattern() {
        StencilMatcher pageMatcher = applicationContext.getBean(StencilMatcher.class);

        assert pageMatcher.getPagePattern(new P1()) == "/test/{myurl}"
        assert pageMatcher.getPagePattern(new P2()) == "/test/test"
        assert pageMatcher.getPagePattern(new P3()) == "/**"
        assert pageMatcher.getPagePattern(new P4WithoutAnnotation()) == null
    }

    @Test
    public void testFindMatchFor() {
        StencilMatcher pageMatcher = applicationContext.getBean(StencilMatcher.class);
        StencilMatchResults pmResults = null

        assert pageMatcher.findMatchFor("") == null

        // testing from least specific to most specific for optimal result

        // wildcard test
        pmResults = pageMatcher.findMatchFor("/this/is/my/url")
        assert pmResults != null
        assert pmResults.pathParameters.isEmpty()
        assert pmResults.matchedPage instanceof P3

        // wildcard match on part of the url
        pmResults = pageMatcher.findMatchFor("/test/mytesturl")
        assert pmResults != null
        assert pmResults.pathParameters['myurl'] == 'mytesturl'
        assert pmResults.matchedPage instanceof P1

        // wildcard match on part of the url shouldn't match more than one URL level
        pmResults = pageMatcher.findMatchFor("/test/mytesturl/more")
        assert pmResults != null
        assert pmResults.matchedPage instanceof P3

        // exact match
        pmResults = pageMatcher.findMatchFor("/test/test")
        assert pmResults != null
        assert pmResults.pathParameters.isEmpty()
        assert pmResults.matchedPage instanceof P2
    }


    @Test
    public void testDiscardLastSlash() {
        StencilMatcher pageMatcher = applicationContext.getBean(StencilMatcher.class);
        StencilMatchResults pmResults = pageMatcher.findMatchFor("/test/test/")

        assert pmResults != null
        assert pmResults.matchedPage instanceof P2
    }

    // ------------------------------------------------------------------------------------------
    //              helper classes
    // ------------------------------------------------------------------------------------------

    public static class BasePage implements Stencil {

        /**
         * The hook to run when a page has been matched
         *
         * @param pathParameters
         */
        @Override
        public void render(HttpServletRequest request, HttpServletResponse response, Map<String, String> pathParameters) {
        }

    }

    @Path("/test/{myurl}")
    public static class P1 extends BasePage {
    }

    @Path("/test/test")
    @DefaultStencil
    public static class P2 extends BasePage {
    }

    @Path("/**")
    public static class P3 extends BasePage {
    }

    public static class P4WithoutAnnotation extends BasePage {

    }

}
