package org.xwiki.extension.xar.internal.handler.packager.xml;

import org.xwiki.component.manager.ComponentManager;

import com.xpn.xwiki.objects.BaseObject;

public class ObjectHandler extends AbstractHandler
{
    public ObjectHandler(ComponentManager componentManager)
    {
        super(componentManager, new BaseObject());
    }

    public BaseObject getObject()
    {
        return (BaseObject) getCurrentBean();
    }
}
