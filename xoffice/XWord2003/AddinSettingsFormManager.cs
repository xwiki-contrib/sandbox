using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UICommons;
using UICommons.UIActionsManagement;
using XWord;
using XWiki;
using System.Windows.Forms;
using XWiki.Logging;
using XWiki.Clients;
using System.IO;
using System.Threading;


namespace XWord2003
{
    /// <summary>
    /// Manages the public events handlers for <code>AddinSettingsForm</code> instances.
    /// </summary>
    public class AddinSettingsFormManager : AbstractAddinSettingsFormActionsManager
    {
        private AddinSettingsForm addinSettingsForm;
        private XOfficeCommonSettings addinSettings;
        private XWord2003AddIn addin;
        private XWikiClientType currentClientType;

        /// <summary>
        /// Default constructor.
        /// </summary>
        /// <param name="addinSettingsForm">A reference to an <code>AddinSettingsForm</code> instance.</param>
        public AddinSettingsFormManager(ref AddinSettingsForm addinSettingsForm)
        {
            this.addinSettingsForm = addinSettingsForm;
            this.addinSettings = new XOfficeCommonSettings();
            this.addin = Globals.XWord2003AddIn;
            this.currentClientType = addin.ClientType;
        }

        /// <summary>
        /// Actions to be performed when the user applies addin settings (ex: presses the "Apply" button)
        /// </summary>
        /// <param name="sender">Sender object.</param>
        /// <param name="e">Event arguments</param>
        protected override void ActionApply(object sender, EventArgs e)
        {
            if (addinSettingsForm.IsTabConnectionSelected)
            {
                ApplyConnectionSettings();
                addinSettingsForm.ConnectionSettingsApplied = true;
            }
            else if (addinSettingsForm.IsTabFileRepositorySelected)
            {
                ApplyRepositoriesSettings();
                addinSettingsForm.AddinSettingsApplied = true;
            }
        }

        /// <summary>
        /// Action to be performed when the user cancels the addin settings(ex: presses the "Cancel" button,
        /// presses the ESC key, etc).
        /// </summary>
        /// <param name="sender">Sender object.</param>
        /// <param name="e">Event arguments.</param>
        protected override void ActionCancel(object sender, EventArgs e)
        {
            //rollback to initial value of addin client type
            addin.ClientType = currentClientType;
        }

        /// <summary>
        /// Actions to be performed when on form load.
        /// </summary>
        /// <param name="sender">Sender object.</param>
        /// <param name="e">Event arguments</param>
        protected override void ActionFormLoad(object sender, EventArgs e)
        {
            addinSettingsForm.LoadingDialogFlag = true;

            if (addin.ServerURL == "" || addin.ServerURL == null)
            {
                addinSettingsForm.GroupBox1Text = "Test server settings";
            }
            else
            {
                addinSettingsForm.GroupBox1Text = "Settings";
                addinSettingsForm.ServerURL = addin.ServerURL;
                addinSettingsForm.UserName = addin.Username;
                addinSettingsForm.Password = addin.Password;
            }
            addinSettings = new XOfficeCommonSettings();
            addinSettings.PagesRepository = addin.PagesRepository;
            addinSettings.DownloadedAttachmentsRepository = addin.DownloadedAttachmentsRepository;
            addinSettings.ClientType = addin.ClientType;
            addinSettingsForm.TxtPagesRepoText = addin.PagesRepository;
            addinSettingsForm.TxtAttachmentsRepoText = addin.DownloadedAttachmentsRepository;

            //init protocol settings
            addinSettingsForm.ConnectDictionary.Add(addinSettingsForm.ConnectMethods[0], XWikiClientType.XML_RPC);
            addinSettingsForm.ConnectDictionary.Add(addinSettingsForm.ConnectMethods[1], XWikiClientType.HTTP_Client);
            addinSettingsForm.ComboProtocolDataSource = addinSettingsForm.ConnectMethods;
            switch (addin.ClientType)
            {
                case XWikiClientType.XML_RPC:
                    addinSettingsForm.ComboProtocolSelectedIndex = 0;
                    break;
                case XWikiClientType.HTTP_Client:
                    addinSettingsForm.ComboProtocolSelectedIndex = 1;
                    break;
            }
            addinSettingsForm.LoadingDialogFlag = false;
        }

