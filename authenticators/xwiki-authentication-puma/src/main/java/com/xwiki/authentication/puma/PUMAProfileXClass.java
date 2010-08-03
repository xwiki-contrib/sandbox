package com.xwiki.authentication.puma;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;

/**
 * Helper to manager PUMA profile XClass and XObject.
 * 
 * @version $Id$
 */
public class PUMAProfileXClass
{
    public static final String PUMA_XCLASS = "XWiki.PUMAProfileClass";

    public static final String PUMA_XFIELD_UID = "uid";

    public static final String PUMA_XFIELDPN_UID = "PUMA user unique identifier";

    /**
     * The XWiki space where users are stored.
     */
    private static final String XWIKI_USER_SPACE = "XWiki";

    /**
     * Logging tool.
     */
    private static final Log LOG = LogFactory.getLog(PUMAProfileXClass.class);

    private XWikiContext context;

    private BaseClass pumaClass;

    public PUMAProfileXClass(XWikiContext context) throws XWikiException
    {
        this.context = context;

        XWikiDocument pumaClassDoc = context.getWiki().getDocument(PUMA_XCLASS, context);

        this.pumaClass = pumaClassDoc.getxWikiClass();

        boolean needsUpdate = this.pumaClass.addTextField(PUMA_XFIELD_UID, PUMA_XFIELDPN_UID, 80);

        if (needsUpdate) {
            context.getWiki().saveDocument(pumaClassDoc, "Update PUMA user profile class", context);
        }
    }

    /**
     * @param userDocument the user profile page.
     * @return the uid store in the user profile. Null if it can't find any or if it's empty.
     */
    public String getUid(XWikiDocument userDocument)
    {
        BaseObject pumaObject = userDocument.getObject(this.pumaClass.getName());

        return pumaObject == null ? null : getUid(pumaObject);
    }

    /**
     * @param pumaObject the puma profile object.
     * @return the uid store in the user profile. Null if it can't find any or if it's empty.
     */
    public String getUid(BaseObject pumaObject)
    {
        String uid = pumaObject.getStringValue(PUMA_XFIELD_UID);

        return uid.length() == 0 ? null : uid;
    }

    /**
     * Update or create PUMA profile of an existing user profile with provided PUMA user informations.
     * 
     * @param xwikiUserName the name of the XWiki user to update PUMA profile.
     * @param dn the dn to store in the PUMA profile.
     * @param uid the uid to store in the PUMA profile.
     * @throws XWikiException error when storing information in user profile.
     */
    public void updatePUMAObject(String xwikiUserName, String uid) throws XWikiException
    {
        XWikiDocument userDocument =
            this.context.getWiki().getDocument(XWIKI_USER_SPACE + "." + xwikiUserName, this.context);

        boolean needsUpdate = updatePUMAObject(userDocument, uid);

        if (needsUpdate) {
            this.context.getWiki().saveDocument(userDocument, "Update PUMA user profile", this.context);
        }
    }

    /**
     * Update PUMA profile object with provided PUMA user informations.
     * 
     * @param userDocument the user profile page to update.
     * @param dn the dn to store in the PUMA profile.
     * @param uid the uid to store in the PUMA profile.
     * @return true if modifications has been made to provided user profile, false otherwise.
     */
    public boolean updatePUMAObject(XWikiDocument userDocument, String uid)
    {
        BaseObject pumaObject = userDocument.getObject(this.pumaClass.getName(), true, this.context);

        Map<String, String> map = new HashMap<String, String>();

        boolean needsUpdate = false;

        String objUid = getUid(pumaObject);
        if (!uid.equalsIgnoreCase(objUid)) {
            map.put(PUMA_XFIELD_UID, uid);
            needsUpdate = true;
        }

        if (needsUpdate) {
            this.pumaClass.fromMap(map, pumaObject);
        }

        return needsUpdate;
    }

    /**
     * Search the XWiki storage for a existing user profile with provided PUMA user uid stored.
     * <p>
     * If more than one profile is found the first one in returned and an error is logged.
     * 
     * @param uid the PUMA unique id.
     * @return the user profile containing PUMA uid.
     */
    public XWikiDocument searchDocumentByUid(String uid)
    {
        XWikiDocument doc = null;

        List<XWikiDocument> documentList;
        try {
            // Search for uid in database
            String sql =
                ", BaseObject as obj, StringProperty as prop where doc.fullName=obj.name and obj.className=? and obj.id=prop.id.id and prop.name=? and lower(prop.value)=?";

            documentList =
                this.context.getWiki().getStore().searchDocuments(sql, false, false, false, 0, 0,
                    Arrays.asList(pumaClass.getName(), PUMA_XFIELD_UID, uid.toLowerCase()), this.context);
        } catch (XWikiException e) {
            LOG.error("Fail to search for document containing puma uid [" + uid + "]", e);

            documentList = Collections.emptyList();
        }

        if (documentList.size() > 1) {
            LOG.error("There is more than one user profile for PUMA uid [" + uid + "]");
        }

        if (!documentList.isEmpty()) {
            doc = documentList.get(0);
        }

        return doc;
    }
}
