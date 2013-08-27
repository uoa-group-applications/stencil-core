package nz.ac.auckland.stencil

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Author: Marnix
 *
 * Page interface is used to indicate that an object is a page. It is meant to be used in
 * combination with the @Path annotation. When the StencilDispatchFilter is able to match the
 * current URL against the pattern in the @Path annotation it will invoke the render method, passing
 * the request, response and possible matched pathParameters.
 *
 * This command-pattern style interface does not have a return type. Use the response object to write your
 * responses to the browser. Check out PageService for a convenient way to render JSPs.
 */
interface Stencil {

    /**
     * The hook to run when a page has been matched.
     *
     * @param request is the httpservletrequest for this particular incoming request
     * @param response is the httpservletresponse instance for this particular incoming request
     * @param pathParameters is a key/value-mapping that contains path parameters
     */
    public void render(HttpServletRequest request, HttpServletResponse response, Map<String, String> pathParameters);

}
