/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwoot.contentprovider;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xmlrpc.XmlRpcException;
import org.codehaus.swizzle.confluence.Attachment;
import org.xwiki.xmlrpc.XWikiXmlRpcClient;
import org.xwiki.xmlrpc.model.XWikiObject;
import org.xwiki.xmlrpc.model.XWikiPage;

/**
 * A class containing utility methods.
 * 
 * @version $Id$
 */
public class Utils
{
    public static final String LIST_CONVERSION_SEPARATOR = "\n";

    /**
     * Convert an XWikiPage to an XWootObject.
     * 
     * @param page The XWikiPage to be converted.
     * @param newlyCreated The value of the newlyCreated field in the returned XWootObject.
     * @return The XWootObject corresponding to the XWiki page.
     */
    public static XWootObject xwikiPageToXWootObject(XWikiPage page, boolean newlyCreated)
    {
        List<XWootObjectField> fields = new ArrayList<XWootObjectField>();

        XWootObjectField field;

        /* These are the relevant fields */
        field = new XWootObjectField("content", page.getContent(), true);
        fields.add(field);

        field = new XWootObjectField("title", page.getTitle(), false);
        fields.add(field);

        field = new XWootObjectField("parentId", page.getParentId(), false);
        fields.add(field);

        XWootObject result =
            new XWootObject(page.getId(), page.getVersion(), page.getMinorVersion(), String.format("%s:%s",
                Constants.PAGE_NAMESPACE, page.getId()), false, fields, newlyCreated);

        return result;
    }

    /**
     * Convert a list to a string containing list's elements separated by a separator.
     * 
     * @param list
     * @param separator
     * @return
     */
    public static String listToString(List list, String separator)
    {
        Formatter f = new Formatter();

        for (int i = 0; i < list.size(); i++) {
            if (i == list.size() - 1) {
                f.format("%s", list.get(i));
            } else {
                f.format("%s%s", list.get(i), separator);
            }
        }

        return f.toString();
    }

    /**
     * Convert a string of elements separated by a separator into a list.
     * 
     * @param string
     * @param separator
     * @return
     */
    public static List<String> stringToList(String string, String separator)
    {
        String[] values = string.split(separator);

        List<String> result = new ArrayList<String>();
        for (String value : values) {
            result.add(value);
        }

        return result;
    }

    /**
     * Build an XWootObject from a corresponding XWikiObject.
     * 
     * @param object
     * @param newlyCreated The value of the newlyCreated field in the returned XWootObject.
     * @return
     */
    public static XWootObject xwikiObjectToXWootObject(XWikiObject object, boolean newlyCreated,
        XWootContentProviderConfiguration config)
    {
        List<XWootObjectField> fields = new ArrayList<XWootObjectField>();

        for (String property : object.getProperties()) {
            Object valueObject = object.getProperty(property);
            if (!(valueObject instanceof Serializable)) {
                System.out.format("%s.%s is not serializable (%s)\n", object.getPrettyName(), property, valueObject
                    .getClass().getName());
            } else {
                boolean isWootable = config.isWootable(object.getClassName(), property);
                Serializable value = (Serializable) valueObject;
                Class originalType = value.getClass();

                if (isWootable && (value instanceof List)) {
                    value = listToString((List) value, LIST_CONVERSION_SEPARATOR);
                }

                XWootObjectField field = new XWootObjectField(property, value, originalType, isWootable);
                fields.add(field);
            }
        }

        boolean isCumulative = config.isCumulative(object.getClassName());
        String guid;

        /* Create a guid depending on the fact that the object is cumulative or not */
        if (isCumulative) {
            guid = String.format("%s:%s", Constants.OBJECT_NAMESPACE, object.getGuid());
        } else {
            guid =
                String.format("%s:%s:%s[%d]", Constants.OBJECT_NAMESPACE, object.getPageId(), object.getClassName(),
                    object.getId());
        }

        XWootObject result =
            new XWootObject(object.getPageId(), object.getPageVersion(), object.getPageMinorVersion(), guid,
                isCumulative, fields, newlyCreated);

        result.putMetadata(Constants.CLASS_NAME_METADATA, object.getClassName());

        return result;
    }

