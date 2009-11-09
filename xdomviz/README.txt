= Introduction =

XDOMViz is a small utility that is able to parse a text using the XWiki
rendering engine and to produce a GraphViz file representing the structure
of the XDOM.

This utility can be used for debugging purposes in order to visualize
graphically the structure of the XDOM.

A shell script 'xdomviz.sh' is provided to run the application. 
Be sure to do a 'mvn install' before running 'xdomviz.sh'

The script accepts the following parameters:

xdomviz.sh [options] [inputFile]

If the input file is not specified, the text is read from stdin.
This makes xdomviz.sh "pipeable"

Options are:

* -h Displays help
* -o outputFile Write the result in outputFile. 
                If this option is not specified then stdout is used.
* -n Normalize the XDOM

== XDOM Normalization ==

The XDOM build by the rendering engine might contain the same node
that could be the child of different parents (i.e., a single node 
that have multiple parents). The API for retrieving the parent is 
able to return only one parent. This fact might lead to a confusing
representation.

When an XDOM is normalized, all the nodes that have multiple parents are
duplicated so that the resulting representation will only have nodes that
have at most one parent.

To check what's described you can run the following tests:

echo "**This is bold**" | xdomviz.sh -o standard.gv
echo "**This is bold**" | xdomviz.sh -o normalized.gv -n

and look at the two resulting representations.   