using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using XWiki.Logging;
using System.IO;
using XWiki.Clients;
using XWord2003.UIManagement;

namespace XWord2003
{
    public class ViewPageAttachmentsFormManager : AbstractViewPageAttachmentsFormActionsManager
    {
        private ViewPageAttachmentsForm viewPageAttachmentsForm;
        private string pageFullName;
        private string attachmentName;
        private List<string> attachments;
        private AddinActionsDispatcher addinActions;
        private IXWikiClient client;

        public ViewPageAttachmentsFormManager(ref ViewPageAttachmentsForm viewPageAttachmentsForm)
        {
            this.viewPageAttachmentsForm = viewPageAttachmentsForm;
            this.addinActions = Globals.XWord2003AddIn.AddinActions;
            this.client = Globals.XWord2003AddIn.Client;
        }

        public override void EnqueueAllHandlers()
        {
            viewPageAttachmentsForm.OnFormLoad = this.ActionFormLoad;
            viewPageAttachmentsForm.OnAttachmentChange = this.ActionAttachmentChange;
            viewPageAttachmentsForm.OnOpenAttachment = this.ActionOpenAttachment;
            viewPageAttachmentsForm.OnCancel = this.ActionCancel;
        }

        protected override void ActionFormLoad(object sender, EventArgs e)
        {
            pageFullName = viewPageAttachmentsForm.PageFullName;
            try
            {
                attachments = client.GetDocumentAttachmentList(pageFullName);
            }
            catch
            {
                UserNotifier.Error(UIMessages.ERROR_GET_ATTACHMENTS_LIST);
                viewPageAttachmentsForm.Close();
                return;
            }
            viewPageAttachmentsForm.LabelPageNameText = pageFullName;
            viewPageAttachmentsForm.ListBoxAttachmentsDataSource = attachments;
            if (attachments.Count > 0)
            {
                viewPageAttachmentsForm.ListBoxAttachmentsIndex = 0;
                attachmentName = attachments[0];
            }

        }

        protected override void ActionAttachmentChange(object sender, EventArgs e)
        {
            attachmentName = attachments[viewPageAttachmentsForm.ListBoxAttachmentsIndex];
        }

        protected override void ActionOpenAttachment(object sender, EventArgs e)
        {
            if (viewPageAttachmentsForm.ListBoxAttachmentsIndex < 0 || attachments == null || attachments.Count < 1)
            {
                UserNotifier.Exclamation("No attachment selected!");
            }
            else
            {
                string path = addinActions.SaveFileDialog(attachmentName);
                if (path != null)
                {
                    FileInfo attachmentInfo = addinActions.DownloadAttachment(pageFullName, attachmentName, path);
                    addinActions.StartProcess(attachmentInfo.FullName);
                }
            }
        }

        protected override void ActionCancel(object sender, EventArgs e)
        {

        }
    }
}
