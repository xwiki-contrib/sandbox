using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using UICommons.UIActionsManagement;

namespace XWord2003
{
    /// <summary>
    /// Defines the methods to be implemented by action managers for <code>EditPageForm</code>.
    /// </summary>
    public abstract class AbstractEditPageFormActionsManager : IActionsManager<EditPageForm>
    {
        #region IActionsManager<EditPageForm> Members

        /// <summary>
        /// Enqueues all know event handlers for <code>EditPageForm</code>.
        /// </summary>
        public abstract void EnqueueAllHandlers();

        #endregion

        /// <summary>
        /// Action to be performed on form load.
        /// </summary>
        /// <param name="sender">Sender object.</param>
        /// <param name="e">Event arguments.</param>
        protected abstract void ActionFormLoad(object sender, EventArgs e);

        /// <summary>
        /// Action to be performed on edit page (ex: when the user clicks 'Edit' button)
        /// </summary>
        /// <param name="sender">Sender object.</param>
        /// <param name="e">Event arguments.</param>
        protected abstract void ActionEditPage(object sender, EventArgs e);

        /// <summary>
        /// Action to be performed on canceling (ex: user clicks 'Cancel' button or presses ESC key).
        /// </summary>
        /// <param name="sender">Sender object.</param>
        /// <param name="e">Event arguments.</param>
        protected abstract void ActionCancel(object sender, EventArgs e);

        /// <summary>
        /// Action to be performed when selected space is changed.
        /// </summary>
        /// <param name="sender">Sender object.</param>
        /// <param name="e">Event arguments.</param>
        protected abstract void ActionSpaceChange(object sender, EventArgs e);

        /// <summary>
        /// Action to be performed when viewing page attachments.
        /// </summary>
        /// <param name="sender">Sender object.</param>
        /// <param name="e">Event arguments.</param>
        protected abstract void ActionViewAttachments(object sender, EventArgs e);
    }
}
