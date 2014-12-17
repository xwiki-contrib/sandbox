namespace XWord2003
{
    partial class EditPageForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.lblSpace = new System.Windows.Forms.Label();
            this.lblPage = new System.Windows.Forms.Label();
            this.listBoxSpaces = new System.Windows.Forms.ListBox();
            this.listBoxPages = new System.Windows.Forms.ListBox();
            this.btnEdit = new System.Windows.Forms.Button();
            this.btnCancel = new System.Windows.Forms.Button();
            this.btnAttachments = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // lblSpace
            // 
            this.lblSpace.AutoSize = true;
            this.lblSpace.Location = new System.Drawing.Point(12, 12);
            this.lblSpace.Name = "lblSpace";
            this.lblSpace.Size = new System.Drawing.Size(38, 13);
            this.lblSpace.TabIndex = 0;
            this.lblSpace.Text = "Space";
            // 
            // lblPage
            // 
            this.lblPage.AutoSize = true;
            this.lblPage.Location = new System.Drawing.Point(225, 12);
            this.lblPage.Name = "lblPage";
            this.lblPage.Size = new System.Drawing.Size(32, 13);
            this.lblPage.TabIndex = 1;
            this.lblPage.Text = "Page";
            // 
            // listBoxSpaces
            // 
            this.listBoxSpaces.FormattingEnabled = true;
            this.listBoxSpaces.Location = new System.Drawing.Point(12, 38);
            this.listBoxSpaces.Name = "listBoxSpaces";
            this.listBoxSpaces.Size = new System.Drawing.Size(210, 212);
            this.listBoxSpaces.TabIndex = 2;
            this.listBoxSpaces.SelectedIndexChanged += new System.EventHandler(this.listBoxSpaces_SelectedIndexChanged);
            this.listBoxSpaces.SelectedValueChanged += new System.EventHandler(this.listBoxSpaces_SelectedValueChanged);
            // 
            // listBoxPages
            // 
            this.listBoxPages.FormattingEnabled = true;
            this.listBoxPages.Location = new System.Drawing.Point(228, 38);
            this.listBoxPages.Name = "listBoxPages";
            this.listBoxPages.Size = new System.Drawing.Size(210, 212);
            this.listBoxPages.TabIndex = 3;
            // 
            // btnEdit
            // 
            this.btnEdit.Location = new System.Drawing.Point(282, 261);
            this.btnEdit.Name = "btnEdit";
            this.btnEdit.Size = new System.Drawing.Size(75, 23);
            this.btnEdit.TabIndex = 4;
            this.btnEdit.Text = "Edit";
            this.btnEdit.UseVisualStyleBackColor = true;
            this.btnEdit.Click += new System.EventHandler(this.btnEdit_Click);
            // 
            // btnCancel
            // 
            this.btnCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.btnCancel.Location = new System.Drawing.Point(363, 261);
            this.btnCancel.Name = "btnCancel";
            this.btnCancel.Size = new System.Drawing.Size(75, 23);
            this.btnCancel.TabIndex = 5;
            this.btnCancel.Text = "Cancel";
            this.btnCancel.UseVisualStyleBackColor = true;
            this.btnCancel.Click += new System.EventHandler(this.btnCancel_Click);
            // 
            // btnAttachments
            // 
            this.btnAttachments.Location = new System.Drawing.Point(166, 261);
            this.btnAttachments.Name = "btnAttachments";
            this.btnAttachments.Size = new System.Drawing.Size(110, 23);
            this.btnAttachments.TabIndex = 6;
            this.btnAttachments.Text = "View Attachments";
            this.btnAttachments.UseVisualStyleBackColor = true;
            this.btnAttachments.Click += new System.EventHandler(this.btnAttachments_Click);
            // 
            // EditPageForm
            // 
            this.AcceptButton = this.btnEdit;
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.CancelButton = this.btnCancel;
            this.ClientSize = new System.Drawing.Size(450, 296);
            this.Controls.Add(this.btnAttachments);
            this.Controls.Add(this.btnCancel);
            this.Controls.Add(this.btnEdit);
            this.Controls.Add(this.listBoxPages);
            this.Controls.Add(this.listBoxSpaces);
            this.Controls.Add(this.lblPage);
            this.Controls.Add(this.lblSpace);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "EditPageForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "XWord - Edit page";
            this.Load += new System.EventHandler(this.EditPageForm_Load);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label lblSpace;
        private System.Windows.Forms.Label lblPage;
        private System.Windows.Forms.ListBox listBoxSpaces;
        private System.Windows.Forms.ListBox listBoxPages;
        private System.Windows.Forms.Button btnEdit;
        private System.Windows.Forms.Button btnCancel;
        private System.Windows.Forms.Button btnAttachments;
    }
}