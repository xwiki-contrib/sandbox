/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 *
 */
package org.xwiki.eclipse.ui.properties;

import java.net.URL;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.dialogs.PropertyPage;
import org.xwiki.eclipse.core.model.XWikiEclipsePageSummary;

public class XWikiEclipsePageSummaryPropertiesPage extends PropertyPage
{

    public XWikiEclipsePageSummaryPropertiesPage()
    {
        super();
    }

    @Override
    protected Control createContents(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);

        XWikiEclipsePageSummary pageSummary =
            (XWikiEclipsePageSummary) getElement().getAdapter(XWikiEclipsePageSummary.class);

        Label property = new Label(composite, SWT.NONE);
        property.setText("Id:");
        Label value = new Label(composite, SWT.NONE);
        value.setText(pageSummary.getData().getId() == null ? "" : pageSummary.getData().getId());

        property = new Label(composite, SWT.NONE);
        property.setText("Title:");
        value = new Label(composite, SWT.NONE);
        value.setText(pageSummary.getData().getTitle() == null ? "" : pageSummary.getData().getTitle());

        property = new Label(composite, SWT.NONE);
        property.setText("Parent:");
        value = new Label(composite, SWT.NONE);
        value.setText(pageSummary.getData().getParentId() == null ? "" : pageSummary.getData().getParentId());

        property = new Label(composite, SWT.NONE);
        property.setText("Space:");
        value = new Label(composite, SWT.NONE);
        value.setText(pageSummary.getData().getSpace() == null ? "" : pageSummary.getData().getSpace());

        property = new Label(composite, SWT.NONE);
        property.setText("Translations:");
        value = new Label(composite, SWT.NONE);
        value.setText(pageSummary.getData().getTranslations().toString());

        property = new Label(composite, SWT.NONE);
        property.setText("Url:");
        Link link = new Link(composite, SWT.NONE);
        link.setText(pageSummary.getData().getUrl() == null ? "" : String.format("<a>%s</a>", pageSummary.getData()
            .getUrl()));
        link.addSelectionListener(new SelectionListener()
        {

            public void widgetDefaultSelected(SelectionEvent e)
            {
                // TODO Auto-generated method stub
            }

            public void widgetSelected(final SelectionEvent e)
            {
                SafeRunner.run(new SafeRunnable()
                {
                    public void run() throws Exception
                    {
                        IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
                        IWebBrowser browser = browserSupport.createBrowser("XWiki Eclipse");
                        browser.openURL(new URL(e.text));
                    }
                });
            }

        });

        return composite;
    }

}
