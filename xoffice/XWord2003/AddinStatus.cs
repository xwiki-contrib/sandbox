using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace XWord2003
{
    /// <summary>
    /// Represents the context of the add-in.
    /// </summary>
    public class AddinStatus
    {
        private bool loggedIn;
        private bool connectedWithServer;
        private String serverURL;
        private String syntax;

        /// <summary>
        /// The syntax used to save the wiki pages.
        /// <remarks>The conversion is done on the web server</remarks>
        /// </summary>
        public String Syntax
        {
            get { return syntax; }
            set { syntax = value; }
        }

        /// <summary>
        /// The URL of the server to which the user is conected.
        /// </summary>
        public String ServerURL
        {
            get { return serverURL; }
            set { serverURL = value; }
        }

        /// <summary>
        /// Specifies if the user is logged in to XWiki server or not.
        /// </summary>
        public bool LoggedIn
        {
            get { return loggedIn; }
            set { loggedIn = value; }
        }

        /// <summary>
        /// Specifies if the server is connected to the XWiki server or not.
        /// </summary>
        public bool ConnectedWithServer
        {
            get { return connectedWithServer; }
            set { connectedWithServer = value; }
        }
    }
}
