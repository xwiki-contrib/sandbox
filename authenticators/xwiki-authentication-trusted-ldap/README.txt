= What it does =

Support SAAS remote user based SSO authentication and get informations from LDAP server.

= This authenticator execute the following process =

* get remote user from Portlet or Servlet request and retrieve informations from LDAP server
* if nothing is provided it falback on standard LDAP authentication

If SSO fail, it tries standard LDAP authentication.

= Configuration =

== xwiki.cfg file ==

#-# A Java regexp used to parse the remote user provided by JAAS
#
# xwiki.authentication.trustedldap.remoteUserParser=(.+)@(.+)

#-# Indicate which of the regexp group correspond to which LDAP properties
#-# The following LDAP properties are supported:
#-#   login, password, ldap_server, ldap_base_DN, ldap_bind_DN, ldap_bind_pass
#
# xwiki.authentication.trustedldap.remoteUserMapping.1=login
# xwiki.authentication.trustedldap.remoteUserMapping.2=ldap_server,ldap_base_DN

#-# Indicate how to convert each found property
#
# xwiki.authentication.trustedldap.remoteUserMapping.ldap_server=MYDOMAIN=my.domain.com|MYDOMAIN2=my.domain2.com
# xwiki.authentication.trustedldap.remoteUserMapping.ldap_base_DN=MYDOMAIN=dc=my,dc=domain,dc=com|MYDOMAIN2=dc=my,dc=domain2,dc=com
# xwiki.authentication.trustedldap.remoteUserMapping.ldap_bind_DN=MYDOMAIN=cn=bind,dc=my,dc=domain,dc=com|MYDOMAIN2=cn=bind,dc=my,dc=domain2,dc=com
# xwiki.authentication.trustedldap.remoteUserMapping.ldap_bind_pass=MYDOMAIN=password|MYDOMAIN2=password2

#-# For all LDAP related configuration refer to standard LDAP authenticator documentation

== XWikiPreferences == 

It's also possible to put any of theses configuration in the XWiki.XWikiPreferences object in the XWiki.XWikiPreferences page. Add a string field with the proper name to the class and put the value you want.

The fields names are not exactly the same, you have to change "xwiki.authentication.trustedldap." prefix to "trustedldap_":

xwiki.authentication.trustedldap.remoteUserParser -> trustedldap_remoteUserParser
...

= Install =

* copy this authenticator jar file into WEB_INF/lib/
* setup xwiki.cfg with:
xwiki.authentication.authclass=com.xwiki.authentication.trustedldap.TrustedLDAPAuthServiceImpl

= Troubleshoot =

== Debug log ==

<!-- Standard LDAP debugging -->
<logger name="com.xpn.xwiki.plugin.ldap" level="trace"/>
<logger name="com.xpn.xwiki.user.impl.LDAP" level="trace"/>
<!-- Trusted LDAP debugging -->
<logger name="com.xwiki.authentication.trustedldap" level="trace"/>

= TODO =

* generic support of LDAP property in remoteUserMapping configuration
