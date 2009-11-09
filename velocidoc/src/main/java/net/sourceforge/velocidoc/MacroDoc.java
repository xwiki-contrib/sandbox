package net.sourceforge.velocidoc;

import net.sourceforge.velocidoc.parser.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Vector;

/**
 * 	Company:  ObjectWave Corporation
 *
 * @author Trever M. Shick
 * @version 0.1 alpha
 */
public class MacroDoc extends Doc implements Comparable {
    /**
     * The DocMacro node created by the parser
     */
    DocMacro docMacro;
    /**
     * The comment Doc node created by the parser
     */
    CommentDoc commentDoc;
    /**
     *
     */
    TemplateDoc parentDoc;
    /**
     * An array of argument names
     */
    String[] arguments;
    /**
     * returns the arguments specified in the macro definition
     *
     * @return an array of argument names
     */
    public String[] getArguments() {
        return arguments;
    }

    /**
     * returns the comment doc object created by the parser
     * associated with this macrodoc object
     *
     *
     * @return a CommentDoc object
     */
    public CommentDoc getComment() {
        return commentDoc;
    }
    public TemplateDoc getTemplate() {
        return this.parentDoc;
    }
    /**
     * constructs a MacroDoc with the specified DocMacro node created by the parser
     *
     * @param docMacro The DocMacro node created by the parser
     */
    public MacroDoc(TemplateDoc templateDoc, DocMacro docMacro) {
        this.docMacro = docMacro;
        this.parentDoc = templateDoc;
        arguments = new String[docMacro.getArgumentCount()];
        for (int i=0;i < docMacro.getArgumentCount();i ++) {
            DocMacroArgument docMacroArgument = docMacro.getArgument(i);
            arguments[i] = docMacroArgument.getName();
        }
        System.out.println("MacroDoc::arguments = " + arguments.length);
        commentDoc = new CommentDoc(docMacro.getComment());

    }

    /**
      * constructs a MacroDoc with the specified DocMacro node created by the parser
      *
      * @param docMacroUsage The DocMacro Usage node created by the parser
      */
     public MacroDoc(TemplateDoc templateDoc, DocMacroUsage docMacroUsage) {
         this.docMacro = new DocMacro(null, 0);
         this.docMacro.setMacroName(docMacroUsage.getMacroName());
         this.docMacro.setMacroBody("");
         this.parentDoc = templateDoc;
     }


    /**
     * returns the name of the macro
     *
     * @return The name of the macro
     */
    public String getName() {
        return docMacro.getName();
    }
    /**
     * returns the body of the macro
     *
     * @return The body of the macro
     */
    public String getBody() {
        return docMacro.getBody();
    }
    /**
     * returns the argument name at the specified index
     *
     * @param i the index of the argument name to get
     * @return an argument name
     */
    public String getArgument(int i) {
        return arguments[i];
    }
    /**
     * the number of arguments specified in the macro definition
     *
     * @return the number of arguments
     */
    public int getArgumentCount() {
        if (arguments == null) return 0;
        return arguments.length;
    }

    public String toString() {
        return getName();
    }
    public int compareTo(Object o) {
        return (this.toString().compareTo(String.valueOf(o)));
    }

    /**
     * Get the list of templates using this macro
     */
    public TemplateDoc[] getUsages() {
        Vector usages = new Vector();
        PackageDoc rootPackage = parentDoc.packageDoc.rootPackage;
        MacroDoc[] macroUsages = rootPackage.getMacroUsages();
        System.out.println("Looking through " + macroUsages.length + " for macro " + getName());
        for (int i=0;i<macroUsages.length;i++) {
            System.out.println("Comparing " + macroUsages[i].getName() + " and " + getName());
            if (macroUsages[i].getName().equals(getName())) {
              TemplateDoc tdoc = macroUsages[i].getTemplate();
                System.out.println("Adding " + tdoc.getName());
                if (!usages.contains(tdoc))
                usages.add(tdoc);
            }
        }
        TemplateDoc[] result = new TemplateDoc[usages.size()];
        usages.copyInto(result);
        return result;
    }
}
