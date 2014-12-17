using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Word = Microsoft.Office.Interop.Word;
using Office = Microsoft.Office.Core;
using System.Windows.Forms;

namespace XWord2003.UIManagement
{
    public sealed class ToolbarManager
    {
        #region Singleton

        private static ToolbarManager _instance = new ToolbarManager();

        /// <summary>
        /// Explicit static constructor to tell C# compiler
        /// not to mark type as beforefieldinit.
        /// </summary>
        static ToolbarManager()
        {
        }

        /// <summary>
        /// Private constructor. <code>ToolbarManager</code> is a singleton.
        /// </summary>
        private ToolbarManager()
        {
        }

        /// <summary>
        /// Gets the instance of <code>ToolbarManager</code>.
        /// </summary>
        public static ToolbarManager Instance
        {
            get { return _instance; }
        }
        #endregion Singleton

        #region Private members

        private object missing = (Object)Type.Missing;

        private Word.Application application;

        private Office.CommandBar xWordToolbar;
        private Office.CommandBarButton btnNew;
        private Office.CommandBarButton btnOpen;
        private Office.CommandBarButton btnPublish;
        private Office.CommandBarButton btnViewInBrowser;

        private Office.CommandBarButton btnViewAttachments;
        private Office.CommandBarButton btnAddAttachment;

        private Office.CommandBarButton btnXWordSettings;

        private Office.CommandBarButton btnAboutXWord;

        #endregion Private members

        /// <summary>
        /// Creates the XWord toolbar.
        /// </summary>
        /// <param name="wordApplication">The <code>Word.Application</code> instance.</param>
        public void CreateXWordToolbar(Word.Application wordApplication)
        {
            this.application = wordApplication;
            BuildToolbar();
        }

        /// <summary>
        /// Enables all buttons from the XWord toolbar.
        /// </summary>
        public void EnableAllXWordToolbarButtons()
        {
            if (xWordToolbar != null && btnNew != null)
            {
                btnNew.Enabled = true;
                btnOpen.Enabled = true;
                btnPublish.Enabled = true;
                btnViewInBrowser.Enabled = true;
                btnViewAttachments.Enabled = true;
                btnAddAttachment.Enabled = true;
                btnXWordSettings.Enabled = true;
                btnAboutXWord.Enabled = true;
            }
        }

        /// <summary>
        /// Disables XWord toolbar buttons, except 'XWord Settings' and 'About XWord'.
        /// </summary>
        public void DisableXWordToolbarButtons()
        {
            if (xWordToolbar != null && btnNew != null)
            {
                btnNew.Enabled = false;
                btnOpen.Enabled = false;
                btnPublish.Enabled = false;
                btnViewInBrowser.Enabled = false;
                btnViewAttachments.Enabled = false;
                btnAddAttachment.Enabled = false;
                btnXWordSettings.Enabled = true;
                btnAboutXWord.Enabled = true;
            }
        }

