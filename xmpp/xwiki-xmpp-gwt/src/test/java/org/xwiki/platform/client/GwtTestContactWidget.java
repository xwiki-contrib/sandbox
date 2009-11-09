package org.xwiki.platform.client;

import com.google.gwt.junit.client.GWTTestCase;

public class GwtTestContactWidget extends GWTTestCase
{

    public String getModuleName()
    {
        return "org.xwiki.platform.Application";
    }

    public void testCreateContactWidget()
    {
        ContactsWidget cw = new ContactsWidget();
        cw.add("Tharindu Madushanka");
        cw.add("Harsha Sanjeewa");
        cw.add("Naveen Sathsara");
        assertTrue(cw.isAdded("Tharindu Madushanka"));
        cw.clear();
        assertTrue(cw.isAdded("Harsha Sanjeewa"));
    }
}
