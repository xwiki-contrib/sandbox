### SAML SSO configuration
xwiki.authentication.authclass=com.xwiki.authentication.saml.XWikiSAMLAuthenticator
xwiki.authentication.saml.fields_mapping=email=mail,first_name=givenName,last_name=sn
## certification file
xwiki.authentication.saml.cert=/WEB-INF/cert.txt
## Identity provider URL
xwiki.authentication.saml.authurl=https://www.ip-url.fr/
## Service provider configuration
xwiki.authentication.saml.issuer=www.sp-url.com
xwiki.authentication.saml.namequalifier= www.sp-url.com
xwiki.authentication.saml.auth_field=saml_user
xwiki.authentication.saml.xwiki_user_rule=first_name,last_name
xwiki.authentication.saml.xwiki_user_rule_capitalize=1

