using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using XWiki;

namespace XWord2003.AddinActions
{
    public class ProcessStarter : IAction<bool>
    {
        private string fullFileName;
        private bool result;

        public ProcessStarter(string fullFileName)
        {
            this.fullFileName = fullFileName;
        }

        #region IAction<bool> Members

        public void Perform()
        {
            result = true;
            Process p = new Process();
            try
            {
                ProcessStartInfo psi = new ProcessStartInfo(fullFileName);
                p.StartInfo = psi;
                p.Start();
            }
            catch (Win32Exception)
            {
                p.StartInfo = new ProcessStartInfo("explorer.exe", Path.GetDirectoryName(fullFileName));
                p.Start();
            }
            catch (Exception ex)
            {
                Log.ExceptionSummary(ex);
                result = false;
            }
        }

        public bool GetResults()
        {
            return result;
        }

        #endregion
    }
}
