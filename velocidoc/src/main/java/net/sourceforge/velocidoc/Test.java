package net.sourceforge.velocidoc;


import java.io.File;
/**
 *
 *
 *
 * Company:  ObjectWave Corporation
 * @author
 * @version 0.1 alpha
 */

public class Test {
    public Test() {
        File f = new File("p:/projects/velocidoc/vm");
        RootDoc rootDoc = new RootDoc(f);
        PackageDoc[] pdocs = rootDoc.getAllPackages();
        for (int i=0;i < pdocs.length;i++) {
            System.out.println("Package:" + pdocs[i].getPackageName());
        }
        TemplateDoc[] tdocs = rootDoc.getAllTemplates();
        for (int i=0;i < tdocs.length; i++) {
            System.out.println("Template:" + tdocs[i].getName());
            MacroDoc[] mdocs = tdocs[i].getMacros();
            for (int j=0;j < mdocs.length; j++) {
                System.out.println("--> Macro : " + mdocs[j].getName());
            }
        }

        MacroDoc[] md = rootDoc.getAllMacros();
        for (int i=0;i < md.length;i++) {
            System.out.println("Macro:" + md[i].getName());
        }
    }

    public static void main(String[] args) {
        Test test1 = new Test();
    }
}