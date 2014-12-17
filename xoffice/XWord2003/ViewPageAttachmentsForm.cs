using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace XWord2003
{
    public partial class ViewPageAttachmentsForm : Form
    {
        private string spaceName;
        private string pageName;
        private string pageFullName;

        #region Properties

        /// <summary>
        /// Gets or sets the space name of the page.
        /// </summary>
        public string SpaceName
        {
            get { return spaceName; }
            set { spaceName = value; }
        }

        /// <summary>
        /// Gets ors sets the page name.
        /// </summary>
        public string PageName
        {
            get { return pageName; }
            set { pageName = value; }
        }

        /// <summary>
        /// Gets or sets the full name of the page.
        /// </summary>
        public string PageFullName
        {
            get { return pageFullName; }
            set
            {
                pageFullName = value;
                char[] separator = { '.' };
                string[] split = pageFullName.Split(separator, StringSplitOptions.RemoveEmptyEntries);
                spaceName = split[0];
                pageName = split[1];
            }
        }

        /// <summary>
        /// Gets or sets the data source for attachments ListBox.
        /// </summary>
        public object ListBoxAttachmentsDataSource
        {
            get { return listBoxAttachments.DataSource; }
            set { listBoxAttachments.DataSource = value; }
        }

        /// <summary>
        /// Gets or sets the zero-based index of the currently selected item from the attachments ListBox.
        /// </summary>
        public int ListBoxAttachmentsIndex
        {
            get { return listBoxAttachments.SelectedIndex; }
            set { listBoxAttachments.SelectedIndex = value; }
        }

        /// <summary>
        /// Gets or sets the selected value from the attachments ListBox.
        /// </summary>
        public object ListBoxAttachmentsValue
        {
            get { return listBoxAttachments.SelectedValue; }
            set { listBoxAttachments.SelectedValue = value; }
        }

        /// <summary>
        /// Gets or sets the text for the page name Label.
        /// </summary>
        public string LabelPageNameText
        {
            get { return lblPageName.Text; }
            set { lblPageName.Text = value; }
        }

        #endregion Properties

        #region Public event handlers

        public EventHandler OnFormLoad;
        public EventHandler OnAttachmentChange;
        public EventHandler OnOpenAttachment;
        public EventHandler OnCancel;

        #endregion Public event handlers


        public ViewPageAttachmentsForm(string pageFullName)
        {
            this.PageFullName = pageFullName;
            InitializeComponent();
            listBoxAttachments.SelectionMode = SelectionMode.One;
        }

        private void ViewPageAttachmentsForm_Load(object sender, EventArgs e)
        {
            this.OnFormLoad(sender, e);
        }

        private void listBoxAttachments_SelectedIndexChanged(object sender, EventArgs e)
        {
            this.OnAttachmentChange(sender, e);
        }

        private void listBoxAttachments_SelectedValueChanged(object sender, EventArgs e)
        {
            this.OnAttachmentChange(sender, e);
        }

        private void btnOpenAttachment_Click(object sender, EventArgs e)
        {
            this.OnOpenAttachment(sender, e);
        }

        private void btnCancel_Click(object sender, EventArgs e)
        {
            this.OnCancel(sender, e);
        }
    }
}
