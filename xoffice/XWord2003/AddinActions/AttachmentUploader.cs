using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using XWiki.Clients;
using XWiki;
using XWiki.Logging;
using System.IO;
using XWord2003.UIManagement;

namespace XWord2003.AddinActions
{
    /// <summary>
    /// Uploads attachments to an wiki page.
    /// </summary>
    public class AttachmentUploader : IAction<bool>
    {
        private XWord2003AddIn addin;
        private IXWikiClient client;
        private string space = null, page = null;
        private bool result;
        private string[] fileNames;

        public AttachmentUploader(XWord2003AddIn addin, string pageFullName, IXWikiClient client, string[] fileNames)
        {
            this.addin = addin;
            this.client = client;
            this.fileNames = fileNames;

            if (pageFullName != null)
            {
                if (!this.client.LoggedIn)
                {
                    client.Login(addin.Username, addin.Password);
                }
                int index = pageFullName.IndexOf(".");
                space = pageFullName.Substring(0, index);
                page = pageFullName.Substring(index + 1);
            }
            else
            {
                result = false;
            }
        }

        public AttachmentUploader(XWord2003AddIn addin, string space, string page, IXWikiClient client, string[] fileNames)
        {
            this.addin = addin;
            this.client = client;
            this.space = space;
            this.page = page;
            this.fileNames = fileNames;
        }


        #region IAction<bool> Members

        /// <summary>
        /// Tries to upload attachments to an xwiki page.
        /// Stops when one upload fails.
        /// </summary>
        public void Perform()
        {
            if (space == null || page == null)
            {
                Log.Error("Trying to attach a file to a page with an invalid name!");
                result = false;
            }
            if (!this.client.LoggedIn)
            {
                client.Login(addin.Username, addin.Password);
            }
            bool operationCompleted = false;

            if (addin.Application.ActiveDocument == null)
            {
                UserNotifier.Message(UIMessages.NO_OPENED_DOCUMENT);
            }
            operationCompleted = false;
            int totalUploadedAttachments = 0;
            foreach (string fileName in fileNames)
            {
                if (client.AddAttachment(space, page, fileName))
                {
                    totalUploadedAttachments++;
                }
                else
                {
                    break;
                }
            }
            operationCompleted = (totalUploadedAttachments == fileNames.Length);

            result = operationCompleted;
        }

        /// <summary>
        /// Gets the results of the attachments upload.
        /// TRUE when all attachments were successfuly uploaded.
        /// </summary>
        /// <returns></returns>
        public bool GetResults()
        {
            return result;
        }

        #endregion
    }
}
