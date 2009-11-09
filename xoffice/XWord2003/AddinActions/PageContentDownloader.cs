using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using XWiki.Office.Word;
using XWiki.Clients;
using System.IO;
using XWiki.Logging;

namespace XWord2003.AddinActions
{
    public class PageContentDownloader : IAction<string>
    {
        private XWord2003AddIn addin;
        private IXWikiClient client;
        private Dictionary<String, ConversionManager> pageConverters;
        private string pageFullName;

        private string localFileName;

        public PageContentDownloader(ref XWord2003AddIn addin, string pageFullName, IXWikiClient client, Dictionary<String, ConversionManager> pageConverters)
        {
            this.addin = addin;
            this.pageFullName = pageFullName;
            this.client = client;
            this.pageConverters = pageConverters;
        }

        #region IAction<string> Members

        public void Perform()
        {
            try
            {
                if (!client.LoggedIn)
                {
                    client.Login(addin.Username, addin.Password);
                }
                string content = client.GetRenderedPageContent(pageFullName);
                localFileName = pageFullName.Replace(".", "-");
                string folder = addin.PagesRepository + "TempPages";
                new FolderAttributesCleaner(folder).Perform();

                ConversionManager pageConverter;
                if (pageConverters.ContainsKey(pageFullName))
                {
                    pageConverter = pageConverters[pageFullName];
                }
                else
                {
                    pageConverter = new ConversionManager(addin.ServerURL, folder, pageFullName, localFileName, addin.Client);
                    pageConverters.Add(pageFullName, pageConverter);
                }

                content = pageConverter.ConvertFromWebToWord(content);
                localFileName = folder + "\\" + localFileName + ".html";
                addin.CurrentLocalFilePath = localFileName;

                if (!Directory.Exists(folder))
                {
                    Directory.CreateDirectory(folder);
                }
                FileStream stream = new FileStream(localFileName, FileMode.Create);
                byte[] buffer = UTF8Encoding.UTF8.GetBytes(content);
                stream.Write(buffer, 0, buffer.Length);
                stream.Close();
            }
            catch (IOException ex)
            {
                UserNotifier.Error(ex.Message);
            }

        }

        /// <summary>
        /// Gets the result of the performed action.
        /// </summary>
        /// <returns>The local file name of the downloaded wiki page.</returns>
        public string GetResults()
        {
            return localFileName;
        }

        #endregion
    }
}
