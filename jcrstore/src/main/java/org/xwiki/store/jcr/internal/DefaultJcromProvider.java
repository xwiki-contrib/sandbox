package org.xwiki.store.jcr.internal;

import java.util.Arrays;
import java.util.HashSet;

import org.jcrom.Jcrom;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.store.jcr.JcromProvider;
import org.xwiki.store.value.DocumentValue;

public class DefaultJcromProvider implements JcromProvider, Initializable
{
    protected Jcrom jcrom;

    public Jcrom getJcrom()
    {
        return jcrom;
    }

    @SuppressWarnings("unchecked")
    public void initialize() throws InitializationException
    {
        jcrom = new Jcrom(true, false, new HashSet<Class>(Arrays.asList(DocumentValue.class)));
    }
}