        /// <summary>
        /// Action to be performed when the user presses OK button.
        /// </summary>
        /// <param name="sender">Sender object.</param>
        /// <param name="e">Event arguments.</param>
        protected override void ActionOK(object sender, EventArgs e)
        {
            //If settings changes were made or there is no client instance
            if (!addinSettingsForm.ConnectionSettingsApplied || (addin.Client == null))
            {
                ApplyConnectionSettings();
            }
            if (!addinSettingsForm.AddinSettingsApplied)
            {
                ApplyRepositoriesSettings();
            }
            addinSettingsForm.DialogResult = DialogResult.OK;
            addinSettingsForm.Close();
        }

        protected override void ActionProtocolChanged(object sender, EventArgs e)
        {
            //If user generated event
            if (!addinSettingsForm.LoadingDialogFlag)
            {
                String selectedValue = (String)addinSettingsForm.ComboProtocolSelectedValue;
                if (addinSettingsForm.ConnectDictionary.Keys.Contains(selectedValue))
                {
                    addinSettings.ClientType = addinSettingsForm.ConnectDictionary[selectedValue];
                    addin.ClientType = addinSettings.ClientType;
                    addinSettingsForm.ConnectionSettingsApplied = false;
                }
                else
                {
                    UserNotifier.StopHand("The selected value is not valid.");
                }
            }
        }

        /// <summary>
        /// Enqueues all known event handler for <code>AddinSettingsForm</code>.
        /// </summary>
        public override void EnqueueAllHandlers()
        {
            addinSettingsForm.OnApply = this.ActionApply;
            addinSettingsForm.OnCancel = this.ActionCancel;
            addinSettingsForm.OnFormLoad = this.ActionFormLoad;
            addinSettingsForm.OnOK = this.ActionOK;
            addinSettingsForm.OnProtocolChange = this.ActionProtocolChanged;
        }

        /// <summary>
        /// Sets the new connection settings for the addin,
        /// connects to the server and refreshes the active wiki explorer.
        /// </summary>
        private void ApplyConnectionSettings()
        {
            Cursor c = addinSettingsForm.Cursor;
            addinSettingsForm.Cursor = Cursors.WaitCursor;
            if (addinSettingsForm.ServerURL.EndsWith("/"))
            {
                addinSettingsForm.ServerURL = addinSettingsForm.ServerURL.Substring(0, addinSettingsForm.ServerURL.Length - 1);
            }
            addin.ServerURL = addinSettingsForm.ServerURL;
            addin.Username = addinSettingsForm.UserName;
            addin.Password = addinSettingsForm.Password;
            LoginData loginData = new LoginData(LoginData.XWORD_LOGIN_DATA_FILENAME);
            addin.Client = XWikiClientFactory.CreateXWikiClient(addin.ClientType,
                addin.ServerURL, addin.Username, addin.Password);

            if (addinSettingsForm.IsCkRememberMeChecked)
            {
                String[] credentials = new String[3];
                credentials[0] = Globals.XWord2003AddIn.ServerURL;
                credentials[1] = Globals.XWord2003AddIn.Username;
                credentials[2] = Globals.XWord2003AddIn.Password;
                loginData.WriteCredentials(credentials);
            }
            else
            {
                loginData.ClearCredentials();
            }
            //Write the settings to isolated storage. 
            XOfficeCommonSettingsHandler.WriteRepositorySettings(addinSettings);

            addinSettingsForm.Cursor = c;
        }


        /// <summary>
        /// Sets the pages and attachments repositories.
        /// </summary>
        private void ApplyRepositoriesSettings()
        {
            Cursor c = addinSettingsForm.Cursor;
            addinSettingsForm.Cursor = Cursors.WaitCursor;
            if (addinSettingsForm.ValidatePath(addinSettingsForm.TxtPagesRepoText))
            {
                addin.PagesRepository = addinSettingsForm.TxtPagesRepoText;
            }
            else
            {
                addin.PagesRepository = Path.GetTempPath();
            }
            if (addinSettingsForm.ValidatePath(addinSettingsForm.TxtAttachmentsRepoText))
            {
                addin.DownloadedAttachmentsRepository = addinSettingsForm.TxtAttachmentsRepoText;
            }
            else
            {
                addin.DownloadedAttachmentsRepository = Path.GetTempPath();
            }
            XOfficeCommonSettingsHandler.WriteRepositorySettings(addinSettings);
            addinSettingsForm.AddinSettingsApplied = true;
            Thread.Sleep(500);
            addinSettingsForm.Cursor = c;
        }

    }
}
