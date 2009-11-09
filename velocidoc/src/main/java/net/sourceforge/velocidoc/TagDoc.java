package net.sourceforge.velocidoc;

import net.sourceforge.velocidoc.parser.*;
/**
 * Represents a custom tag within a VTL comment block
 * #**
 *  * @mytag
 *  *#
 * Company:  ObjectWave Corporation
 * 
 * @author Trever M. Shick
 * @version 0.1 alpha
 */
public class TagDoc extends Doc {
	/**
	 * The parser tree node used for information
	 */
    DocTag docTag = null;
	/**
	 * Constructs a TagDoc with the given DocTag tree node
	 * 
	 * @param docTag The DocTag node created by the parser
	 */
    public TagDoc(DocTag docTag) {
        this.docTag = docTag;
    }
	/**
	 * Returns the name of the tag
	 * 
	 * @return returns the custom tag name, including the "@"
	 */
    public String getName() {
        return docTag.getTagName();
    }
	/**
	 * Returns the value of the tag
	 * "@mytag the description
	 * would return "the description"
	 * 
	 * @return the tag value
	 */
    public String getValue() {
        return docTag.getValue();
    }

}
