package nz.ac.auckland.stencil

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.web.context.support.SpringBeanAutowiringSupport

import javax.inject.Inject
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRegistration
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Author: Marnix
 *
 * Stencil dispatcher filter. This filter intercepts certain requests and tries to find a Page instance
 * with a @Path annotation that defines a pattern that matches the URL being requested from the server.
 *
 * It is a filter implementation, so that, when there are no matches, the request will be processed by
 * Servlets that have registered themselves in the container. One can prevent certain requests from being
 * processed by the filter , set them in your system properties under:
 *
 * - stencil.skip.starts (expects a comma-separated list of 'startsWith' patterns to skip
 * - stencil.skip.ends (expects a comma-separated list of 'endsWith' patterns to skip
 *
 * When these properties have not been specified this filter automatically searches for
 * servlets that have registered themselves in the container and adds them to the "stencil.skip.starts"
 */
@CompileStatic
class StencilDispatchFilter implements Filter {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(StencilDispatchFilter)

    /**
     * Parameter location for stencil 'startsWith' skip URL patterns
     */
    private static final String PARAM_STENCILSKIP_START = "stencil.skip.starts" // e.g. /api/*

    /**
     * Parameter location for stencil 'endsWith' skip URL patterns
     */
    private static final String PARAM_STENCILSKIP_ENDS = "stencil.skip.ends" // e.g. *.jsp, *.css, *.js

    /**
     * Root parameter
     */
    private static final String PARAM_STENCIL_ROOT = "stencil.root.redirect"

    /**
     * Page matcher bound here
     */
    @Inject StencilMatcher pageMatcher;

    /**
     * Page service functions accessible here.
     */
    @Inject StencilService pageService;

    /**
     * List of elements to skip when their value is found at the beginning of the URL request
     */
    String[] skipPathsStartsWith;

    /**
     * List of elements to skip when their value is found at the end of the URL request.
     */
    String[] skipPathsEndsWith;

    /**
     * Initializes spring.
     *
     * @param filterConfig
     */
    protected void initSpring(FilterConfig filterConfig) {
      SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, filterConfig.servletContext);
    }

    /**
     * Initialize beans
     *
     * @param filterConfig is the filter configuration
     * @throws ServletException
     */
    @Override
    @CompileStatic(TypeCheckingMode.SKIP)
    void init(FilterConfig filterConfig) throws ServletException {

        this.initSpring(filterConfig)

        if (System.getProperty(PARAM_STENCILSKIP_START) == null && System.getProperty(PARAM_STENCILSKIP_ENDS) == null) {
            List<String> startPaths = []
            List<String> endPaths = []

            for (Map.Entry<String, ? extends ServletRegistration> entry : filterConfig.servletContext.servletRegistrations) {
                entry.value.mappings.each { String mapping ->
                    int asteriskCount = mapping.count('*')
                    if (asteriskCount == 0 && mapping != '/') {
                        startPaths.add(mapping)
                    }
                    else if (mapping.startsWith("*") && asteriskCount == 1) {
                        endPaths.add(mapping.substring(1))
                    }
                    else if (mapping.endsWith('*') && asteriskCount == 1) {
                        if (mapping != '/*') {
                            startPaths.add(mapping.substring(0, mapping.length() - 1))
                        }
                    }
                    else if (mapping != '/') {
                        log.warn("stencil: derived mapping to complex to add, consider adding overrides ${mapping}")
                    }
                }
            }

            skipPathsStartsWith = startPaths.toArray()
            skipPathsEndsWith = endPaths.toArray()

            log.info("stencil: skippable paths start: ${skipPathsStartsWith.join(',')}, ends: ${skipPathsEndsWith.join(',')}")
        } else {
            skipPathsStartsWith = this.parseSkippablePaths(System.getProperty(PARAM_STENCILSKIP_START));
            skipPathsEndsWith = this.parseSkippablePaths(System.getProperty(PARAM_STENCILSKIP_ENDS));
        }
    }

    /**
     * Filter implementation checks whether there any of the specified URLs match. If so,
     * render a JSP and cancel the chain. If not just let it fall through.
     *
     * @param request is the request object
     * @param response is the response object
     * @param chain is the filter chain to call when we're falling through
     *
     * @throws IOException
     * @throws ServletException
     */
    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        // we know we're in an http container
        HttpServletRequest httpRequest = request as HttpServletRequest
        HttpServletResponse httpResponse = response as HttpServletResponse
        String url = httpRequest.servletPath;

        // if no url specified, or empty, do redirect
        if (!url || url == "/") {
            String baseRedirect = pageMatcher.defaultPageUrl ?: this.getStencilRootRedirectParameter(request);

            if (baseRedirect) {
                url = baseRedirect;
            }
        }


        // should we process this url ?
        if (this.processThisUrl(url)) {

            // match
            StencilMatchResults matchResults = pageMatcher.findMatchFor(url)

            // if matched, render.
            if (matchResults) {
                matchResults.matchedPage.render(httpRequest, httpResponse, matchResults.pathParameters)
                return;
            }
        }

        // no match, or not processing, pass through chain
        chain.doFilter(request, response);
    }

    /**
     * @return the stencil skip initialize context-param
     */
    protected String getStencilRootRedirectParameter(ServletRequest request) {
        return System.getProperty(PARAM_STENCIL_ROOT)
    }

    /**
     * @return true if the url needs to processed by this filter. This needs to be FAST.
     */
    protected boolean processThisUrl(String url) {

        for (String skipMe : skipPathsStartsWith) {
            if (url.startsWith(skipMe)) {
                return false  // exit ASAP
            }
        }

        for (String skipMe : skipPathsEndsWith) {
            if (url.endsWith(skipMe)) {
                return false  // exit ASAP
            }
        }

        return true
    }

    /**
     * @return a list of skippable path prefixes
     */
    protected String[] parseSkippablePaths(String pathString) {
        if (pathString == null) {
            return [] as String[]
        }

        String[] skipPaths = pathString.split(",")

        // trim all paths
        skipPaths = skipPaths.collect { String path -> path.trim() }

        // return them
        return skipPaths;
    }

    @Override
    void destroy() {
    }
}
