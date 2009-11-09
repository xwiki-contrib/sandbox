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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.VerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.xwiki.eclipse.core.model.XWikiEclipseObjectProperty;
import org.xwiki.eclipse.ui.editors.XWikiSourceViewerConfiguration;
import org.xwiki.eclipse.ui.editors.scanners.XWikiPartitionScanner;

public class TextAreaPropertyEditorDialog extends Dialog
{

    private XWikiEclipseObjectProperty property;

    private String text;

    public TextAreaPropertyEditorDialog(Shell parentShell, XWikiEclipseObjectProperty object)
    {
        super(parentShell);
        this.property = object;
    }

    @Override
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
    }

    @Override
    protected Point getInitialSize()
    {
        // TODO Auto-generated method stub
        return new Point(800, 600);
    }

    @Override
    protected Control createContents(Composite parent)
    {
        Composite composite = new Composite(parent, SWT.NONE);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
        GridLayoutFactory.fillDefaults().applyTo(composite);

        FormToolkit toolkit = new FormToolkit(parent.getDisplay());
        Form form = toolkit.createForm(composite);
        toolkit.decorateFormHeading(form);
        form.setText(String.format("Property %s of object %s on page %s", property.getPrettyName(), property
            .getObject().getName(), property.getObject().getData().getPageId()));
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(form);
        GridLayoutFactory.fillDefaults().applyTo(form.getBody());

        IDocument document = new Document(property.getValue() != null ? (String) property.getValue() : "");
        IDocumentPartitioner partitioner =
            new FastPartitioner(new XWikiPartitionScanner(), XWikiPartitionScanner.ALL_PARTITIONS);
        partitioner.connect(document);
        document.setDocumentPartitioner(partitioner);

        final SourceViewer sourceViewer =
            new SourceViewer(form.getBody(), new VerticalRuler(10), SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        sourceViewer.configure(new XWikiSourceViewerConfiguration());
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(sourceViewer.getControl());
        sourceViewer.setDocument(document);
        sourceViewer.getTextWidget().addModifyListener(new ModifyListener()
        {
            public void modifyText(ModifyEvent e)
            {
                text = sourceViewer.getTextWidget().getText();
            }
        });
        sourceViewer.getTextWidget().setWordWrap(true);

        Composite buttonBar = toolkit.createComposite(form.getBody());
        GridLayoutFactory.fillDefaults().numColumns(3).extendedMargins(0, 0, 0, 10).applyTo(buttonBar);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(buttonBar);

        Label filler = toolkit.createLabel(buttonBar, " ");
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(filler);

        Button cancel = toolkit.createButton(buttonBar, "Cancel", SWT.PUSH);
        cancel.addSelectionListener(new SelectionListener()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                // TODO Auto-generated method stub

            }

            public void widgetSelected(SelectionEvent e)
            {
                buttonPressed(IDialogConstants.CANCEL_ID);
            }

        });
        Button ok = toolkit.createButton(buttonBar, "Ok", SWT.PUSH);
        ok.addSelectionListener(new SelectionListener()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                // TODO Auto-generated method stub
            }

            public void widgetSelected(SelectionEvent e)
            {
                buttonPressed(IDialogConstants.OK_ID);
            }
        });

        return form;
    }

    public String getText()
    {
        return text;
    }
}
