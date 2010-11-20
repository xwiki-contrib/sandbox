Event.observe(document, 'xwiki:dom:loaded', function() {
  var resourceURL = $('resourceURL').value;
  var fRequest = Ajax.Request.prototype.request;
  Ajax.Request.prototype.request = function(servletURL) {
    if (typeof servletURL == 'string' && servletURL.startsWith('/')) {
      // URL relative to the servlet container root.
      this.options.parameters['org.xwiki.portlet.parameter.dispatchURL'] =  servletURL;
      servletURL = resourceURL;
    }
    fRequest.call(this, servletURL);
  }
});

