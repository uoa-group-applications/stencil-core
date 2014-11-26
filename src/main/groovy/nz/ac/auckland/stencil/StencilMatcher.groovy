package nz.ac.auckland.stencil

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import nz.ac.auckland.common.stereotypes.UniversityComponent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.util.AntPathMatcher
import org.springframework.util.PathMatcher

import javax.annotation.PostConstruct
import javax.inject.Inject

/**
 * Author: Marnix
 *
 * The goal of this class is to find the correct Page implementation for a specific URL pattern.
 * It will match by specificity, the most specific URL pattern will be matched a URL pattern.
 */
@UniversityComponent
@CompileStatic
class StencilMatcher {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(StencilMatcher)

    /**
     * A list of pages
     */
    @Inject protected List<Stencil> pages;

    /**
     * A map of page patterns
     */
    private Map<Stencil, String> pagePatterns = [:]

    /**
     * The Ant Path Matcher is thread safe
    */
    private PathMatcher pathMatcher = new AntPathMatcher()

    /*
     * If we find a DefaultPage annotation, set this to the Page url
     */
    String defaultPageUrl = null

    /**
     * initialize this bean's state
     */
    @PostConstruct
    public void keepAnnotatedPageInstances() {

        // filter out page instances without an annotation
        this.pages =
            pages?.findAll { Stencil page ->
                boolean correctAnnotation = page.class.getAnnotation(Path.class) != null

                if (!correctAnnotation)
                    log.warn("stencil: page ${page.class.name} has no @Stencil annotation") // doesn't make sense not to have one?
                else {
                    if (page.class.getAnnotation(DefaultStencil) != null) { // need to do this here to have pageService be able to pick it up
                        if (defaultPageUrl == null)
                            defaultPageUrl = page.class.getAnnotation(Path.class).value()
                        else {
                            throw new RuntimeException("Duplicate DefaultPage annotation ${defaultPageUrl} and ${page.class.name}")
                        }
                    }
                }

                return correctAnnotation
            } as List<Stencil>

        this.pagePatterns = [:]
    }

    /**
     * Try to find a matching page result for a match
     *
     * @param url is the url to find a match for
     * @return a page match result or null when nothing matched
     */
    public StencilMatchResults findMatchFor(String url) {
        if (!url) {
            url = ""
        }

        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }

        PathMatcher matcher = this.getPathMatcherInstance()

        // should check by correct order
        List<Stencil> orderedPagePatternPairs = this.orderPagesBySpecificity(url)

        // convert into PageMatchResults or null when not matching
        List<StencilMatchResults> results = orderedPagePatternPairs?.collect { Stencil page ->
                return provideMatchResultsIfMatches(matcher, page, url);
            }

        // find first non-null
        return results?.find { it != null }
    }

    /**
     * Order the list of pages by their specificity and put them in an ordered tree map
     *
     * @param url is the url to sort for
     * @return an ordered list of pages
     */
    protected List<Stencil> orderPagesBySpecificity(String url) {

        if (url == null) {
            throw new IllegalArgumentException("Url should be specified");
        }

        PathMatcher matcher = this.getPathMatcherInstance()

        // get comparator
        Comparator<String> pageCmp = matcher.getPatternComparator(url);

        // order pages
        List<Stencil> orderedPages = this.pages.sort(false) { Stencil page, Stencil other ->

            // get page patterns
            String pagePattern = this.getPagePattern(page),
                   otherPattern = this.getPagePattern(other);

            // compare patterns to sort page list
            return pageCmp.compare(pagePattern, otherPattern)
        }

        return orderedPages;
    }

    /**
     * This method returns a page match results instance when the pattern matches
     *
     * @param page is the page to test for
     * @param pathPattern is the pattern to test against
     *
     * @return a page match result instance if matches completely, otherwise returns null
     */
    @CompileStatic(TypeCheckingMode.SKIP)
    protected StencilMatchResults provideMatchResultsIfMatches(PathMatcher matcher, Stencil page, String url) {
        if (!matcher) {
            throw new IllegalArgumentException("matcher should be specified")
        }
        if (url == null) {
            throw new IllegalArgumentException("url should be specified");
        }
        if (!page) {
            throw new IllegalArgumentException("page should be specified");
        }

        Map<String, String> patternMatches = [:]
        boolean matches = matcher.doMatch(this.getPagePattern(page), url, true, patternMatches)
        if (matches) {
            return new StencilMatchResults(pathParameters: patternMatches, matchedPage: page)
        }

        return null
    }

    /**
     * Get a page pattern either from the class directly, or when queried before, from the
     *
     * @param page is the page to get the pattern for
     * @return the pattern string or null when page is null
     */
    protected String getPagePattern(Stencil page) {
        if (!page) {
            return null
        }

        if (!this.pagePatterns[page]) {
            Path pathAnnotation = page.class.getAnnotation(Path.class)
            if (!pathAnnotation) {
                return null
            }

            this.pagePatterns[page] = pathAnnotation.value()
        }

        return this.pagePatterns[page]

    }

    /**
     * @return a new path matcher instance
     */
    protected PathMatcher getPathMatcherInstance() {
        return pathMatcher;
    }
}
