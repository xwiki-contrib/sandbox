package net.sourceforge.velocidoc;

import org.apache.velocity.*;
import org.apache.velocity.texen.Generator;

import org.apache.velocity.app.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;

import java.util.*;
import java.io.*;

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.xpn.xwiki.XWikiContext;

/**
 * The Velocidoc class is the main tool class that is executed.
 * It's main method takes two arguments at this point,
 * the first argument - the source directory
 * the second argument - the destination directory
 *
 *
 *
 * Company:  ObjectWave Corporation
 *
 * @author Trever M. Shick
 * @version 0.1 alpha
 */

public class Velocidoc {
    private boolean zip = false;
    private String sourceDir = null;
    private String outputDir = null;
    private String controlFile = "net/sourceforge/velocidoc/template/control.vm";
    private String defaultName = "main";
    private String xwikiUser;
    private String xwikiPass;
    private String xwikiURL;
    private List spaceList;
    private XWikiContext context;
    private boolean debug = false;

    /**
     * default constructor, does nothing
     */
    public Velocidoc() {
    }


    /**
     * Make a temporary directory
     * @return
     */
    public File getTempDir() {
        File workDir;
        if ((context!=null)&&(context.getWiki()!=null)) {
            workDir = new File(context.getWiki().getTempDirectory(context),"velocidocTemp" + RandomStringUtils.randomAlphabetic(5)) ;
        } else {
            // Let's make a tempdir in java.io.tmpdir
            workDir = new File(System.getProperty("java.io.tmpdir"), "velocidocTemp"+ RandomStringUtils.randomAlphabetic(5));
        }
        
        if (workDir.exists()) {
            workDir.deleteOnExit();
        } else {
            workDir.mkdir();
            workDir.deleteOnExit();
        }

        return workDir;

    }

    /**
     * Generates the documentation with a File System Root Doc to a File System Root Doc
     * @exception Exception Upon any type of error
     */
    public File generate() throws Exception {
        File sourceDirectory = null;
        if (sourceDir==null) {
            if (context==null)
                context = new XWikiContext();
            if (xwikiURL!=null) {
                // let's init the XML-RPC connection
                XWikiXmlRpcClient rpc  = new XWikiXmlRpcClient(xwikiURL);
                rpc.login(xwikiUser, xwikiPass);
                context.put("velocidoc_xmlrpc", rpc);
            }
            if (getSpaceList()!=null)
               context.put("spaces", getSpaceList());
        } else {
            sourceDirectory = new File(this.sourceDir);
        }
        RootDoc rootDoc = new RootDoc(sourceDirectory, this.defaultName, context);

        File outputDirectory;
        if ((this.outputDir==null)||isZip()) {
            outputDirectory = getTempDir();
        } else {
            outputDirectory = new File(this.outputDir);
        }
        return generate(rootDoc, outputDirectory);
    }


    /**
     * Generates the documentation with a File System Root Doc to a ZIP file
     *
     * @exception Exception Upon any type of error
     */
    public void generateZipFile() throws Exception {
        byte[] zipdata = generateZip();

        System.out.println("Saving zip to file: " + outputDir);
        File outputFile = new File(outputDir);
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write(zipdata);
        fos.close();
    }
    /**
     * Generates the documentation with a File System Root Doc to a ZIP file
     *
     * @exception Exception Upon any type of error
     */
    public byte[] generateZip() throws Exception {
        File outputDirectory = generate();

        System.out.println("Generating zip from directory: " + outputDirectory.getName());
        // And now make a zip
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zos = new ZipOutputStream(baos);
        addDirectory(zos, outputDirectory);
        zos.close();
        return baos.toByteArray();
    }

