Authenticate user on main wiki based on a HTTP request.

= Configuration

xwiki.authentication.authclass=org.xwiki.contrib.authentication.http.XWikiHTTPAuthenticator

## The URI to authenticate on
xwiki.authentication.http.uri=https//localhost/auth

= Todo

* add support for update in both ways
* more encrypted passwords