XWiki Authenticator for Sun SSO.

CONFIGURATION

You can setup this authenticator with the following properties in xwiki.cfg file:
- xwiki.authentication.sunsso.fields_mapping : indicate which XWiki user profile field has to be updated based on AM user fields. The default value is "email=mail,first_name=givenName,last_name=sn"

INSTALL

You need amclientsdk.jar (provided by Sun Access Manager) and configure a AMClient.properties file (to put in the classpath).
See http://docs.sun.com/source/817-7649/clientSDK.html for more details.