package nz.ac.auckland.stencil

import nz.ac.auckland.common.stereotypes.UniversityComponent
import nz.ac.auckland.stencil.Path
import nz.ac.auckland.stencil.Stencil

import javax.inject.Inject

/**
 * Author: Marnix
 *
 * Builds links to stencils with certain parameter maps
 */
@UniversityComponent
class LinkBuilder {

	/**
	 * default context path if nothing is defined
	 */
	private static final String DEFAULT_CONTEXTPATH = '/'

	/**
	 * name of the property
	 */
	private static final String WEBAPP_CONTEXT_PROPERTY = 'webapp.context'

	/**
	 * cached context path
	 */
	private String contextPath

	/**
	 * @return the web applications context path
	 */
	public String getContextPath() {
		if (contextPath == null)
			contextPath = System.getProperty(WEBAPP_CONTEXT_PROPERTY, DEFAULT_CONTEXTPATH);

		return contextPath
	}

	/**
	 * Create a simple link
	 *
	 * @param url is the URL to link to
	 * @return a link that includes the context path
	 */
	public String linkTo(String url) {

		if (url == null) {
			throw new IllegalArgumentException('Should pass URL argument');
		}

		// always should start with a /
		if (!url.startsWith('/')) {
			url = "/$url";
		}

		StringBuilder strBuilder = new StringBuilder()
		strBuilder.append(getContextPath()).append(url);
		return strBuilder.toString().replaceAll('//','/')
	}

	/**
	 * Build a link
	 *
	 * @param stencilClass is the class to be linking to
	 * @param pathParameters are the path parameters to fill out for the link
	 * @return the URL that is generated
	 */
	public String linkTo(Class<? extends Stencil> stencilClass, Map<String, String> pathParameters = [:]) {

		if (!stencilClass) {
			throw new IllegalArgumentException('Expected stencilclass to be a proper class');
		}

		if (pathParameters == null) {
			throw new IllegalArgumentException('Should pass a proper map');
		}

		Path pathAnnotation = stencilClass.getAnnotation(Path);

		if (!pathAnnotation) {
			return null;
		}

		// replace all matchable elements
		String matcher = pathAnnotation.value();
		pathParameters.each { String key, String replaceWith ->
			matcher = matcher.replace("{$key}", replaceWith);
		}

		if (matcher.indexOf("{") > -1) {
			String sub = matcher.substring(matcher.indexOf("{"))
			if (sub.indexOf("}") > -1){
				sub = sub.substring(0, sub.indexOf("}")+1)
			} else{
				sub = sub.take(10)
			}
			throw new IllegalArgumentException("Unable to match element $sub");
		}

		StringBuilder strBuilder = new StringBuilder();
		strBuilder
				.append(getContextPath())
				.append(matcher)

		return strBuilder.toString().replaceAll('//','/');

	}

}
