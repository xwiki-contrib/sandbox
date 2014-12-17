using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using XWiki.Logging;
using System.Windows.Forms;
using UICommons;

namespace XWord2003.UIManagement
{
    /// <summary>
    /// Utility class that contains methods coresponding to UI actions
    /// (XWord menu items and XWord toolbar buttons).
    /// </summary>
    class UIActions
    {
        /// <summary>
        /// Private constructor.
        /// </summary>
        private UIActions()
        {
        }

        public static void ActionAddAttachments()
        {
            if (Globals.XWord2003AddIn.Wiki != null
                && ("" + Globals.XWord2003AddIn.CurrentPageFullName).Length > 1
                && Globals.XWord2003AddIn.CurrentPagePublished)
            {
                Globals.XWord2003AddIn.AddinActions.AttachFiles(Globals.XWord2003AddIn.CurrentPageFullName);
            }
            else
            {
                UserNotifier.Exclamation(UIMessages.NOT_A_PUBLISHED_PAGE);
            }
        }

        public static void ActionViewInBrowser()
        {
            if (Globals.XWord2003AddIn.Wiki != null
                && ("" + Globals.XWord2003AddIn.CurrentPageFullName).Length > 1
                && Globals.XWord2003AddIn.CurrentPagePublished)
            {
                string pageFullName = Globals.XWord2003AddIn.CurrentPageFullName;
                string pageURL = Globals.XWord2003AddIn.Client.GetURL(pageFullName);
                Globals.XWord2003AddIn.AddinActions.StartProcess(pageURL);
            }
            else
            {
                UserNotifier.Exclamation(UIMessages.NOT_A_PUBLISHED_PAGE);
            }
        }

        public static void ActionViewAttachments()
        {
            if (Globals.XWord2003AddIn.Wiki != null
                && ("" + Globals.XWord2003AddIn.CurrentPageFullName).Length > 1
                && Globals.XWord2003AddIn.CurrentPagePublished)
            {
                ViewPageAttachmentsForm viewPageAttachmentsForm = new ViewPageAttachmentsForm(Globals.XWord2003AddIn.CurrentPageFullName);
                ViewPageAttachmentsFormManager viewAttachmentsManager = new ViewPageAttachmentsFormManager(ref viewPageAttachmentsForm);
                viewAttachmentsManager.EnqueueAllHandlers();
                viewPageAttachmentsForm.ShowDialog();
            }
            else
            {
                UserNotifier.Exclamation(UIMessages.NOT_A_PUBLISHED_PAGE);
            }
        }

        public static void ActionNewSpaceOrPage()
        {
            if (Globals.XWord2003AddIn.Wiki != null)
            {
                AddPageForm addPageForm = new AddPageForm(ref Globals.XWord2003AddIn.Wiki);
                new AddPageFormManager(ref addPageForm).EnqueueAllHandlers();
                addPageForm.ShowDialog();
            }
            else
            {
                UserNotifier.Error(UIMessages.WIKI_STRUCTURE_NOT_LOADED);
            }
        }

        public static void ActionOpenWikiPage()
        {
            if (Globals.XWord2003AddIn.Wiki != null)
            {
                EditPageForm editPageForm = new EditPageForm();
                new EditPageFormManager(ref editPageForm).EnqueueAllHandlers();
                editPageForm.ShowDialog();
            }
            else
            {
                UserNotifier.Error(UIMessages.WIKI_STRUCTURE_NOT_LOADED);
            }
        }

        public static void ActionPublishCurrentPage()
        {
            Globals.XWord2003AddIn.AddinActions.SaveToServer();
        }

        public static void ActionAboutXWord()
        {
            MessageBox.Show("XWiki integration with MS Office 2003", "XWord2003", MessageBoxButtons.OK, MessageBoxIcon.Information);
        }

        public static void ActionXWordSettings()
        {
            AddinSettingsForm addinSettingsForm = new AddinSettingsForm();
            new AddinSettingsFormManager(ref addinSettingsForm).EnqueueAllHandlers();
            addinSettingsForm.ShowDialog();
        }
    }
}
