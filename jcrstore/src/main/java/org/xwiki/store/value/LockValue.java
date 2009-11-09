package org.xwiki.store.value;

import java.util.Date;

import org.jcrom.annotations.JcrName;
import org.jcrom.annotations.JcrPath;
import org.jcrom.annotations.JcrProperty;

public class LockValue
{
    @JcrProperty public String userName;
    @JcrProperty public Date date;

    @JcrName protected String name = "lock";
    @SuppressWarnings("unused")
    @JcrPath private String jcrPath;
}
