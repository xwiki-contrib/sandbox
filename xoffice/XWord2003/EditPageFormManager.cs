using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using XWiki.Logging;
using XWord2003.UIManagement;

namespace XWord2003
{
    /// <summary>
    /// Actions manager for <code>EditPageForm</code>.
    /// </summary>
    public class EditPageFormManager : AbstractEditPageFormActionsManager
    {
        private EditPageForm editPageForm;
        private List<string> spacesNames;
        private List<string> pagesNames;
        private string pageFullName;
        private bool editPageOnClose=false;

        /// <summary>
        /// Creates an instance of the <code>EditPageFormManager</code> that can be used to
        /// enqueue known event handlers for an instance of the <code>EditPageForm</code>.
        /// </summary>
        /// <param name="editPageForm">A reference to an <code>EditPageForm</code>.</param>
        public EditPageFormManager(ref EditPageForm editPageForm)
        {
            this.editPageForm = editPageForm;
        }

        /// <summary>
        /// Enqueues know event handlers for an instance of the <code>EditPageForm</code>.
        /// </summary>
        public override void EnqueueAllHandlers()
        {
            editPageForm.OnCancel = this.ActionCancel;
            editPageForm.OnEditPage = this.ActionEditPage;
            editPageForm.OnFormLoad = this.ActionFormLoad;
            editPageForm.OnSpaceChange = this.ActionSpaceChange;
            editPageForm.OnViewAttachments = this.ActionViewAttachments;
            editPageForm.Disposed += new EventHandler(editPageForm_Disposed);

            editPageOnClose = false;
        }
 
        /// <summary>
        /// AFTER the form is closed and disposed, the selected wiki page will be edited.
        /// </summary>
        /// <param name="sender">Sender object.</param>
        /// <param name="e">Event arguments.</param>
        void editPageForm_Disposed(object sender, EventArgs e)
        {
            if (editPageOnClose)
            {
                Globals.XWord2003AddIn.AddinActions.EditPage(pageFullName);
            }
        }

        /// <summary>
        /// Action to be performed on form load.
        /// If the wiki structure is loaded, populates the spaces ListBox, selects the first space
        /// and populates the pages ListBox with pages names from selected space.
        /// </summary>
        /// <param name="sender">Sender object.</param>
        /// <param name="e">Event arguments.</param>
        protected override void ActionFormLoad(object sender, EventArgs e)
        {
            if (Globals.XWord2003AddIn.Wiki != null)
            {
                spacesNames = new List<string>();
                pagesNames = new List<string>();

                foreach (XWiki.Space space in Globals.XWord2003AddIn.Wiki.spaces)
                {
                    spacesNames.Add(space.name);
                }

                XWiki.Space firstSpace = Globals.XWord2003AddIn.Wiki.spaces[0];

                foreach (XWiki.XWikiDocument page in firstSpace.documents)
                {
                    pagesNames.Add(page.name);
                }

                editPageForm.ListBoxPagesDataSource = pagesNames;
                editPageForm.ListBoxPagesSelectedIndex = 0;
                editPageForm.ListBoxSpacesDataSource = spacesNames;
                editPageForm.ListBoxSpacesSelectedIndex = 0;
            }
            else
            {
                UserNotifier.Error(UIMessages.WIKI_STRUCTURE_NOT_LOADED);
            }
        }

        /// <summary>
        /// Action to be performed on edit page (ex: when the user clicks 'Edit' button).
        /// Closed the form, and the page wil be edited when the form is disposed.
        /// </summary>
        /// <param name="sender">Sender object.</param>
        /// <param name="e">Event arguments.</param>
        protected override void ActionEditPage(object sender, EventArgs e)
        {
            if (editPageForm.ListBoxPagesSelectedValue != null && editPageForm.ListBoxSpacesSelectedValue != null)
            {
                pageFullName = editPageForm.ListBoxSpacesSelectedValue.ToString() + "." + editPageForm.ListBoxPagesSelectedValue.ToString();
                editPageOnClose = true;
                editPageForm.DialogResult = DialogResult.OK;
                editPageForm.Visible = false;
                editPageForm.Close();
                editPageForm.Dispose();
            }
            else
            {
                editPageOnClose = false;
                UserNotifier.Exclamation(UIMessages.SELECT_SPACE_AND_PAGE);
            }
        }

        /// <summary>
        /// Action to be performed on canceling (ex: user clicks 'Cancel' button or presses ESC key).
        /// The dialog result of the form is set to <code>DialogResult.Cancel</code> and then the form is closed.
        /// </summary>
        /// <param name="sender">Sender object.</param>
        /// <param name="e">Event arguments.</param>
        protected override void ActionCancel(object sender, EventArgs e)
        {
            editPageForm.DialogResult = DialogResult.Cancel;
            editPageForm.Close();
        }

        /// <summary>
        /// Action to be performed when selected space is changed.
        /// The pages ListBox is populated with pages names from the selected space.
        /// </summary>
        /// <param name="sender">Sender object.</param>
        /// <param name="e">Event arguments.</param>
        protected override void ActionSpaceChange(object sender, EventArgs e)
        {
            int selectedIndex = editPageForm.ListBoxSpacesSelectedIndex;
            XWiki.Space selectedSpace = null;
            try
            {
                selectedSpace = Globals.XWord2003AddIn.Wiki.spaces[selectedIndex];
            }
            catch { }
            if (selectedSpace != null)
            {
                pagesNames.Clear();
                foreach (XWiki.XWikiDocument page in selectedSpace.documents)
                {
                    pagesNames.Add(page.name);
                }
                editPageForm.ListBoxPagesDataSource = null;
                editPageForm.ListBoxPagesDataSource = pagesNames;
            }

        }

        protected override void ActionViewAttachments(object sender, EventArgs e)
        {
            editPageOnClose = false;
            if (editPageForm.ListBoxPagesSelectedValue != null && editPageForm.ListBoxSpacesSelectedValue != null)
            {
                pageFullName = editPageForm.ListBoxSpacesSelectedValue.ToString() + "." + editPageForm.ListBoxPagesSelectedValue.ToString();
                ViewPageAttachmentsForm viewPageAttachmentsForm = new ViewPageAttachmentsForm(pageFullName);
                ViewPageAttachmentsFormManager viewAttachmentsManager = new ViewPageAttachmentsFormManager(ref viewPageAttachmentsForm);
                viewAttachmentsManager.EnqueueAllHandlers();
                viewPageAttachmentsForm.ShowDialog();
            }
            else
            {
                UserNotifier.Exclamation(UIMessages.SELECT_SPACE_AND_PAGE);
            }
        }
    }
}
