package nz.ac.auckland.stencil

import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mockito
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletContext
import javax.servlet.ServletRegistration
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

/**
 * Author: Marnix
 *
 * Tests for stencil dispatch filter
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:applicationContext.xml")
class StencilDispatchFilterTest {

    @Test
    public void testSkipPathParser() {

        StencilDispatchFilter filter = new StencilDispatchFilter();
        String[] paths = filter.parseSkippablePaths("/css, /js\n, /images")
        assert paths == ['/css', '/js', '/images']

    }

    @Test
    public void testProcessThisUrl() {
        StencilDispatchFilter filter = new StencilDispatchFilter();
        String[] paths = filter.parseSkippablePaths("/css, /js\n, /images")
        assert paths == ['/css', '/js', '/images']

        filter.skipPathsStartsWith = paths

        assert filter.processThisUrl("/not/images") == true
        assert filter.processThisUrl("/images") == false
        assert filter.processThisUrl("/images/") == false
        assert filter.processThisUrl("/images/my-image.png") == false

        assert filter.processThisUrl("/css/my-stylesheet.png") == false

        assert filter.processThisUrl("/js/my-javascript.png") == false

        filter.skipPathsStartsWith = []
        filter.skipPathsEndsWith = ['.css', '.js']
        assert filter.processThisUrl('/munchkin') == true
        assert filter.processThisUrl('mine.css') == false
        assert filter.processThisUrl('minecss') == true
        assert filter.processThisUrl('mine.js') == false
    }

    @Test
    public void pathsConfigureCorrectlyIfNoSystemPropertiesSet() {
      StencilDispatchFilter filter = new StencilDispatchFilter() {
        protected void initSpring(FilterConfig f) {
        }
      }
      ServletContext servletContext = mock(ServletContext)
      FilterConfig filterConfig = mock(FilterConfig)
      ServletRegistration servletRegistration = mock(ServletRegistration)
      when(filterConfig.servletContext).thenReturn(servletContext)

      Map<String, ? extends ServletRegistration> regs = ['mine': servletRegistration] as Map<String, ? extends ServletRegistration>

      when(servletContext.servletRegistrations).thenReturn(regs)
      when(servletRegistration.mappings).thenReturn(['*.css', '*.js', '/api/*', '/'])

      filter.init(filterConfig)

      assert filter.skipPathsStartsWith == ['/api/']
      assert filter.skipPathsEndsWith == ['.css', '.js']
    }


    @Test
    public void testDoFilterHappyFlow() {

        // setup filter with some prepopulated values
        StencilDispatchFilter filter = [
                getStencilSkipParameter : { HttpServletRequest request ->
                    return "/css, /js\n, /images"
                },

                getStencilRootRedirectParameter : { HttpServletRequest request ->
                    return "/home"
                }
        ] as StencilDispatchFilter;

        // mock objects
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class)
        HttpServletResponse resp = Mockito.mock(HttpServletResponse.class)
        FilterChain chain = Mockito.mock(FilterChain.class)

        // the request URI is /test/test
        Mockito.when(req.getRequestURI()).thenReturn("/test/test")

//        filter.init(null);
//        filter.doFilter(req, resp, chain);
    }

}