    private void addDirectory(ZipOutputStream zos, File outputDirectory) throws Exception {
        // Create a buffer for reading the files
        byte[] buf = new byte[4096];
        // List the files in the directory
        File[] fileList = outputDirectory.listFiles();

        if (fileList!=null) {
            for (int i=0;i<fileList.length;i++) {
                File fileItem = fileList[i];
                FileInputStream in = new FileInputStream(fileItem);

                // Add ZIP entry to output stream.
                zos.putNextEntry(new ZipEntry(fileItem.getName()));

                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0) {
                    zos.write(buf, 0, len);
                }

                // Complete the entry
                zos.closeEntry();
                in.close();
            }
        }
    }

    /**
     * Generates the documentation with a File System Root Doc to a File System Root Doc
     * @exception Exception Upon any type of error
     */
    public File generate(File outputDirectory) throws Exception {
        File sourceDirectory = new File(this.sourceDir);
        RootDoc rootDoc = new RootDoc(sourceDirectory, this.defaultName, context);
        return generate(rootDoc, outputDirectory);
    }


    /**
     * Generates the documentation
     *
     * @param rootDoc rootDoc to start generation with
     * @param outputDirectory Where to output the HTML to
     **/ 
    public File generate(RootDoc rootDoc, File outputDirectory) throws Exception {
        Generator gen = null;
        try {
            Properties p = new Properties();
            p.setProperty("resource.loader", "class");
            p.setProperty("class.resource.loader.description","Velocity Classpath Resource Loader");
            p.setProperty("class.resource.loader.class","org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            p.setProperty("velocimacro.permissions.allowInline","true");
            Velocity.init(p);
            gen = Generator.getInstance();

            gen.setOutputPath(outputDirectory.getAbsolutePath());
            //log.debug("outputDir:" + outputDirectory.getAbsolutePath());
            gen.setTemplatePath("/");
            gen.setVelocityEngine(new VelocityEngine(p));

            VelocityContext vcontext = new VelocityContext();
            vcontext.put("rootDoc", rootDoc);
            System.out.println("Control file: " + controlFile);
            String s = gen.parse(controlFile, vcontext);
            System.out.println(s);
            return outputDirectory;
        } finally {
            if (gen!=null)
                gen.shutdown();
        }

    }

    private static final void parseArguments(String[] args, Velocidoc compiler) {
        for (int i=0;i < args.length; i++) {
            String a = args[i];
            if (a.equals("-src")) {
                String src = args[i+1];
                if (src.startsWith("http"))
                    compiler.setXwikiURL(src);
                else
                    compiler.setSourceDir(src);
            } else if (a.equals("-dst")) {
                compiler.setOutputDir(args[i+1]);
            } else if (a.equals("-name")) {
                compiler.setDefaultName(args[i+1]);
            } else if (a.equals("-user")) {
                compiler.setXwikiUser(args[i+1]);
            } else if (a.equals("-pass")) {
                compiler.setXwikiPass(args[i+1]);
            } else if (a.equals("-ctl")) {
                compiler.setControlFile(args[i+1]);
            } else if (a.equals("-pkg")) {
                compiler.setSpaceList(args[i+1]);
            } else if (a.equals("-z")) {
                compiler.setZip(true);
            } else if (a.equals("-h")) {
                printUsage();
                System.exit(0);
                // do nothing
            }
        }
    }

    public String validateConfig() {
        StringBuffer errors = new StringBuffer();
        try {
            if (xwikiURL==null) {
                File src = new File(this.sourceDir);
                System.out.println("Source Directory:" + src.getAbsolutePath());
                if (!src.exists()) errors.append("the source directory does not exist\n");
                if (!src.canRead()) errors.append("the source directory is not writable\n");
            }
            File dest = new File(this.outputDir);
            System.out.println("Destination Directory:" + dest.getAbsolutePath());
            if (!isZip()) {
                if (!dest.exists()) errors.append("the destination directory does not exist\n");
                if (!dest.canRead()) errors.append("the destination directory is not writable\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
            errors.append(e.toString());
        }
        return errors.toString();
    }
    /**
     * main entry point
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        try {
            Velocidoc velocidoc1 = new Velocidoc();
            parseArguments(args, velocidoc1);
            String err = velocidoc1.validateConfig();
            if (err.length() > 0) {
                printUsage();
                System.err.println(err);
                System.exit(1);
            }
            if ((velocidoc1.getXwikiURL()==null)&&(velocidoc1.getSourceDir()==null)) {
                velocidoc1.setSourceDir(".");
            }

            if (velocidoc1.isZip())
             velocidoc1.generateZipFile();
            else
             velocidoc1.generate();
            System.out.println("done");
            System.exit(0);
        } catch (Exception e) {
            printUsage();
            e.printStackTrace();
            System.exit(1);
        }
    }
    /**
     * prints the usage statement
     */
    public static void printUsage() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n\nUsage:");
        sb.append("\n  Velocidoc -src <sourcedirorurl> -dst <destdirorfile> -z -user <xwikiuser> -pass <xwikipass>");
        System.err.println(sb);
    }
    /**
     *
     */
    public void setSourceDir(String newSourceDir) {
        sourceDir = newSourceDir;
    }
    /**
     *
     */
    public String getSourceDir() {
        return sourceDir;
    }
    /**
     *
     */
    public void setOutputDir(String newDestinationDir) {
        outputDir = newDestinationDir;
    }
    /**
     *
     */
    public String getOutputDir() {
        return outputDir;
    }
    /**
     *
     */
    public void setControlFile(String newControlFile) {
        controlFile = newControlFile;
    }
    /**
     *
     */
    public String getControlFile() {
        return controlFile;
    }

    /**
     *
     */
    public void setZip(boolean zip) {
        this.zip = zip;
    }

    public boolean isZip() {
        return this.zip;
    }

    public String getXwikiUser() {
        return xwikiUser;
    }

    public void setXwikiUser(String xwikiUser) {
        this.xwikiUser = xwikiUser;
    }

    public String getXwikiPass() {
        return xwikiPass;
    }

    public void setXwikiPass(String xwikiPass) {
        this.xwikiPass = xwikiPass;
    }

    public String getXwikiURL() {
        return xwikiURL;
    }

    public void setXwikiURL(String xwikiURL) {
        this.xwikiURL = xwikiURL;
    }

    public XWikiContext getContext() {
        return context;
    }

    public void setContext(XWikiContext context) {
        this.context = context;
    }

    public List getSpaceList() {
        return spaceList;
    }

    public void setSpaceList(List spaceList) {
        this.spaceList = spaceList;
    }

    public void setSpaceList(String spaceList) {
        String[] sp = StringUtils.split(spaceList,",");
        ArrayList list =  new ArrayList();
        for (int j=0;j<sp.length;j++) {
            list.add(sp[j]);
        }
        setSpaceList(list);
    }

    public String getDefaultName() {
        return defaultName;
    }

    public void setDefaultName(String defaultName) {
        this.defaultName = defaultName;
    }
}
