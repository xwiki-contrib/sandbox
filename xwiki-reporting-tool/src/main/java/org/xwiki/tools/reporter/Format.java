package org.xwiki.tools.reporter;


public interface Format
{
    public static final Format PLAIN = new Plain();

    public static final Format HTML = new Html();

    String formatHeader(final String header);

    String formatSubheader(final String header);

    /** Format a message from the test such as a failure message or a stack trace. */
    String formatMessage(final String message);

    String escape(final String content);

    String newLine();

    String horizontalRuler();

    String link(final String text, final String href);

    static class Plain implements Format
    {
        public String formatHeader(final String header)
         {
             return header;
         }

         public String formatSubheader(final String header)
         {
             return header;
         }

         public String formatMessage(final String message)
         {
             return this.newLine() + message + this.newLine();
         }

         public String escape(final String input)
         {
             return input;
         }

         public String newLine()
         {
             return "\n";
         }

         public String horizontalRuler()
         {
             return "-------------------------------------------------------------------------------";
         }

         public String link(final String text, final String href)
         {
             return text + "  " + href + this.newLine();
         }
    }

    static class Html implements Format
    {
        public String formatHeader(final String header)
        {
            return "<span style='font-size:200%;color:e00;'>" + this.escape(header) + "</span>";
        }

        public String formatSubheader(final String header)
        {
            return "<span style='font-size:150%;color:e00;'>" + this.escape(header) + "</span>";
        }

        /** Format a message from the test such as a failure message or a stack trace. */
        public String formatMessage(final String message)
        {
            return "<div style='font-family:monospace;background-color:#eee;'>" + this.escape(message) + "</div>";
        }

        public String escape(final String content)
        {
            return content.replaceAll("&", "&amp;")
                          .replaceAll("<", "&lt;")
                          .replaceAll(">", "&gt;")
                          .replaceAll("'", "&#39;")
                          .replaceAll("\"", "&#34;");
        }

        public String newLine()
        {
            return "<br />";
        }

        public String horizontalRuler()
        {
            return "<hr />";
        }

        public String link(final String text, final String href)
        {
            return "<a href=\"" + href + "\">" + text + "</a>" + this.newLine();
        }
    }
}
