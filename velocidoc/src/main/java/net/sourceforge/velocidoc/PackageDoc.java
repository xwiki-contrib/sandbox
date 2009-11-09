package net.sourceforge.velocidoc;

import com.xpn.xwiki.XWikiContext;

import java.io.*;
import java.util.*;

import org.codehaus.swizzle.confluence.SpaceSummary;
import org.codehaus.swizzle.confluence.SearchResult;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;

/**
 * this should be extended to pull the package.html
 * file and allow access to it.
 * company:  ObjectWave Corporation
 *
 * @author Trever M. Shick
 * @version 0.1 alpha
 */
public class PackageDoc extends Doc implements Comparable {
    /**
     * The basename of the package
     */
    String packageName = null;
    /**
     * The name of the parent package
     */
    String parentName = null;
    /**
     * All the sub packages in this package
     */
    PackageDoc[] subPackages = null;

    /**
     * Root Package of this hierarchy
     */
    PackageDoc rootPackage;

    /**
     * The templates contained within this package
     */
    TemplateDoc[] templates = null;


    /**
     * Root package
     */
    boolean root = false;

    /**
     * Constructs a root packagedoc starting in this
     * directory with this name.  This is used from the
     * RootDoc with the source_directory and "Default" as the
     * two parameters.
     *
     * @param dir The root directory
     * @param packageName the name to use for this package, generall "default"
     */
    public PackageDoc(PackageDoc rootPackage, File dir, String packageName, XWikiContext context) {
        this(rootPackage, dir, packageName, false, context);
    }

    /**
     * Constructs a root packagedoc starting in this
     * directory with this name.  This is used from the
     * RootDoc with the source_directory and "Default" as the
     * two parameters.
     *
     * @param dir The root directory
     * @param packageName the name to use for this package, generall "default"
     */
    public PackageDoc(PackageDoc rootPackage, File dir, String packageName, boolean root, XWikiContext context) {
        this(rootPackage, null, dir, packageName, root, context);
    }

    /**
     * constructs a PackageDoc with the given parent name
     * starting at the specified directory and the flag specifies if
     * it's the root package or not.
     *
     * @param parentName the name of the parent package
     * @param dir the directly to load from
     * @param root true if this is the root package, false otherwise
     */
    public PackageDoc(PackageDoc rootPackage, String parentName, File dir, String packageName, boolean root, XWikiContext context) {
        this.rootPackage = (rootPackage==null) ? this : rootPackage;
        System.out.println("Working on package " + packageName);
        this.root = root;
        this.parentName = parentName;
        this.packageName = packageName;
        if (this.packageName==null)
         this.packageName = (dir==null) ? "" : dir.getName();
        Vector subdirs = new Vector();
        Vector templates = new Vector();
        // get the list of subdirectories, subspaces, templates or wiki pages
        getSubdirectoriesAndTemplates(dir, subdirs, templates, context);
        subPackages = new PackageDoc[subdirs.size()];
        for (int i=0;i < subdirs.size(); i++) {
            if (context==null) {
                File subdir = (File) subdirs.get(i);
                subPackages[i] = new PackageDoc(rootPackage, this.getPackageName(), subdir, subdir.getName(), false, context);
            } else {
                String spaceName = (String) subdirs.get(i);
                subPackages[i] = new PackageDoc(rootPackage, this.getPackageName(), null, spaceName, false, context);
            }
        }
        List tlist = new ArrayList();
        for (int i=0;i < templates.size();i++) {
            TemplateDoc tdoc;
            if (context==null) {
                File f = (File) templates.get(i);
                tdoc = new TemplateDoc(this, f);
            } else {
                String pageName = (String) templates.get(i);
                tdoc = new TemplateDoc(this, pageName, context);
            }
            if ((tdoc.getMacros().length>0)||(tdoc.getMacroUsages().length>0)||(tdoc.getComment()!=null))  {
                System.out.println("Adding Template: " + tdoc.getName());
                tlist.add(tdoc);
            }
        }
        this.templates = new TemplateDoc[tlist.size()];
        for (int i=0;i<tlist.size();i++) {
            this.templates[i] = (TemplateDoc) tlist.get(i);
        }
    }
    /**
     * Returns the templates in this package
     *
     * @return returns all the templates in the package, not subpackages
     */
    public TemplateDoc[] getTemplates() {
        return templates;
    }
    /**
     * returns an array of MacroDocs from all the templates in
     * this package.
     *
     * @return all the macros from this package
     */
    public MacroDoc[] getMacros() {
        TemplateDoc[] tdocs = this.getTemplates();
        Vector v = new Vector();
        for (int i=0;i < tdocs.length; i++) {
            MacroDoc[] mdocs = tdocs[i].getMacros();
            for (int j=0;j < mdocs.length; j++) {
                v.add(mdocs[j]);
            }
        }
        MacroDoc[] ret = new MacroDoc[v.size()];
        v.copyInto(ret);
        return ret;
    }

