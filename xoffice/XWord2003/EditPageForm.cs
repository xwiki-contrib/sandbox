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
    public partial class EditPageForm : Form
    {
        #region Properties

        /// <summary>
        /// Gets or sets the data source for spaces ListBox.
        /// </summary>
        public object ListBoxSpacesDataSource
        {
            get { return listBoxSpaces.DataSource; }
            set { listBoxSpaces.DataSource = value; }
        }

        /// <summary>
        /// Gets or sets the data source for pages ListBox.
        /// </summary>
        public object ListBoxPagesDataSource
        {
            get { return listBoxPages.DataSource; }
            set { listBoxPages.DataSource = value; }
        }

        /// <summary>
        /// Gets or sets the selected index for spaces ListBox.
        /// </summary>
        public int ListBoxSpacesSelectedIndex
        {
            get { return listBoxSpaces.SelectedIndex; }
            set { listBoxSpaces.SelectedIndex = value; }
        }

        /// <summary>
        /// Gets or sets the selected index for pages ListBox.
        /// </summary>
        public int ListBoxPagesSelectedIndex
        {
            get { return listBoxPages.SelectedIndex; }
            set { listBoxPages.SelectedIndex = value; }
        }

        /// <summary>
        /// Gets the selected value from spaces ListBox.
        /// </summary>
        public object ListBoxSpacesSelectedValue
        {
            get { return listBoxSpaces.SelectedValue; }
        }

        /// <summary>
        /// Gets the selected value from pages ListBox.
        /// </summary>
        public object ListBoxPagesSelectedValue
        {
            get { return listBoxPages.SelectedValue; }
        }

        #endregion Properties

        #region Public event handlers

        public EventHandler OnFormLoad;
        public EventHandler OnEditPage;
        public EventHandler OnViewAttachments;
        public EventHandler OnCancel;
        public EventHandler OnSpaceChange;

        #endregion Public event handlers

        public EditPageForm()
        {
            InitializeComponent();
            listBoxPages.SelectionMode = SelectionMode.One;
            listBoxSpaces.SelectionMode = SelectionMode.One;
        }

        private void EditPageForm_Load(object sender, EventArgs e)
        {
            this.OnFormLoad(sender, e);
        }

        private void btnEdit_Click(object sender, EventArgs e)
        {
            this.OnEditPage(sender, e);
        }

        private void btnCancel_Click(object sender, EventArgs e)
        {
            this.OnCancel(sender, e);
        }

        private void listBoxSpaces_SelectedIndexChanged(object sender, EventArgs e)
        {
            this.OnSpaceChange(sender, e);
        }

        private void listBoxSpaces_SelectedValueChanged(object sender, EventArgs e)
        {
            this.OnSpaceChange(sender, e);
        }

        private void btnAttachments_Click(object sender, EventArgs e)
        {
            this.OnViewAttachments(sender, e);
        }
    }
}
