package org.xwoot.iwoot.xwootclient.servlet;

import org.xwoot.iwoot.xwootclient.XWootClientException;

public class XWootClientServletException extends XWootClientException
{

    /**
     * 
     */
    private static final long serialVersionUID = -2568698052987298242L;

    public XWootClientServletException()
    {
        super();
    }

    public XWootClientServletException(Throwable arg0)
    {
        super(arg0);
        // TODO Auto-generated constructor stub
    }

    public XWootClientServletException(String arg0)
    {
        super(arg0);
    }

    public XWootClientServletException(String arg0, Throwable t)
    {
        super(arg0, t);
    }
}
