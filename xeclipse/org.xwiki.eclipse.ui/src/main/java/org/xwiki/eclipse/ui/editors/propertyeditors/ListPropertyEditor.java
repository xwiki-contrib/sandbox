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
package org.xwiki.eclipse.ui.editors.propertyeditors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.xwiki.eclipse.core.model.XWikiEclipseObjectProperty;

class ListContentProvider implements IStructuredContentProvider
{
    private Object[] NO_OBJECT = new Object[0];

    public void dispose()
    {
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
    }

    public Object[] getElements(Object inputElement)
    {
        if (inputElement instanceof List) {
            List list = (List) inputElement;
            return list.toArray();
        }

        if (inputElement instanceof String) {
            String string = (String) inputElement;
            return string.split("\\|");
        }

        return NO_OBJECT;
    }
}

public class ListPropertyEditor extends BasePropertyEditor
{
    ListViewer listViewer;

    boolean multiSelect;

    public ListPropertyEditor(FormToolkit toolkit, Composite parent, XWikiEclipseObjectProperty property)
    {
        super(toolkit, parent, property);
    }

    @Override
    public Composite createControl(Composite parent)
    {
        Section section = toolkit.createSection(parent, Section.TITLE_BAR | Section.EXPANDED);
        section.setText(property.getPrettyName());

        Composite composite = toolkit.createComposite(section, SWT.NONE);
        GridLayoutFactory.fillDefaults().extendedMargins(0, 0, 0, 10).applyTo(composite);

        String unModifiableAttributeString = property.getAttribute("unmodifiable");
        if (unModifiableAttributeString == null || unModifiableAttributeString.equals("0")) {
            createModifiableStaticListField(toolkit, composite, property);
        } else {
            createReadOnlyStaticListField(toolkit, composite, property);
        }

        section.setClient(composite);

        return section;
    }

    private Control createReadOnlyStaticListField(FormToolkit toolkit, Composite parent,
        final XWikiEclipseObjectProperty property)
    {
        Composite composite = toolkit.createComposite(parent);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);

        toolkit.createLabel(composite, "Current selection:");
        final Label currentSelectionLabel = toolkit.createLabel(composite, "");
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(currentSelectionLabel);

        String multiSelectAttributeString = property.getAttribute("multiSelect").toString();
        if (multiSelectAttributeString == null || multiSelectAttributeString.equals("0")) {
            multiSelect = false;
        } else {
            multiSelect = true;
        }

        listViewer =
            new ListViewer(composite, SWT.BORDER | SWT.V_SCROLL | SWT.FULL_SELECTION
                | (multiSelect ? SWT.MULTI : SWT.NONE));
        listViewer.setContentProvider(new ListContentProvider());
        listViewer.setLabelProvider(new LabelProvider());
        listViewer.setInput(property.getAttribute("values"));

        StructuredSelection structuredSelection = null;
        if (multiSelect) {
            List list = (List) property.getValue();
            if (list != null) {
                structuredSelection = new StructuredSelection(list);
            }
        } else {
            Object value = property.getValue();
            if (value != null) {
                structuredSelection = new StructuredSelection(value);
            }
        }

        if (structuredSelection != null) {
            listViewer.setSelection(structuredSelection, true);
        }
        currentSelectionLabel.setText(listViewer.getSelection().toString());

        listViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {
            public void selectionChanged(SelectionChangedEvent event)
            {
                List<String> selectedValues = new ArrayList<String>();

                IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
                currentSelectionLabel.setText(selection.toString());

                Iterator iterator = selection.iterator();
                while (iterator.hasNext()) {
                    selectedValues.add((String) iterator.next());
                }

                if (!selectedValues.isEmpty()) {
                    if (multiSelect) {
                        property.setValue(selectedValues);
                    } else {
                        property.setValue(selectedValues.get(0));
                    }
                } else {
                    currentSelectionLabel.setText("");
                }

                firePropertyModifyListener();
            }
        });

        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2, 1).hint(0,
            5 * listViewer.getList().getItemHeight()).grab(true, false).applyTo(listViewer.getList());

        return composite;
    }

    private Control createModifiableStaticListField(FormToolkit toolkit, final Composite parent,
        final XWikiEclipseObjectProperty property)
    {
        Composite composite = toolkit.createComposite(parent);
        GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);

        listViewer = new ListViewer(composite, SWT.BORDER | SWT.V_SCROLL);
        listViewer.setContentProvider(new ListContentProvider());
        listViewer.setLabelProvider(new LabelProvider());
        listViewer.setInput(property.getValue());

        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).hint(0, 5 * listViewer.getList().getItemHeight())
            .grab(true, false).applyTo(listViewer.getList());
        Composite buttonBar = toolkit.createComposite(composite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING).applyTo(buttonBar);
        GridLayoutFactory.fillDefaults().applyTo(buttonBar);

        Button button = toolkit.createButton(buttonBar, "Add", SWT.PUSH);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(button);
        button.addSelectionListener(new SelectionListener()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                // TODO Auto-generated method stub
            }

            public void widgetSelected(SelectionEvent e)
            {
                InputDialog input =
                    new InputDialog(parent.getShell(), "Add value", "Please enter the value to add to the list", "",
                        null);
                input.open();

                List list = (List) property.getValue();
                if (list == null) {
                    list = new ArrayList<String>();
                    property.setValue(list);
                    listViewer.setInput(list);
                }

                if (!input.getValue().equals("")) {
                    list.add(input.getValue());
                    listViewer.refresh();
                    firePropertyModifyListener();
                }
            }
        });

        button = toolkit.createButton(buttonBar, "Remove", SWT.PUSH);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(button);
        button.addSelectionListener(new SelectionListener()
        {

            public void widgetDefaultSelected(SelectionEvent e)
            {
                // TODO Auto-generated method stub

            }

            public void widgetSelected(SelectionEvent e)
            {
                IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
                Iterator iterator = selection.iterator();
                while (iterator.hasNext()) {
                    ((List) property.getValue()).remove(iterator.next());
                }
                listViewer.refresh();
                firePropertyModifyListener();
            }

        });

        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).hint(0, 5 * listViewer.getList().getItemHeight())
            .grab(true, false).applyTo(listViewer.getList());

        return composite;

    }

    @Override
    public void setValue(Object value)
    {
        StructuredSelection structuredSelection = null;
        if (multiSelect) {
            List list = (List) value;
            if (list != null) {
                structuredSelection = new StructuredSelection(list);
            }
        } else {
            if (value != null) {
                structuredSelection = new StructuredSelection(value);
            }
        }

        if (structuredSelection != null) {
            listViewer.setSelection(structuredSelection, true);
        }
    }

}
