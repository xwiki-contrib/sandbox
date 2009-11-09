package org.xwiki.model.internal;

import javax.jcr.Repository;
import javax.jcr.Session;

import org.apache.jackrabbit.core.TransientRepository;
import org.xwiki.context.ExecutionContext;
import org.xwiki.context.ExecutionContextException;
import org.xwiki.context.ExecutionContextInitializer;

public class ModelExecutionContextInitializer implements ExecutionContextInitializer
{
    /**
     * Key under which to save the JCR session in the Execution Context
     */
    public static final String REPOSITORY_SESSION = "session";
    
    /**
     * {@inheritDoc} Adds the JCR Session to the Execution Context.
     * 
     * @see ExecutionContextInitializer#initialize(ExecutionContext)
     */
    public void initialize(ExecutionContext context) throws ExecutionContextException
    {
        Session session;
        try {
            Repository repository = new TransientRepository();
            session = repository.login();
        } catch (Exception e) {
            throw new ExecutionContextException("Failed to connect to Data Repository", e);
        }
        
        context.setProperty(REPOSITORY_SESSION, session);
    }
}
