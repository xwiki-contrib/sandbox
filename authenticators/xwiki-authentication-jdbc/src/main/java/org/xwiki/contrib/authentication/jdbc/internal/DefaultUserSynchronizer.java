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
package org.xwiki.contrib.authentication.jdbc.internal;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.xwiki.component.annotation.Component;
import org.xwiki.context.Execution;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.classes.BaseClass;

@Component
@Singleton
public class DefaultUserSynchronizer implements UserSynchronizer
{
    private static final EntityReference USERCLASS_REFERENCE = new EntityReference("XWikiUsers", EntityType.DOCUMENT,
        new EntityReference("XWiki", EntityType.SPACE));

    @Inject
    private Execution execution;

    @Inject
    private Logger logger;

    @Inject
    private JDBCConfiguration configuration;

    private XWikiContext getXWikiContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty(XWikiContext.EXECUTIONCONTEXT_KEY);
    }

    @Override
    public Connection getConnection() throws SQLException
    {
        return DriverManager.getConnection(this.configuration.getConnectionURL(),
            this.configuration.getConnectionProperties());
    }

    // JDBC -> XWIki

    @Override
    public DocumentReference updateXWikiUser(String username, Map<String, Object> properties) throws XWikiException
    {
        XWikiContext context = getXWikiContext();

        // Synch user
        DocumentReference userReference = new DocumentReference(context.getDatabase(), "XWiki", username);

        String database = context.getDatabase();
        try {
            // Switch to main wiki to force users to be global users
            context.setDatabase(context.getMainXWiki());

            // test if user already exists
            if (!context.getWiki().exists(userReference, context)) {
                this.logger.debug("Need to create user " + username);

                // create user
                if (properties.containsKey("active")) {
                    properties.put("active", "1");
                }

                // Convert some properties
                Object nameProperty = properties.get("name");
                if (nameProperty != null && nameProperty instanceof String) {
                    String name = (String) nameProperty;

                    name = name.trim();
                    int index = name.indexOf(' ');
                    if (index != -1) {
                        properties.put("first_name", name.substring(0, index));
                        properties.put("last_name", name.substring(index + 1));
                    } else {
                        properties.put("first_name", name);
                    }
                }

                context.getWiki().createUser(username, properties, context);

                this.logger.debug("User " + username + " has been successfully created");
            } else {
                XWikiDocument userDocument = context.getWiki().getDocument(userReference, context);
                BaseObject userObject = userDocument.getXObject(USERCLASS_REFERENCE);

                BaseClass userClass = userObject.getXClass(context);

                Map<String, String> mapToSave = new HashMap<String, String>();
                for (Map.Entry<String, Object> entry : properties.entrySet()) {
                    String key = entry.getKey();
                    if (userClass.get(key) == null) {
                        continue;
                    }
                    String value = (String) entry.getValue();

                    String objValue = userObject.getStringValue(key);
                    if (objValue == null || !objValue.equals(value)) {
                        mapToSave.put(key, value);
                    }
                }

                if (!mapToSave.isEmpty()) {
                    userClass.fromMap(mapToSave, userObject);

                    context.getWiki().saveDocument(userDocument, "Synchronized user profile with JDBC informations",
                        true, context);
                }
            }
        } finally {
            context.setDatabase(database);
        }

        return userReference;
    }

    // XWiki -> JDBC

    private String getRequestPassword()
    {
        String password = getXWikiContext().getRequest().get("XWiki.XWikiUsers_0_password");
        if (password == null) {
            password = getXWikiContext().getRequest().get("register_password");
        }

        return password;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getXWikiUserProperties(XWikiDocument userDocument) throws NoSuchAlgorithmException,
        InvalidKeySpecException
    {
        Map<String, String> userProperties = new HashMap<String, String>();
        userProperties.put("login", userDocument.getDocumentReference().getName());
        String password = getRequestPassword();
        if (password != null) {
            userProperties.put("password", password);
            userProperties.put("passwordsha1base64", Base64.encodeBase64String(DigestUtils.sha1(password)));
        }

        // object fields
        BaseObject userObject = userDocument.getXObject(USERCLASS_REFERENCE);

        for (BaseProperty<EntityReference> property : (Collection<BaseProperty<EntityReference>>) userObject
            .getFieldList()) {
            if (!property.getName().equals("password")) {
                userProperties.put(property.getName(), property.toText());
            }
        }

        // name
        String firstName = userProperties.get("first_name");
        String lastName = userProperties.get("last_name");
        if (StringUtils.isNotEmpty(firstName)) {
            if (StringUtils.isNotEmpty(lastName)) {
                userProperties.put("name", firstName + ' ' + lastName);
            } else {
                userProperties.put("name", firstName);
            }
        } else if (StringUtils.isNotEmpty(lastName)) {
            userProperties.put("name", lastName);
        }

        return userProperties;
    }

    private void writeQuery(XWikiDocument userDocument, String type) throws Exception
    {
        this.configuration.checkDriver();

        String query = this.configuration.getQuery(type);

        if (query == null) {
            throw new Exception("Query should be provided for type [" + type + "]");
        }

        String[] queryParameters = this.configuration.getQueryParameters(type);

        Connection connection = getConnection();
        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(query);

            Map<String, String> fields = getXWikiUserProperties(userDocument);

            int index = 1;
            for (String parameter : queryParameters) {
                statement.setString(index++, fields.get(parameter));
            }

            statement.executeUpdate();
        } finally {
            if (statement != null) {
                statement.close();
            }
            connection.close();
        }
    }

    @Override
    public void insertJDBCUser(XWikiDocument userDocument) throws Exception
    {
        writeQuery(userDocument, "insert");
    }

    @Override
    public void deleteJDBCUser(XWikiDocument userDocument) throws Exception
    {
        writeQuery(userDocument, "delete");
    }

    @Override
    public void updateJDBCUser(XWikiDocument userDocument) throws Exception
    {
        writeQuery(userDocument, "update");
    }
}
