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

#-# Retrieve the following fields from PUMA and store them in the XWiki user object (puma-attribute=xwiki-attribute)
# xwiki.authentication.puma.userMapping=sn=last_name,givenName=first_name,mail=email

#-# Maps XWiki groups to PUMA groups, separator is "|"
# xwiki.authentication.puma.groupsMapping=XWiki.XWikiAdminGroup=cn=AdminRole,ou=groups,o=MegaNova,c=US|\
#                                         XWiki.Organisation=cn=testers,ou=groups,o=MegaNova,c=US

#-# Indicate which authenticator to use when no PUMA informations are not provided (i.e. not SSO mode), does not falback on anything by default
# xwiki.authentication.puma.falback=com.xpn.xwiki.user.impl.LDAP.XWikiLDAPAuthServiceImpl 

= Install =

* copy this authenticator jar file into WEB_INF/lib/
* setup xwiki.cfg with:
xwiki.authentication.authclass=com.xwiki.authentication.puma.PUMAAuthServiceImpl

= Troubleshoot =

