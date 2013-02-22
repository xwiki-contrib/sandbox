Authenticate user on main wiki based on a custom database trough JDBC API.

= Configuration (xwiki.cfg)

xwiki.authentication.authclass=org.xwiki.contrib.authentication.jdbc.XWikiJDBCAuthenticator

= Configuration (xwiki.properties)

## The JDBC driver class to use
authentication.jdbc.connection.driver_class=com.mysql.jdbc.Driver
OR
authentication.jdbc.connection.driver_class=org.postgresql.Driver

#-# The JDBC connection URL
authentication.jdbc.connection.url=jdbc:mysql://localhost/database?useServerPrepStmts=false&useUnicode=true&characterEncoding=UTF-8

#-# The JDBC connection properties
authentication.jdbc.connection.properties=user=xwiki
authentication.jdbc.connection.properties=password=xwiki

## Choosing your password hashing scheme
authentication.jdbc.password_hasher=sha1base64
OR
authentication.jdbc.password_hasher=pbkdf2
OR
authentication.jdbc.password_hasher=plaintext

## SELECT QUERY

#-# The query to retrieve user information from the database including the password which will be compared against the password provided by user and hashed by a selected hashing class
authentication.jdbc.select.query=select mail\, name from users where login=? and active = 1 and (? IS NULL or password=?)
#-# The well knows information to replace the ? with
authentication.jdbc.select.parameters=login,password,password
#-# Mapping between the index in the JDBC select query result and the XWikiUsers xobject fields
authentication.jdbc.select.mapping=email,name
#-# The name of the column in the select query that contains the password
authentication.jdbc.select.password_column=password

## INSERT QUERY

#-# The query to use to insert a new JDBC user when a new XWiki user has been created
authentication.jdbc.insert.query=INSERT INTO users (login\, password\, mail\, name\, home\, uid\, creation_date\, modification_time\, active\, level) SELECT ?\, ?\, ?\, ?\, CONCAT('/data/users/'\, ?)\, max(uid) + 1\, now()\, now()\, 1\, 0 from users
#-# The well knows information to replace the ? with
authentication.jdbc.insert.parameters=login,password,email,name,login

## UPDATE QUERY

#-# The query to use to update JDBC user when the XWiki user has been modified
authentication.jdbc.update.query=UPDATE users SET mail=?\, name=?\, password=IF(? IS NULL\, password\, ?)\, modification_time=now() WHERE login=?
#-# The well knows information to replace the ? with
authentication.jdbc.update.parameters=email,name,password,password,login

## DELETE QUERY

#-# The query to use to delete the JDBC user when the XWiki user has been deleted
authentication.jdbc.delete.query=DELETE from users WHERE login=?
#-# The well knows information to replace the ? with
authentication.jdbc.delete.parameters=login

= Creating your own password hashing implementation
1. Implement interface org.xwiki.contrib.authentication.jdbc.PasswordHasher
2. Make your custom hashing class into a component, add @Component, @Singleton annotation to the class
3. Name your class using @Named("hashing_scheme_name"), this name will be used in the property authentication.jdbc.password_hasher
4. Add full package name of the class to components.txt in META-INF directory, e.g. org.xwiki.contrib.authentication.jdbc.internal.Sha1Base64PasswordHasher

= Provided properties

authentication:
* login: login entered in the login form
* password: password entered in the login form

save:
* all XWiki.XWikiUsers fields (email, first_name...)
* login: the id of the user (the name of the XWiki document)
* name: a combination of the XWiki.XWikiUsers first_name and last_name fields

creation:
* all the save properties
* password: password entered in the register form

= TODO

* more encrypted passwords
* add support for more than just String properties