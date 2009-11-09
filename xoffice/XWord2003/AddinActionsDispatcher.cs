using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using XWiki.Clients;
using System.Windows.Forms;
using XWiki.Logging;
using XWord2003.AddinActions;
using System.IO;
using XWiki;
using XWord2003.Util;
using XWiki.Office.Word;
using ContentFiltering.Office.Word.Cleaners;
using System.Runtime.InteropServices;
using XWord2003.UIManagement;
using Word = Microsoft.Office.Interop.Word;

namespace XWord2003
{
    public class AddinActionsDispatcher
    {
        #region Private members

        private XWord2003AddIn addin;
        private string newPageText = "Hi! This is your new page. Please put your content here and then share it with others by saving it on the wiki.";

        //A dictionary, storing the converter instances for each opened page.
        Dictionary<String, ConversionManager> pageConverters = new Dictionary<string, ConversionManager>();

        //The instance of the newest Word document.
        private Word.Document newDoc;              

        #endregion Private members


        /// <summary>
        /// Generic webclient used for conneting to xwiki.
        /// </summary>        
        public IXWikiClient Client
        {
            get { return addin.Client; }
        }

        /// <summary>
        /// Creates an instance of the <code>AddinActionsDispatcher</code>.
        /// </summary>
        /// <param name="addin">The <code>XWord2003AddIn</code>.</param>
        public AddinActionsDispatcher(XWord2003AddIn addin)
        {
            this.addin = addin;
            addin.Application.WindowDeactivate += new Word.ApplicationEvents4_WindowDeactivateEventHandler(KeepNewWindowActivated);        
        }


        /// <summary>
        /// Starts editing a new wiki page. The page will not be created in the wiki until the fisrt save.
        /// </summary>
        /// <param name="space">The name of the wiki space.</param>
        /// <param name="pageName">The name of page.</param>
        public void AddNewPage(string space, string pageName)
        {
            AddNewPage(space, pageName, null, null);
        }

        /// <summary>
        /// Starts editing a new wiki page. The page will not be created in the wiki until the fisrt save.
        /// </summary>
        /// <param name="spaceName">The name of the wiki space.</param>
        /// <param name="pageName">The name of page.</param>
        /// <param name="pageTitle">The title of the page.</param>
        /// <param name="sender">
        /// The instance of the form that started the action.
        /// This form need to be closed before swithing the Active Word Document.
        /// </param>
        public void AddNewPage(string spaceName, string pageName, string pageTitle, Form sender)
        {
            //Any modal dialog nust be closed before opening or closing active documents.
            if (sender != null)
            {
                //hide the form before closing to prevent reactivation and focus request.
                sender.Hide();
                sender.Close();
                Application.DoEvents();
            }
            try
            {
                if (!this.Client.LoggedIn)
                {
                    Client.Login(addin.Username, addin.Password);
                }
                String pageFullName = spaceName + "." + pageName;
                String localFileName = pageFullName.Replace(".", "-");
                String folder = addin.PagesRepository + "TempPages";
                new FolderAttributesCleaner(folder).Perform();
                //content = new WebToLocalHTML(addin.serverURL, folder, localFileName).AdaptSource(content);
                ConversionManager pageConverter = new ConversionManager(addin.ServerURL, folder, pageFullName, localFileName, addin.Client);
                localFileName = folder + "\\" + localFileName + ".html";
                addin.CurrentLocalFilePath = localFileName;
                //Save the file
                if (!Directory.Exists(folder))
                {
                    Directory.CreateDirectory(folder);
                }
                String pageContent = "<h1>" + pageTitle + "</h1>" + Environment.NewLine;
                pageContent = pageContent + newPageText;
                FileStream stream = new FileStream(localFileName, FileMode.Create);
                //byte[] buffer = UTF8Encoding.UTF8.GetBytes(pageContent.ToString());
                Encoding iso = Client.ServerEncoding;
                byte[] buffer = iso.GetBytes(pageContent);
                stream.Write(buffer, 0, buffer.Length);
                stream.Close();
                addin.CurrentPageFullName = pageFullName;
                //Since it's a new page, it's not published
                addin.CurrentPagePublished = false;
                addin.PublishedStatus.Add(pageFullName, false);

                addin.EditedPages.Add(localFileName, pageFullName);

                //Open the file with Word
                HTMLDocumentOpener htmlDocOpener = new HTMLDocumentOpener(addin, localFileName);
                htmlDocOpener.Perform();
                Word.Document doc = htmlDocOpener.GetResults();
                doc.Activate();
                newDoc = doc;
                
                //If it's a new space, add it to the wiki structure and mark it as unpublished
                List<Space> spaces = Globals.XWord2003AddIn.Wiki.spaces;
                Space space = null;
                foreach (Space sp in spaces)
                {
                    if (sp.name == spaceName)
                    {
                        space = sp;

                        //Add the new page to the wiki structure and mark it as unpublished
                        XWikiDocument xwdoc = new XWikiDocument();
                        xwdoc.name = pageName;
                        xwdoc.published = false;
                        xwdoc.space = spaceName;
                        space.documents.Add(xwdoc);
                        break;
                    }
                }

                if (space == null)
                {
                    space = new Space();
                    space.name = spaceName;
                    space.published = false;
                    Globals.XWord2003AddIn.Wiki.spaces.Add(space);

                    //Add the new page to the wiki structure and mark it as unpublished
                    XWikiDocument xwdoc = new XWikiDocument();
                    xwdoc.name = pageName;
                    xwdoc.published = false;
                    xwdoc.space = spaceName;
                    space.documents.Add(xwdoc);
                }
            }
            catch (IOException ex)
            {
                UserNotifier.Error(ex.Message);
            }
        }

