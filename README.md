
# Stencil

Stencil essentially is a very light-weight request dispatcher. It leverages Spring and the Servlet 3 API to provide you with an easy to use way to show your users dynamic HTML.

## Principles

Stencil is built on a number of solid principles and conventions that are described below.

* _Opinionated on how things are supposed to be done_; having a single approach to a common problem allows for greater knowledge sharing, higher quality of code and better long-term maintainability of the product.

* _each type of page has its own class we call a Stencil_; unlike Grails that bundles multiple page types into controllers with several actions, Stencil mandates you have one class per page type. As we are moving away from rich page state on the server (now provided by AngularJS) we are left with occasions where it is important a page is crawleable. These types of pages shouldn't do more than simple displaying information.

* _the URL mapping of the page is part of the class annotations_; each Stencil has an annotation on the class that describes how it can be reached through the browser. The mapping allows wildcard matching and named parameters that can be used while the Stencil is being rendered.

* _JSPs are used to render the content to the browser_; a proven industry standard that allows you to easily integrate common view-logic patterns. Cherry picking JSTL, fmt and spring tag-libraries we have chosen not to use scriptlets.

## Configuration settings

Despite Stencil's strong opinions on the way pages should be built there are still a number of settings you can influence.

A sample of these settings are:

	stencil.skip.starts=/api, /css, /images, /app-resources
	stencil.skip.ends=*.css,*.js,*.png,*.jpg
	stencil.root.redirect=/home

Stencil has been implemented as a filter, this allows it certain freedoms when dealing with requests. A side-effect of this is that it processes every incoming request by default, this can include existing servlet mappings and even asset requests.

By default Stencil is smart enough to inspect the current servlet container and skip over registered servlet mappings, reducing the amount of time unnecessary spent on analyzing requests. Sometimes, however, you will need a number of additional paths. Thankfully Stencil allows you to override them in your local configuration file.

Here's an overview of the elements you are able to override:

* `stencil.skip.starts` allows you to specify a list of comma-separated path prefixes that should be skipped over. In the example below you see that we don't want Stencil to handle API requests (always beyond `/api`) and any assets that are associated with it.

* `stencil.skip.end` is similar to the property above, but allows us to specify the suffixes to skip.

* `stencil.root.redirect` is necessary to specify the Stencil root page. One cannot match on the '`/`' path. This is why you need to specify the root redirect. It's an internal redirect, good practice dictates you should set the `canonical` value of the page (more information here: https://support.google.com/webmasters/answer/139394)


## Creating a stencil

To create a stencil you simply create a class with the following properties:

* it implements the Stencil interface and its methods
* it is annotated with the Path annotation

The path annotation is scraped by Stencil to determine which page to call when a certain URL is request by the user. As you will soon discover, one can insert wildcards, and pattern matches into the path annotation's value. Stencil is smart enough to know which match is more precise. Given the following two path definitions:

	@Path("/home/page")
	@Path("/home/{subname}")

When the user requests `/home/page`, Stencil will know that the definition of /home/page is more specific than /home/{subname} and will call it.

Anyway, returning to our simple example, here it is:

	@Path("/home")
	public class MyExampleStencil implements Stencil {

		@Inject StencilService stencilService;

		public void render(
			HttpServletRequest request,
			HttpServletResponse response,
			Map<String, String> pathParameters) {

				stencilService.render(request, response, "pages/myJsp", [:])

		}
	}

As you can see it adheres to all the requirements as it has a Path-annotation and implements the Stencil class.

Once you have annotated your class with the @Path annotation, it will automatically be turned into a Spring-bean. This means you will be able to inject other Spring-beans into your stencil. One of the services you most definitely will be using inside your stencils is the StencilService. This bean contains a number of methods that facilitate the rendering of JSPs, the preferred presentation-template.

Because your stencils are beans you will not get a new instance per request. This means you will have to be very careful storing state a page's member variables. Instead, make sure to keep everything localized inside your `render`-method.

You might have noticed the parameters on the render method:

* `request`; the servlet request instance you can use to read and write attributes and parameters etc.
* `response`; the servlet's reponse instance. Use this if you need to set a status code or some response headers (caching headers for example)
* `pathParameters`; when you use pattern matching inside your Path annotation, the named attributes will be stored inside a map and passed to your render method for you to deal with.

