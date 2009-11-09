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

using System;
using System.Windows.Forms;
using Microsoft.VisualStudio.Tools.Applications.Runtime;
using Word = Microsoft.Office.Interop.Word;
using Office = Microsoft.Office.Core;
using XWiki;
using XWiki.Clients;
using XWord;
using System.IO;
using System.Timers;
using System.Runtime.InteropServices;
using System.Collections.Generic;
using XWiki.Logging;
using UICommons;
using XWord2003.UIManagement;

namespace XWord2003
{
    /// <summary>
    /// XWord2003AddIn: XWiki integration with Microsoft Office Word 2003.
    /// </summary>
    public partial class XWord2003AddIn
    {
        #region Private members

        private string statusMessage = "";
        private MenuManager menuManager = MenuManager.Instance;
        private ToolbarManager toolbarManager = ToolbarManager.Instance;
        private AddinActionsDispatcher addinActionsDispatcher;

        private IXWikiClient client;
        private XWikiClientType clientType = XWikiClientType.XML_RPC;

        private string serverURL = "";
        private string username = "";
        private string password = "";

        private string currentLocalFilePath = "";
        private string currentPageFullName = "";

        private string pagesRepository;
        private string downloadedAttachmentsRepository;

        private Word.WdSaveFormat saveFormat;
        private AddinStatus addinStatus;
        private string defaultSyntax = "XWiki 2.0";

        System.Timers.Timer timer;
        const int TIMER_INTERVAL = 2000;

        /// <summary>
        /// A list with the pages that cannot be edited with Word.
        /// </summary>
        private List<String> protectedPages = new List<string>();

        /// <summary>
        /// A dictionary that contains a key value pair with the local file name of the document
        /// and the full name of the associated wiki page.
        /// </summary>
        Dictionary<String, String> editedPages = new Dictionary<string, string>();

        /// <summary>
        /// A dictionary that contains a key value pair with the local file name of the document
        /// and a boolean value indicating if it has a corresponding wiki page.
        /// </summary>
        Dictionary<string, bool> publishedStatus = new Dictionary<string, bool>();

        /// <summary>
        /// Specifies if the current page was published on the server.
        /// It does not specify if the last modifications were saved, but
        /// if the local document has a coresponding wiki page. It's FALSE
        /// until first saving to wiki.
        /// </summary>
        private bool currentPagePublished = false;

        private Word.Document lastActiveDocument;
        private Word.Range activeDocumentContent;
        private String lastActiveDocumentFullName;


        #endregion Private members

        #region Properties

        /// <summary>
        /// Gets or sets the status message shown in the status bar.
        /// </summary>
        public string StatusMessage
        {
            get { return statusMessage; }
            set { statusMessage = value; }
        }

        /// <summary>
        /// Gets or sets the addin action dispatcher.
        /// </summary>
        public AddinActionsDispatcher AddinActions
        {
            get { return addinActionsDispatcher; }
            set { addinActionsDispatcher = value; }
        }

        /// <summary>
        /// Gets or set the <code>IXWikiClient</code> used by the addin.
        /// </summary>
        public IXWikiClient Client
        {
            get { return client; }
            set
            {
                client = value;
                ClientInstanceChanged(this, null);
            }
        }

        /// <summary>
        /// Gets or sets the client type.
        /// </summary>
        public XWikiClientType ClientType
        {
            get { return clientType; }
            set { clientType = value; }
        }

        /// <summary>
        /// Gets or sets the server URL.
        /// </summary>
        public string ServerURL
        {
            get { return serverURL; }
            set { serverURL = value; }
        }

        /// <summary>
        /// Gets or sets the username.
        /// </summary>
        public string Username
        {
            get { return username; }
            set { username = value; }
        }

        /// <summary>
        /// Gets or sets the password.
        /// </summary>
        public string Password
        {
            get { return password; }
            set { password = value; }
        }

        /// <summary>
        /// Gets or sets the path to the active document.
        /// </summary>
        public string CurrentLocalFilePath
        {
            get { return currentLocalFilePath; }
            set { currentLocalFilePath = value; }
        }

        /// <summary>
        /// Gets or sets the full name (space name + "." + page name) of the current page.
        /// </summary>
        public string CurrentPageFullName
        {
            get { return currentPageFullName; }
            set { currentPageFullName = value; }
        }

