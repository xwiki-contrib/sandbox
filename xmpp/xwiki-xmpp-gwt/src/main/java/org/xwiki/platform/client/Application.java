package org.xwiki.platform.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.RootPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Application implements EntryPoint
{

    /**
     * This is the entry point method.
     */
    public void onModuleLoad()
    {
        ContactsWidget cw = new ContactsWidget();
        cw.add("Asiri Rathnayake");
        cw.add("Ludovic Dubost");
        cw.add("Sergiu Dumitriu");
        cw.add("Harsha Sanjeewa");
        cw.add("Fred Nassel");
        cw.add("Naveen Sathsara");
        // Adds custom Contacts widget
        RootPanel.get("container").add(cw);
    }
}
