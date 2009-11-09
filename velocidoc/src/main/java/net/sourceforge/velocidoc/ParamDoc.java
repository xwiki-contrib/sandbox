package net.sourceforge.velocidoc;

import net.sourceforge.velocidoc.parser.DocParam;
/**
 * Company:  ObjectWave Corporation
 *
 * @author Trever M. shick
 * @version 0.1 alpha
 */

public class ParamDoc extends Doc {
    /**
     * The docParam node created by the parser
     */
    DocParam docParam = null;
    /**
     * constructs an instance of this object with the given
     * docparam node created by the parser
     *
     * @param docParam The DocParam node created by the parser
     */
    public ParamDoc(DocParam docParam) {
        this.docParam = docParam;
    }
    /**
     * returns the name of the parameter
     * "@param my-name my-description
     * returns "my-name"
     *
     * @return the name of the Parameter
     */
    public String getName() {
        return docParam.getName();
    }
    /**
     * returns the description of the param tag
     * "@param my-name my-desc
     * would return "my-desc"
     *
     * @return the parameter description
     */
    public String getDescription() {
        return docParam.getDescription();
    }

    public String toString() {
        return getName();
    }

}
