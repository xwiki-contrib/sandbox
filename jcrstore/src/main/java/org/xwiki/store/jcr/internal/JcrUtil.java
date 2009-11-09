package org.xwiki.store.jcr.internal;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

public class JcrUtil
{
    private JcrUtil() {};

    public static Node createNodeHierarhy(Node root, String path, String... nodeTypes) throws RepositoryException
    {
        int ind = 0;
        for (String s : splitPath(path)) {
            try {
                Node n = root.getNode(s);
                root = n;
            } catch (PathNotFoundException e) {
                if (ind < nodeTypes.length) {
                    root = root.addNode(s, nodeTypes[ind]);
                } else {
                    root = root.addNode(s);
                }
            }
            ind++;
        }
        return root;
    }

    public static String[] splitPath(String path)
    {
        // TODO: encoding
        return StringUtils.split(path, '/');
    }
}
