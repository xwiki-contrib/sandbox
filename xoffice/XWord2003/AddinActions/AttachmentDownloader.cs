using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using XWiki.Clients;
using XWord2003.Util;

namespace XWord2003.AddinActions
{
    // <summary>
    /// Downloads the file to a local folder.
    /// </summary>
    public class AttachmentDownloader : IAction<FileInfo>
    {
        private XWord2003AddIn addin;
        private string pageFullName, attachmentName, path;
        private IXWikiClient client;
        private FileInfo fileInfo;

        public AttachmentDownloader(XWord2003AddIn addin, string pageFullName, string attachmentName, string path, IXWikiClient client)
        {
            this.addin = addin;
            this.pageFullName = pageFullName;
            this.attachmentName = attachmentName;
            this.path = path;
            this.client = client;
            this.fileInfo = null;
        }

        #region IAction<FileInfo> Members

        public void Perform()
        {
            if (!client.LoggedIn)
            {
                client.Login(addin.Username, addin.Password);
            }
            if (path == null)
            {
                path = addin.DownloadedAttachmentsRepository;
                if (new FileInfo(path).Exists)
                {
                    File.Create(path);
                }
                path = path + "\\" + UtilityClass.GenerateUniqueFileName(attachmentName, path);
            }
            fileInfo = new FileInfo(path);
            Directory.CreateDirectory(Path.GetDirectoryName(path));
            byte[] binaryContent = client.GetAttachmentContent(pageFullName, attachmentName);
            FileStream fileStream = fileInfo.Create();
            fileStream.Write(binaryContent, 0, binaryContent.Length);
            fileStream.Close();
        }

        public FileInfo GetResults()
        {
            return fileInfo;
        }

        #endregion
    }
}
