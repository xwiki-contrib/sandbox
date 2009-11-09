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

import java.util.ResourceBundle;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.editors.text.TextEditorActionContributor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.RetargetTextEditorAction;

public class XWikiEditorActionContributor extends TextEditorActionContributor
{
    private RetargetTextEditorAction contentAssistProposal;

    private RetargetTextEditorAction editorInfoAction;

    private static class EditorInfoAction extends Action
    {
        private ITextEditor editor;

        public EditorInfoAction(ITextEditor editor)
        {
            this.editor = editor;
        }

        @Override
        public void run()
        {
            if (editor instanceof PageEditor) {
                PageEditor pageEditor = (PageEditor) editor;
                int offset = pageEditor.getCaretOffset();

                IDocument document = pageEditor.getDocumentProvider().getDocument(editor.getEditorInput());

                try {
                    ITypedRegion partition = document.getPartition(offset);
                    System.out.format("Offset: %d Partition:(%d, %d, %s)\n", offset, partition.getOffset(), partition
                        .getLength(), partition.getType());
                    pageEditor.setSelectionRange(partition.getOffset(), partition.getLength());

                } catch (BadLocationException e) {
                }
            }
        }
    }

    public XWikiEditorActionContributor()
    {
        super();

        ResourceBundle bundle = ResourceBundle.getBundle("org.xwiki.eclipse.ui.editors.Editor");

        contentAssistProposal = new RetargetTextEditorAction(bundle, "ContentAssistProposal.");

        editorInfoAction = new RetargetTextEditorAction(bundle, "EditorInfo.");
    }

    public void contributeToMenu(IMenuManager menuManager)
    {
        super.contributeToMenu(menuManager);
        IMenuManager editMenu = menuManager.findMenuUsingPath(IWorkbenchActionConstants.M_EDIT);
        if (editMenu != null) {
            editMenu.add(new Separator());
            editMenu.add(contentAssistProposal);
            editMenu.add(editorInfoAction);
        }
    }

    @Override
    public void setActiveEditor(IEditorPart part)
    {
        super.setActiveEditor(part);

        ITextEditor editor = null;
        if (part instanceof ITextEditor) {
            editor = (ITextEditor) part;
        }

        contentAssistProposal.setAction(getAction(editor, "ContentAssistProposal"));
        editorInfoAction.setAction(new EditorInfoAction(editor));
    }

}
