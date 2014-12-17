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
package com.xwiki.authentication.saml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnContextComparisonTypeEnumeration;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.EncryptedAttribute;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.NameIDPolicy;
import org.opensaml.saml2.core.RequestedAuthnContext;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml2.core.impl.AuthnRequestBuilder;
import org.opensaml.saml2.core.impl.IssuerBuilder;
import org.opensaml.saml2.core.impl.NameIDPolicyBuilder;
import org.opensaml.saml2.core.impl.RequestedAuthnContextBuilder;
import org.opensaml.security.SAMLSignatureProfileValidator;
import org.opensaml.xml.Configuration;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.parse.BasicParserPool;
import org.opensaml.xml.schema.impl.XSStringImpl;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureValidator;
import org.opensaml.xml.util.Base64;
import org.opensaml.xml.util.XMLHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.user.api.XWikiUser;
import com.xpn.xwiki.user.impl.xwiki.XWikiAuthServiceImpl;
import com.xpn.xwiki.web.XWikiRequest;
import com.xpn.xwiki.web.XWikiResponse;

/**
 * Authentication based on HTTP headers.
 * <p>
 * Some parameters can be used to customized its behavior in xwiki.cfg:
 * <ul>
 * <li>xwiki.authentication.headers.auth_field: if this header filed has any value the authentication is apply,
 * otherwise it's trying standard XWiki authentication. The default field is <code>{@value #DEFAULT_AUTH_FIELD}</code>.</li>
 * <li>xwiki.authentication.headers.id_field: the value in header containing the string to use when creating the XWiki
 * user profile page. The default field is the same as auth field.</li>
 * <li>xwiki.authentication.headers.fields_mapping: mapping between HTTP header values and XWiki user profile values.
 * The default mapping is <code>{@value #DEFAULT_FILEDS_MAPPING}.</code></li>
 * </ul>
 *
 * @version $Id$
 */
public class XWikiSAMLAuthenticator extends XWikiAuthServiceImpl
{
    /**
     * Logging tool.
     */
    private static final Log LOG = LogFactory.getLog(XWikiSAMLAuthenticator.class);

    private static final String DEFAULT_AUTH_FIELD = "saml_user";

    private static final String DEFAULT_ID_FIELD = "userPrincipalName";

    private static final String DEFAULT_FIELDS_MAPPING = "email=mail,first_name=givenName,last_name=sn";

    private static final String DEFAULT_XWIKI_USERNAME_RULE = "first_name,last_name";

    private static final String DEFAULT_XWIKI_USERNAME_RULE_CAPITALIZE = "1";

    private Map<String, String> userMappings;

    @Override
    public void showLogin(XWikiContext context) throws XWikiException
    {
        XWikiRequest request = context.getRequest();
        XWikiResponse response = context.getResponse();

        try {
            DefaultBootstrap.bootstrap();
        } catch (ConfigurationException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Failed to bootstrap saml module");
            }
            throw new XWikiException(XWikiException.MODULE_XWIKI_USER,
                XWikiException.ERROR_XWIKI_USER_INIT,
                "Failed to bootstrap saml module");
        }
        XMLObjectBuilderFactory builderFactory = org.opensaml.Configuration.getBuilderFactory();