    /**
     * Build an xwiki page from the corresponding XWoot object
     * 
     * @param object
     * @return
     */
    public static XWikiPage xwootObjectToXWikiPage(XWootObject object)
    {
        String namespace = object.getGuid().split(":")[0];
        if (!namespace.equals(Constants.PAGE_NAMESPACE)) {
            throw new IllegalArgumentException(String.format("Invalid namespace. Expected '%s', got '%s'\n",
                Constants.PAGE_NAMESPACE, namespace));
        }

        XWikiPage xwikiPage = new XWikiPage();

        xwikiPage.setId(object.getPageId());
        xwikiPage.setVersion(object.getPageVersion());
        xwikiPage.setMinorVersion(object.getPageMinorVersion());

        /*
         * String value = (String) object.getFieldValue("title"); xwikiPage.setTitle(value != null ? value : "");
         */
        /*
         * Ignore the title field. We cannot change the title through the store function because it would imply a
         * rename, due to the Confluence semantics of the store operation. A dedicated API will be needed here
         */
        xwikiPage.setTitle("");

        String value = (String) object.getFieldValue("parentId");
        xwikiPage.setParentId(value != null ? value : "");

        value = (String) object.getFieldValue("content");
        xwikiPage.setContent(value != null ? value : "");

        return xwikiPage;
    }

    /**
     * Build an XWikiObject from the corresponding XWootObject by performing necessary conversions.
     * 
     * @param object An XWootObject
     * @return The corresponding XWikiObject
     */
    public static XWikiObject xwootObjectToXWikiObject(XWootObject object)
    {
        String[] components = object.getGuid().split(":", 2);

        String namespace = components[0];
        String guid = components[1];
        if (!namespace.equals(Constants.OBJECT_NAMESPACE)) {
            throw new IllegalArgumentException(String.format("Invalid namespace. Expected '%s', got '%s'\n",
                Constants.OBJECT_NAMESPACE, namespace));
        }

        XWikiObject xwikiObject = new XWikiObject();

        xwikiObject.setPageId(object.getPageId());

        /*
         * By convention the version 0 means that the object is newly created so adjust the version to 1 if it's the
         * case
         */
        if (object.getPageVersion() == 0) {
            xwikiObject.setPageVersion(1);
        } else {
            xwikiObject.setPageVersion(object.getPageVersion());
        }
        xwikiObject.setPageMinorVersion(object.getPageMinorVersion());

        /*
         * If the object is cumulative then we use the guid to reference it. Otherwise the XWootObject guid is in the
         * form PageId:ClassName[ObjectNumber]. In this case we parse this guid and fill the relevant fields of the
         * final XWikiObject.
         */
        if (object.isCumulative()) {
            xwikiObject.setGuid(guid);
        } else {
            Pattern pattern = Pattern.compile("([^:]+):([^\\[]+)\\[(\\p{Digit}+)\\]");
            Matcher matcher = pattern.matcher(guid);
            if (matcher.matches()) {
                String className = matcher.group(2);
                int number = Integer.parseInt(matcher.group(3));

                xwikiObject.setClassName(className);
                xwikiObject.setId(number);
            } else {
                throw new IllegalArgumentException(String.format("Invalid guid for non cumulative object: %s\n", guid));
            }
        }

        /* Populate xwiki object's values */
        for (XWootObjectField field : object.getFields()) {
            Serializable value = field.getValue();

            /*
             * Perform string to list conversion. This happens when a list field has been declared wootable: it is
             * translated to a String. Here we do the opposite
             */
            if (List.class.isAssignableFrom(field.getOriginalType()) && (field.getValue() instanceof String)) {
                value = (Serializable) stringToList((String) field.getValue(), LIST_CONVERSION_SEPARATOR);
            }

            xwikiObject.setProperty(field.getName(), value);
        }

        xwikiObject.setClassName(object.getMetadata(Constants.CLASS_NAME_METADATA));

        return xwikiObject;
    }

