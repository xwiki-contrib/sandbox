using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Office.Core;
using System.IO;
using XWiki;
using XWiki.Logging;

namespace XWord2003.AddinActions
{
    public class DocumentCopier : IAction<bool>
    {
        private Microsoft.Office.Interop.Word.Document document;
        private string path;
        private Microsoft.Office.Interop.Word.WdSaveFormat saveFormat;
        private bool result;


        public DocumentCopier(Microsoft.Office.Interop.Word.Document document, string path, Microsoft.Office.Interop.Word.WdSaveFormat saveFormat)
        {
            this.document = document;
            this.path = path;
            this.saveFormat = saveFormat;
            this.result = false;
        }

        #region IAction<bool> Members

        public void Perform()
        {
            result = true;
            try
            {
                Object format = saveFormat;
                Object copyPath = path;
                Object encoding = MsoEncoding.msoEncodingUnicodeLittleEndian;
                Object missing = Type.Missing;
                Object originalFilePath = document.FullName;
                Object initialDocSaveFormat = document.SaveFormat;
                document.SaveAs(ref copyPath, ref format, ref missing, ref missing, ref missing, ref missing,
                                ref missing, ref missing, ref missing, ref missing, ref missing, ref encoding,
                                ref missing, ref missing, ref missing, ref missing);
                document.SaveAs(ref originalFilePath, ref initialDocSaveFormat, ref missing, ref missing, ref missing, ref missing,
                                ref missing, ref missing, ref missing, ref missing, ref missing, ref missing,
                                ref missing, ref missing, ref missing, ref missing);
            }
            catch (IOException ioex)
            {
                Log.Exception(ioex);
                UserNotifier.Error(ioex.Message);
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
