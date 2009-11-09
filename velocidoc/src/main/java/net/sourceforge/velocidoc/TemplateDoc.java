package net.sourceforge.velocidoc;

import java.io.*;
import java.util.*;
import net.sourceforge.velocidoc.parser.*;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import org.xwiki.xmlrpc.model.XWikiPage;
import org.apache.xmlrpc.XmlRpcException;

/**
 * The template doc represents a VM template file
 *
 * Company:  ObjectWave Corporation
 *
 * @version 0.1 alpha
 */
public class TemplateDoc extends Doc implements Comparable {
    /**
     * This is any formal comments that are contained in the tempalte
     * that are not "attached" to a macro
     */
    Vector commentDocs = new Vector();
    /**
     * This is the package that this template is contained within
     */
    PackageDoc packageDoc;

    /**
     * This is the DocTemplate node constructed by the parser.
     */
    DocTemplate docTemplate = null;
    /**
     * All the macros contained within this template
     */
    Vector macroDocs = new Vector();

    /**
     * All the macros contained within this template
     */
    Vector macroUsageDocs = new Vector();

    /**
     * The name of the template, including the file extension
     */
    String name = null;


    /**
     * Constructs a TemplateDoc object with the given file and parent packagespec
     *
     * @param parent The package that this template is contained within
     * @param f The .vm file ot read
     */
    public TemplateDoc(PackageDoc parent, File f) {
        System.out.println("Working on Template: " + f.getName());
        try {
            name= f.getName();
            this.packageDoc = parent;
            InputStream is = getContentInputStream(f);
            parseContent(is);
        } catch (Exception e) {
            e.printStackTrace();
            errors.add(e.toString());
        }
    }

      /**
     * Constructs a TemplateDoc object with the given file and parent packagespec
     *
     * @param parent The package that this template is contained within
     * @param pageName The wiki page Name
     */
    public TemplateDoc(PackageDoc parent, String pageName, XWikiContext context) {
        System.out.println("Working on Template: " + pageName);
        try {
            packageDoc = parent;
            name = pageName;
            InputStream is = getContentInputStream(pageName, context);
            if (is!=null)
                parseContent(is);
        } catch (Exception e) {
            e.printStackTrace();
            errors.add(e.toString());
        }
    }

    private void parseContent(InputStream is) throws Exception {
        MacroParser parser = new MacroParser(is);
        docTemplate = parser.parse();
        docTemplate.dump("");
        for (int i=0;i < docTemplate.getMacroCount();i ++) {
            DocMacro docMacro = docTemplate.getMacro(i);
            if ((docMacro!=null)&&(docMacro.getName()!=null)) {
                MacroDoc macroDoc = new MacroDoc( this, docMacro);
                macroDocs.add(macroDoc);
            }
        }
        System.out.println("Adding macro usage in template " + getName() + " "  + docTemplate.getMacroUsageCount());        
        for (int i=0;i < docTemplate.getMacroUsageCount();i ++) {
            DocMacroUsage docMacro = docTemplate.getUsageMacro(i);
            MacroDoc macroDoc = new MacroDoc( this, docMacro);
            macroUsageDocs.add(macroDoc);
        } 
        for (int i=0;i < docTemplate.getCommentCount(); i++) {
            DocFormalComment d = docTemplate.getComment(i);
            CommentDoc cd = new CommentDoc(d);
            this.commentDocs.add(cd);
        }
        System.out.println("Number of macros=" + macroDocs.size());
        System.out.println("Number of macros used=" + macroUsageDocs.size());
        System.out.println("Number of comments=" + commentDocs.size());
    }

    private InputStream getContentInputStream(String pageName, XWikiContext context) throws IOException, XWikiException {
        XWikiXmlRpcClient rpc  = (XWikiXmlRpcClient) context.get("velocidoc_xmlrpc");
        if (rpc!=null) {
            try {
                XWikiPage page = rpc.getPage(pageName);

                // Debug output
                File debugFile = new File("debug_wiki.txt");
                FileOutputStream fos = new FileOutputStream(debugFile);
                fos.write(page.getContent().getBytes());
                fos.close();
                return new ByteArrayInputStream(page.getContent().getBytes());
            } catch (XmlRpcException e) {
                System.err.println("Cannot read " + pageName);
                e.printStackTrace();
                return null;
            }
        }
        else {
            XWikiDocument doc =  context.getWiki().getDocument(pageName, context);
            return new ByteArrayInputStream(doc.getContent().getBytes());
        }
    }

    private InputStream getContentInputStream(File f) throws IOException, XWikiException {
        InputStream is;
        if (f.getName().endsWith(".vm")) {
            is = new FileInputStream(f);
        } else {
            String xml = getFileContent(f);
            XWikiDocument doc = new XWikiDocument();
            doc.fromXML(xml);
            is = new ByteArrayInputStream(doc.getContent().getBytes());
        }
        return is;
    }

    public static String getFileContent(File file) throws IOException
      {
          return getFileContent(new FileReader(file));
      }

      public static String getFileContent(Reader reader) throws IOException
      {
          StringBuffer content = new StringBuffer();
          BufferedReader fr = new BufferedReader(reader);
          String line;
          line = fr.readLine();
          while (true) {
              if (line == null) {
                  fr.close();
                  return content.toString();
              }
              content.append(line);
              content.append("\n");
              line = fr.readLine();
          }
      }

    /**
     * returns the parent PackageDoc object
     *
     * @return The parent PackageDoc
     */
    public PackageDoc getPackage() {
        return packageDoc;
    }
    /**
     * returns an array of all the macros contained in this template
     *
     * @return the macros from this template
     */
    public MacroDoc[] getMacros() {
        MacroDoc[] ret = new MacroDoc[macroDocs.size()];
        macroDocs.copyInto(ret);
        return ret;
    }

    /**
     * returns an array of all the macros used contained in this template
     *
     * @return the macros from this template
     */
    public MacroDoc[] getMacroUsages() {
        MacroDoc[] ret = new MacroDoc[macroUsageDocs.size()];
        macroUsageDocs.copyInto(ret);
        return ret;
    }

    public CommentDoc getComment() {
        if (commentDocs.size() == 0) return null;
        return (CommentDoc) commentDocs.get(0);
    }
    /**
     *
     */
    public CommentDoc[] getComments() {
        CommentDoc[] ret = new CommentDoc[commentDocs.size()];
        commentDocs.copyInto(ret);
        return ret;
    }
    /**
     * returns the name of this template, including the file extension
     *
     * @return the name of this template
     */
    public String getName() {
        return name;
    }
    public String toString() {
        return getName();
    }
    public int compareTo(Object o) {
        return toString().compareTo(String.valueOf(o));
    }
}
