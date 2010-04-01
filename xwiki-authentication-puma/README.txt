= What it does =

IBM WebSphere Portables PUMA api based authentication.

= This authenticator execute the following process =

* get the current PUMA user and synchronize it and its membership with XWiki users/groups
* if none can be found it fallback on the configured authenticator

= Configuration (in xwiki.cfg file) =

If SSO fail, it tries standard LDAP authentication.

xwiki.authentication.puma.userMapping: indicate which PUMA user field to synchronize with which XWiki user field
xwiki.authentication.puma.groupsMapping: indicate which PUMA group to synchronize with which XWiki group

= Install =

* copy this authenticator jar file into WEB_INF/lib/

= Troubleshoot =

