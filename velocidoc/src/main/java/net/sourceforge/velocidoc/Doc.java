package net.sourceforge.velocidoc;

import com.xpn.xwiki.XWikiContext;

import java.util.*;
/**
 * Company:  ObjectWave Corporation
 * 
 * @author Trever M. Shick
 * @version 0.1 alpha
 */
public class Doc {

    /**
	 * This vector contains a list of errors that occur during document generation
	 */
    protected Vector errors = new Vector();
	/**
	 * this vector contains a list of warnings that occur during the document generation
	 */
    protected Vector warnings = new Vector();
	/**
	 * The string options from the command line...
	 */
    static String[][] options;

	/**
	 * default constructor
	 */
    public Doc() {
    }
}