        /// <summary>
        /// Creates the tooblar, adds the buttons and enqueues click events handlers.
        /// </summary>
        private void BuildToolbar()
        {
            bool toolbarExists = FindXWordToolbar();
            if (toolbarExists)
            {
                xWordToolbar.Delete();
            }

            try
            {
                xWordToolbar = application.CommandBars.Add(
                     "XWord2003 ToolBar",
                     Office.MsoBarPosition.msoBarTop,
                     false,
                     true);
                xWordToolbar.Enabled = true;
                xWordToolbar.Visible = false;
                xWordToolbar.Protection = Office.MsoBarProtection.msoBarNoCustomize;
                xWordToolbar.Name = "XWord2003 ToolBar";

                if (xWordToolbar != null)
                {
                    btnNew = (Office.CommandBarButton)xWordToolbar.Controls.Add(
                           Office.MsoControlType.msoControlButton,
                           missing,
                           missing,
                           missing,
                           true);
                    btnNew.Caption = "New...";
                    btnNew.FaceId = 1;
                    btnNew.Enabled = false;
                    btnNew.Visible = true;
                    btnNew.Style = Office.MsoButtonStyle.msoButtonIconAndCaption;
                    btnNew.Tag = "XWord2003ToolbarBtnNew";
                    btnNew.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(btnNew_Click);

                    btnOpen = (Office.CommandBarButton)xWordToolbar.Controls.Add(
                           Office.MsoControlType.msoControlButton,
                           missing,
                           missing,
                           missing,
                           true);
                    btnOpen.Caption = "Open XWiki page";
                    btnOpen.FaceId = 1;
                    btnOpen.Enabled = false;
                    btnOpen.Visible = true;
                    btnOpen.Style = Office.MsoButtonStyle.msoButtonIconAndCaption;
                    btnOpen.Tag = "XWord2003ToolbarBtnOpen";
                    btnOpen.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(btnOpen_Click);

                    btnPublish = (Office.CommandBarButton)xWordToolbar.Controls.Add(
                           Office.MsoControlType.msoControlButton,
                           missing,
                           missing,
                           missing,
                           true);
                    btnPublish.Caption = "Publish XWiki page";
                    btnPublish.FaceId = 1;
                    btnPublish.Enabled = false;
                    btnPublish.Visible = true;
                    btnPublish.Style = Office.MsoButtonStyle.msoButtonIconAndCaption;
                    btnPublish.Tag = "XWord2003ToolbarBtnPublish";
                    btnPublish.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(btnPublish_Click);

                    btnViewInBrowser = (Office.CommandBarButton)xWordToolbar.Controls.Add(
                           Office.MsoControlType.msoControlButton,
                           missing,
                           missing,
                           missing,
                           true);
                    btnViewInBrowser.Caption = "View in Browser";
                    btnViewInBrowser.FaceId = 1;
                    btnViewInBrowser.Enabled = false;
                    btnViewInBrowser.Visible = true;
                    btnViewInBrowser.Style = Office.MsoButtonStyle.msoButtonIconAndCaption;
                    btnViewInBrowser.Tag = "XWord2003ToolbarBtnViewInBrowser";
                    btnViewInBrowser.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(btnViewInBrowser_Click);

                    btnViewAttachments = (Office.CommandBarButton)xWordToolbar.Controls.Add(
                           Office.MsoControlType.msoControlButton,
                           missing,
                           missing,
                           missing,
                           true);
                    btnViewAttachments.Caption = "View Attachments";
                    btnViewAttachments.FaceId = 1;
                    btnViewAttachments.Enabled = false;
                    btnViewAttachments.Visible = true;
                    btnViewAttachments.Style = Office.MsoButtonStyle.msoButtonIconAndCaption;
                    btnViewAttachments.Tag = "XWord2003ToolbarBtnViewAttachments";
                    btnViewAttachments.BeginGroup = true;
                    btnViewAttachments.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(btnViewAttachments_Click);

                    btnAddAttachment = (Office.CommandBarButton)xWordToolbar.Controls.Add(
                           Office.MsoControlType.msoControlButton,
                           missing,
                           missing,
                           missing,
                           true);
                    btnAddAttachment.Caption = "Add Attachment";
                    btnAddAttachment.FaceId = 1;
                    btnAddAttachment.Enabled = false;
                    btnAddAttachment.Visible = true;
                    btnAddAttachment.Style = Office.MsoButtonStyle.msoButtonIconAndCaption;
                    btnAddAttachment.Tag = "XWord2003ToolbarBtnAddAttachment";
                    btnAddAttachment.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(btnAddAttachment_Click);

                    btnXWordSettings = (Office.CommandBarButton)xWordToolbar.Controls.Add(
                           Office.MsoControlType.msoControlButton,
                           missing,
                           missing,
                           missing,
                           true);
                    btnXWordSettings.Caption = "XWord Settings";
                    btnXWordSettings.FaceId = 1;
                    btnXWordSettings.Enabled = true;
                    btnXWordSettings.Visible = true;
                    btnXWordSettings.Style = Office.MsoButtonStyle.msoButtonIconAndCaption;
                    btnXWordSettings.Tag = "XWord2003ToolbarBtnXWordSettings";
                    btnXWordSettings.BeginGroup = true;
                    btnXWordSettings.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(btnXWordSettings_Click);

                    btnAboutXWord = (Office.CommandBarButton)xWordToolbar.Controls.Add(
                           Office.MsoControlType.msoControlButton,
                           missing,
                           missing,
                           missing,
                           true);
                    btnAboutXWord.Caption = "About XWord";
                    btnAboutXWord.FaceId = 1;
                    btnAboutXWord.Enabled = true;
                    btnAboutXWord.Visible = true;
                    btnAboutXWord.Style = Office.MsoButtonStyle.msoButtonIconAndCaption;
                    btnAboutXWord.Tag = "XWord2003ToolbarBtnAbout";
                    btnAboutXWord.BeginGroup = true;
                    btnAboutXWord.Click += new Microsoft.Office.Core._CommandBarButtonEvents_ClickEventHandler(btnAboutXWord_Click);

                    xWordToolbar.Visible = true;
                }
            }
            catch { }
        }

        /// <summary>
        /// Searches for existing XWord toolbar.
        /// </summary>
        /// <returns>TRUE if XWord toolbar was found.</returns>
        private bool FindXWordToolbar()
        {
            bool found = false;
            foreach (Office.CommandBar cmdBar in application.CommandBars)
            {
                if (cmdBar.Type != Office.MsoBarType.msoBarTypeMenuBar)
                {
                    if (cmdBar.Name == "XWord2003 ToolBar")
                    {
                        xWordToolbar = cmdBar;
                        found = true;
                        break;
                    }
                }
            }
            return found;
        }


        void btnAboutXWord_Click(Microsoft.Office.Core.CommandBarButton Ctrl, ref bool CancelDefault)
        {
            UIActions.ActionAboutXWord();
        }

        void btnXWordSettings_Click(Microsoft.Office.Core.CommandBarButton Ctrl, ref bool CancelDefault)
        {
            UIActions.ActionXWordSettings();
        }

        void btnAddAttachment_Click(Microsoft.Office.Core.CommandBarButton Ctrl, ref bool CancelDefault)
        {
            UIActions.ActionAddAttachments();
        }

        void btnViewAttachments_Click(Microsoft.Office.Core.CommandBarButton Ctrl, ref bool CancelDefault)
        {
            UIActions.ActionViewAttachments();
        }

        void btnViewInBrowser_Click(Microsoft.Office.Core.CommandBarButton Ctrl, ref bool CancelDefault)
        {
            UIActions.ActionViewInBrowser();
        }

        void btnPublish_Click(Microsoft.Office.Core.CommandBarButton Ctrl, ref bool CancelDefault)
        {
            UIActions.ActionPublishCurrentPage();
        }

        void btnOpen_Click(Microsoft.Office.Core.CommandBarButton Ctrl, ref bool CancelDefault)
        {
            UIActions.ActionOpenWikiPage();
        }

        void btnNew_Click(Microsoft.Office.Core.CommandBarButton Ctrl, ref bool CancelDefault)
        {
            UIActions.ActionNewSpaceOrPage();
        }
    }

}
