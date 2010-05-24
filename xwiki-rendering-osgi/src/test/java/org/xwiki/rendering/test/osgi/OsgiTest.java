package org.xwiki.rendering.test.osgi;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Inject;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.xwiki.component.annotation.ComponentAnnotationLoader;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.rendering.converter.Converter;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;

import java.io.StringReader;

import static org.ops4j.pax.exam.CoreOptions.*;

@RunWith(JUnit4TestRunner.class)
public class OsgiTest
{
    @Inject
    BundleContext context;

    @Configuration
    public static Option[] configure()
    {
        return options(
            systemPackages(
                // Needed for "xwiki-core-rendering-api"
                "org.dom4j", "org.dom4j.io", "org.dom4j.tree",
                "org.xml.sax", "org.xml.sax.helpers",
                "javax.validation",
                "org.apache.commons.beanutils; version=1.8",
                "org.htmlcleaner",
                // Needed for "xwiki-core-rendering-syntax-wikimodel"
                "org.jdom", "org.jdom.input", "org.jdom.output",
                "org.wikimodel.wem", "org.wikimodel.wem.confluence", "org.wikimodel.wem.creole",
                "org.wikimodel.wem.impl", "org.wikimodel.wem.jspwiki", "org.wikimodel.wem.mediawiki",
                "org.wikimodel.wem.tex", "org.wikimodel.wem.xhtml", "org.wikimodel.wem.xhtml.filter",
                "org.wikimodel.wem.xhtml.handler", "org.wikimodel.wem.xhtml.impl",
                "org.wikimodel.wem.xwiki.xwiki20",
                // Needed for "xwiki-core-component-default"
                "org.apache.commons.logging"
            ),
            mavenBundle().groupId("commons-lang").artifactId("commons-lang").version("2.5"),
// Note: seems like the manifest info for commons-beanutils require commons collection whereas the maven pom.xml doesn't...            
// mavenBundle().groupId("commons-beanutils").artifactId("commons-beanutils").version("1.8.3"),
            mavenBundle().groupId("org.xwiki.platform").artifactId("xwiki-core-xml").version("2.4-SNAPSHOT"),
            mavenBundle().groupId("org.xwiki.platform").artifactId("xwiki-core-properties").version("2.4-SNAPSHOT"),
            mavenBundle().groupId("org.xwiki.platform").artifactId("xwiki-core-component-api").version("2.4-SNAPSHOT"),
            mavenBundle().groupId("org.xwiki.platform").artifactId("xwiki-core-rendering-api").version("2.4-SNAPSHOT"),
            mavenBundle().groupId("org.xwiki.platform").artifactId("xwiki-core-rendering-syntax-wikimodel").version("2.4-SNAPSHOT"),
            // Needed for the moment since till we replace the ECM by an OSGi equivalent
            mavenBundle().groupId("org.xwiki.platform").artifactId("xwiki-core-component-default").version("2.4-SNAPSHOT"),
            mavenBundle().groupId("org.xwiki.platform").artifactId("xwiki-core-observation-local").version("2.4-SNAPSHOT")
        );
    }

    @Test
    public void testOsgi() throws Exception
    {
        // Initialize Rendering components and allow getting instances
        EmbeddableComponentManager ecm = new EmbeddableComponentManager();
        ecm.initialize(this.getClass().getClassLoader());

        // Use a the Converter component to convert between one syntax to another.
        Converter converter = ecm.lookup(Converter.class);

        // Convert input in XWiki Syntax 2.0 into XHTML. The result is stored in the printer.
        WikiPrinter printer = new DefaultWikiPrinter();
        converter.convert(new StringReader("This is **bold**"), Syntax.XWIKI_2_0, Syntax.XHTML_1_0, printer);

        Assert.assertEquals("<p>This is <strong>bold</strong></p>", printer.toString());
    }
}