        void KeepNewWindowActivated(Word.Document Doc, Word.Window Wn)
        {
            if (newDoc == Doc)
            {
                Doc.Activate();
            }
        }


        /// <summary>
        /// Saves the currently edited page or document to the server.
        /// </summary>
        public void SaveToServer()
        {
            string msgSaving = "Saving to server ...";
            string msgPublished = "Page published!";

            addin.StatusMessage = msgSaving;
            addin.Application.StatusBar = msgSaving;

            if (addin.CurrentPageFullName == "" || addin.CurrentPageFullName == null)
            {
                UserNotifier.Exclamation(UIMessages.NOT_A_WIKI_PAGE);
                return;
            }
            SaveToXWiki();

            if (!addin.CurrentPagePublished)
            {
                //mark page as published
                addin.CurrentPagePublished = true;
                if (addin.PublishedStatus.ContainsKey(addin.CurrentPageFullName))
                {
                    addin.PublishedStatus[addin.CurrentPageFullName] = true;
                }
                else
                {
                    addin.PublishedStatus.Add(addin.CurrentPageFullName, true);
                }

                char[] separator = { '.' };
                string[] pageFullName = addin.CurrentPageFullName.Split(separator, StringSplitOptions.RemoveEmptyEntries);
                string spaceName = pageFullName[0].Trim();
                string pageName = pageFullName[1].Trim();

                XWiki.Space currentSpace = null;

                //if it's a new space add it to the local wiki structure
                bool spaceExists = false;
                foreach (XWiki.Space space in addin.Wiki.spaces)
                {
                    if (space.name == spaceName)
                    {
                        spaceExists = true;
                        currentSpace = space;
                        break;
                    }
                }
                if (!spaceExists)
                {
                    currentSpace = new Space();
                    currentSpace.name = spaceName;
                    currentSpace.documents = new List<XWikiDocument>();
                    currentSpace.hidden = false;
                    currentSpace.published = true;
                }

                //add the new page to the space in the local wiki structure
                bool pageExists = false;
                foreach (XWiki.XWikiDocument page in currentSpace.documents)
                {
                    if (page.name == pageName)
                    {
                        pageExists = true;
                        break;
                    }
                }
                if (!pageExists)
                {
                    XWiki.XWikiDocument currentPage = new XWikiDocument();
                    currentPage.name = pageName;
                    currentPage.published = true;
                    currentPage.space = spaceName;
                    currentSpace.documents.Add(currentPage);
                }
            }

            addin.StatusMessage = msgPublished;
            addin.Application.StatusBar = msgPublished;
        }

