#!/bin/sh

## For velocidoc.sh to work you need to download the jars from http://dev.xwiki.org/xwiki/bin/download/Drafts/Documenting+XWiki+Velocity+Macros/velocidoc%2Dbin.zip
ORIGDIR=`pwd`
cd `dirname $0`
DIR=.
java -cp $DIR/:$DIR/ws-commons-util-1.0.2.jar:$DIR/xmlrpc-common-3.1.jar:$DIR/xmlrpc-client-3.1.jar:$DIR/swizzle-confluence-1.2-20080419-xwiki.jar:$DIR/xwiki-core-xmlrpc-client-1.5.jar:$DIR/xwiki-core-xmlrpc-model-1.5.jar:$DIR/commons-codec-1.3.jar:$DIR/jrcs.diff-0.4.2.jar:$DIR/jrcs.rcs-0.4.2.jar:$DIR/hibernate-3.1.2.jar:$DIR/ecs-1.4.2.jar:$DIR/commons-logging-1.1.jar:$DIR/commons-logging-api-1.1.jar:$DIR/servlet-api-2.4.jar:$DIR/dom4j-1.6.1.jar:$DIR/xwiki-core-1.4.jar:$DIR/commons-lang-2.4.jar:$DIR/commons-collections-3.2.1.jar:$DIR/oro-2.0.8.jar:$DIR/velocity-1.5.jar:$DIR/xwiki-plugin-velocidoc-0.1-SNAPSHOT.jar net.sourceforge.velocidoc.Velocidoc $*
cd $ORIGDIR
