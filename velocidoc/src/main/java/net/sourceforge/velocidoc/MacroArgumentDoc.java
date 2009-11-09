package net.sourceforge.velocidoc;

import net.sourceforge.velocidoc.parser.*;
/**
 * Company:  ObjectWave Corporation
 *
 * @version 0.1 alpha
 */

public class MacroArgumentDoc extends Doc {
    /**
     * The DocMacroArgument node created by the parser
     */
    DocMacroArgument arg;

    /**
     * constructs an instance of the MacroArgumentDoc with the
     * given node as the document source
     *
     * @param dma The tree node created by the parser
     */
    public MacroArgumentDoc(DocMacroArgument dma) {
        this.arg = dma;
    }
    /**
     * returns the name of the macro argument
     * #macro (test $arg1 $arg2)
     * returns arg1
     *
     * @return the name of the macro argument
     */
    public String getName() {
        return arg.getName();
    }

    public String toString() {
        return getName();
    }
}
