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
package org.xwiki.eclipse.ui.dialogs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.CompareViewerPane;
import org.eclipse.compare.IEditableContent;
import org.eclipse.compare.IStreamContentAccessor;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.structuremergeviewer.DiffNode;
import org.eclipse.compare.structuremergeviewer.Differencer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.xwiki.eclipse.core.model.XWikiEclipsePage;
import org.xwiki.eclipse.ui.UIConstants;
import org.xwiki.eclipse.ui.UIPlugin;

public class PageConflictDialog extends TitleAreaDialog
{
    public static final int ID_USE_LOCAL = 1001;

    public static final int ID_USE_REMOTE = 1002;

    private XWikiEclipsePage page;

    private XWikiEclipsePage conflictingPage;

    private XWikiEclipsePage ancestorPage;

    private TextMergeViewer viewer;

    static class CompareElement extends Document implements IEditableContent, ITypedElement, IStreamContentAccessor
    {
        private XWikiEclipsePage page;

        private Boolean editable;

        public CompareElement(XWikiEclipsePage page, Boolean editable)
        {
            this.page = page;
            this.editable = editable;
            set(page.getData().getContent());
        }

        public Image getImage()
        {
            return null;
        }

        public String getName()
        {
            return page.getData().getId();
        }

        public String getType()
        {
            return TEXT_TYPE;
        }

        public InputStream getContents() throws CoreException
        {
            return new ByteArrayInputStream(page.getData().getContent().getBytes()); //$NON-NLS-1$
        }

        public boolean isEditable()
        {
            return editable;
        }

        public ITypedElement replace(ITypedElement dest, ITypedElement src)
        {
            return null;
        }

        public void setContent(byte[] newContent)
        {
            page.getData().setContent(new String(newContent));
        }

    }

    public PageConflictDialog(Shell parentShell, XWikiEclipsePage page, XWikiEclipsePage conflictingPage,
        XWikiEclipsePage ancestorPage)
    {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        this.page = page;
        this.conflictingPage = conflictingPage;
        this.ancestorPage = ancestorPage;
    }

    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Page conflict");
    }

    @Override
    protected Point getInitialSize()
    {
        // TODO Auto-generated method stub
        return new Point(800, 600);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent)
    {
        Button button = createButton(parent, ID_USE_LOCAL, "Use local", false);
        button.addSelectionListener(new SelectionListener()
        {

            public void widgetDefaultSelected(SelectionEvent e)
            {
                // Do nothing.
            }

            public void widgetSelected(SelectionEvent e)
            {
                viewer.flush(new NullProgressMonitor());
                PageConflictDialog.this.setReturnCode(ID_USE_LOCAL);
                PageConflictDialog.this.close();
            }

        });

        createButton(parent, IDialogConstants.CANCEL_ID, "Solve later", false);

        button = createButton(parent, ID_USE_REMOTE, "Use remote", true);
        button.addSelectionListener(new SelectionListener()
        {

            public void widgetDefaultSelected(SelectionEvent e)
            {
                // Do nothing.
            }

            public void widgetSelected(SelectionEvent e)
            {
                viewer.flush(new NullProgressMonitor());
                PageConflictDialog.this.setReturnCode(ID_USE_REMOTE);
                PageConflictDialog.this.close();
            }
        });
    }

    @Override
    protected Control createContents(Composite parent)
    {
        Control contents = super.createContents(parent);

        setTitle("Page conflict");
        setMessage(
            "A conflict has been detected. Please review the data and choose an action to perform in order to solve the conflict.",
            IMessageProvider.WARNING);
        setTitleImage(UIPlugin.getImageDescriptor(UIConstants.CONFLICT_BANNER).createImage());

        return contents;
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.fillDefaults().applyTo(composite);

        CompareViewerPane pane = new CompareViewerPane(composite, SWT.BORDER | SWT.FLAT);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(pane);

        CompareConfiguration cc = new CompareConfiguration();

        if (ancestorPage != null) {
            cc.setAncestorLabel(String.format("%s (Original)", ancestorPage.getData().getId()));
        }

        cc.setLeftLabel(String.format("%s (Local)", page.getData().getId()));
        cc.setRightLabel(String.format("%s (Conflicting)", conflictingPage.getData().getId()));
        viewer = new TextMergeViewer(pane, cc);
        pane.setContent(viewer.getControl());
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(viewer.getControl());

        CompareElement left = new CompareElement(page, true);
        CompareElement right = new CompareElement(conflictingPage, false);
        if (ancestorPage != null) {
            CompareElement ancestor = new CompareElement(ancestorPage, false);

            viewer.setInput(new DiffNode(null, Differencer.CONFLICTING, ancestor, left, right));
        } else {
            viewer.setInput(new DiffNode(left, right));
        }

        return pane;
    }

}
