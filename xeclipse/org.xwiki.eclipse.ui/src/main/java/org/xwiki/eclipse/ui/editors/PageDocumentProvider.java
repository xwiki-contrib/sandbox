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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.xwiki.eclipse.core.model.XWikiEclipsePage;
import org.xwiki.eclipse.ui.editors.scanners.XWikiPartitionScanner;
import org.xwiki.eclipse.ui.utils.XWikiEclipseSafeRunnable;

public class PageDocumentProvider extends FileDocumentProvider
{
    private PageEditor pageEditor;

    public PageDocumentProvider(PageEditor pageEditor)
    {
        super();
        this.pageEditor = pageEditor;
    }

    @Override
    protected IDocument createDocument(Object element) throws CoreException
    {
        if (element instanceof PageEditorInput) {
            PageEditorInput pageEditorInput = (PageEditorInput) element;

            IDocument document = new Document(pageEditorInput.getPage().getData().getContent());

            IDocumentPartitioner partitioner =
                new FastPartitioner(new XWikiPartitionScanner(), XWikiPartitionScanner.ALL_PARTITIONS);
            partitioner.connect(document);
            document.setDocumentPartitioner(partitioner);

            return document;
        }

        return super.createDocument(element);
    }

    @Override
    public boolean isModifiable(Object element)
    {
        if (element instanceof PageEditorInput) {
            PageEditorInput pageEditorInput = (PageEditorInput) element;
            return !pageEditorInput.isReadOnly();
        }

        return super.isModifiable(element);
    }

    @Override
    public boolean isReadOnly(Object element)
    {
        if (element instanceof PageEditorInput) {
            PageEditorInput pageEditorInput = (PageEditorInput) element;
            return pageEditorInput.isReadOnly();
        }

        return super.isReadOnly(element);
    }

    @Override
    protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean overwrite)
        throws CoreException
    {
        if (element instanceof PageEditorInput) {
            final PageEditorInput pageEditorInput = (PageEditorInput) element;
            pageEditorInput.getPage().getData().setContent(document.get());

            SafeRunner.run(new XWikiEclipseSafeRunnable()
            {
                public void run() throws Exception
                {
                    final XWikiEclipsePage page =
                        pageEditorInput.getPage().getDataManager().storePage(pageEditorInput.getPage());

                    Display.getDefault().syncExec(new Runnable()
                    {
                        public void run()
                        {
                            pageEditor.setInput(new PageEditorInput(page, pageEditorInput.isReadOnly()));
                        }
                    });
                }
            });

        }

        super.doSaveDocument(monitor, element, document, overwrite);
    }

}
