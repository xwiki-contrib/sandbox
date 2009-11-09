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
package org.xwiki.eclipse.ui.editors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.xwiki.eclipse.core.model.XWikiEclipseObject;
import org.xwiki.eclipse.core.model.XWikiEclipseObjectProperty;
import org.xwiki.eclipse.core.notifications.CoreEvent;
import org.xwiki.eclipse.core.notifications.NotificationManager;
import org.xwiki.eclipse.ui.UIConstants;
import org.xwiki.eclipse.ui.UIPlugin;
import org.xwiki.eclipse.ui.editors.propertyeditors.BasePropertyEditor;
import org.xwiki.eclipse.ui.editors.propertyeditors.BooleanPropertyEditor;
import org.xwiki.eclipse.ui.editors.propertyeditors.DatePropertyEditor;
import org.xwiki.eclipse.ui.editors.propertyeditors.IPropertyModifyListener;
import org.xwiki.eclipse.ui.editors.propertyeditors.ListPropertyEditor;
import org.xwiki.eclipse.ui.editors.propertyeditors.NumberPropertyEditor;
import org.xwiki.eclipse.ui.editors.propertyeditors.PasswordPropertyEditor;
import org.xwiki.eclipse.ui.editors.propertyeditors.StringPropertyEditor;
import org.xwiki.eclipse.ui.editors.propertyeditors.TextAreaPropertyEditor;
import org.xwiki.eclipse.ui.utils.XWikiEclipseSafeRunnable;
import org.xwiki.xmlrpc.model.XWikiClass;

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

public class ObjectEditor extends EditorPart
{
    public static final String ID = "org.xwiki.eclipse.ui.editors.Object";

    private ScrolledForm scrolledForm;

    private Map<XWikiEclipseObjectProperty, BasePropertyEditor> propertyToEditorMap;

    private boolean dirty;

    @Override
    public void doSave(IProgressMonitor monitor)
    {
        final ObjectEditorInput input = (ObjectEditorInput) getEditorInput();

        SafeRunner.run(new XWikiEclipseSafeRunnable()
        {
            public void run() throws Exception
            {
                input.getObject().getDataManager().storeObject(input.getObject());
            }
        });

        setDirty(false);
    }

