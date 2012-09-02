Authenticate user on main wiki based on a custom database trough JDBC API.

= Configuration

xwiki.authentication.authclass=org.xwiki.contrib.authentication.jdbc.XWikiJDBCAuthenticator

## The JDBC driver class to use
xwiki.authentication.jdbc.connection.driver_class=com.mysql.jdbc.Driver

## The JDBC URL
xwiki.authentication.jdbc.connection.url=jdbc:mysql://localhost/di

## The JDBC connection properties
## Put all properties starting with xwiki.authentication.jdbc.connection
## For example:
xwiki.authentication.jdbc.connection.user=xwiki
xwiki.authentication.jdbc.connection.password=xwiki

## SELECT QUERY

## The query to use to validate and get user information at the same time
## For example:
xwiki.authentication.jdbc.mapping.select.query=select mail, name from users where login=? and active = 1 and password=concat('{SHA1}', ?)

## The well knows information to replace the ? with
## The available properties are:
## * login: the login provided by the user
## * password: the password provided by the user
## * passwordsha1base64: the passwords provided by the user encoded with SH1 and then converted in BASE64
xwiki.authentication.jdbc.mapping.select.fields=login,passwordsha1base64

## Mapping between the index in the JDBS select query result and the XWikiUsers xobject fields
xwiki.authentication.jdbc.mapping.select.mapping=email,name

= Todo

* add support for update in both ways
* more encrypted passwords