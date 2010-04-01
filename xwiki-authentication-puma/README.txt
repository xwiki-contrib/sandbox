= What it does =

IBM WebSphere Portal PUMA api based authentication.

= This authenticator execute the following process =

* get the current PUMA user and synchronize it and its membership with XWiki users/groups
* if none can be found it falback on the configured authenticator

= How to build it =

You will need to following jars to build the authenticator (you can find them in your WebSphere install):
* wp.user.api.jar
* wp.portletservices.api.legacy.jar
* wp.portletservices.api.standard.jar
* wp.pe.api.standard.jar

= Configuration (in xwiki.cfg file) =

xwiki.authentication.puma.userMapping: indicate which PUMA user field to synchronize with which XWiki user field
xwiki.authentication.puma.groupsMapping: indicate which PUMA group to synchronize with which XWiki group
xwiki.authentication.puma.falback: indicate which authenticator to use when no PUMA informations are not provided (i.e. not SSO mode), does not falback on anything by default

= Install =

* copy this authenticator jar file into WEB_INF/lib/

= Troubleshoot =

