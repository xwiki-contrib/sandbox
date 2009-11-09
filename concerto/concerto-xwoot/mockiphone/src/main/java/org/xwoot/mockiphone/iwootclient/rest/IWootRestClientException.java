package org.xwoot.mockiphone.iwootclient.rest;

import org.xwoot.mockiphone.iwootclient.IWootClientException;

public class IWootRestClientException extends IWootClientException
{
    
    /**
     * 
     */
    private static final long serialVersionUID = 2360283191280119689L;

    public IWootRestClientException()
    {
        super();
    }

    public IWootRestClientException(Throwable arg0)
    {
        super(arg0);
    }

    public IWootRestClientException(String arg0)
    {
        super(arg0);
    }

    public IWootRestClientException(String arg0, Throwable t)
    {
        super(arg0, t);
    }
}
