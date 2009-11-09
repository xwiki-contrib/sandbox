using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Office.Core;

namespace XWord2003.AddinActions
{
    /// <summary>
    /// Opens a local (HTML document) file with Word.
    /// </summary>
    public class HTMLDocumentOpener : IAction<Microsoft.Office.Interop.Word.Document>
    {
        private string path;
        private XWord2003AddIn addin;
        private Microsoft.Office.Interop.Word.Document document;
        private Object missing = Type.Missing;

        public HTMLDocumentOpener(XWord2003AddIn addin, string path)
        {
            this.addin = addin;
            this.path = path;
            this.document = null;
        }


        #region IAction<Document> Members

        /// <summary>
        /// Opens a local (HTML document) file with Word.
        /// </summary>
        public void Perform()
        {
            object format = Microsoft.Office.Interop.Word.WdOpenFormat.wdOpenFormatWebPages;
            object filePath = path;
            addin.ActiveDocumentInstance.WebOptions.AllowPNG = true;
            addin.ActiveDocumentInstance.WebOptions.Encoding = MsoEncoding.msoEncodingUnicodeLittleEndian;
            document = addin.Application.Documents.Open(ref filePath,
                                                     ref missing, ref missing, ref missing,
                                                     ref missing, ref missing, ref missing,
                                                     ref missing, ref missing, ref format,
                                                     ref missing, ref missing, ref missing,
                                                     ref missing, ref missing, ref missing);
        }

        /// <summary>
        /// Gets the result of the performed operation.
        /// </summary>
        /// <returns>A <code>Word.Document</code> instance.</returns>
        public Microsoft.Office.Interop.Word.Document GetResults()
        {
            return document;
        }

        #endregion
    }
}
