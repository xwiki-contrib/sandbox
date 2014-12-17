namespace XWord2003
{
    partial class ViewPageAttachmentsForm
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
            this.btnOpenAttachment = new System.Windows.Forms.Button();
            this.btnCancel = new System.Windows.Forms.Button();
            this.lblPageName = new System.Windows.Forms.Label();
            this.listBoxAttachments = new System.Windows.Forms.ListBox();
            this.SuspendLayout();
            // 
            // btnOpenAttachment
            // 
            this.btnOpenAttachment.Location = new System.Drawing.Point(52, 259);
            this.btnOpenAttachment.Name = "btnOpenAttachment";
            this.btnOpenAttachment.Size = new System.Drawing.Size(75, 23);
            this.btnOpenAttachment.TabIndex = 0;
            this.btnOpenAttachment.Text = "Open";
            this.btnOpenAttachment.UseVisualStyleBackColor = true;
            this.btnOpenAttachment.Click += new System.EventHandler(this.btnOpenAttachment_Click);
            // 
            // btnCancel
            // 
            this.btnCancel.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            this.btnCancel.Location = new System.Drawing.Point(133, 259);
            this.btnCancel.Name = "btnCancel";
            this.btnCancel.Size = new System.Drawing.Size(75, 23);
            this.btnCancel.TabIndex = 1;
            this.btnCancel.Text = "Cancel";
            this.btnCancel.UseVisualStyleBackColor = true;
            this.btnCancel.Click += new System.EventHandler(this.btnCancel_Click);
            // 
            // lblPageName
            // 
            this.lblPageName.AutoSize = true;
            this.lblPageName.Location = new System.Drawing.Point(12, 9);
            this.lblPageName.Name = "lblPageName";
            this.lblPageName.Size = new System.Drawing.Size(32, 13);
            this.lblPageName.TabIndex = 2;
            this.lblPageName.Text = "Page";
            // 
            // listBoxAttachments
            // 
            this.listBoxAttachments.FormattingEnabled = true;
            this.listBoxAttachments.Location = new System.Drawing.Point(12, 25);
            this.listBoxAttachments.Name = "listBoxAttachments";
            this.listBoxAttachments.Size = new System.Drawing.Size(196, 225);
            this.listBoxAttachments.TabIndex = 3;
            this.listBoxAttachments.SelectedIndexChanged += new System.EventHandler(this.listBoxAttachments_SelectedIndexChanged);
            this.listBoxAttachments.SelectedValueChanged += new System.EventHandler(this.listBoxAttachments_SelectedValueChanged);
            // 
            // ViewPageAttachmentsForm
            // 
            this.AcceptButton = this.btnOpenAttachment;
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.CancelButton = this.btnCancel;
            this.ClientSize = new System.Drawing.Size(220, 294);
            this.Controls.Add(this.listBoxAttachments);
            this.Controls.Add(this.lblPageName);
            this.Controls.Add(this.btnCancel);
            this.Controls.Add(this.btnOpenAttachment);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "ViewPageAttachmentsForm";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterParent;
            this.Text = "View Page Attachments";
            this.Load += new System.EventHandler(this.ViewPageAttachmentsForm_Load);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button btnOpenAttachment;
        private System.Windows.Forms.Button btnCancel;
        private System.Windows.Forms.Label lblPageName;
        private System.Windows.Forms.ListBox listBoxAttachments;
    }
}