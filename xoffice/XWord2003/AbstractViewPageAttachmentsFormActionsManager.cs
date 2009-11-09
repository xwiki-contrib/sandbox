using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UICommons.UIActionsManagement;

namespace XWord2003
{
    public abstract class AbstractViewPageAttachmentsFormActionsManager : IActionsManager<ViewPageAttachmentsForm>
    {
        #region IActionsManager<ViewPageAttachmentsForm> Members

        public abstract void EnqueueAllHandlers();

        #endregion

        /// <summary>
        /// Action to be performed on form load.
        /// </summary>
        /// <param name="sender">Sender object.</param>
        /// <param name="e">Event arguments.</param>
        protected abstract void ActionFormLoad(object sender, EventArgs e);

        /// <summary>
        /// Action to be performed when users select a different attachment from the listbox.
        /// </summary>
        /// <param name="sender">Sender object.</param>
        /// <param name="e">Event arguments.</param>
        protected abstract void ActionAttachmentChange(object sender, EventArgs e);

        /// <summary>
        /// Action to be performed when users want to open an attachment (ex: by clicking on "Open" button).
        /// </summary>
        /// <param name="sender">Sender object (ex: Open button, ViewPageAttachments form)</param>
        /// <param name="e">Event arguments</param>
        protected abstract void ActionOpenAttachment(object sender, EventArgs e);

        /// <summary>
        /// Action to be performed when users cancel viewing attachments (by clicking "Cancel" button, or by pressing ESC key).
        /// </summary>
        /// <param name="sender">Sender object.</param>
        /// <param name="e">Event arguments.</param>
        protected abstract void ActionCancel(object sender, EventArgs e);
    }
}
