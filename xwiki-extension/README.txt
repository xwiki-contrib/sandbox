= INSTALL

== Framework

* You can use mvn dependency:copy-dependencies to get all needed dependencies jars
* Put all that and the extension-manager jar in WEB-INF/lib/

== UI

* Import xwiki-extension-ui xar
* Go to ExtensionManager.WebHome page
** resolve: check extension dependencies and show it as a tree
** install: install an extension and all its dependencies

= TODO

* miscellaneous rights protections in extension manager script service
* find a way to not loose initialization events like ApplicationStartedEvent
* directly download into XWiki local repository instead of having an Aether local repository
* asynchronous install UI (resolve, download, etc...) with events
* xar installer
* uninstall
* local repository descriptors
* create install/uninstall plans
* install extension not coming from a repository