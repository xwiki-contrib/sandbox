= INSTALL

== Framework

* You can use mvn dependency:copy-dependencies to get all needed dependencies jars
* Put all that and the extension-manager jar in WEB-INF/lib/

== UI

* Import xwiki-extension-ui xar
* Go to ExtensionManager.WebHome page
** resolve: check extension dependencies and show it as a tree
** install: install an extension and all its dependencies