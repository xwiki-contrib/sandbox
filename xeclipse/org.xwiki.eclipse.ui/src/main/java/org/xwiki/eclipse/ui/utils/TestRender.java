package org.xwiki.eclipse.ui.utils;

import org.xwiki.eclipse.ui.render.XHTMLRenderer;

public class TestRender
{

    public static void main(String[] args)
    {
        try {
            XHTMLRenderer render = new XHTMLRenderer();
            System.out.println(render.XWIKI20toHTML("{{velocity}}Velocity Fails!{{velocity}}"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