    @Override
    public void doSaveAs()
    {
        // TODO Auto-generated method stub
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException
    {
        setSite(site);
        setInput(input);
        setPartName(input.getName());
        propertyToEditorMap = new HashMap<XWikiEclipseObjectProperty, BasePropertyEditor>();
    }

    @Override
    protected void setInput(IEditorInput input)
    {
        if (!(input instanceof ObjectEditorInput)) {
            throw new IllegalArgumentException("Invalid input for editor");
        }

        super.setInput(input);
    }

    @Override
    public boolean isDirty()
    {
        return dirty;
    }

    @Override
    public boolean isSaveAsAllowed()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void createPartControl(Composite parent)
    {
        FormToolkit toolkit = new FormToolkit(parent.getDisplay());
        scrolledForm = toolkit.createScrolledForm(parent);
        toolkit.decorateFormHeading(scrolledForm.getForm());

        TableWrapLayout tableWrapLayout = new TableWrapLayout();
        tableWrapLayout.numColumns = 1;
        scrolledForm.getBody().setLayout(tableWrapLayout);

        ObjectEditorInput input = (ObjectEditorInput) getEditorInput();
        XWikiEclipseObject object = input.getObject();

        /* Just for spacing */
        toolkit.createLabel(scrolledForm.getBody(), "");

        for (XWikiEclipseObjectProperty property : object.getProperties()) {
            BasePropertyEditor propertyEditor = null;

            if ("com.xpn.xwiki.objects.classes.StringClass".equals(property
                .getAttribute(XWikiClass.XWIKICLASS_ATTRIBUTE))) {
                propertyEditor = new StringPropertyEditor(toolkit, scrolledForm.getBody(), property);
            }

            if ("com.xpn.xwiki.objects.classes.PasswordClass".equals(property
                .getAttribute(XWikiClass.XWIKICLASS_ATTRIBUTE))) {
                propertyEditor = new PasswordPropertyEditor(toolkit, scrolledForm.getBody(), property);
            }

            if ("com.xpn.xwiki.objects.classes.NumberClass".equals(property
                .getAttribute(XWikiClass.XWIKICLASS_ATTRIBUTE))) {
                propertyEditor = new NumberPropertyEditor(toolkit, scrolledForm.getBody(), property);
            }

            if ("com.xpn.xwiki.objects.classes.BooleanClass".equals(property
                .getAttribute(XWikiClass.XWIKICLASS_ATTRIBUTE))) {
                propertyEditor = new BooleanPropertyEditor(toolkit, scrolledForm.getBody(), property);
            }

            if ("com.xpn.xwiki.objects.classes.TextAreaClass".equals(property
                .getAttribute(XWikiClass.XWIKICLASS_ATTRIBUTE))) {
                propertyEditor = new TextAreaPropertyEditor(toolkit, scrolledForm.getBody(), property);
            }

            if ("com.xpn.xwiki.objects.classes.DateClass"
                .equals(property.getAttribute(XWikiClass.XWIKICLASS_ATTRIBUTE))) {
                propertyEditor = new DatePropertyEditor(toolkit, scrolledForm.getBody(), property);
            }

            if ("com.xpn.xwiki.objects.classes.StaticListClass".equals(property
                .getAttribute(XWikiClass.XWIKICLASS_ATTRIBUTE))
                || "com.xpn.xwiki.objects.classes.DBListClass".equals(property
                    .getAttribute(XWikiClass.XWIKICLASS_ATTRIBUTE))) {
                propertyEditor = new ListPropertyEditor(toolkit, scrolledForm.getBody(), property);
            }

            if (propertyEditor != null) {
                propertyToEditorMap.put(property, propertyEditor);
                propertyEditor.getControl().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
                propertyEditor.addPropertyModifyListener(new IPropertyModifyListener()
                {
                    public void modifyProperty(XWikiEclipseObjectProperty property)
                    {
                        setDirty(true);
                    }
                });
            }
        }

        updateInfo();
    }

    private void setDirty(boolean dirty)
    {
        this.dirty = dirty;
        firePropertyChange(PROP_DIRTY);
    }

    @Override
    public void setFocus()
    {
        ObjectEditorInput input = (ObjectEditorInput) getEditorInput();
        XWikiEclipseObject object = input.getObject();

        NotificationManager.getDefault().fireCoreEvent(CoreEvent.Type.OBJECT_SELECTED, this, object);
    }

    private void updateInfo()
    {
        if (scrolledForm != null) {
            ObjectEditorInput input = (ObjectEditorInput) getEditorInput();
            if (input != null) {
                XWikiEclipseObject object = input.getObject();
                scrolledForm.setText(String.format("%s (Class %s) on page %s", object.getName(), object.getXWikiClass()
                    .getId(), object.getData().getPageId()));

                scrolledForm.getForm().setMessage(object.getDataManager().getName());
            }
        }
    }

    @Override
    public Object getAdapter(Class adapter)
    {
        if (adapter.equals(IContentOutlinePage.class)) {
            ObjectEditorInput input = (ObjectEditorInput) getEditorInput();
            XWikiEclipseObject object = input.getObject();
            return new ObjectEditorContentOutlinePage(object, this);
        }

        return super.getAdapter(adapter);
    }

    public void scrollTo(XWikiEclipseObjectProperty propertyName)
    {
        Control control = propertyToEditorMap.get(propertyName).getControl();
        scrolledForm.setOrigin(control.getLocation());
    }

}

class ObjectEditorContentOutlinePage extends ContentOutlinePage
{
    private XWikiEclipseObject object;

    private ObjectEditor editor;

    public ObjectEditorContentOutlinePage(XWikiEclipseObject object, ObjectEditor editor)
    {
        this.object = object;
        this.editor = editor;
    }

    @Override
    public void createControl(Composite parent)
    {
        super.createControl(parent);
        TreeViewer treeViewer = getTreeViewer();

        treeViewer.setContentProvider(new ITreeContentProvider()
        {
            private Object[] NO_OBJECTS = new Object[0];

            public Object[] getChildren(Object parentElement)
            {
                // TODO Auto-generated method stub
                return null;
            }

            public Object getParent(Object element)
            {
                // TODO Auto-generated method stub
                return null;
            }

            public boolean hasChildren(Object element)
            {
                // TODO Auto-generated method stub
                return false;
            }

            public Object[] getElements(Object inputElement)
            {
                if (inputElement instanceof XWikiEclipseObject) {
                    return object.getProperties().toArray();
                }

                return NO_OBJECTS;
            }

            public void dispose()
            {

            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }
        });

        treeViewer.setLabelProvider(new LabelProvider()
        {
            @Override
            public String getText(Object element)
            {
                if (element instanceof XWikiEclipseObjectProperty) {
                    XWikiEclipseObjectProperty property = (XWikiEclipseObjectProperty) element;
                    return property.getPrettyName();
                }
                return super.getText(element);
            }

            @Override
            public Image getImage(Object element)
            {
                if (element instanceof XWikiEclipseObjectProperty) {
                    return UIPlugin.getImageDescriptor(UIConstants.OBJECT_ICON).createImage();
                }

                return super.getImage(element);
            }
        });

        treeViewer.setInput(object);
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {
            public void selectionChanged(SelectionChangedEvent event)
            {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                if (!selection.isEmpty()) {
                    editor.scrollTo((XWikiEclipseObjectProperty) selection.getFirstElement());
                }
            }

        });
    }
}
