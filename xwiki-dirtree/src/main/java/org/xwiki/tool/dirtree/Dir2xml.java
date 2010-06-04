package org.xwiki.tool.dirtree;

import java.io.File;


import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

import com.xpn.xwiki.doc.XWikiDocument;

//import java.util.List;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;

import org.xwiki.context.Execution;
/*
import org.xwiki.component.manager.*;
import org.xwiki.component.descriptor.*;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.DocumentReference;
import com.xpn.xwiki.internal.model.reference.CurrentStringDocumentReferenceResolver;
import org.xwiki.rendering.syntax.Syntax;
import com.xpn.xwiki.CoreConfiguration;
*/
import org.xwiki.test.AbstractComponentTestCase;

public class Dir2xml
{
    private XWikiContext context;

    public static void main(String[] args) throws Exception
    {
        if (args.length < 1) {
            System.out.println("Dir2xml directoryPath");
            return;
        }
        File dir = new File(args[0]);
        if (!dir.exists()) {
            System.out.println("ERROR: No file found named: " + dir.getAbsolutePath());
            return;
        }
        new Dir2xml().run(dir);
    }

    public void run(File dir) throws Exception
    {
        initXWiki();
        Runner runner = new Runner();
        runner.addParser(XWikiDocument.class, new Runner.StringParser(){
            public Object parse(String input) throws Exception {
                XWikiDocument x = new XWikiDocument();
                x.fromXML(input);
                return x;
            }
        });
        XWikiDocument doc = runner.run(XWikiDocument.class, dir);

        System.out.println(doc.toXML(false, false, false, false, this.context));
    }

    private void initXWiki() throws Exception
    {
        AbstractComponentTestCase abtc = new AbstractComponentTestCase() {{
            setUp();
        }};
        Utils.setComponentManager(abtc.getComponentManager());
        this.context = new XWikiContext() {{
            put("wiki", "xwiki");
            setWiki(new XWiki() {
                public String getEncoding()
                {
                    return "UTF-8";
                }
            });
        }};
        Execution exec = Utils.getComponent(Execution.class);
        exec.getContext().setProperty("xwikicontext", this.context);
    }

/*
        Utils.setComponentManager(new ComponentManager() {
            public <T> boolean hasComponent(Class<T> role) {return true;}
            public <T> boolean hasComponent(Class<T> role, String roleHint) {return true;}
            public <T> T lookup(Class<T> role) throws ComponentLookupException {return null;}
            public <T> T lookup(Class<T> role, String roleHint) throws ComponentLookupException 
            {
                if (role == DocumentReferenceResolver.class) {
                    return (T) new CurrentStringDocumentReferenceResolver() {
                        public DocumentReference resolve(String documentReferenceRepresentation, Object... parameters)
                        {
                            return new DocumentReference("xwiki", "Main", "Test");
                        }
                    };
                } else if (role == CoreConfiguration.class) {
                    return (T) new CoreConfiguration() {
                        public Syntax getDefaultDocumentSyntax()
                        {
                            return Syntax.XWIKI_2_0;
                        }
                    };
                }
                return null;
            }
            public <T> void release(T component) throws ComponentLifecycleException {}
            public <T> Map<String, T> lookupMap(Class<T> role) throws ComponentLookupException {return null;}
            public <T> List<T> lookupList(Class<T> role) throws ComponentLookupException {return null;}
            public <T> void registerComponent(ComponentDescriptor<T> componentDescriptor) throws ComponentRepositoryException {}
            public <T> void registerComponent(ComponentDescriptor<T> componentDescriptor, T componentInstance)
                throws ComponentRepositoryException {}
            public void unregisterComponent(Class< ? > role, String roleHint) {}
            public <T> ComponentDescriptor<T> getComponentDescriptor(Class<T> role, String roleHint) {return null;}
            public <T> List<ComponentDescriptor<T>> getComponentDescriptorList(Class<T> role) {return null;}
            public ComponentEventManager getComponentEventManager() {return null;}
            public void setComponentEventManager(ComponentEventManager eventManager) {}
            public ComponentManager getParent() {return null;}
            public void setParent(ComponentManager parentComponentManager) {}
        });
*/
 //   }
}
