= What it does =

Support NTLM and Basic authenticator (from browsers not supporting NTLM).

= This authenticator execute the following process =

* if it can find NTLM or Basic header it tries to resolve the user name and search it in LDAP server 
* if none is found it reply to the browser asking for NTLM or BASIC authentication

= Configuration (in xwiki.cfg file) =

* xwiki.authentication.ntlm.domainController: address of the domain controller used to resolve NTLM token into LDAP identifier
* xwiki.authentication.ntlm.defaultDomain: the default domain to use when none is provided (when using basic authentication)
* xwiki.authentication.ntlm.validate: can be 1 or 0 (default to 1), if 1 the resolved NTLM login and pass are used to validate credentials
* xwiki.authentication.ntlm.domainMapping: if set LDAP identifier is composed with domain and user name the following way userName@domain. Mapping is used to be able to transform some domain coming form NTLM into another to put after the @ sign.
  ex: NTLMDOMAIN1=domain1.xwiki.org|NTLMDOMAIN2=domain2.xwiki.org
* xwiki.authentication.ntlm.realm: the message to show to the user in basic authentication popup

It's possible to overwrite theses parameters in the wiki preferences using the following names (need to add them to XWiki.XWikiPreferences class):
* xwiki.authentication.ntlm.domainController -> ntlm_domainController
* xwiki.authentication.ntlm.defaultDomain -> ntlm_defaultDomain
* xwiki.authentication.ntlm.validate -> ntlm_validate
* xwiki.authentication.ntlm.domainMapping -> ntlm_domainMapping
* xwiki.authentication.ntlm.realm -> ntlm_realm

See generic LDAP authenticator configuration for more about how the LDAP identifier is used after being resolver from NTLM

= Install =

* copy this authenticator jar file into WEB_INF/lib/
* make sure WEB_INF/lib/ contains jcifs library. See pom.xml file to make sure to get the proper version (should also work with more recent version).