        /// <summary>
        /// Saves the currently edited page or document to the server.
        /// </summary>
        private void SaveToXWiki()
        {
            try
            {
                String contentFilePath = "";
                addin.ReinforceApplicationOptions();
                String filePath = addin.ActiveDocumentFullName;
                String currentFileName = Path.GetDirectoryName(addin.ActiveDocumentFullName);
                currentFileName += "\\" + Path.GetFileNameWithoutExtension(addin.ActiveDocumentFullName);
                String tempExportFileName = currentFileName + "_TempExport.html";
                if (!ShadowCopyDocument(addin.ActiveDocumentInstance, tempExportFileName, addin.SaveFormat))
                {
                    UserNotifier.Error(UIMessages.ERROR_SAVING_PAGE);
                    return;
                }
                contentFilePath = tempExportFileName;
                StreamReader sr = new StreamReader(contentFilePath);
                String fileContent = sr.ReadToEnd();
                sr.Close();
                File.Delete(contentFilePath);
                String cleanHTML = "";

                cleanHTML = new CommentsRemover().Clean(fileContent);
                cleanHTML = new HeadSectionRemover().Clean(cleanHTML);

                ConversionManager pageConverter;
                if (pageConverters.ContainsKey(addin.CurrentPageFullName))
                {
                    pageConverter = pageConverters[addin.CurrentPageFullName];
                }
                else
                {
                    pageConverter = new ConversionManager(addin.ServerURL, Path.GetDirectoryName(contentFilePath),
                                                          addin.CurrentPageFullName, Path.GetFileName(contentFilePath), addin.Client);
                }
                cleanHTML = pageConverter.ConvertFromWordToWeb(cleanHTML);
                cleanHTML = new BodyContentExtractor().Clean(cleanHTML);

                if (addin.AddinStatus.Syntax == null)
                {
                    addin.AddinStatus.Syntax = addin.DefaultSyntax;
                }


                //Convert the source to the propper encoding.
                Encoding iso = Client.ServerEncoding;
                byte[] content = Encoding.Unicode.GetBytes(cleanHTML);
                byte[] wikiContent = null;
                wikiContent = Encoding.Convert(Encoding.Unicode, iso, content);
                cleanHTML = iso.GetString(wikiContent);
                SavePage(addin.CurrentPageFullName, ref cleanHTML, addin.AddinStatus.Syntax);
            }
            catch (COMException ex)
            {
                string message = "An internal Word error appeared when trying to save your file.";
                message += Environment.NewLine + ex.Message;
                Log.Exception(ex);
                UserNotifier.Error(message);
            }
        }

        /// <summary>
        /// Saves the page to the wiki. Shows a message box with an error if the operation fails.
        /// </summary>
        /// <param name="pageName">The full name of the wiki page.</param>
        /// <param name="pageContent">The contant to be saved.</param>
        /// <param name="syntax">The wiki syntax of the saved page.</param>
        /// <returns>TRUE if the page was saved successfully.</returns>
        private bool SavePage(String pageName, ref string pageContent, string syntax)
        {
            bool saveSucceeded = false;
            GrammarAndSpellingSettings.Save(ref addin);
            GrammarAndSpellingSettings.Disable();

            if (!this.Client.LoggedIn)
            {
                Client.Login(addin.Username, addin.Password);
            }


            if (!Client.SavePageHTML(pageName, pageContent, syntax))
            {
                Log.Error("Failed to save page " + pageName + "on server " + addin.ServerURL);
                UserNotifier.Error(UIMessages.SERVER_ERROR_SAVING_PAGE);
                saveSucceeded = false;
            }
            else
            {
                saveSucceeded = true;
                //mark the page from wiki structure as published
                bool markedDone = false;
                foreach (Space sp in addin.Wiki.spaces)
                {
                    foreach (XWikiDocument xwdoc in sp.documents)
                    {
                        if ((xwdoc.space + "." + xwdoc.name) == pageName)
                        {
                            sp.published = true;
                            xwdoc.published = true;
                            markedDone = true;
                            break;//inner foreach
                        }
                    }
                    if (markedDone)
                    {
                        break;//outer foreach
                    }
                }
            }

            GrammarAndSpellingSettings.Restore();
            return saveSucceeded;
        }

        /// <summary>
        /// Edits a wiki page.
        /// </summary>
        /// <param name="pageFullName">The full name of the wiki page that is being opened for editing.</param>
        public void EditPage(string pageFullName)
        {
            if (IsOpened(pageFullName))
            {
                UserNotifier.Message(UIMessages.ALREADY_EDITING_PAGE);
                return;
            }
            if (!this.Client.LoggedIn)
            {
                Client.Login(addin.Username, addin.Password);
            }

            if (IsProtectedPage(pageFullName, addin.ProtectedPages))
            {
                UserNotifier.StopHand(UIMessages.PROTECTED_PAGE);
                return;
            }


            GetPage(pageFullName);
        }

