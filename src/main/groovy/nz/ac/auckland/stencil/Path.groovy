package nz.ac.auckland.stencil

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 * Author: Marnix
 *
 * This annotation is used to indicate the following:
 *
 *  - a spring bean, it will listen to @Inject annotations
 *  - by convention expects the class it's been put on to implement the Page interface
 *
 * The value of the annotation is a URL pattern that allows "ant-style" pattern matching, for details on how
 * to match see the documentation below:
 *
 *   http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/util/AntPathMatcher.html
 *
 * Matched URL elements are stored in the pathParameters map that is passed to your Page.render implementation
 */
@Retention(RetentionPolicy.RUNTIME)
@interface Path {

    /**
     * @return the value of the annotation which contains an ant-path matcher URL
     */
    String value()

}