## Path parameters

One of the cool features of Stencil is the ability to map parts of the URL. This allows you to create logical URL structures with humanly-readable parts that allow for a better UX and SEO optimization.

Of course, with Stencil, we have leveraged existing implementations. The matching is done using the Spring ant matcher classes. More information about that can be found [here](http://static.springsource.org/spring/docs/3.0.x/javadoc-api/org/springframework/util/AntPathMatcher.html).

Consider the following Stencil:

	@Path("/subject/{id}/{slug}")
	public class MyExampleStencil implements Stencil {

		@Inject StencilService stencilService;

		/**
		 * Called when stencil is invoked.
		 */
		public void render(
			HttpServletRequest request,
			HttpServletResponse response,
			Map<String, String> pathParameters) {

				// logic + call for view rendering

		}
	}

As you can see it adheres to the requirements of a Stencil. It has been annotated with `@Path` and implements the Stencil interface. Now, if someone navigates to the URL below the `render` method is called.

	http://auckland.ac.nz/my-app/subject/10/this-is-my-subject


It is passed the servlet `request` and `response` objects one needs to render anything. The third argument is `pathParameters`. This contains a map of all the parameters that have been mapped from the `Path`-annotation, with exactly the following content:

	{
		"id" : "10",
		"slug" : "this-is-my-subject"
	}

These of course can be used to properly process the web request and build a view model for. Then, after we have the information we need, in the form we need it, we push it into the JSP using the `StencilService`.

## Rendering the JSP

The last step in processing a web request is outputting the response. Stencil was specifically created to render to JSPs. Your JSPs should be put in the `WEB-INF/` folder of your project (depending on whether your Stencil project is a webfragment or not these locations differ).

The following code is a common way to render your JSP to the JspWriter in the request object. It assumes you have `@Inject`-ed the StencilService into your class.

    // render the JSP
    stencilService.renderJsp(
            request,
            response,
            "subject/index",
            [
				title: "My title",
				description:
				"Lorem ipsum dolor sit amet, consectetur adipisicing elit. " +
				"Tempora, dolore necessitatibus sunt iusto praesentium nemo " +
				"a voluptas nostrum delectus sit libero minima dicta esse" +
				fuga reprehenderit perspiciatis provident aperiam ab?",
				faculties: ["Arts", "Science", "Languages"]
            ]
    );


This code will look for a file called `/WEB-INF/jsp/pages/subject/index.jsp` and push the attributes of the map into the request attributes. This allows you to access them later on in the JSP expression language.

This, in a nutshell, is how you should be setting up your Stencil.

Remember, the `@Path`-annotation causes the class to become a Spring bean, so you can inject any type of Spring bean. Common practice dictates that your classes should really be built for one thing only. This means that it's a good idea to inject data-layer services into your Stencil to query it for the information you need.

Another common pattern that you should adhere to is the transformation of the structure of your domain models to that of your view. Very rarely is there a one-to-one mapping between the data you store and the way you display it; undoubtedly there will be instances where you need to change formatting, enrich your objects etc. That is why it is encouraged to have a separate set of view-model objects that will do these things for you. Alternately you could implement the Decorator pattern -- a wrapper for data objects that could contain view logic.

Actual scriptlet and transformation logic in your views is forbidden as it leads to unmanagable and unmaintainable code.

## JSTL, Expression Language

An example of the `subject/index.jsp` file above is shown below:

	<h1><spring:escapeBody>${title}</spring:escapeBody></h1>

	<p>
		${description}
	</p>

	<c:if test="${! empty faculties}">
		<ul>
			<c:forEach items="${faculties}" var="faculty">
				<li>${faculty}</li>
			</c:forEach>
		</ul>
	</c:if>

In our example the parts that are JSTL are the `<c:if>` and `<c:forEach>` tags that are part of the core library (hence the `c`-namespace). Expression language ranges from simply reading and outputting variables to performing expression checks on them as we did to check whether the was anything in the `faculties` request attribute by doing: `! empty faculties`.

To learn more about JSPs, JSTL and the expression language please go through one of the many Sun/Oracle tutorials on the matter.