        /// <summary>
        /// Gets or sets the pages repository.
        /// </summary>
        public string PagesRepository
        {
            get { return pagesRepository; }
            set { pagesRepository = value; }
        }

        /// <summary>
        /// Gets or sets the downloaded attachments repository.
        /// </summary>
        public string DownloadedAttachmentsRepository
        {
            get { return downloadedAttachmentsRepository; }
            set { downloadedAttachmentsRepository = value; }
        }

        /// <summary>
        /// Gets or sets the word save format.
        /// </summary>
        public Word.WdSaveFormat SaveFormat
        {
            get { return saveFormat; }
            set { saveFormat = value; }
        }

        /// <summary>
        /// Gets or sets the <code>AddinStatus</code>.
        /// </summary>
        public AddinStatus AddinStatus
        {
            get
            {
                if (addinStatus == null)
                {
                    addinStatus = new AddinStatus();
                }
                return addinStatus;
            }
            set { addinStatus = value; }
        }

        /// <summary>
        /// Gets or sets the default syntax.
        /// </summary>
        public string DefaultSyntax
        {
            get { return defaultSyntax; }
            set { defaultSyntax = value; }
        }

        /// <summary>
        /// Gets the currently edited pages.
        /// </summary>
        public Dictionary<String, String> EditedPages
        {
            get { return editedPages; }
        }

        /// <summary>
        /// Gets a dictionary that contains a key value pair with the local file name of the document
        /// and a boolean value indicating if it has a corresponding wiki page.
        /// </summary>
        public Dictionary<string, bool> PublishedStatus
        {
            get { return publishedStatus; }
        }

        /// <summary>
        /// Gets or sets the wildcards for the protected pages.
        /// The protected pages contain scripts and cannot be editited with Word.
        /// </summary>
        public List<String> ProtectedPages
        {
            get { return protectedPages; }
            set { protectedPages = value; }
        }

        /// <summary>
        /// Gets or sets the value of 'currentPagePublished', indicating if 
        /// current page has a corresponding wiki page.
        /// </summary>
        public bool CurrentPagePublished
        {
            get { return currentPagePublished; }
            set { currentPagePublished = value; }
        }

        /// <summary>
        /// Gets the last known instance for the active document.
        /// </summary>
        public Word.Document ActiveDocumentInstance
        {
            get
            {
                try
                {
                    if (this.Application.ActiveDocument != null && this.Application.ActiveDocument.FullName != null)
                    {
                        lastActiveDocument = this.Application.ActiveDocument;
                        lastActiveDocumentFullName = this.Application.ActiveDocument.FullName;
                        return this.Application.ActiveDocument;
                    }
                    else
                    {
                        //lastActiveDocument.Activate();
                        return lastActiveDocument;
                    }
                }
                catch (COMException)
                {
                    return lastActiveDocument;
                }
            }
        }

        /// <summary>
        /// Gets the full name of the active document.
        /// <remarks>
        /// It is recomended to use this property instead of VSTO's Application.ActiveDocument.FullName
        /// </remarks>
        /// </summary>
        public String ActiveDocumentFullName
        {
            get
            {
                try
                {
                    if (this.Application.ActiveDocument.FullName != null)
                    {
                        lastActiveDocumentFullName = Application.ActiveDocument.FullName;
                    }
                    return lastActiveDocumentFullName;
                }
                //Word deletes the FullName object;
                catch (COMException)
                {
                    return lastActiveDocumentFullName;
                }
            }
        }

        /// <summary>
        /// Gets a range of the active document's content.
        /// </summary>
        public Word.Range ActiveDocumentContentRange
        {
            get
            {
                try
                {
                    if (this.Application.ActiveDocument.Content != null)
                    {
                        activeDocumentContent = this.Application.ActiveDocument.Content;
                    }
                    return activeDocumentContent;
                }
                catch (COMException)
                {
                    return activeDocumentContent;
                }
            }
        }

        #endregion Properties

        #region Delegates&Events

        // A delegate type for hooking up Client instance change notifications.
        public delegate void ClientInstanceChangedHandler(object sender, EventArgs e);
        //Event triggered when the client instance is changed.
        public event ClientInstanceChangedHandler ClientInstanceChanged;

        /// <summary>
        /// Delegate for handling successful logins.
        /// </summary>
        public delegate void LoginSuccessfulHandler();
        /// <summary>
        /// Envent triggered when a successful login is made.
        /// </summary>
        public event LoginSuccessfulHandler LoginSuccessul;

