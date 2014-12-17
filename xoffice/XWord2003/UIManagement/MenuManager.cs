using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Word = Microsoft.Office.Interop.Word;
using Office = Microsoft.Office.Core;
using UICommons;
using XWiki.Logging;
using System.Windows.Forms;
using Microsoft.Office.Core;


namespace XWord2003.UIManagement
{
    public sealed class MenuManager
    {
        #region Singleton

        private static MenuManager _instance = new MenuManager();

        /// <summary>
        /// Explicit static constructor to tell C# compiler
        /// not to mark type as beforefieldinit.
        /// </summary>
        static MenuManager()
        {
        }

        /// <summary>
        /// Private constructor. This is a singleton. Should not be instanciated or extended.
        /// </summary>
        private MenuManager()
        {
        }

        /// <summary>
        /// Gets the instance of <code>MenuManager</code>.
        /// </summary>
        public static MenuManager Instance
        {
            get { return _instance; }
        }

        #endregion Singleton

        #region Private members

        private object missing = (Object)Type.Missing;

        private Word.Application application;
        private Office.CommandBar officeMenuBar;

        private Office.CommandBarPopup xwordMenu;

        private Office.CommandBarButton menuItemNew;
        private Office.CommandBarButton menuItemOpenXWikiPage;
        private Office.CommandBarButton menuItemPublishXWikiPage;

        private Office.CommandBarButton menuItemViewAttachments;
        private Office.CommandBarButton menuItemAddAttachments;
        private Office.CommandBarButton menuItemViewInBrowser;

        private Office.CommandBarButton menuItemXWordSettings;
        private Office.CommandBarButton menuItemAboutXWord;

        #endregion Private members

        /// <summary>
        /// Creates the XWord menu.
        /// </summary>
        /// <param name="wordApplication">The <code>Word.Application</code>.</param>
        public void CreateXWordMenu(Word.Application wordApplication)
        {
            this.application = wordApplication;
            this.officeMenuBar = wordApplication.CommandBars.ActiveMenuBar;
            BuildMenus();
            EnqueueEventHandlers();
            DisableXWordMenus();
        }

        /// <summary>
        /// Enables all XWord menu items.
        /// </summary>
        public void EnableAllXWordMenus()
        {
            menuItemNew.Enabled = true;
            menuItemOpenXWikiPage.Enabled = true;
            menuItemPublishXWikiPage.Enabled = true;
            menuItemViewAttachments.Enabled = true;
            menuItemAddAttachments.Enabled = true;
            menuItemViewInBrowser.Enabled = true;
            menuItemXWordSettings.Enabled = true;
            menuItemAboutXWord.Enabled = true;
        }

        /// <summary>
        /// Disables XWord menu items, except 'XWord Settings' and 'About XWord'.
        /// </summary>
        public void DisableXWordMenus()
        {
            menuItemNew.Enabled = false;
            menuItemOpenXWikiPage.Enabled = false;
            menuItemPublishXWikiPage.Enabled = false;
            menuItemViewAttachments.Enabled = false;
            menuItemAddAttachments.Enabled = false;
            menuItemViewInBrowser.Enabled = false;
            menuItemXWordSettings.Enabled = true;
            menuItemAboutXWord.Enabled = true;
        }

