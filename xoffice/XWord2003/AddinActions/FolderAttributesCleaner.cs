using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

namespace XWord2003.AddinActions
{
    /// <summary>
    /// Converts the folder to a "Normal" type folder.
    /// </summary>
    public class FolderAttributesCleaner : IAction<bool>
    {
        private string folder;

        public FolderAttributesCleaner(string folder)
        {
            this.folder = folder;
        }

        #region IAction<bool> Members

        /// <summary>
        /// Converts the folder to a "Normal" type folder.
        /// </summary>
        public void Perform()
        {
            DirectoryInfo di = new DirectoryInfo(folder);
            if (di.Attributes == FileAttributes.ReadOnly)
            {
                di.Attributes = FileAttributes.Normal;
            }
        }

        public bool GetResults()
        {
            return true;
        }

        #endregion
    }
}
