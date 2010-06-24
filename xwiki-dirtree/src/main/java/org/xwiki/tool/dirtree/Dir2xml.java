package org.xwiki.tool.dirtree;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.Writer;
import java.io.IOException;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.LinkedList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Dir2xml
{
    private final String lineBreak = System.getProperty("line.separator");

    // Keep track of attributes for each tag so that they can be made into &!attributes files
    private final Map<String, String> attributesByTag = new HashMap<String, String>();

    private final List<List<String>> paths = new ArrayList<List<String>>();

    /** Keep track of the order of elements in the document. */
    private final int[] numbersByStackDepth = new int[100];

    /** If true then we try to make XML which is exactly the same as XWiki export. */
    private boolean conformMode;

    /** If true then avoid outputting characters which are not ascii lower 128. */
    private boolean asciiOnly;

    public static void main(String[] args) throws Exception
    {
        if (args.length < 1) {
            System.out.println("Call with a file name to convert an XML file to a directory tree.");
            System.out.println("Call with a directory name to a directory tree to an XML file.");
            System.out.println("-o Directory to output to");
            System.out.println("-c Try to conform to XWiki export format for escaping and do not tab in sub-elements");
            System.out.println("-a When generating XML, escape any character which is not ascii (characters 0-127)");
            return;
        }
        File dir = null;
        File output = new File(".");
        Dir2xml d = new Dir2xml();

        // Get output directory if specified.
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-o") && args.length > i + 1) {
                i++;
                output = new File(args[i]);
            } else if (args[i].equals("-c")) {
                d.conformMode = true;
            } else if (args[i].equals("-a")) {
                d.asciiOnly = true;
            }
        }

        // Cycle through files.
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                if (args[i].equals("-o")) {
                    // Skip -o and the argument after it.
                    i++;
                }
            } else {

                dir = new File(args[i]);

                if (!dir.exists()) {
                    System.out.println("ERROR: No directory found named: " + dir.getAbsolutePath());
                    return;
                }

                if (dir.isDirectory()) {
                    SchlemielsStringBuilder appendTo = new SchlemielsStringBuilder();
                    for (File subFile : dir.listFiles()) {
                        if (subFile.getName().equals("&!metaData")) {
                            appendTo.append(d.readContent(subFile));
                        }
                    }
                    for (File subFile : dir.listFiles()) {
                        d.toXML(subFile, appendTo);
                    }
                    final String fileName = dir.getName() + ".xml";
                    d.createFile(appendTo.toString().trim(), new ArrayList<String>(){{add(fileName);}}, output);

                } else {
                    File out = new File(output, dir.getName().replaceAll("\\.[^.]*$", ""));
                    if (out.exists()) {
                        d.deleteRecursive(out);
                    }
                    d.fromXML(dir, out);
                }
            }
        }
    }

    private void deleteRecursive(File f)
    {
        if (f.isDirectory()) {
            File[] subFiles = f.listFiles();
            for (int i = 0; i < subFiles.length; i++) {
                deleteRecursive(subFiles[i]);
            }
        }
        f.delete();
        if(f.exists()) {
            throw new RuntimeException("Couldn't delete file " + f.getAbsolutePath());
        }
    }

    public void toXML(final File fileOrDir, final SchlemielsStringBuilder appendTo) throws Exception
    {
        this.toXML(fileOrDir, appendTo, 0);
    }

    public void toXML(final File fileOrDir, final SchlemielsStringBuilder appendTo, final int nestDepth)
        throws Exception
    {
        String fileName = fileOrDir.getName();

        if (fileName.startsWith("&!")) {
            // special files have to be handled seperately.
            return;
        }

        // remove the file number and change the escaping...
        String XMLfilename = fileNameToXML(fileName.substring(1 + fileName.indexOf('.')));
        if (asciiOnly) {
            XMLfilename = escapeNonAscii(XMLfilename);
        }
        this.addSpaces(appendTo, nestDepth);
        appendTo.append("<").append(XMLfilename);
        this.addAttributes(fileOrDir, appendTo);
        appendTo.append(">");

        if (fileOrDir.isDirectory()) {
            // Sort files by number...
            File[] subFiles = fileOrDir.listFiles();
            // This will blow up if the files are sparasely numbered. TODO?
            File[] sortedFiles = new File[subFiles.length + 1];
            for (int i = 0; i < subFiles.length; i++) {
                String subFileName = subFiles[i].getName();
                sortedFiles[Integer.parseInt(subFileName.substring(0, subFileName.indexOf('.')))] = subFiles[i];
            }

            for (int i = 0; i < sortedFiles.length; i++) {
                File subFile = sortedFiles[i];
                if (subFile == null) {
                    continue;
                }
                this.toXML(subFile, appendTo, nestDepth + 1);
            }
            this.addSpaces(appendTo, nestDepth);
        } else {
            String content = this.readContent(fileOrDir);
            if (this.conformMode) {
                // If we are trying to conform exactly to XWiki format, use our own escaper.
                content = escapeXML(content);
            } else {
                content = StringEscapeUtils.escapeXml(content);
            }
            if (asciiOnly) {
                // If we're trying to only output ascii characters, escape again...
                content = escapeNonAscii(content);
            }
            appendTo.append(content);
        }

        appendTo.append("</").append(XMLfilename).append(">");
    }

    private void addAttributes(final File fileOrDir, final SchlemielsStringBuilder appendTo) throws Exception
    {
        if (fileOrDir.isDirectory()) {
            for (File subFile : fileOrDir.listFiles()) {
                if (subFile.getName().equals("&!attributes")) {
                    appendTo.append(" ").append(readContent(subFile));
                    break;
                }
            }
        }
    }

    private void addSpaces(final SchlemielsStringBuilder appendTo, final int nestDepth)
    {
        appendTo.append(this.lineBreak);
        if (!this.conformMode) {
            for (int i = 0; i < nestDepth; i++) {
                appendTo.append("  ");
            }
        }
    }

    public String readContent(File file) throws Exception
    {
        Reader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            // This will be over sized because of encoding.
            char[] fileBuffer = new char[(int) file.length()];
            int numberOfCharactersInBuffer = in.read(fileBuffer);
            return String.copyValueOf(fileBuffer, 0, numberOfCharactersInBuffer);
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                // If it couldn't be opened then it can't be closed.
            }
        }
    }

    /** Keeps track of the state for fromXML psudo xml parser. */
    enum State {
        /** Outside of any tag, in regular content. */
        IN_CONTENT,

        /** On < character */
        ON_LT_BRACKET,

        /** Inside tag */
        INSIDE_TAG,

        /** Inside <?tag?> or <!tag!> */
        INSIDE_META_TAG,

        /** found / in tag, if next char is a > then it's a self closing tag. */
        ON_SLASH_IN_TAG,

        /** Found < followed by / so tag is an </end> tag. */
        INSIDE_END_TAG
    }

    /**
     * My "roll your own" xml parser. I chose not to use a real parser for the simple reason that it would
     * give me a dom structure which had lots of attributes and nuances which I don't understand nor want to 
     * understand as long as I can faithfully reproduce them on the other side.
     * EG: I don't care what the attributes are or if they are namespaces, I just need to be able to have the
     *     same ones in the output as the input.
     *
     * @param xmlFile the {@link java.io.File} to read the XML from.
     * @param outDir create a directory tree in this location.
     */
    public void fromXML(File xmlFile, File outDir) throws Exception
    {
        State currentState = State.IN_CONTENT;

        // The index of the last <, / or >
        int lastEntityIndex  = 0;

        // The index of the last >
        int lastClosingBracketIndex = 0;

        // The "stack depth" of the element currently being parsed.
        int number = 0;
        ArrayList<String> tagList = new ArrayList<String>();
        SchlemielsStringBuilder meta = new SchlemielsStringBuilder();

        String content = readContent(xmlFile);

        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);

            if (currentState == State.IN_CONTENT) {
                if (ch == '<') {
                    currentState = State.ON_LT_BRACKET;
                    lastEntityIndex = i;
                }

            } else if (currentState == State.ON_LT_BRACKET && (ch == '?' || ch == '!')) {
                currentState = State.INSIDE_META_TAG;

            } else if (ch == '/' && (currentState == State.INSIDE_TAG || currentState == State.ON_LT_BRACKET)) {
                if (currentState == State.INSIDE_TAG) {
                    // Expecting <tag/>
                    currentState = State.ON_SLASH_IN_TAG;
                } else {
                    // Expecting </tag>
                    currentState = State.INSIDE_END_TAG;
                }

            } else if (ch == '>') {
                // tag closed. If meta then write to meta file, otherwise, push to stack.
                if (currentState == State.INSIDE_TAG) {

                    // Advance the number of tags in this element.
                    number++;
                    numbersByStackDepth[tagList.size()] = number;

                    // opening tag, push to stack.
                    tagList.add(number + "." + XMLToFileName(content.substring(lastEntityIndex + 1, i)));

                    // Switch to a new number since we are now parsing a subelement.
                    number = 0;
                    lastEntityIndex = i;
                    lastClosingBracketIndex = i;
                    currentState = State.IN_CONTENT;

                } else if (currentState == State.INSIDE_META_TAG) {
                    if (tagList.size() != 0) {
                        throw new UnsupportedOperationException("Can't have meta data inside of a tag.\n"
                                                                + "This is the problem: "
                                                                + content.substring(lastEntityIndex, i + 1));
                    }
                    meta.append(content.substring(lastEntityIndex, i + 1));
                    lastEntityIndex = i;
                    lastClosingBracketIndex = i;
                    currentState = State.IN_CONTENT;

                } else if (currentState == State.ON_SLASH_IN_TAG) {
                    // Empty <tag/>

                    // Increment the number first because there is no open tag.
                    number++;

                    // <thisIsATag/>
                    // "lastEntityIndex + 1" skips the < and "i - 1" skips > substring skips last character (/)
                    tagList.add(number + "." + XMLToFileName(content.substring(lastEntityIndex + 1, i - 1)));
                    createFile("", tagList, outDir);
                    tagList.remove(tagList.size() - 1);

                    lastEntityIndex = i;
                    lastClosingBracketIndex = i;
                    currentState = State.IN_CONTENT;

                } else if (currentState == State.INSIDE_END_TAG) {
                    // An </end> tag
                    String lastTag = tagList.get(tagList.size() - 1);

                    // Test is the content from the last < to the current position (>)
                    // the same (when converted to filename format) as the last opened tag? (without it's number)
                    // +2 is because we want to skip '</'
                    if (XMLToFileName(content.substring(lastEntityIndex + 2, i)).equals(
                            lastTag.substring(1 + lastTag.indexOf(".")))) 
                    {
                        createFile(
                            StringEscapeUtils.unescapeXml(
                                content.substring(lastClosingBracketIndex + 1, lastEntityIndex)),
                                    tagList, outDir);

                        // drop back one level on the stack.
                        tagList.remove(tagList.size() - 1);
                        number = numbersByStackDepth[tagList.size()];

                        currentState = State.IN_CONTENT;
                        lastEntityIndex = i;
                        lastClosingBracketIndex = i;
                    }
                }
            } else if (currentState == State.ON_LT_BRACKET) {
                // if we were on a < now we're in a tag.
                currentState = State.INSIDE_TAG;
            } else if (currentState == State.ON_SLASH_IN_TAG) {
                // <tag with a / in it is not a self closed tag>
                currentState = State.INSIDE_TAG;
            }
        }
        if (meta.length() > 0) {
            createFile(meta.toString(), "&!metaData", outDir);
        }
    }

    public void createFile(String content, List<String> tagList, File containingDir) throws Exception
    {
        SchlemielsStringBuilder ssb = new SchlemielsStringBuilder();
        for (int i = 0; i < tagList.size(); i++) {
            String tag = tagList.get(i);
            if (tag.trim().indexOf(" ") != -1) {
                // The tag has attributes...
                // Trim to make sure < tag attrib1="value"> will take "attrib1..." rather than "tag"
                // That is legal HTML but not XML. Let's be safe...
                String tagNoAttribs = tag.trim();
                tagNoAttribs = tagNoAttribs.substring(0, tagNoAttribs.indexOf(" "));
                ssb.append(tagNoAttribs).append("/");

                // Save the attributes to be made into an &!attributes file when we reach the closing tag.
                // We want keep trailing whitespace but not leading
                // so that < tag param="value" param="value"  >
                // becomes [param="value" param="value"  ]
                String attributes = tag;
                while (attributes.startsWith(" ")) {
                    attributes = attributes.substring(1);
                }
                // now get rid of the tag name...
                attributes = attributes.substring(attributes.indexOf(" ") + 1);

                attributesByTag.put(tagNoAttribs, attributes);

                // Finally we will swap out the tag for the version without attributes so that this will not happen for
                // every sub-element.
                tagList.set(i, tagNoAttribs);

            } else if (i + 1 == tagList.size() && attributesByTag.get(tag) != null) {
                // Found the closing tag for a tag with attributes, lets make an &!attributes file.
                if (!"".equals(content.trim())) {
                    throw new UnsupportedOperationException("Tags with attributes _and_ (non tag) content" 
                                                            + " not supported\nThe problem is here: "
                                                            + tag);
                }

                ssb.append(tag).append("/").append("&!attributes").append("/");
                content = attributesByTag.get(tag);
            } else {
                // nothing special, just a <tag>
                ssb.append(tag).append("/");
            }
        }

        String path = ssb.toString();
        // peel of the last /
        path = path.substring(0, path.length() - 1);
        this.createFile(content, path, containingDir);
    }

    public void createFile(String content, String path, File containingDir) throws IOException
    {
        // unsafe (security) but good enough for a script...
        File file = new File(containingDir, path);

        // If the file was already created because it has subfiles (it's a directory)
        // let's not throw an error unless it was supposed to have content as well.
        if (!file.exists() || !"".equals(content.trim())) {
            Writer out = null;
            try {
                file.getParentFile().mkdirs();
                out = new BufferedWriter(new FileWriter(file));
                out.write(content);
            } finally {
                try {
                    out.close();
                } catch (Exception e) {
                    // If it couldn't be opened then it can't be closed.
                }
            }
        }
    }

    public String XMLToFileName(String xml)
    {
        return StringEscapeUtils.unescapeXml(xml)
                   .replaceAll("&", "&amp;")
                       .replaceAll(":", "&#58;")
                           .replaceAll("/", "&#47;");
    }

    public String fileNameToXML(String fileName)
    {
        return StringEscapeUtils.escapeXml(
                   fileName.replaceAll("&#47;", "/")
                       .replaceAll("&#58;", ":")
                           .replaceAll("&amp;", "&"));
    }

    public String escapeNonAscii(String content)
    {
        SchlemielsStringBuilder out = new SchlemielsStringBuilder();
        int charAfterLastNonAscii = 0;
        for (int i = 0; i < content.length(); i++) {
            if (content.charAt(i) > (char) 127) {
                out.append(content.substring(charAfterLastNonAscii, i));
                // This might mangle charactes not representable by a single UTF-16 entity...
                out.append("&#" + (int) content.charAt(i) + ";");
                charAfterLastNonAscii = i + 1;
            }
        }
        if (charAfterLastNonAscii < content.length()) {
            out.append(content.substring(charAfterLastNonAscii));
        }
        return out.toString();
    }

    /**
     * An escape xml implementation which matches XWiki export escaping.
     * This should only be used for content, not for inside of tags.
     */
    public String escapeXML(String content)
    {
        SchlemielsStringBuilder ssb = new SchlemielsStringBuilder();

        // The index of the last non xml entity.
        int lastContent = 0;

        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (ch == '&') {
                ssb.append(content.substring(lastContent, i)).append("&amp;");
            } else if (ch == '<') {
                ssb.append(content.substring(lastContent, i)).append("&lt;");                
            } else if (ch == '>') {
                ssb.append(content.substring(lastContent, i)).append("&gt;");
            } else {
                continue;
            }
            // if any of the above (not continue;)
            lastContent = i + 1;
        }
        if (lastContent < content.length()) {
            ssb.append(content.substring(lastContent));
        }
        return ssb.toString();
    }

    public static class SchlemielsStringBuilder implements CharSequence
    {
        private final LinkedList<String> contents = new LinkedList<String>();

        private int totalLength;

        public SchlemielsStringBuilder append(String string)
        {
            this.contents.add(string);
            this.totalLength += string.length();
            return this;
        }

        public SchlemielsStringBuilder append(CharSequence cs)
        {
            return this.append(cs.toString());
        }

        public String toString()
        {
            StringBuilder out = new StringBuilder(this.totalLength);
            for (String str : contents) {
                out.append(str);
            }
            return out.toString();
        }

        public int length()
        {
            return this.totalLength;
        }

        public char charAt(int index) throws IndexOutOfBoundsException
        {
            if (index < 0 || index >= this.totalLength) {
                throw new IndexOutOfBoundsException("Index: " + index + " Total size: " + this.totalLength);
            }
            int i = index;
            for (String str : contents) {
                if (i < str.length()) {
                    return str.charAt(i);
                }
                i -= str.length();
            }
            throw new RuntimeException("something went wrong.");
        }

        /** TODO: Debug! */
        public CharSequence subSequence(int startIndex, int endIndex) throws IndexOutOfBoundsException
        {
            if (startIndex > endIndex) {
                throw new IndexOutOfBoundsException("StartIndex greater than EndIndex.\nStartIndex: " 
                                                    + startIndex + " EndIndex: " + endIndex);
            }
            if (startIndex < 0 || endIndex >= this.totalLength) {
                throw new IndexOutOfBoundsException("StartIndex: " + startIndex + " EndIndex: " + endIndex 
                                                    + " Total size: " + this.totalLength);
            }
            SchlemielsStringBuilder out = new SchlemielsStringBuilder();
            int i = startIndex;
            boolean copying = false;
            for (String str : contents) {
                if (i < str.length()) {
                    if (!copying) {
                        copying = true;
                        out.append(str.substring(i));
                        i = endIndex - (i + str.length());
                    } else {
                        out.append(str.substring(0, i));
                        return out;
                    }
                } else {
                    if (copying) {
                        out.append(str);
                    }
                    i -= str.length();
                }
            }
            throw new RuntimeException("Something went wrong...");
        }
    }
}
