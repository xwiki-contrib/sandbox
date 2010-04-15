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

Put them in the /lib folder and that's all you can build with maven (mvn package).

= Configuration =

== xwiki.cfg file ==

#-# Retrieve the following fields from PUMA and store them in the XWiki user object (puma-attribute=xwiki-attribute)
# xwiki.authentication.puma.userMapping=sn=last_name,givenName=first_name,mail=email

#-# Maps XWiki groups to PUMA groups, separator is "|"
# xwiki.authentication.puma.groupsMapping=XWiki.XWikiAdminGroup=cn=AdminRole,ou=groups,o=MegaNova,c=US|\
#                                         XWiki.Organisation=cn=testers,ou=groups,o=MegaNova,c=US

#-# Indicate which authenticator to use when no PUMA informations are not provided (i.e. not SSO mode), does not falback on anything by default
# xwiki.authentication.puma.fallback=com.xpn.xwiki.user.impl.LDAP.XWikiLDAPAuthServiceImpl

== XWikiPreferences == 

It's also possible to put any of theses configuration in the XWiki.XWikiPreferences object in the XWiki.XWikiPreferences page. Add a string field with the proper name to the class and put the value you want.

The fields names are not exactly the same, you have to change "xwiki.authentication.puma." prefix to "puma_":

xwiki.authentication.puma.userMapping -> puma_userMapping
xwiki.authentication.puma.groupsMapping -> puma_groupsMapping
xwiki.authentication.puma.fallback -> puma_fallback

= Install =

* copy this authenticator jar file into WEB_INF/lib/
* setup xwiki.cfg with:
xwiki.authentication.authclass=com.xwiki.authentication.puma.PUMAAuthServiceImpl

= Troubleshoot =

= IDEAS =

* add support for servlet PUMA authentication (looks like PUMA is not only about portlet when i look at the api but i may be wrong)

= TODO =

* support mapping of more than string fields (images as profile photo for example, lists, etc...)