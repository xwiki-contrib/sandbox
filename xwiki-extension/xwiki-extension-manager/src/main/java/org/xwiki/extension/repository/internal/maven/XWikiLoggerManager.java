package org.xwiki.extension.repository.internal.maven;

import org.codehaus.plexus.logging.AbstractLoggerManager;
import org.xwiki.component.logging.Logger;

public class XWikiLoggerManager extends AbstractLoggerManager
{
    private XWikiLogger logger;

    public XWikiLoggerManager(Logger logger)
    {
        this.logger = new XWikiLogger(logger);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.plexus.logging.LoggerManager#getActiveLoggerCount()
     */
    public int getActiveLoggerCount()
    {
        return 1;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.plexus.logging.LoggerManager#getLoggerForComponent(java.lang.String, java.lang.String)
     */
    public org.codehaus.plexus.logging.Logger getLoggerForComponent(String arg0, String arg1)
    {
        return this.logger;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.plexus.logging.LoggerManager#getThreshold()
     */
    public int getThreshold()
    {
        return this.logger.getThreshold();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.plexus.logging.LoggerManager#getThreshold(java.lang.String, java.lang.String)
     */
    public int getThreshold(String arg0, String arg1)
    {
        return this.logger.getThreshold();
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.plexus.logging.LoggerManager#returnComponentLogger(java.lang.String, java.lang.String)
     */
    public void returnComponentLogger(String arg0, String arg1)
    {
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.plexus.logging.LoggerManager#setThreshold(int)
     */
    public void setThreshold(int arg0)
    {
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.plexus.logging.LoggerManager#setThreshold(java.lang.String, java.lang.String, int)
     */
    public void setThreshold(String arg0, String arg1, int arg2)
    {
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.plexus.logging.LoggerManager#setThresholds(int)
     */
    /**
     * {@inheritDoc}
     * 
     * @see org.codehaus.plexus.logging.LoggerManager#setThresholds(int)
     */
    public void setThresholds(int arg0)
    {
    }
}