    /**
     * returns an array of MacroDocs from all the templates in
     * this package.
     *
     * @return all the macros from this package
     */
    public MacroDoc[] getMacroUsages() {
        TemplateDoc[] tdocs = this.getTemplates();
        Vector v = new Vector();
        for (int i=0;i < tdocs.length; i++) {
            MacroDoc[] mdocs = tdocs[i].getMacroUsages();
            for (int j=0;j < mdocs.length; j++) {
                v.add(mdocs[j]);
            }
        }
        MacroDoc[] ret = new MacroDoc[v.size()];
        v.copyInto(ret);
        return ret;
    }

    /**
     * returns all the sub-packages for this package
     * call this on "com" would return com.test, and com.test2
     *
     * @return All the subpackages directly under this pakcage
     */
    public PackageDoc[] getPackages() {
        return this.subPackages;
    }
    /**
     * recursively returns all packages under this package
     *
     * @return all subpackages o fthis package, recursively
     */
    public PackageDoc[] getAllPackages() {
        Vector al = new Vector();
        for (int i=0;i < subPackages.length; i++) {
            PackageDoc packageDoc = subPackages[i];
            if (packageDoc.getTemplates().length>0)
               al.add(packageDoc);
            PackageDoc[] packs = packageDoc.getAllPackages();
            for (int j=0;j < packs.length;j ++)
                al.add(packs[j]);
        }
        PackageDoc[] ret= new PackageDoc[al.size()];
        al.copyInto(ret);
        return ret;
    }


    private void getSubdirectoriesAndTemplates(File dir, Vector subdirs, Vector templates, XWikiContext context) {
        if (context==null) {
            File[] files = dir.listFiles();
            for (int i=0;i < files.length;i++) {
                File f = files[i];
                String sname = f.getName().toLowerCase();
                if (f.isDirectory() && !sname.equalsIgnoreCase("cvs")  && !sname.startsWith(".svn"))
                    subdirs.add(f);
                else if (f.isFile()&& !sname.endsWith(".txt")&& !sname.endsWith(".gif")&& !sname.endsWith(".jpg")
                        && !sname.endsWith(".jpeg")&& !sname.endsWith(".png")&& !sname.endsWith(".css")&& !sname.endsWith("js"))
                    templates.add(f);
            }
        } else {
            List spaces = (List) context.get("spaces");
            if (this.root) {
                Collection spacesNames = getSpaceNames(context);
                Iterator it = spacesNames.iterator();
                while (it.hasNext()) {
                    String spaceName = (String) it.next();
                    if ((spaces==null)||(spaces.contains(spaceName)))
                        subdirs.add(spaceName);
                }
            } else {
                templates.addAll(getPageNames(packageName, context));
            }
        }
    }

    private Collection getSpaceNames(XWikiContext context) {
        try {
            XWikiXmlRpcClient rpc  = (XWikiXmlRpcClient) context.get("velocidoc_xmlrpc");
            if (rpc!=null) {
                ArrayList list = new ArrayList();
                List list2 = rpc.getSpaces();
                for (int i=0;i<list2.size();i++) {
                    SpaceSummary space = (SpaceSummary) list2.get(i);
                    System.out.println("Adding space: " + space.getKey());
                    list.add(space.getKey());
                }
                return list;
            }
            else
             return context.getWiki().getSpaces(context);
        } catch (Exception e) {
            System.err.println("Error getting space names: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList();
        }
    }

    private Collection getPageNames(String spaceName, XWikiContext context) {
        try {
            XWikiXmlRpcClient rpc  = (XWikiXmlRpcClient) context.get("velocidoc_xmlrpc");
            if (rpc!=null) {
                ArrayList list = new ArrayList();
                List list2a = rpc.search("#", 1000);
                for (int i=0;i<list2a.size();i++) {
                    SearchResult sr = (SearchResult) list2a.get(i);
                    if (sr.getId().startsWith(spaceName + "."))
                        list.add(sr.getId());
                }
                /*
                List list2b = rpc.search("#*", 1000);
                for (int i=0;i<list2b.size();i++) {
                    SearchResult sr = (SearchResult) list2b.get(i);
                    if (!list.contains(sr.getId())&&sr.getId().startsWith(spaceName + "."))
                        list.add(sr.getId());
                } */
                return list;
            }
            else {
                ArrayList list = new ArrayList();
                List list2 = context.getWiki().getStore().searchDocumentsNames("where doc.content like '%#%' and doc.web='" + spaceName + "'", context);
                for (int i=0;i<list2.size();i++) {
                    String pageName = (String) list2.get(i);
                    list.add(pageName);
                }
                return list;
            }
        } catch (Exception e) {
            System.err.println("Error getting space page names: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList();
        }
    }

    /**
     * returns the FQN of the package.
     *
     * @return the package name
     */
    public String getPackageName() {
        if (parentName != null)
            return parentName + "." + packageName;
        else
            return packageName;
    }
	/**
	 * @return The package name
	 */
    public String toString() {
        return this.getPackageName();
    }
    /**
     * @see java.lang.Comparable#compareTo(Object)
     */
    public int compareTo(Object o) {
        return (this.toString().compareTo(String.valueOf(o)));
    }
}
