package org.xwiki.store.jcr;

import org.jcrom.Jcrom;

public interface JcromProvider
{
    String ROLE = JcromProvider.class.getName();

    Jcrom getJcrom();
}