        /// <summary>
        /// Builds the <code>CommandBarPopup</code> and <code>CommandBarButton</code> elements
        /// from the XWord menu.
        /// </summary>
        private void BuildMenus()
        {
            bool menuExists = FindXWordMenus();
            if (menuExists)
            {
                xwordMenu.Delete(false);
            }

            if (officeMenuBar != null)
            {
                xwordMenu = (Office.CommandBarPopup)officeMenuBar.Controls.Add(
                    Office.MsoControlType.msoControlPopup, missing, missing, missing, true);
                xwordMenu.Caption = "&XWord";
                xwordMenu.Tag = "XWord2003Menu";
                xwordMenu.Enabled = true;

                menuItemNew = (Office.CommandBarButton)xwordMenu.Controls.Add(
                    Office.MsoControlType.msoControlButton, missing, missing, missing, true);
                menuItemNew.Caption = "&New...";
                menuItemNew.Tag = "MenuItemNewXWikiPage";
                menuItemNew.Enabled = true;
                menuItemNew.FaceId = 18;

                menuItemOpenXWikiPage = (Office.CommandBarButton)xwordMenu.Controls.Add(
                    Office.MsoControlType.msoControlButton, missing, missing, missing, true);
                menuItemOpenXWikiPage.Caption = "&Open XWiki Page";
                menuItemOpenXWikiPage.Tag = "MenuItemOpenXWikiPage";
                menuItemOpenXWikiPage.Enabled = false;
                menuItemOpenXWikiPage.FaceId = 23;

                menuItemPublishXWikiPage = (Office.CommandBarButton)xwordMenu.Controls.Add(
                    Office.MsoControlType.msoControlButton, missing, missing, missing, true);
                menuItemPublishXWikiPage.Caption = "&Publish XWiki Page";
                menuItemPublishXWikiPage.Tag = "MenuItemPublishXWikiPage";
                menuItemPublishXWikiPage.Enabled = false;
                menuItemPublishXWikiPage.FaceId = 3823;

                menuItemViewInBrowser = (Office.CommandBarButton)xwordMenu.Controls.Add(
                    Office.MsoControlType.msoControlButton, missing, missing, missing, true);
                menuItemViewInBrowser.Caption = "View in &Browser";
                menuItemViewInBrowser.Tag = "MenuItemViewInBrowser";
                menuItemViewInBrowser.Enabled = false;

                menuItemViewAttachments = (Office.CommandBarButton)xwordMenu.Controls.Add(
                    Office.MsoControlType.msoControlButton, missing, missing, missing, true);
                menuItemViewAttachments.Caption = "&View Attachments";
                menuItemViewAttachments.Tag = "MenuItemViewAttachments";
                menuItemViewAttachments.Enabled = false;
                menuItemViewAttachments.BeginGroup = true;

                menuItemAddAttachments = (Office.CommandBarButton)xwordMenu.Controls.Add(
                    Office.MsoControlType.msoControlButton, missing, missing, missing, true);
                menuItemAddAttachments.Caption = "Add A&ttachments";
                menuItemAddAttachments.Tag = "MenuItemAddAttachments";
                menuItemAddAttachments.Enabled = false;


                menuItemXWordSettings = (Office.CommandBarButton)xwordMenu.Controls.Add(
                    Office.MsoControlType.msoControlButton, missing, missing, missing, true);
                menuItemXWordSettings.Caption = "XWord &Settings";
                menuItemXWordSettings.Tag = "MenuItemXWordSettings";
                menuItemXWordSettings.Enabled = true;
                menuItemXWordSettings.BeginGroup = true;

                menuItemAboutXWord = (Office.CommandBarButton)xwordMenu.Controls.Add(
                    Office.MsoControlType.msoControlButton, missing, missing, missing, true);
                menuItemAboutXWord.Caption = "&About XWord";
                menuItemAboutXWord.Tag = "MenuItemAboutXWord";
                menuItemAboutXWord.Enabled = true;
                menuItemAboutXWord.BeginGroup = true;

            }
        }

        /// <summary>
        /// Searches for already created XWord menu.
        /// </summary>
        /// <returns>TRUE if XWord menu is found.</returns>
        private bool FindXWordMenus()
        {
            bool found = false;
            foreach (CommandBarControl control in officeMenuBar.Controls)
            {
                if (control.Type == MsoControlType.msoControlPopup)
                {
                    CommandBarPopup popup = (CommandBarPopup)control;
                    if (popup.Caption.ToUpper().IndexOf("XWORD") >= 0)
                    {
                        xwordMenu = popup;
                        found = true;
                        break;
                    }
                }
            }
            return found;
        }


        /// <summary>
        /// Enqueues event handlers for XWord menu items.
        /// </summary>
        private void EnqueueEventHandlers()
        {
            menuItemNew.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(menuItemNew_Click);
            menuItemOpenXWikiPage.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(menuItemOpenXWikiPage_Click);
            menuItemXWordSettings.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(menuItemXWordSettings_Click);
            menuItemPublishXWikiPage.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(menuItemPublishXWikiPage_Click);
            menuItemViewAttachments.Click += new _CommandBarButtonEvents_ClickEventHandler(menuItemViewAttachments_Click);
            menuItemAddAttachments.Click += new _CommandBarButtonEvents_ClickEventHandler(menuItemAddAttachments_Click);
            menuItemViewInBrowser.Click += new _CommandBarButtonEvents_ClickEventHandler(menuItemViewInBrowser_Click);
            menuItemAboutXWord.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(menuItemAboutXWord_Click);
        }

        void menuItemAddAttachments_Click(CommandBarButton Ctrl, ref bool CancelDefault)
        {
            UIActions.ActionAddAttachments();
        }

        void menuItemViewInBrowser_Click(CommandBarButton Ctrl, ref bool CancelDefault)
        {
            UIActions.ActionViewInBrowser();
        }

        void menuItemViewAttachments_Click(CommandBarButton Ctrl, ref bool CancelDefault)
        {
            UIActions.ActionViewAttachments();
        }

        void menuItemNew_Click(Microsoft.Office.Core.CommandBarButton Ctrl, ref bool CancelDefault)
        {
            UIActions.ActionNewSpaceOrPage();
        }

        void menuItemOpenXWikiPage_Click(Microsoft.Office.Core.CommandBarButton Ctrl, ref bool CancelDefault)
        {
            UIActions.ActionOpenWikiPage();
        }


        void menuItemPublishXWikiPage_Click(Microsoft.Office.Core.CommandBarButton Ctrl, ref bool CancelDefault)
        {
            UIActions.ActionPublishCurrentPage();
        }

        void menuItemAboutXWord_Click(Microsoft.Office.Core.CommandBarButton Ctrl, ref bool CancelDefault)
        {
            UIActions.ActionAboutXWord();
        }

        void menuItemXWordSettings_Click(Microsoft.Office.Core.CommandBarButton Ctrl, ref bool CancelDefault)
        {
            UIActions.ActionXWordSettings();
        }



    }
}
