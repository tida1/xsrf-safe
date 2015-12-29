The GWT New Web Application Project sample code modified to be xsrf-safe.

## Q&A ##

Whats the foundation of this technique?

http://code.google.com/webtoolkit/doc/latest/DevGuideSecurityRpcXsrf.html


What is the com.google.gwt.util.tools.Utility class for?

http://code.google.com/p/google-web-toolkit/issues/detail?id=6367


Where is XsrfTokenUtil.java used?

XsrfTokenUtil is used in the jsp file.  This technique in general reduces the number of requests to get data on the screen.  However, since we need the XsrfToken before we can make any remote calls, we would have to wait for this response...making all xsrf-safe remote service instantiations asynchronous.  By using a JSP as the host page, we are able to insert the XsrfToken into the javascript namespace, eliminating the need to make all xsrf-safe remote service instantiations asynchronous.

http://code.google.com/webtoolkit/articles/dynamic_host_page.html