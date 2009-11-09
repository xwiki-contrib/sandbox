package org.xwiki.model;

import javax.jcr.Node;
import javax.jcr.Session;

import org.xwiki.context.Execution;
import org.xwiki.model.internal.ModelExecutionContextInitializer;
import org.xwiki.test.AbstractXWikiComponentTestCase;

public class ModelTest extends AbstractXWikiComponentTestCase
{
    private Session session;
    
    private Node currentNode;
    
    protected void setUp() throws Exception
    {
        super.setUp();
        
        Execution execution = (Execution) getComponentManager().lookup(Execution.class);
        this.session = (Session) execution.getContext().getProperty(
            ModelExecutionContextInitializer.REPOSITORY_SESSION);
        this.currentNode = this.session.getRootNode(); 
    }
}
