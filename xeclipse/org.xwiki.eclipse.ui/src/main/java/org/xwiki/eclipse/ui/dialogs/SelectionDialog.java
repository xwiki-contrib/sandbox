package org.xwiki.eclipse.ui.dialogs;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.xwiki.eclipse.core.DataManager;
import org.xwiki.eclipse.core.model.XWikiEclipseSpaceSummary;

public class SelectionDialog extends TitleAreaDialog
{
    private Set<Object> objects;

    private String title;

    private String message;

    private TreeViewer viewer;

    private Set<Object> selectedObjects;

    private static class SelectionDialogContentProvider implements ITreeContentProvider
    {
        private static final Object[] NO_OBJECTS = new Object[0];

        private Set<Object> objects;

        public SelectionDialogContentProvider(Set<Object> objects)
        {
            this.objects = objects;
        }

        public Object[] getChildren(Object parentElement)
        {
            return NO_OBJECTS;
        }

        public Object getParent(Object element)
        {
            return null;
        }

        public boolean hasChildren(Object element)
        {
            return false;
        }

        public Object[] getElements(Object inputElement)
        {
            return objects.toArray();
        }

        public void dispose()
        {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {
        }
    }

    public SelectionDialog(Shell parentShell, String title, String message, Set<Object> objects)
    {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        this.objects = objects;
        this.title = title;
        this.message = message;
        selectedObjects = new HashSet<Object>();
    }

    @Override
    protected void configureShell(Shell newShell)
    {
        super.configureShell(newShell);
        newShell.setText("Object selection");
    }

    @Override
    protected Point getInitialSize()
    {
        return new Point(800, 600);
    }

    @Override
    protected Control createContents(Composite parent)
    {
        Control contents = super.createContents(parent);

        setTitle(title);
        setMessage(message, IMessageProvider.INFORMATION);

        return contents;
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.fillDefaults().applyTo(composite);

        viewer = new TreeViewer(composite, SWT.BORDER | SWT.CHECK);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(viewer.getControl());
        viewer.setLabelProvider(new WorkbenchLabelProvider());
        viewer.setContentProvider(new SelectionDialogContentProvider(objects));
        viewer.setLabelProvider(new WorkbenchLabelProvider());
        viewer.setSorter(new ViewerSorter());
        viewer.setInput(this);

        viewer.getTree().addListener(SWT.Selection, new Listener()
        {
            public void handleEvent(Event event)
            {
                if (event.detail == SWT.CHECK) {
                    TreeItem item = (TreeItem) event.item;
                    if (item.getChecked()) {
                        selectedObjects.add(item.getData());
                    } else {
                        selectedObjects.remove(item.getData());
                    }
                }
            }
        });

        selectAll(viewer);

        return composite;
    }

    private void selectAll(TreeViewer viewer)
    {
        TreeItem[] items = viewer.getTree().getItems();
        for (TreeItem item : items) {
            item.setChecked(true);
            selectedObjects.add(item.getData());
        }
    }

    private void selectNone(TreeViewer viewer)
    {
        TreeItem[] items = viewer.getTree().getItems();
        for (TreeItem item : items) {
            item.setChecked(false);
            selectedObjects.remove(item.getData());
        }
    }

    public Set<Object> getSelectedObjects()
    {
        return selectedObjects;
    }

    @Override
    public boolean close()
    {
        if (getReturnCode() == Window.CANCEL) {
            return super.close();
        }

        boolean warn = false;
        for (Object o : selectedObjects) {
            if ((o instanceof DataManager) || (o instanceof XWikiEclipseSpaceSummary)) {
                warn = true;
                break;
            }
        }

        if (warn) {
            MessageBox messageBox = new MessageBox(getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION);
            messageBox
                .setMessage("Your selection contains spaces or data managers. Are you really sure that you want to delete them?");
            int result = messageBox.open();
            if (result == SWT.YES) {
                return super.close();
            } else {
                return false;
            }
        }

        return super.close();
    }

}
