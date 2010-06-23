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

    public static void main(String[] args) throws Exception
    {
        if (args.length < 1) {
            System.out.println("Call with a file name to convert an XML file to a directory tree.");
            System.out.println("Call with a directory name to a directory tree to an XML file.");
            return;
        }
        File dir = null;
        dir = new File(args[0]);

        if (dir == null) {
            System.out.println("ERROR: No recognizable file name entered.");
        }
        if (!dir.exists()) {
            System.out.println("ERROR: No directory found named: " + dir.getAbsolutePath());
            return;
        }

        SchlemielsStringBuilder appendTo = new SchlemielsStringBuilder();

        if (dir.isDirectory()) {
            Dir2xml d = new Dir2xml();
            for (File subFile : dir.listFiles()) {
                if (subFile.getName().equals("&!metaData")) {
                    appendTo.append(d.readContent(subFile));
                }
            }
            for (File subFile : dir.listFiles()) {
                d.toXML(subFile, appendTo);
            }
            final String fileName = dir.getName() + ".xml";
            d.createFile(appendTo.toString().trim(), new ArrayList<String>(){{add(fileName);}}, new File("./"));
        } else {
            new Dir2xml().fromXML(dir, new File(dir.getName().replaceAll("\\.[^.]*$", "")));
        }
        return;
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
System.out.println(subFile.getName());
                this.toXML(subFile, appendTo, nestDepth + 1);
            }
            this.addSpaces(appendTo, nestDepth);
        } else {
            appendTo.append(StringEscapeUtils.escapeXml(this.readContent(fileOrDir)));
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
        for (int i = 0; i < nestDepth; i++) {
            appendTo.append("  ");
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
            if (numberOfCharactersInBuffer > 0 && fileBuffer[numberOfCharactersInBuffer - 1] == '\n') {
                numberOfCharactersInBuffer--;
                // Windxws compatabulity...
                if (numberOfCharactersInBuffer > 0 && fileBuffer[numberOfCharactersInBuffer - 1] == '\r') {
                    numberOfCharactersInBuffer--;
                }
            }
            return String.copyValueOf(fileBuffer, 0, numberOfCharactersInBuffer);
        } finally {
            try {
                in.close();
            } catch (Exception e) {
                // If it couldn't be opened then it can't be closed.
            }
        }
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
        // 0 = out of tag.
        // 1 = on <
        // 2 = inside tag.
        // 3 = inside meta data (? or !) tag.
        // 4 = detected a possible close of a tag.
        // 5 = detected a < followed by a /
        int state = 0;

        int lastEntityIndex  = 0;
        int lastClosingBracketIndex = 0;
        int number = 0;
        ArrayList<String> tagList = new ArrayList<String>();
        SchlemielsStringBuilder meta = new SchlemielsStringBuilder();

        String content = readContent(xmlFile);
        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);
            if (state == 0) {
                if (ch == '<') {
                    state = 1;
                    lastEntityIndex = i;
                }
            } else if (state == 1 && (ch == '?' || ch == '!')) {
                state = 3;
            } else if (ch == '/' && (state == 2 || state == 1)) {
                if (state == 2) {
                    // Expecting <tag/>
                    state = 4;
                } else {
                    // Expecting </tag>
                    state = 5;
                }
            } else if (ch == '>') {
                // tag closed. If meta then write to meta file, otherwise, push to stack.
                if (state == 2) {

                    // Advance the number of tags in this element.
                    number++;
                    numbersByStackDepth[tagList.size()] = number;

                    // opening tag, push to stack.
                    tagList.add(number + "." + XMLToFileName(content.substring(lastEntityIndex + 1, i)));

                    // Switch to a new number since we are now parsing a subelement.
                    number = 0;
                    lastEntityIndex = i;
                    lastClosingBracketIndex = i;
                    state = 0;
                } else if (state == 3) {
                    if (tagList.size() != 0) {
                        throw new UnsupportedOperationException("Can't have meta data inside of a tag.\n"
                                                                + "This is the problem: "
                                                                + content.substring(lastEntityIndex, i + 1));
                    }
                    meta.append(content.substring(lastEntityIndex, i + 1));
                    lastEntityIndex = i;
                    lastClosingBracketIndex = i;
                    state = 0;
                } else if (state == 4) {
                    // Empty <tag/>
                    tagList.add(number + "." + XMLToFileName(content.substring(lastEntityIndex + 2, i)));
                    number++;
                    createFile("", tagList, outDir);
                    tagList.remove(tagList.size() - 1);
                    lastEntityIndex = i;
                    lastClosingBracketIndex = i;
                } else if (state == 5) {
                    // An </end> tag
                    String lastTag = tagList.get(tagList.size() - 1);

                    // Test is the content from the last < to the current position (>)
                    // the same (when converted to filename format) as the last opened tag? (without it's number)
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

                        state = 0;
                        lastEntityIndex = i;
                        lastClosingBracketIndex = i;
                    }
                }
            } else if (state == 1) {
                // if we were on a < now we're in a tag.
                state = 2;
            } else if (state == 4) {
                // <tag with a / in it is not a self closed tag>
                state = 2;
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