        /// <summary>
        /// Downloads page content, opens the page in word and marks the documents as saved.
        /// </summary>
        /// <param name="_pageFullName">Page full name.</param>
        private void GetPage(Object _pageFullName)
        {
            try
            {
                //Download page content
                string pageFullName = _pageFullName.ToString();
                PageContentDownloader pageContentDownloader = new PageContentDownloader(ref addin, _pageFullName.ToString(), Client, pageConverters);
                pageContentDownloader.Perform();
                string localFileName = pageContentDownloader.GetResults();

                //Register new local filename as a wiki page.
                addin.EditedPages.Add(localFileName, pageFullName);
                addin.CurrentPageFullName = pageFullName;
                //Since the page exists on server, consider it published
                addin.CurrentPagePublished = true;
                if (addin.PublishedStatus.ContainsKey(pageFullName))
                {
                    addin.PublishedStatus[pageFullName] = true;
                }
                else
                {
                    addin.PublishedStatus.Add(pageFullName, true);
                }

                //Open the file with Word
                HTMLDocumentOpener htmlDocOpener = new HTMLDocumentOpener(addin, localFileName);
                try
                {
                    htmlDocOpener.Perform();
                }
                catch { } //MS Word might thing some modal forms are still opened

                Word.Document doc = htmlDocOpener.GetResults();
                doc.Activate();
                newDoc = doc;
                if (doc != null)
                {
                    //Mark just-opened document as saved. This prevents a silly confirmation box that
                    //warns about unsaved changes when closing an unchanged document.
                    doc.Saved = true;
                }

            }
            catch (Exception ex)
            {
                UserNotifier.Error(ex.Message);
            }
        }

        /// <summary>
        /// Downloads the file to a local folder. The folder is located in MyDocuments
        /// </summary>
        /// <param name="pageFullName">FullName of the wiki page - Space.Name</param>
        /// <param name="attachmentName">Name of the attached file</param>
        /// <param name="path">The location where the attachment will be downloaded. Use null for default location.</param>
        /// <returns></returns>
        public FileInfo DownloadAttachment(String pageFullName, String attachmentName, String path)
        {
            AttachmentDownloader attachmentDownloader = new AttachmentDownloader(addin, pageFullName, attachmentName, path, this.Client);
            attachmentDownloader.Perform();
            return attachmentDownloader.GetResults();
        }

        /// <summary>
        /// Uploads attachments to a page.
        /// </summary>
        /// <param name="pageFullName">The full name of the wiki page.</param>
        public bool AttachFiles(String pageFullName)
        {

            string[] fileNames = SelectLocalFiles();
            addin.Application.StatusBar = "Adding " + fileNames.Length + " attachments to " + pageFullName + " ...";
            AttachmentUploader attachmentUploader = new AttachmentUploader(addin, pageFullName, Client, fileNames);
            attachmentUploader.Perform();
            bool attached = attachmentUploader.GetResults();
            if (attached)
            {
                addin.Application.StatusBar = "Attachments uploaded!";
            }
            else
            {
                addin.Application.StatusBar = "Error uploading attachments!";
                UserNotifier.Error(UIMessages.ERROR_UPLOADING_ATTACHMENTS);
            }
            return attached;
        }

        /// <summary>
        /// Starts a new process. 
        ///  - If the file is executable then it will execute that file.
        ///  - If the file file is resistered to be opened with another application
        ///  then it will be opened with that application
        ///  - If the file doesnt have an assigned application then Windows Explorer openes it's directory.
        /// </summary>
        /// <param name="fullFileName">The full path and name of the file.</param>
        public void StartProcess(String fullFileName)
        {
            ProcessStarter processStarter = new ProcessStarter(fullFileName);
            processStarter.Perform();
        }

        /// <summary>
        /// Specifies if a wiki page is opened for editing.
        /// </summary>
        /// <param name="pageFullName">The full name of the page.</param>
        /// <returns>True if the page is already opened. False otherwise.</returns>
        public bool IsOpened(String pageFullName)
        {
            if (addin.EditedPages.ContainsValue(pageFullName))
            {
                return true;
            }
            return false;
        }

        /// <summary>
        /// Displays a SaveFileDialog
        /// </summary>
        /// <param name="fileName">Optional name of file to be saved.</param>
        /// <returns>The path to the file to be saved. Null if the user cancels the saving.</returns>
        public String SaveFileDialog(String fileName)
        {
            SaveFileDialog dialog = new SaveFileDialog();
            if (fileName != null)
            {
                dialog.DefaultExt = Path.GetExtension(fileName);
                dialog.FileName = Path.GetFileNameWithoutExtension(fileName);
                dialog.Filter = "(*." + dialog.DefaultExt + ")|*." + dialog.DefaultExt;
            }
            dialog.CheckPathExists = true;
            dialog.CustomPlaces.Add(addin.DownloadedAttachmentsRepository);
            dialog.CustomPlaces.Add(addin.PagesRepository);
            DialogResult result = dialog.ShowDialog();
            if (result == DialogResult.OK)
            {
                return dialog.FileName;
            }
            else
            {
                return null;
            }
        }

