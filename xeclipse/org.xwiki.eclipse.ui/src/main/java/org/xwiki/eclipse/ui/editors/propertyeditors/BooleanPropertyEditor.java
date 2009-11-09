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

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.xwiki.eclipse.core.model.XWikiEclipseObjectProperty;

public class BooleanPropertyEditor extends BasePropertyEditor
{
    Button button;

    public BooleanPropertyEditor(FormToolkit toolkit, Composite parent, XWikiEclipseObjectProperty property)
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

        button = toolkit.createButton(composite, property.getPrettyName(), SWT.BORDER | SWT.CHECK);
        button.addSelectionListener(new SelectionListener()
        {
            public void widgetDefaultSelected(SelectionEvent e)
            {
                // TODO Auto-generated method stub
            }

            public void widgetSelected(SelectionEvent e)
            {
                if (button.getSelection()) {
                    property.setValue("1");
                } else {
                    property.setValue("0");
                }
                firePropertyModifyListener();
            }
        });

        section.setClient(composite);

        return section;
    }

    @Override
    public void setValue(Object value)
    {
        if (value != null && value.equals("1")) {
            button.setSelection(true);
        } else {
            button.setSelection(false);
        }

    }

}
