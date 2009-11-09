using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace XWord2003.UIManagement
{
    /// <summary>
    /// Utility class that contains XWord notifications messages.
    /// </summary>
    public class UIMessages
    {
        /// <summary>
        /// Private constructor.
        /// </summary>
        private UIMessages()
        {
        }

        public const string NOT_A_WIKI_PAGE = "You are not currently editing a wiki page";
        public const string NOT_A_PUBLISHED_PAGE = "You are not currently editing a published wiki page!";
        public const string ALREADY_EDITING_PAGE = "You are already editing this page.";
        public const string PROTECTED_PAGE = "You cannot edit this page.\n"
            + "This page contains scrips that provide functionality to the wiki.";

        public const string SELECT_SPACE_AND_PAGE = "Please select a space and a page!";

        public const string WIKI_STRUCTURE_NOT_LOADED = "Wiki structure not loaded, yet!\n"
            + "Are you logged in?\n"
            + "Is the server running?";

        public const string NO_OPENED_DOCUMENT = "This command is not available because no document is open.";

        public const string ERROR_SAVING_PAGE = "There was an error when trying to save the page.";
        public const string ERROR_UPLOADING_ATTACHMENTS = "Error uploading attachments!";
        public const string ERROR_GET_ATTACHMENTS_LIST = "Unable to get list of attachments!\n"
                    + "Is this page published?\n"
                    + "Is the server running?";

        public const string SERVER_ERROR_SAVING_PAGE = "There was an error on the server when trying to save the page";
        public const string SERVER_ERROR_NO_PROGRAMMING_RIGHTS = "Server error: the pages in MSOffice space don't have programming rights.";
        public const string SERVER_ERROR_WRONG_REQUEST = "Server error: Wrong request!";
        public const string SERVER_ERROR_NO_EDIT_RIGHTS = "Server error: You dont have the right to edit this page!";
        public const string SERVER_ERROR_NO_GROOVY_RIGHTS = "Server error: Please contact your server adminitrator. Error on executing groovy page in MSOffice space!";
        public const string SERVER_ERROR_INSUFFICIENT_MEMORY = "Server error: The server has insufficient memmory to execute the current tasks!";
        public const string SERVER_ERROR_VELOCITY_PARSER = "Server error: Error while parsing velocity page!";
    }
}