    /**
     * Build an XWootObject from an Attachment and its associated data.
     * 
     * @param attachment
     * @param pageVersion
     * @param pageMinorVersion
     * @param attachmentData
     * @param newlyCreated
     * @return The created XWootObject.
     */
    public static XWootObject attachmentToXWootObject(Attachment attachment, int pageVersion, int pageMinorVersion,
        byte[] attachmentData, boolean newlyCreated)
    {
        List<XWootObjectField> fields = new ArrayList<XWootObjectField>();

        XWootObjectField field;

        /* These are the relevant fields */
        field = new XWootObjectField("fileName", attachment.getFileName(), false);
        fields.add(field);

        field = new XWootObjectField("attachmentData", attachmentData, false);
        fields.add(field);

        XWootObject result =
            new XWootObject(attachment.getPageId(), pageVersion, pageMinorVersion, String.format("%s:%s:%s",
                Constants.ATTACHMENT_NAMESPACE, attachment.getPageId(), attachment.getFileName()), false, fields,
                newlyCreated);

        return result;
    }

    /**
     * Create the Attachment from the corresponding XWootObject
     * 
     * @param object The XWoot object encoding attachment data.
     * @return The Attachment structure.
     */
    public static Attachment xwootObjectToAttachment(XWootObject object)
    {
        Attachment attachment = new Attachment();

        attachment.setPageId(object.getPageId());

        String value = (String) object.getFieldValue("fileName");
        attachment.setFileName(value != null ? value : "");

        return attachment;
    }

    /**
     * Retrieve the attachment data from the corresponding XWootObject
     * 
     * @param object The XWootObject containing attachment data.
     * @return The attachment data.
     */
    public static byte[] xwootObjectToAttachmentData(XWootObject object)
    {        
        byte[] result = (byte[]) object.getFieldValue("attachmentData");

        return result;
    }

    /**
     * This function removes from the current object all the fields that have not changed with respect to the reference
     * object.
     * 
     * @param currentObject
     * @param referenceObject
     * @return A copy of currentObject with all the fields that haven't changed with respect to the reference object
     *         removed.
     */
    public static XWootObject removeUnchangedFields(XWootObject currentObject, XWootObject referenceObject)
    {
        List<XWootObjectField> resultFields = new ArrayList<XWootObjectField>();

        for (XWootObjectField field : currentObject.getFields()) {
            if (field.getValue() != null) {
                if (!field.getValue().equals(referenceObject.getFieldValue(field.getName()))) {
                    resultFields.add(field);
                }
            }
        }

        return new XWootObject(currentObject.getPageId(), currentObject.getPageVersion(), currentObject
            .getPageMinorVersion(), currentObject.getGuid(), currentObject.isCumulative(), resultFields, currentObject
            .isNewlyCreated());
    }

    /**
     * Remove all attachments that haven't been modified from a list of attachments.
     * 
     * @param currentAttachments The list of attachments at current page version. 
     * @param previousAttachments The list of attachments at a previous page version.
     * @return A list containing only changed/added attachments.
     */
    public static List<Attachment> removeUnchangedAttachments(List<Attachment> currentAttachments,
        List<Attachment> previousAttachments)
    {
        List<Attachment> result = new ArrayList<Attachment>();

        for (Attachment current : currentAttachments) {
            boolean add = true;
            for (Attachment previous : previousAttachments) {
                if (current.getFileName().equals(previous.getFileName())) {
                    if (current.getFileSize().equals(previous.getFileSize())) {                        
                        add = false;
                        break;
                    }
                }
            }

            if (add) {
                result.add(current);
            }
        }

        return result;
    }

    /**
     * Check if the login data are correct.
     * 
     * @param endpoint
     * @param userName
     * @param password
     * @return True if user credentials are accepted, false otherwise.
     * @throws MalformedURLException If a malformed endpoint is provided.
     */
    public static boolean checkLogin(String endpoint, String userName, String password) throws MalformedURLException
    {
        XWikiXmlRpcClient rpc = new XWikiXmlRpcClient(endpoint);

        try {
            rpc.login(userName, password);
            rpc.logout();
        } catch (XmlRpcException e) {
            return false;
        }

        return true;
    }
}
