package nz.ac.auckland.stencil

import groovy.transform.CompileStatic
import nz.ac.auckland.common.stereotypes.UniversityComponent

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Author: Marnix
 *
 * Page service implementation contains often used functionality when rendering pages.
 */
@UniversityComponent
@CompileStatic
class StencilServiceImpl implements StencilService {

    /**
     * Content-Type header
     */
    private static final String HEADER_CONTENT_TYPE = "Content-Type"

    /**
     * Default value for Content-Type header, set when none present in response object.
     */
    private static final String DEFAULT_CONTENT_TYPE = "text/html; charset=utf-8"

    /**
     * Retrieve the template path
     *
     * @param page is url to the page
     * @return
     */
    public String getTemplatePath(String page) {
        if (page.endsWith(".jsp"))
            return page;
        else
            return "/WEB-INF/jsp/pages/${page}.jsp"
    }

    /**
     * Get the value for a URL parameter
     *
     * @param parameterName is the parameter name to retrieve
     * @return the value of this parameter or null when not found
     */
    public String getUrlParameter(HttpServletRequest request, String parameterName) {
        if (!parameterName) {
            throw new IllegalArgumentException("parameterName cannot be empty");
        }

        return request.getParameter(parameterName);
    }

    /**
     * Render a JSP
     *
     * @param page is the path to the JSP to render
     * @param model is the model to insert into the request before rendering anything
     */
    public void renderJsp(HttpServletRequest request, HttpServletResponse response, String page, Map<String, Object> model) {

        if (!page) {
            throw new IllegalArgumentException("No page parameter specified");
        }

        model?.each { String key, Object modelVal ->
            request.setAttribute(key, modelVal);
        }

        if (!response.getHeader(HEADER_CONTENT_TYPE)) {
            response.setHeader(HEADER_CONTENT_TYPE, DEFAULT_CONTENT_TYPE);
        }

        request.getRequestDispatcher(this.getTemplatePath(page)).include(request, response)

    }


}
