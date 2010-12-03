package org.xwiki.extension.xar.internal.handler.packager.xml;

import org.xwiki.component.manager.ComponentManager;

import com.xpn.xwiki.objects.classes.BaseClass;

public class ClassHandler extends AbstractHandler
{
    public ClassHandler(ComponentManager componentManager, BaseClass xclass)
    {
        super(componentManager, xclass);
    }

    public BaseClass getObject()
    {
        return (BaseClass) getCurrentBean();
    }
}
