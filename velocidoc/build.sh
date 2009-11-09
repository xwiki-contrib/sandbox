#!/bin/sh
mvn javacc:jjtree 
cp target/generated-sources/jjtree/net/sourceforge/velocidoc/parser/doc.jj src/main/javacc/net/sourceforge/velocidoc/parser/doc.jj 
mvn javacc:javacc install
cp target/xwiki-plugin-velocidoc-0.1-SNAPSHOT.jar dist/bin/
