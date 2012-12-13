# XWiki IP Authenticator

IP address based authentication.
When you open a wiki page, you're automatically logged in as your IP address with dots converted to underscores.

## Configuration

1. Create a new user from the admin interface which is your IP address with dots converted to underscores.
2. Make this user an administrator.
3. Shut down the wiki and edit xwiki.cfg
Add this: `xwiki.authentication.authclass=org.xwiki.contrib.authentication.ip.XWikiIPAuthenticator`
4. Restart the wiki and you will be automatically logged in as your IP address.

## Q&A

* OMG IP Addresses are so insecure!?!!1
 * [not necessarily][cjdns].

* What about logging in normally while it's running?
 * todo


## Todo

* Set user's firstname to their unescaped IP address so it displays better.
* Remove logout button.
* Allow normal login while IP auth is in effect.


[cjdns]: https://github.com/cjdelisle/cjdns
