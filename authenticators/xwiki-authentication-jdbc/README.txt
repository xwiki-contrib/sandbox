Authenticate user on main wiki based on a custom database trough JDBC API.

= Configuration (xwiki.cfg)

xwiki.authentication.authclass=org.xwiki.contrib.authentication.jdbc.XWikiJDBCAuthenticator

= Configuration (xwiki.properties)

## The JDBC driver class to use
authentication.jdbc.connection.driver_class=com.mysql.jdbc.Driver

#-# The JDBC connection URL
authentication.jdbc.connection.url=jdbc:mysql://localhost/database?useServerPrepStmts=false&useUnicode=true&characterEncoding=UTF-8

#-# The JDBC connection properties
authentication.jdbc.connection.properties=user=xwiki
authentication.jdbc.connection.properties=password=xwiki

## SELECT QUERY

#-# The query to use to validate and get user information at the same time
authentication.jdbc.select.query=select mail\, name from users where login=? and active = 1 and (? IS NULL or password=concat('{SHA1}'\, ?))
#-# The well knows information to replace the ? with
authentication.jdbc.select.parameters=login,passwordsha1base64,passwordsha1base64
#-# Mapping between the index in the JDBS select query result and the XWikiUsers xobject fields
authentication.jdbc.select.mapping=email,name

## INSERT QUERY

#-# The query to use to insert a new JDBC user when a new XWiki user has been created
authentication.jdbc.insert.query=INSERT INTO users (login\, password\, mail\, name\, home\, uid\, creation_date\, modification_time\, active\, level) SELECT ?\, concat('{SHA1}'\, ?)\, ?\, ?\, CONCAT('/data/users/'\, ?)\, max(uid) + 1\, now()\, now()\, 1\, 0 from users
#-# The well knows information to replace the ? with
authentication.jdbc.insert.parameters=login,passwordsha1base64,email,name,login

## UPDATE QUERY

#-# The query to use to update JDBC user when the XWiki user has been modified
authentication.jdbc.update.query=UPDATE users SET mail=?\, name=?\, password=IF(? IS NULL\, password\, concat('{SHA1}'\, ?))\, modification_time=now() WHERE login=?
#-# The well knows information to replace the ? with
authentication.jdbc.update.parameters=email,name,passwordsha1base64,passwordsha1base64,login

## DELETE QUERY

#-# The query to use to delete the JDBC user when the XWiki user has been deleted
authentication.jdbc.delete.query=DELETE from users WHERE login=?
#-# The well knows information to replace the ? with
authentication.jdbc.delete.parameters=login

= Provided properties

* all XWiki.XWikiUsers fields (email, first_name...)
* login: the id of the user (the name of the XWiki document or what the user typed in the login form)
* name: a combination of the XWiki.XWikiUsers first_name and last_name fields
* passwordsha1base64: the passwords provided by the user encoded with SH1 and then converted in BASE64

= TODO

* more encrypted passwords
* add support for more than just string properties