        /// <summary>
        /// Delegate for handling failed logins
        /// </summary>
        public delegate void LoginFailedHandler();
        /// <summary>
        /// Event triggered when a failed login is made.
        /// </summary>
        public event LoginFailedHandler LoginFailed;


        #endregion Delegates&Events

        /// <summary>
        /// The wiki structure (Spaces,Pages,Attachment names) of the 
        /// wiki the user is connected to.
        /// </summary>
        public WikiStructure Wiki = null;

        /// <summary>
        /// A list of the web client's cookies.
        /// </summary>
        public static List<String> cookies = new List<string>();


        private void ThisAddIn_Startup(object sender, System.EventArgs e)
        {
            menuManager.CreateXWordMenu(this.Application);
            toolbarManager.CreateXWordToolbar(this.Application);
            InitializeAddin();
            this.Application.DocumentBeforeClose += new Microsoft.Office.Interop.Word.ApplicationEvents4_DocumentBeforeCloseEventHandler(Application_DocumentBeforeClose);
            this.Application.DocumentChange += new Microsoft.Office.Interop.Word.ApplicationEvents4_DocumentChangeEventHandler(Application_DocumentChange);
            Log.Information("XWord started.");
        }

        private void ThisAddIn_Shutdown(object sender, System.EventArgs e)
        {
            Log.Information("XWord closed");
        }

        /// <summary>
        /// Makes the login to the server, using the ConnectionSettingsForm
        /// or the last stored credentials.
        /// </summary>
        private void InitializeAddin()
        {
            //Set encoding to ISO-8859-1(Western)
            Application.Options.DefaultTextEncoding = Microsoft.Office.Core.MsoEncoding.msoEncodingWestern;
            this.SaveFormat = Word.WdSaveFormat.wdFormatHTML;
            this.AddinStatus.Syntax = this.DefaultSyntax;

            timer = new System.Timers.Timer(TIMER_INTERVAL);
            //Repositories and temporary files settings
            if (XOfficeCommonSettingsHandler.HasSettings())
            {
                XOfficeCommonSettings addinSettings = XOfficeCommonSettingsHandler.GetSettings();
                this.DownloadedAttachmentsRepository = addinSettings.DownloadedAttachmentsRepository;
                this.PagesRepository = addinSettings.PagesRepository;
                this.clientType = addinSettings.ClientType;
            }
            else
            {
                this.PagesRepository = Path.GetTempPath();
                this.DownloadedAttachmentsRepository = Path.GetTempPath();
                this.clientType = XWikiClientType.XML_RPC;
            }
            timer.Elapsed += new ElapsedEventHandler(timer_Elapsed);
            timer.Start();

            InitializeEventsHandlers();

            //Authentication settings
            if (!AutoLogin())
            {
                ShowConnectToServerUI();
            }
            addinActionsDispatcher = new AddinActionsDispatcher(this);
            Log.Success("XWord initialized!");
        }

        private void InitializeEventsHandlers()
        {
            this.ClientInstanceChanged += new ClientInstanceChangedHandler(XWikiAddIn_ClientInstanceChanged);
            this.LoginFailed += new LoginFailedHandler(XWikiAddIn_LoginFailed);
            this.LoginSuccessul += new LoginSuccessfulHandler(XWikiAddIn_LoginSuccessul);
        }

        void XWikiAddIn_LoginSuccessul()
        {
            statusMessage = "[XWord] Loading wiki structure ...";
            this.Application.StatusBar = statusMessage;

            this.Wiki = RequestWikiStructure();
            menuManager.EnableAllXWordMenus();
            toolbarManager.EnableAllXWordToolbarButtons();
            statusMessage = "[XWord] Logged in to " + serverURL;
            this.Application.StatusBar = statusMessage;
            Log.Success("Logged in  to " + serverURL);
        }

        void XWikiAddIn_LoginFailed()
        {
            String authMessage = "Login failed!" + Environment.NewLine;
            authMessage += "Unable to login, please check your username & password." + Environment.NewLine;
            authMessage += "Hint: make sure you are using correct letter case. Username and password are case sensitive.";
            UserNotifier.StopHand(authMessage);
            Log.Error("Login to server " + serverURL + " failed");
            menuManager.DisableXWordMenus();
            toolbarManager.DisableXWordToolbarButtons();
        }