        // Generate ID
        String randId = RandomStringUtils.randomAlphanumeric(42);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Random ID: " + randId);
        }

        String sourceurl = request.getParameter("xredirect");
        if (sourceurl == null) {
            if (context.getAction().startsWith("login")) {
                sourceurl = context.getWiki().getURL("Main.WebHome", "view", context);
            } else {
                context.getWiki();
                sourceurl = XWiki.getRequestURL(request).toString();
            }
        }

        request.getSession().setAttribute("saml_url", sourceurl);
        request.getSession().setAttribute("saml_id", randId);

        // Create an issuer Object
        IssuerBuilder issuerBuilder = new IssuerBuilder();
        Issuer issuer = issuerBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:assertion", "Issuer", "samlp");
        issuer.setValue(getSAMLIssuer(context));

        // Create NameIDPolicy
        NameIDPolicyBuilder nameIdPolicyBuilder = new NameIDPolicyBuilder();
        NameIDPolicy nameIdPolicy = nameIdPolicyBuilder.buildObject();
        nameIdPolicy.setFormat("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent");
        nameIdPolicy.setSPNameQualifier(getSAMLNameQualifier(context));
        nameIdPolicy.setAllowCreate(true);

        // Create AuthnContextClassRef
        AuthnContextClassRefBuilder authnContextClassRefBuilder = new AuthnContextClassRefBuilder();
        AuthnContextClassRef authnContextClassRef =
            authnContextClassRefBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:assertion",
                "AuthnContextClassRef", "saml");
        authnContextClassRef
            .setAuthnContextClassRef("urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport");

        // Create RequestedAuthnContext
        RequestedAuthnContextBuilder requestedAuthnContextBuilder = new RequestedAuthnContextBuilder();
        RequestedAuthnContext requestedAuthnContext =
            requestedAuthnContextBuilder.buildObject();
        requestedAuthnContext.setComparison(AuthnContextComparisonTypeEnumeration.EXACT);
        requestedAuthnContext.getAuthnContextClassRefs().add(authnContextClassRef);

        DateTime issueInstant = new DateTime();
        AuthnRequestBuilder authRequestBuilder = new AuthnRequestBuilder();
        AuthnRequest authRequest =
            authRequestBuilder.buildObject("urn:oasis:names:tc:SAML:2.0:protocol", "AuthnRequest", "samlp");
        authRequest.setForceAuthn(false);
        authRequest.setIsPassive(false);
        authRequest.setIssueInstant(issueInstant);
        authRequest.setProtocolBinding("urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST");
        authRequest.setAssertionConsumerServiceURL(getSAMLAuthenticatorURL(context));
        authRequest.setIssuer(issuer);
        authRequest.setNameIDPolicy(nameIdPolicy);
        authRequest.setRequestedAuthnContext(requestedAuthnContext);
        authRequest.setID(randId);
        authRequest.setVersion(SAMLVersion.VERSION_20);
        String stringRep = authRequest.toString();

        if (LOG.isDebugEnabled()) {
            LOG.debug("New AuthnRequestImpl: " + stringRep);
            LOG.debug("Assertion Consumer Service URL: " + authRequest.getAssertionConsumerServiceURL());
        }

        // Now we must build our representation to put into the html form to be submitted to the idp
        MarshallerFactory mfact = org.opensaml.Configuration.getMarshallerFactory();
        Marshaller marshaller = (Marshaller) mfact.getMarshaller(authRequest);
        if (marshaller == null) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Failed to get marshaller for " + authRequest);
            }
            throw new XWikiException(XWikiException.MODULE_XWIKI_USER,
                XWikiException.ERROR_XWIKI_USER_INIT,
                "Failed to get marshaller for " + authRequest);
        } else {
            Element authDOM;
            String samlRequest = "";
            try {
                authDOM = marshaller.marshall(authRequest);
                StringWriter rspWrt = new StringWriter();
                XMLHelper.writeNode(authDOM, rspWrt);
                String messageXML = rspWrt.toString();
                Deflater deflater = new Deflater(Deflater.DEFLATED, true);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream, deflater);
                deflaterOutputStream.write(messageXML.getBytes());
                deflaterOutputStream.close();
                samlRequest = Base64.encodeBytes(byteArrayOutputStream.toByteArray(), Base64.DONT_BREAK_LINES);
                String outputString = new String(byteArrayOutputStream.toByteArray());
                samlRequest = URLEncoder.encode(samlRequest);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Converted AuthRequest: " + messageXML);
                    // LOG.debug("samlRequest: " + samlRequest);
                }
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Failed to marshaller request for " + authRequest);
                }
                throw new XWikiException(XWikiException.MODULE_XWIKI_USER,
                    XWikiException.ERROR_XWIKI_USER_INIT,
                    "Failed to marshaller request for " + authRequest);
            }

            String actionURL = getSAMLAuthenticatorURL(context);
            String url = actionURL + "?SAMLRequest=" + samlRequest;
            if (LOG.isInfoEnabled()) {
                LOG.info("Saml request sent to " + url);
            }
            try {
                response.sendRedirect(url);
                context.setFinished(true);
            } catch (IOException e) {
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.user.impl.xwiki.AppServerTrustedAuthServiceImpl#checkSAMLResponse(com.xpn.xwiki.XWikiContext)
     */
    public boolean checkSAMLResponse(XWikiContext context) throws XWikiException
    {
        // read from SAMLResponse
        XWikiRequest request = context.getRequest();
        Map attributes = new HashMap();

        String samlResponse = request.getParameter("SAMLResponse");
        if (samlResponse == null) {
            return false;
        }

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Reading SAML Response");
            }
            samlResponse = new String(Base64.decode(samlResponse), "UTF-8");

            if (LOG.isDebugEnabled()) {
                LOG.debug("SAML Response is " + samlResponse);
            }

            // Get parser pool manager
            BasicParserPool ppMgr = new BasicParserPool();
            ppMgr.setNamespaceAware(true);
            Document inCommonMDDoc;

            inCommonMDDoc = ppMgr.parse(new StringReader(samlResponse));
            Element ResponseRoot = inCommonMDDoc.getDocumentElement();
            // Get apropriate unmarshaller
            UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
            Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(ResponseRoot);
            // Unmarshall using the document root element, an EntitiesDescriptor
            Response response = (Response) unmarshaller.unmarshall(ResponseRoot);

            // reading cert
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            String cert = getSAMLCertificate(context);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Verification signature using certificate " + cert);
            }
            InputStream sis = context.getEngineContext().getResourceAsStream(cert);
            X509Certificate certificate = (X509Certificate) cf.generateCertificate(sis);
            sis.close();

            response.validate(true);
            Signature signature = response.getSignature();
            SAMLSignatureProfileValidator pv = new SAMLSignatureProfileValidator();
            pv.validate(signature);
            BasicX509Credential credential = new BasicX509Credential();
            credential.setEntityCertificate(certificate);
            SignatureValidator sigValidator = new SignatureValidator(credential);
            sigValidator.validate(signature);

            Assertion subjectAssertion = null;
            boolean isValidDate = true;

            if (LOG.isDebugEnabled()) {
                LOG.debug("Reading SAML User data");
            }

            // Verify assertions
            for (Assertion a : response.getAssertions()) {
                // Find subject assertions
                if (a.getAuthnStatements().size() > 0) {
                    if (a.getConditions().getNotOnOrAfter().isBeforeNow()) {
                        isValidDate = false;
                    }
                }

                // Process all attributes
                for (AttributeStatement attStatement : a.getAttributeStatements()) {
                    for (Attribute att : attStatement.getAttributes()) {
                        for (XMLObject val : att.getAttributeValues()) {
                            attributes.put(att.getName(), ((XSStringImpl) val).getValue());
                        }
                    }
                    for (EncryptedAttribute att : attStatement.getEncryptedAttributes()) {
                        for (XMLObject val : ((Attribute) att).getAttributeValues()) {
                            attributes.put(((Attribute) att).getName(), ((XSStringImpl) val).getValue());
                        }
                    }
                }
            }

            String samlid1 = response.getInResponseTo();
            String samlid2 = (String) request.getSession().getAttribute("saml_id");
            if (isValidDate == false) {
                // invalid ID
                if (LOG.isErrorEnabled()) {
                    LOG.error("SAML Dates are invalid");
                }
                return false;
            }
            if (!samlid1.equals(samlid2)) {
                // invalid ID
                if (LOG.isErrorEnabled()) {
                    LOG.error("SAML ID do not match " + samlid1 + " " + samlid2);
                }
                return false;
            }
        } catch (Exception e1) {
            // failed to read SAMLResponse
            if (LOG.isErrorEnabled()) {
                LOG.error("Failed Reading SAML Response", e1);
            }
            return false;
        }

        // let's map the data
        Map<String, String> userData = getExtendedInformations(attributes, context);

        String nameID = (String) attributes.get(getIdFieldName(context));
        if (LOG.isDebugEnabled()) {
            LOG.debug("SAML ID is " + nameID);
            LOG.debug("SAML attributes are " + attributes);
            LOG.debug("SAML user data are " + userData);
        }

        String sql =
            "select distinct doc.fullName from XWikiDocument as doc, BaseObject as obj, StringProperty as nameidprop where doc.fullName=obj.name and obj.className='XWiki.SAMLAuthClass' and obj.id=nameidprop.id.id and nameidprop.id.name='nameid' and nameidprop.value='"
                + nameID + "'";
        List list = context.getWiki().search(sql, context);
        String validFullUserName = null;
        String validUserName = null;

        if (list.size() == 0) {
            // User does not exist. Let's generate a unique page name
            if (LOG.isDebugEnabled()) {
                LOG.debug("Did not find XWiki User. Generating it.");
            }
            String userName = generateXWikiUsername(userData, context);
            if (userName.equals("")) {
                userName = "user";
            }
            validUserName = context.getWiki().getUniquePageName("XWiki", userName, context);
            validFullUserName = "XWiki." + validUserName;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Generated XWiki User Name " + validFullUserName);
            }
        } else {
            validFullUserName = (String) list.get(0);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Found XWiki User " + validFullUserName);
            }
        }

        // we found a user or generated a unique user name
        if (validFullUserName != null) {
            // check if we need to create/update a user page
            String database = context.getDatabase();
            try {
                // Switch to main wiki to force users to be global users
                context.setDatabase(context.getMainXWiki());

                // test if user already exists
                if (!context.getWiki().exists(validFullUserName, context)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Need to create user " + validFullUserName);
                    }

                    // create user
                    userData.put("active", "1");

                    int result = context.getWiki().createUser(validUserName, userData, "XWiki.XWikiUsers",
                        "#includeForm(\"XWiki.XWikiUserSheet\")", "edit", context);
                    if (result < 0) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Failed to create user " + validFullUserName + " with code " + result);
                        }
                        return false;
                    }
                    XWikiDocument userDoc = context.getWiki().getDocument(validFullUserName, context);
                    BaseObject obj = userDoc.newObject("XWiki.SAMLAuthClass", context);
                    obj.set("nameid", nameID, context);
                    context.getWiki().saveDocument(userDoc, context);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("User " + validFullUserName + " has been successfully created");
                    }
                } else {
                    XWikiDocument userDoc = context.getWiki().getDocument(validFullUserName, context);
                    BaseObject userObj = userDoc.getObject("XWiki.XWikiUsers");
                    boolean updated = false;

                    for (Map.Entry<String, String> entry : userData.entrySet()) {
                        String field = entry.getKey();
                        String value = entry.getValue();
                        BaseProperty prop = (BaseProperty) userObj.get(field);
                        String currentValue =
                            (prop == null || prop.getValue() == null) ? null : prop.getValue().toString();
                        if (value != null && !value.equals(currentValue)) {
                            userObj.set(field, value, context);
                            updated = true;
                        }
                    }

                    if (updated == true) {
                        context.getWiki().saveDocument(userDoc, context);

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("User " + validFullUserName + " has been successfully updated");
                        }
                    }

                }
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Failed to create user " + validFullUserName, e);
                }
                return false;
            } finally {
                context.setDatabase(database);
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting authentication in session for user " + validFullUserName);
        }

        // mark that we have authenticated the user in the session
        context.getRequest().getSession().setAttribute(getAuthFieldName(context), validFullUserName);

        // need to redirect now
        try {
            String sourceurl = (String) request.getSession().getAttribute("saml_url");
            if (LOG.isDebugEnabled()) {
                LOG.debug("Redirecting after valid authentication to " + sourceurl);
            }
            context.getResponse().sendRedirect(sourceurl);
            context.setFinished(true);
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.user.impl.xwiki.AppServerTrustedAuthServiceImpl#checkAuth(com.xpn.xwiki.XWikiContext)
     */
    @Override
    public XWikiUser checkAuth(XWikiContext context) throws XWikiException
    {
        // check in the session if the user is already authenticated
        String samlUserName = (String) context.getRequest().getSession().getAttribute(getAuthFieldName(context));
        if (samlUserName == null) {
            // check if we have a SAML Response to verify
            if (checkSAMLResponse(context)) {
                return null;
            }

            // check standard authentication
            if (context.getRequest().getCookie("username") != null || context.getAction().equals("logout")
                || context.getAction().startsWith("login")) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Fallback to standard authentication");
                }
                return super.checkAuth(context);
            }

            return null;
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Found authentication of user " + samlUserName);
            }
            if (context.isMainWiki()) {
                return new XWikiUser(samlUserName);
            } else {
                return new XWikiUser(context.getMainXWiki() + ":" + samlUserName);
            }
        }
    }

    public String getValidUserName(String userName)
    {
        return userName.replace('.', '=').replace('@', '_');
    }

    /**
     * {@inheritDoc}
     *
     * @see com.xpn.xwiki.user.impl.xwiki.AppServerTrustedAuthServiceImpl#checkAuth(java.lang.String, java.lang.String,
     *      java.lang.String, com.xpn.xwiki.XWikiContext)
     */
    @Override
    public XWikiUser checkAuth(String username, String password, String rememberme, XWikiContext context)
        throws XWikiException
    {
        String auth = getAuthFieldValue(context);

        if ((auth == null) || auth.equals("")) {
            return super.checkAuth(context);
        } else {
            return checkAuth(context);
        }
    }

    private String getSAMLCertificate(XWikiContext context)
    {
        return context.getWiki().Param("xwiki.authentication.saml.cert");
    }

    private String getSAMLAuthenticatorURL(XWikiContext context)
    {
        return context.getWiki().Param("xwiki.authentication.saml.authurl");
    }

    private String getSAMLIssuer(XWikiContext context)
    {
        return context.getWiki().Param("xwiki.authentication.saml.issuer");
    }

    private String getSAMLNameQualifier(XWikiContext context)
    {
        return context.getWiki().Param("xwiki.authentication.saml.namequalifier");
    }

    private String getAuthFieldValue(XWikiContext context)
    {
        return (String) context.getRequest().getSession(true).getAttribute(getAuthFieldName(context));
    }

    private String getAuthFieldName(XWikiContext context)
    {
        return context.getWiki().Param("xwiki.authentication.saml.auth_field", DEFAULT_AUTH_FIELD);
    }

    private String getIdFieldName(XWikiContext context)
    {
        return context.getWiki().Param("xwiki.authentication.saml.id_field", DEFAULT_ID_FIELD);
    }

    private Map<String, String> getExtendedInformations(Map data, XWikiContext context)
    {
        Map<String, String> extInfos = new HashMap<String, String>();

        for (Map.Entry<String, String> entry : getFieldMapping(context).entrySet()) {
            String dataValue = (String) data.get(entry.getKey());

            if (dataValue != null) {
                extInfos.put(entry.getValue(), dataValue);
            }
        }

        return extInfos;
    }

    /**
     * @param context the XWiki context.
     * @return the fields to use to generate the xwiki user name
     */
    private String[] getXWikiUsernameRule(XWikiContext context)
    {
        String userFields =
            context.getWiki().Param("xwiki.authentication.saml.xwiki_user_rule", DEFAULT_XWIKI_USERNAME_RULE);
        return userFields.split(",");
    }

    /**
     * @param context the XWiki context.
     * @return true if the fields should be capitalized
     */
    private boolean getXWikiUsernameRuleCapitalization(XWikiContext context)
    {
        String capitalize =
            context.getWiki().Param("xwiki.authentication.saml.xwiki_user_rule_capitalize",
                DEFAULT_XWIKI_USERNAME_RULE_CAPITALIZE);
        return "1".equals(capitalize);
    }

    private String generateXWikiUsername(Map userData, XWikiContext context)
    {
        String[] userFields = getXWikiUsernameRule(context);
        boolean capitalize = getXWikiUsernameRuleCapitalization(context);
        String userName = "";

        for (String field : userFields) {
            String value = (String) userData.get(field);
            if (value != null && value.length() > 0) {
                if (capitalize) {
                    userName += value.substring(0, 1).toUpperCase() + ((value.length() > 1) ? value.substring(1) : "");
                } else {
                    userName += value;
                }
            }
        }
        return userName;
    }

    /**
     * @param context the XWiki context.
     * @return the mapping between HTTP header fields names and XWiki user profile fields names.
     */
    private Map<String, String> getFieldMapping(XWikiContext context)
    {
        if (this.userMappings == null) {
            this.userMappings = new HashMap<String, String>();

            String fieldMapping =
                context.getWiki().Param("xwiki.authentication.saml.fields_mapping", DEFAULT_FIELDS_MAPPING);

            String[] fields = fieldMapping.split(",");

            for (int j = 0; j < fields.length; j++) {
                String[] field = fields[j].split("=");
                if (2 == field.length) {
                    String xwikiattr = field[0].trim();
                    String headerattr = field[1].trim();

                    this.userMappings.put(headerattr, xwikiattr);
                } else {
                    LOG.error("Error parsing SAML fields_mapping attribute in xwiki.cfg: " + fields[j]);
                }
            }
        }

        return this.userMappings;
    }
}
