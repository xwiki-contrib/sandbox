package net.sourceforge.velocidoc;

import com.xpn.xwiki.XWikiContext;

import java.util.*;
import java.io.*;
/**
 * The root doc is the starting point for document information.
 * This is placed into the VelocityContext and thus you have access to
 * the root doc via the variable reference "$rootDoc"
 *
 * Company:  ObjectWave Corporation
 *
 * @author Trever M. Shick
 * @version 0.1 alpha
 */

public class RootDoc extends Doc {

    /**
     * This is the root package constructed from the
     * source directory,
     * this is always named "default"
     */
    PackageDoc rootPackage = null;

    /**
     * constructs an instance of this class with the given root directory
     *
     * @param rootDir The source directory to start reading VM files from
     */
    public RootDoc(File rootDir, String name, XWikiContext context) {
        rootPackage = new PackageDoc(null, rootDir, (name==null) ? "main" : name, true, context);
    }

    /**
     * returns all templates gathered in an array.
     * the templates are gathered recursively
     *
     * @return returns all templates recursively
     */
    public TemplateDoc[] getAllTemplates() {
        PackageDoc[] pds = this.getAllPackages();
        Vector v = new Vector();
        for (int i=0;i < pds.length; i++) {
            PackageDoc pd = pds[i];
            TemplateDoc[] tds = pd.getTemplates();
            for (int j=0; j < tds.length; j++){
                v.add(tds[j]);
            }
        }
        TemplateDoc[] ret = new TemplateDoc[v.size()];
        try {
            Collections.sort(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
        v.copyInto(ret);
        return ret;
    }

    /**
     * returns all packages contained in the hierarchy.
     * these packages are constructed recursively
     *
     * @return an array of packages
     */
    public PackageDoc[] getAllPackages() {
        Vector all = new Vector();
        all.add(this.rootPackage);
        for (int i=0;i < rootPackage.getAllPackages().length;i++) {
            all.add(this.rootPackage.getAllPackages()[i]);
        }
        Collections.sort(all);
        PackageDoc[] ret = new PackageDoc[all.size()];
        try {
            Collections.sort(all);
        } catch (Exception e) {
            e.printStackTrace();
        }
        all.copyInto(ret);
        return ret;
    }

    /**
     * returns the template with the specified name
     *
     * @param name the name of the template to match
     * @return the template with the given name
     */
    public TemplateDoc getTemplateNamed(String name) {
        TemplateDoc[] td = this.getAllTemplates();
        for (int i=0;i < td.length; i++) {
            if (td[i].getName().equals(name))
                return td[i];
        }
        return null;
    }
    /**
     * returns an array of all macros contained within the object map.
     * the macros are gathered recursively from all the templates
     *
     * @return returns all macros from the object map
     */
    public MacroDoc[] getAllMacros() {
        Vector v = new Vector();
        TemplateDoc[] pdocs = this.getAllTemplates();
        for (int i=0;i < pdocs.length;i++) {
            TemplateDoc pd = pdocs[i];
            MacroDoc[] mdocs = pd.getMacros();
            for (int j=0; j < mdocs.length; j++) {
                v.add(mdocs[j]);
            }
        }
        MacroDoc[] ret = new MacroDoc[v.size()];
        try {
            Collections.sort(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
        v.copyInto(ret);
        return ret;
    }

        /**
     * returns an array of all macros contained within the object map.
     * the macros are gathered recursively from all the templates
     *
     * @return returns all macros from the object map
     */
    public MacroDoc[] getAllMacroUsages() {
        Vector v = new Vector();
        TemplateDoc[] pdocs = this.getAllTemplates();
        for (int i=0;i < pdocs.length;i++) {
            TemplateDoc pd = pdocs[i];
            MacroDoc[] mdocs = pd.getMacroUsages();
            for (int j=0; j < mdocs.length; j++) {
                v.add(mdocs[j]);
            }
        }
        MacroDoc[] ret = new MacroDoc[v.size()];
        try {
            Collections.sort(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
        v.copyInto(ret);
        return ret;
    }
}