        private void ShowConnectToServerUI()
        {
            if (!AddinSettingsForm.IsShown)
            {
                AddinSettingsForm addinSettingsForm = new AddinSettingsForm();
                new AddinSettingsFormManager(ref addinSettingsForm).EnqueueAllHandlers();
                addinSettingsForm.ShowDialog();
            }
        }

        void XWikiAddIn_ClientInstanceChanged(object sender, EventArgs e)
        {
            if (Client != null)
            {
                if (Client.LoggedIn)
                {
                    //notify successfull login
                    LoginSuccessul();
                }
                else
                {
                    //notify failed login
                    LoginFailed();
                }
            }
        }

        /// <summary>
        /// Executes when the specified amount of time has passed.
        /// </summary>
        /// <param name="e">The event parameters.</param>
        /// <param name="sender">The control that triggered the event.</param>
        void timer_Elapsed(object sender, ElapsedEventArgs e)
        {
            ReinforceApplicationOptions();
        }

        /// <summary>
        /// Sets the application(Word) options.
        /// </summary>
        public void ReinforceApplicationOptions()
        {
            try
            {
                //Using UnicodeLittleEndian as we read data from the disk using StreamReader
                //The .NET String has UTF16 littleEndian(Unicode) encoding.
                Application.Options.DefaultTextEncoding = Microsoft.Office.Core.MsoEncoding.msoEncodingUnicodeLittleEndian;
                Application.ActiveDocument.SaveEncoding = Microsoft.Office.Core.MsoEncoding.msoEncodingUnicodeLittleEndian;
            }
            //Is thrown because in some cases the VSTO runtime is stopped after the word instance is closed.
            catch (COMException) { };
        }

        /// <summary>
        /// Logins to the server by using the last used credentials.(If the user choosed to save them).
        /// </summary>
        /// <returns></returns>
        private bool AutoLogin()
        {
            LoginData loginData = new LoginData(LoginData.XWORD_LOGIN_DATA_FILENAME);
            bool canAutoLogin = loginData.CanAutoLogin();
            if (canAutoLogin)
            {
                String[] credentials = loginData.GetCredentials();
                serverURL = credentials[0];
                username = credentials[1];
                password = credentials[2];
                Client = XWikiClientFactory.CreateXWikiClient(ClientType, serverURL, username, password);
            }
            return canAutoLogin;
        }


        /// <summary>
        /// Requests the spaces and pages of the wiki from the server.
        /// </summary>
        /// <returns>A WikiStructure instance.</returns>
        private WikiStructure RequestWikiStructure()
        {
            WikiStructure wikiStructure = new WikiStructure();
            List<String> spacesNames = Client.GetSpacesNames();
            spacesNames.Sort();
            wikiStructure.AddSpaces(spacesNames);
            //TODO: Implement user option
            if (true)
            {
                foreach (String spaceName in spacesNames)
                {
                    List<String> pagesNames = Client.GetPagesNames(spaceName);
                    wikiStructure[spaceName].AddDocuments(pagesNames);
                }
            }
            //TODO: Add opt-in prefetch
            return wikiStructure;
        }


        void Application_DocumentChange()
        {
            //Reassign values to the document and wiki page states. 
            lastActiveDocument = ActiveDocumentInstance;
            lastActiveDocumentFullName = ActiveDocumentFullName;
            activeDocumentContent = ActiveDocumentContentRange;
            //if current document is a wiki page.
            if (EditedPages.ContainsKey(lastActiveDocumentFullName))
            {
                currentPageFullName = EditedPages[lastActiveDocumentFullName];
                CurrentPagePublished = false;
                if (PublishedStatus.ContainsKey(currentPageFullName))
                {
                    if (PublishedStatus[currentPageFullName])
                    {
                        CurrentPagePublished = true;
                    }
                }
            }
            else
            {
                currentPageFullName = null;
                CurrentPagePublished = false;
            }
        }

        void Application_DocumentBeforeClose(Microsoft.Office.Interop.Word.Document doc, ref bool cancel)
        {
            this.Application.StatusBar = statusMessage;
            string docFullName = doc.FullName;
            if (EditedPages.ContainsKey(docFullName))
            {
                doc.Saved = true;
                EditedPages.Remove(doc.FullName);
            }
        }



        #region VSTO generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InternalStartup()
        {
            this.Startup += new System.EventHandler(ThisAddIn_Startup);
            this.Shutdown += new System.EventHandler(ThisAddIn_Shutdown);
        }

        #endregion
    }
}
