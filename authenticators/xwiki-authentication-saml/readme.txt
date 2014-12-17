This module allows SAML authentication over a SAML server. It has been tested only with a custom SAML server.


The following XWiki Class is needed:

XWiki.SAMLAuthClass
with field nameid as a string


The following configuration is needed in xwiki.cfg:

### SAML SSO configuration
# Change the active authenticator to SAML
xwiki.authentication.authclass=com.xwiki.authentication.saml.XWikiSAMLAuthenticator

# Path to certificate file, relative to the webapp directory; required
xwiki.authentication.saml.cert=/WEB-INF/cert.txt
# Identity provider URL; required
xwiki.authentication.saml.authurl=https://www.ip-url.fr/
# Service provider URL; required
xwiki.authentication.saml.issuer=www.sp-url.com
# The SPNameQualifier value; required
xwiki.authentication.saml.namequalifier=www.sp-url.com

# The name of the SAML field containing the SAML user identifier; optional
#xwiki.authentication.saml.id_field=userPrincipalName
# Mapping between XWikiUsers fields and SAML fields; optional
#xwiki.authentication.saml.fields_mapping=email=mail,first_name=givenName,last_name=sn
# The name of the attribute used to cache the authentication result in the current session; optional
#xwiki.authentication.saml.auth_field=saml_user
# List of fields to use for generating an XWiki username; optional
#xwiki.authentication.saml.xwiki_user_rule=first_name,last_name
# Capitalize each field value when generating the username; optional
#xwiki.authentication.saml.xwiki_user_rule_capitalize=1
