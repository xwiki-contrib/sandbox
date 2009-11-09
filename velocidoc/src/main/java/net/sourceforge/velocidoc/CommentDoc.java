package net.sourceforge.velocidoc;

import net.sourceforge.velocidoc.parser.*;
import java.util.*;
/**
 * Company:  ObjectWave Corporation
 *
 * @author Trever M. Shick
 * @version 0.1 alpha
 */
public class CommentDoc extends Doc {
    /**
     * The content of the formal comment block in the VTL
     */
    String content;
    /**
     * The parameters specified with @param tags
     */
    ParamDoc[] paramDocs;
    /**
     * The custom tags defined in the document
     */
    TagDoc[] tagDocs;
    /**
     * Returns all the tags from the VTL Comment Block
     *
     * @return an array of tagdocs that represent custom tags in the document
     */
    public TagDoc[] getTags() {
        //System.out.println("CommentDoc::getTags, length=" + tagDocs.length);
        return tagDocs;
    }
    /**
     * Returns the tags from the VTL comment block that match the given name
     *
     * @param tagname The name of the tags to retrieve (ex.  @usage)
     * @param the name of the tagdocs to retrieve
     * @return an array of tagdocs with the matching name
     */
    public TagDoc[] getTags(String tagname) {
        Vector v = new Vector();
        for (int i=0;i < getTags().length; i++) {
            if (getTags()[i].getName().equals(tagname)) {
                v.add(getTags()[i]);
            }
        }
        TagDoc[] ret = new TagDoc[v.size()];
        v.copyInto(ret);
        return ret;
    }

    public TagDoc getTag(String tagName) {
        System.out.println("CommentDoc::getTag, arg=" + tagName);
        for (int i=0;i < getTags().length; i++) {
            System.out.println("Tag Name:" + getTags()[i].getName());
            if (getTags()[i].getName().equals(tagName)) {
                System.out.println("Returning:" + getTags()[i]);
                return (getTags()[i]);
            } else {
                System.out.println("tagName:" + getTags()[i].getName() + "*");
                System.out.println("Arg:" + tagName + "*");
                System.out.println(getTags()[i].getName().equals(tagName));
            }
        }
        return null;
    }
    /**
     * Returns all the @param tags from the VTL Comment Block
     *
     * @return all the params specified in the VTL comment block
     */
    public ParamDoc[] getParams() {
        //System.out.println("CommentDoc::getParams, length=" + paramDocs.length);
        return paramDocs;
    }
    /**
     * Constructs a CommentDoc object from the DocFormalComment parser node
     *
     * @param node The DocFormalComment node created by the JJTree parser
     */
    public CommentDoc(DocFormalComment node) {
        if (node == null) {
            content = "";
            paramDocs = new ParamDoc[0];
            tagDocs = new TagDoc[0];
        } else {
            content = node.getComment();
            paramDocs = new ParamDoc[node.getParamCount()];
            //System.out.println("CommentDoc::paramDocs.length=" + paramDocs.length);
            for (int i=0;i < node.getParamCount(); i++) {
                DocParam docParam = node.getParam(i);
                paramDocs[i] = new ParamDoc(docParam);
            }
            tagDocs = new TagDoc[node.getTagCount()];
            //System.out.println("CommentDoc::tagDocs.length=" + tagDocs.length);
            for (int i=0;i < node.getTagCount(); i++) {
                DocTag docTag = node.getTag(i);
                tagDocs[i] = new TagDoc(docTag);
            }
        }
    }
    /**
     * Returns the content of the VTL comment block less tags
     *
     * @return the content of the VTL Comment Block less params and tags
     */
    public String getContent() {
        return content;
    }
    /**
     * Called by the Parser
     *
     * @param newContent The content of the VTL Comment Blocks
     */
    public void setContent(String newContent) {
        //System.out.println("CommentDoc::setContent newContent=" + newContent);
        content = newContent;
    }

}