        private string[] SelectLocalFiles()
        {
            OpenFileDialog dialog = new OpenFileDialog();
            dialog.AutoUpgradeEnabled = true;
            dialog.CheckFileExists = true;
            dialog.CheckPathExists = true;
            dialog.DereferenceLinks = true;
            dialog.Multiselect = true;
            DialogResult result = dialog.ShowDialog();
            if (result == DialogResult.OK)
            {
                return dialog.FileNames;
            }
            else
            {
                return new string[0];
            }
        }

        /// <summary>
        /// Searches the server response for error strings.
        /// </summary>
        /// <param name="content">The server response.</param>
        /// <returns>True if the response contains error reports. False if the response does not ocntain error reports.</returns>
        public bool CheckForErrors(string content)
        {
            bool hasErrors = false;
            if (content.Contains(HTTPResponses.NO_PROGRAMMING_RIGHTS))
            {
                Log.Error("Server " + addin.ServerURL + " has no programming rights on getPageservice");
                UserNotifier.Error(UIMessages.SERVER_ERROR_NO_PROGRAMMING_RIGHTS);
                hasErrors = true;
            }
            else if (content.Contains(HTTPResponses.WRONG_REQUEST))
            {
                Log.Error("Server " + addin.ServerURL + " wrong request");
                UserNotifier.Error(UIMessages.SERVER_ERROR_WRONG_REQUEST);
                hasErrors = true;
            }
            else if (content.Contains(HTTPResponses.NO_EDIT_RIGHTS))
            {
                Log.Information("User tried to edit a page on " + addin.ServerURL + " whithout edit rights");
                UserNotifier.Error(UIMessages.SERVER_ERROR_NO_EDIT_RIGHTS);
                hasErrors = true;
            }
            else if (content.Contains(HTTPResponses.NO_GROOVY_RIGHTS))
            {
                Log.Error("Server " + addin.ServerURL + " error on parsing groovy - no groovy rights");
                UserNotifier.Error(UIMessages.SERVER_ERROR_NO_GROOVY_RIGHTS);
                hasErrors = true;
            }
            else if (content.Contains(HTTPResponses.INSUFFICIENT_MEMMORY))
            {
                Log.Error("Server " + addin.ServerURL + " reported OutOfMemmoryException");
                UserNotifier.Error(UIMessages.SERVER_ERROR_INSUFFICIENT_MEMORY);
                hasErrors = true;
            }
            else if (content.Contains(HTTPResponses.VELOCITY_PARSER_ERROR))
            {
                Log.Error("Server " + addin.ServerURL + " error when parsing page. ");
                UserNotifier.Error(UIMessages.SERVER_ERROR_VELOCITY_PARSER);
                hasErrors = true;
            }
            return hasErrors;
        }

        /// <summary>
        /// Removes all protected pages from the Word wiki structure.
        /// </summary>
        /// <param name="wiki">The wiki instance.</param>
        /// <param name="wildcards">The list of protected pages wildcards.</param>
        public void HideProtectedPages(WikiStructure wiki, List<String> wildcards)
        {
            foreach (XWikiDocument doc in wiki.GetAllDocuments())
            {
                foreach (String wildcard in wildcards)
                {
                    String docFullName = doc.space + "." + doc.name;
                    if (UtilityClass.IsWildcardMatch(wildcard, docFullName, true))
                    {
                        wiki.RemoveXWikiDocument(doc);
                        break;
                    }
                }
            }
        }

        /// <summary>
        /// Specifies if a page is protected or not.
        /// </summary>
        /// <param name="pageFullName">The full name of the page.</param>
        /// <param name="wildCards">The wildcard list of protected pages.</param>
        /// <returns></returns>
        public bool IsProtectedPage(String pageFullName, List<String> wildCards)
        {
            bool isProtectedPage = false;
            foreach (String wildcard in wildCards)
            {
                if (UtilityClass.IsWildcardMatch(wildcard, pageFullName, true))
                {
                    isProtectedPage = true;
                }
            }
            return isProtectedPage;
        }

        /// <summary>
        /// Copies a document to a specified path.
        /// </summary>
        /// <param name="document">The Document instance.</param>
        /// <param name="path">The path where the new file will be saved.</param>
        /// <param name="saveFormat">The save format.</param>
        /// <exception cref="IOException">When the file cannot be saved.</exception>
        /// <remarks>The document is saved in Unicode little endian encoding.</remarks>
        /// <returns>True if the operation succedes. False otherwise.</returns>
        public bool ShadowCopyDocument(Microsoft.Office.Interop.Word.Document document, string path, Microsoft.Office.Interop.Word.WdSaveFormat saveFormat)
        {
            DocumentCopier documentCopier = new DocumentCopier(document, path, saveFormat);
            documentCopier.Perform();
            return documentCopier.GetResults();
        }
    }
}
