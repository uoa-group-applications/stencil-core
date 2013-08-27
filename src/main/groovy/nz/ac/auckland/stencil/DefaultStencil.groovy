package nz.ac.auckland.stencil

import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

/**
 If this annotation exists (its a Highlander Annotation - There Can Be Only One) then this becomes the page that the StencilDispatchFilter redirects to
 * author: Richard Vowles - http://gplus.to/RichardVowles
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DefaultStencil {
}