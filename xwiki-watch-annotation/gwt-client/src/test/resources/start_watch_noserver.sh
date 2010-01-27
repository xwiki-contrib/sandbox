#!/bin/bash

XE_VERSION=1.8.1;
WATCH_VERSION=1.1-SNAPSHOT;
GWT_VERSION=1.5.3;

JAVA32_HOME=/usr/lib/jvm/java-6-sun/bin;
M2_REPO=~/.m2/repository;
APP_DIR=`dirname $0`/webapps/xwiki;
WATCH_PATH=bin/download/WatchCode/GWT/watch.zip;

$JAVA32_HOME/java \
-Xmx1024m \
-cp \
$APP_DIR/WEB-INF/lib/xwiki-watch-gwt-client-$WATCH_VERSION.jar:\
$APP_DIR/WEB-INF/lib/xwiki-watch-gwt-server-$WATCH_VERSION.jar:\
$M2_REPO/com/xpn/xwiki/products/xwiki-watch-gwt-client/$WATCH_VERSION/xwiki-watch-gwt-client-$WATCH_VERSION-sources.jar:\
$M2_REPO/com/xpn/xwiki/platform/xwiki-web-gwt/$XE_VERSION/xwiki-web-gwt-$XE_VERSION-sources.jar:\
$M2_REPO/com/google/gwt/gwt-dev/$GWT_VERSION/gwt-dev-$GWT_VERSION-linux.jar:\
$M2_REPO/com/google/gwt/gwt-user/$GWT_VERSION/gwt-user-$GWT_VERSION.jar:\
$M2_REPO/net/sf/gwt-widget/gwt-widgets/0.2.0/gwt-widgets-0.2.0.jar \
com.google.gwt.dev.GWTShell \
-logLevel WARN \
-style DETAILED \
-noserver \
-port 8080 \
-out . \
xwiki/$WATCH_PATH/Watch.html
