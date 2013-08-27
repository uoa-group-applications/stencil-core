package nz.ac.auckland.stencil

import groovy.transform.CompileStatic

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Page service interface contains a number of convenience methods for dealing with
 * rendering JSPs and extracting information from the request.
 */
@CompileStatic
public interface StencilService {

  /**
   * Retrieve the template path
   *
   * @param page is url to the page
   * @return
   */
  public String getTemplatePath(String page)

  /**
   * Get the value for a URL parameter
   *
   * @param parameterName is the parameter name to retrieve
   * @return the value of this parameter or null when not found
   */
  public String getUrlParameter(HttpServletRequest request, String parameterName)

  /**
   * Render a JSP
   *
   * @param page is the path to the JSP to render
   * @param model is the model to insert into the request before rendering anything
   */
  public void renderJsp(HttpServletRequest request, HttpServletResponse response, String page, Map<String, Object> model)
}
