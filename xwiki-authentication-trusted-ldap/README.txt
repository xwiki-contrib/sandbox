= What it does =

Support request remote user based SSO authentication and get informations from LDAP server.

= This authenticator execute the following process =

* get remote user from Portlet or Servlet request and retrieve informations from LDAP server
* if nothing is provided it falback on standard LDAP authentication

= Configuration (in xwiki.cfg file) =

If SSO fail, it tries standard LDAP authentication.

= Install =

* copy this authenticator jar file into WEB_INF/lib/

= Troubleshoot =

