package nz.ac.auckland.stencil

import nz.ac.auckland.stencil.Path
import nz.ac.auckland.stencil.Stencil
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Author: Marnix
 *
 * Test some of the linkbuilder functionality
 */
class LinkBuilderTest {

    /**
     * Test the link builders simple behavior
     */
    @Test
    public void testSimpleLinkBuilder() {
        LinkBuilder builder = createLinkBuilder()

        assert builder.linkTo("my-link") == "/context/my-link";
        assert builder.linkTo("/my-link") == "/context/my-link"
        assert builder.linkTo("") == "/context/";
        assert builder.linkTo("/") == "/context/";

	    builder = createLinkBuilder2()

	    assert builder.linkTo("my-link") == "/my-link";
	    assert builder.linkTo("/my-link") == "/my-link"
	    assert builder.linkTo("") == "/";
	    assert builder.linkTo("/") == "/";
    }

    /**
     * should throw exception on stupid input
     */
    @Test(expected = IllegalArgumentException)
    public void testIllegalInput() {
        LinkBuilder builder = createLinkBuilder()

        builder.linkTo((String) null);
    }

    /**
     * Happy flow
     */
    @Test
    public void testStencilLinkBuilder() {
        LinkBuilder builder = createLinkBuilder()

        // exact match
        String match;

        match = builder.linkTo(TestStencil, [one: "first", two: "second", three: "third"]);
        assert match == "/context/first/second/content/third";

	    builder = createLinkBuilder2()
	    match = builder.linkTo(TestStencil, [one: "first", two: "second", three: "third"])
	    assert match == "/first/second/content/third"

    }

    /**
     * When something can't be matched, it should blow up
     */
    @Test(expected = IllegalArgumentException)
    public void testStencilLinkBuilderWithMissingParameter() {
        LinkBuilder builder = createLinkBuilder()

        // exact match
        String match;

        match = builder.linkTo(TestStencil, [one: "first", two: "second", missing: "missing"]);
    }

    /**
     * Are there too many parameters? shouldn't matter
     */
    public void testWithTooManyParameters() {

        LinkBuilder builder = createLinkBuilder()

        // exact match
        String match;

        match = builder.linkTo(TestStencil, [one: "first", two: "second", three: "third", missing: "missing"]);
        assert match == "/context/first/second/content/third";
    }

    /**
     * Test stencil
     */
    @Path("/{one}/{two}/content/{three}")
    public static class TestStencil implements Stencil {

        @Override
        void render(HttpServletRequest request, HttpServletResponse response, Map<String, String> pathParameters) {
        }

    }

    protected LinkBuilder createLinkBuilder() {
        LinkBuilder builder = new LinkBuilder();
	    builder.contextPath = '/context'


        return builder
    }

	protected LinkBuilder createLinkBuilder2() {
		LinkBuilder builder = new LinkBuilder();
		builder.contextPath = '/'
		return builder
	}

}
