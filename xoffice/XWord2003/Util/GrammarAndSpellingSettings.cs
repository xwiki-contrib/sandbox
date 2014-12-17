using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Microsoft.Office.Interop.Word;

namespace XWord2003.Util
{
    public class GrammarAndSpellingSettings
    {
        private static Options wordOptions = null;
        private static bool checkGrammarAsYouType;
        private static bool checkGrammarWithSpelling;
        private static bool checkSpellingAsYouType;

        /// <summary>
        /// Private constructor, this is an utility class.
        /// </summary>
        private GrammarAndSpellingSettings()
        {
        }

        /// <summary>
        /// Save user settings for grammar and spelling checking.
        /// </summary>
        /// <param name="addin">A reference to the <code>XWikiAddin</code>.</param>
        public static void Save(ref XWord2003AddIn addin)
        {
            wordOptions = addin.Application.Options;
            checkGrammarAsYouType = wordOptions.CheckGrammarAsYouType;
            checkGrammarWithSpelling = wordOptions.CheckGrammarWithSpelling;
            checkSpellingAsYouType = wordOptions.CheckSpellingAsYouType;
        }

        /// <summary>
        /// Disable grammar and spelling checking.
        /// </summary>
        public static void Disable()
        {
            if (wordOptions != null)
            {
                wordOptions.CheckGrammarAsYouType = false;
                wordOptions.CheckGrammarWithSpelling = false;
                wordOptions.CheckSpellingAsYouType = false;
            }
        }

        /// <summary>
        /// Restore user settings for grammar and spelling checking.
        /// </summary>
        public static void Restore()
        {
            wordOptions.CheckGrammarAsYouType = checkGrammarAsYouType;
            wordOptions.CheckGrammarWithSpelling = checkGrammarWithSpelling;
            wordOptions.CheckSpellingAsYouType = checkSpellingAsYouType;
        }
    }
}
