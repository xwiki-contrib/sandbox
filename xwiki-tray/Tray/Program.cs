using System;
using System.Collections.Generic;
using System.Windows.Forms;

namespace Tray
{
    static class Program
    {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        [STAThread]
        static void Main()
        {
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            XWikiForm form = new XWikiForm();
            Application.Run(form);
        }
    }
}
