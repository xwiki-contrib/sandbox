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
 * 
 * Part of the code in this file is copied from: https://github.com/auth10/auth10-java
 * which is based on Microsoft libraries in: https://github.com/WindowsAzure/azure-sdk-for-java-samples. 
 * 
 */

package com.xwiki.authentication.sts;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.common.SignableSAMLObject;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.security.CriteriaSet;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityTestHelper;
import org.opensaml.xml.security.credential.CollectionCredentialResolver;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.security.criteria.EntityIDCriteria;
import org.opensaml.xml.security.keyinfo.KeyInfoCredentialResolver;
import org.opensaml.xml.security.keyinfo.KeyInfoHelper;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.opensaml.xml.signature.KeyInfo;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.impl.ExplicitKeySignatureTrustEngine;
import org.opensaml.xml.validation.ValidationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

@SuppressWarnings("deprecation")
public class STSTokenValidator {
	private static Log log = LogFactory.getLog(STSTokenValidator.class);
	private int maxClockSkew;
	private List<String> trustedSubjectDNs;
	private List<URI> audienceUris;
	private boolean validateExpiration = true;
	private static String entityId;
	private String issuerDN;
	private String context;
	private String issuer;

	public STSTokenValidator() throws ConfigurationException {
		this(new ArrayList<String>(), new ArrayList<URI>());
	}

	public STSTokenValidator(List<String> trustedSubjectDNs, List<URI> audienceUris) throws ConfigurationException {
		super();
		this.trustedSubjectDNs = trustedSubjectDNs;
		this.audienceUris = audienceUris;
		DefaultBootstrap.bootstrap();
	}

	public void setSubjectDNs(List<String> subjectDNs) {
		this.trustedSubjectDNs = subjectDNs;
	}

	public void setAudienceUris(List<URI> audienceUris) {
		this.audienceUris = audienceUris;
	}

	public void setValidateExpiration(boolean value) {
		this.validateExpiration = value;
	}

	public void setEntityId(String value) {
		entityId = value;
	}

	public List<STSClaim> validate(String envelopedToken) throws ParserConfigurationException, SAXException, IOException,
			STSException, ConfigurationException, CertificateException, KeyException, SecurityException,
			ValidationException, UnmarshallingException, URISyntaxException, NoSuchAlgorithmException {

		SignableSAMLObject samlToken;
		boolean trusted = false;

		// Check token metadata
		if (envelopedToken.contains("RequestSecurityTokenResponse")) {
			samlToken = getSamlTokenFromRstr(envelopedToken);
		} else {
			samlToken = getSamlTokenFromSamlResponse(envelopedToken);
		}

		log.debug("\n===== envelopedToken ========\n" + samlToken.getDOM().getTextContent() + "\n==========");
        String currentContext=getAttrVal(envelopedToken, "trust:RequestSecurityTokenResponse", "Context");
		if (!context.equals(currentContext)) {
			throw new STSException("Wrong token Context. Suspected: " + context + " got: " + currentContext);
		}

		if (this.validateExpiration) {
			Instant created = new Instant(getElementVal(envelopedToken, "wsu:Created"));
			Instant expires = new Instant(getElementVal(envelopedToken, "wsu:Expires"));
			if (!checkExpiration(created, expires)) {
				throw new STSException("Token Created or Expires elements have been expired");
			}
		}

		if (!issuer.equals(getAttrVal(envelopedToken, "saml:Assertion", "Issuer"))) {
			throw new STSException("Wrong token Issuer");
		}

		// Check SAML assertions
		if (!validateIssuerDN(samlToken, issuerDN)) {
			throw new STSException("Wrong token IssuerDN");
		}

		for (String subjectDN : this.trustedSubjectDNs) {
			trusted |= validateSubjectDN(samlToken, subjectDN);
		}

		if (!trusted) {
			throw new STSException("Wrong token SubjectDN");
		}

		String address = null;
		if (samlToken instanceof org.opensaml.saml1.core.Assertion) {
			address = getAudienceUri((org.opensaml.saml1.core.Assertion) samlToken);
		}

		URI audience = new URI(address);

		boolean validAudience = false;
		for (URI audienceUri : audienceUris) {
			validAudience |= audience.equals(audienceUri);
		}

		if (!validAudience) {
			throw new STSException(String.format("The token applies to an untrusted audience: %s",
					new Object[] { audience }));
		}

		List<STSClaim> claims = null;
		if (samlToken instanceof org.opensaml.saml1.core.Assertion) {
			claims = getClaims((org.opensaml.saml1.core.Assertion) samlToken);
		}

		if (this.validateExpiration) {
			if (samlToken instanceof org.opensaml.saml1.core.Assertion) {
				Instant notBefore = ((org.opensaml.saml1.core.Assertion) samlToken).getConditions().getNotBefore()
						.toInstant();
				Instant notOnOrAfter = ((org.opensaml.saml1.core.Assertion) samlToken).getConditions()
						.getNotOnOrAfter().toInstant();
				if (!checkExpiration(notBefore, notOnOrAfter)) {
					throw new STSException("Token SAML Conditions: NotBefore or NotOnOrAfter has been expired");
				}
			}
		}
		
		// Check token certificate and signature
		boolean valid = validateToken(samlToken);
		if (!valid) {
			throw new STSException("Invalid signature");
		}

		return claims;
	}

	private static SignableSAMLObject getSamlTokenFromSamlResponse(String samlResponse)
			throws ParserConfigurationException, SAXException, IOException, UnmarshallingException {
		Document document = getDocument(samlResponse);

		Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(
				document.getDocumentElement());
		org.opensaml.saml2.core.Response response = (org.opensaml.saml2.core.Response) unmarshaller.unmarshall(document
				.getDocumentElement());
		SignableSAMLObject samlToken = (SignableSAMLObject) response.getAssertions().get(0);

		return samlToken;
	}

	private static SignableSAMLObject getSamlTokenFromRstr(String rstr) throws ParserConfigurationException,
			SAXException, IOException, UnmarshallingException, STSException {
		Document document = getDocument(rstr);

		String xpath = "//*[local-name() = 'Assertion']";

		NodeList nodes = null;

		try {
			nodes = org.apache.xpath.XPathAPI.selectNodeList(document, xpath);
		} catch (TransformerException e) {
			e.printStackTrace();
		}

		if (nodes.getLength() == 0) {
			throw new STSException("SAML token was not found");
		}

		Element samlTokenElement = (Element) nodes.item(0);
		Unmarshaller unmarshaller = Configuration.getUnmarshallerFactory().getUnmarshaller(samlTokenElement);
		SignableSAMLObject samlToken = (SignableSAMLObject) unmarshaller.unmarshall(samlTokenElement);

		return samlToken;
	}

	private static String getAudienceUri(org.opensaml.saml1.core.Assertion samlAssertion) {
		org.opensaml.saml1.core.Audience audienceUri = samlAssertion.getConditions().getAudienceRestrictionConditions()
				.get(0).getAudiences().get(0);
		return audienceUri.getUri();
	}

	private boolean checkExpiration(Instant notBefore, Instant notOnOrAfter) {
		Instant now = new Instant();
		Duration skew = new Duration(maxClockSkew);
		log.debug("Time expiration. Now:" + now + " now+sqew: " + now.plus(skew) + " now-sqew: " + now.minus(skew)
				+ " notBefore: " + notBefore + " notAfter: " + notOnOrAfter);
		if (now.plus(skew).isAfter(notBefore) && now.minus(skew).isBefore(notOnOrAfter)) {
			log.debug("Time is in range");
			return true;
		}
		return false;
	}

	private static boolean validateToken(SignableSAMLObject samlToken) throws SecurityException, ValidationException,
			ConfigurationException, UnmarshallingException, CertificateException, KeyException {

		// Validate XML structure
		samlToken.validate(true);

		Signature signature = samlToken.getSignature();
		KeyInfo keyInfo = signature.getKeyInfo();
		X509Certificate certificate = (X509Certificate) KeyInfoHelper.getCertificates(keyInfo).get(0);

		// Certificate data
		if (log.isDebugEnabled()) {
			log.debug("certificate issuerDN: " + certificate.getIssuerDN());
			log.debug("certificate issuerUniqueID: " + certificate.getIssuerUniqueID());
			log.debug("certificate issuerX500Principal: " + certificate.getIssuerX500Principal());
			log.debug("certificate notBefore: " + certificate.getNotBefore());
			log.debug("certificate notAfter: " + certificate.getNotAfter());
			log.debug("certificate serialNumber: " + certificate.getSerialNumber());
			log.debug("certificate sigAlgName: " + certificate.getSigAlgName());
			log.debug("certificate sigAlgOID: " + certificate.getSigAlgOID());
			log.debug("certificate signature: " + new String(certificate.getSignature()));
			log.debug("certificate issuerX500Principal: " + certificate.getIssuerX500Principal().toString());
			log.debug("certificate publicKey: " + certificate.getPublicKey());
			log.debug("certificate subjectDN: " + certificate.getSubjectDN());
			log.debug("certificate sigAlgOID: " + certificate.getSigAlgOID());
			log.debug("certificate version: " + certificate.getVersion());
		}

		BasicX509Credential cred = new BasicX509Credential();
		cred.setEntityCertificate(certificate);

		// Credential data
		cred.setEntityId(entityId);
		if (log.isDebugEnabled()) {
			log.debug("cred entityId: " + cred.getEntityId());
			log.debug("cred usageType: " + cred.getUsageType());
			log.debug("cred credentalContextSet: " + cred.getCredentalContextSet());
			log.debug("cred hashCode: " + cred.hashCode());
			log.debug("cred privateKey: " + cred.getPrivateKey());
			log.debug("cred publicKey: " + cred.getPublicKey());
			log.debug("cred secretKey: " + cred.getSecretKey());
			log.debug("cred entityCertificateChain: " + cred.getEntityCertificateChain());
		}

		ArrayList<Credential> trustedCredentials = new ArrayList<Credential>();
		trustedCredentials.add(cred);

		CollectionCredentialResolver credResolver = new CollectionCredentialResolver(trustedCredentials);
		KeyInfoCredentialResolver kiResolver = SecurityTestHelper.buildBasicInlineKeyInfoResolver();
		ExplicitKeySignatureTrustEngine engine = new ExplicitKeySignatureTrustEngine(credResolver, kiResolver);

		CriteriaSet criteriaSet = new CriteriaSet();
		criteriaSet.add(new EntityIDCriteria(entityId));

		Base64 decoder = new Base64();
		// In trace mode write certificate in the file
		if (log.isTraceEnabled()) {
			String certEncoded = new String(decoder.encode(certificate.getEncoded()));
			try {
				FileUtils.writeStringToFile(new File("/tmp/Certificate.cer"), "-----BEGIN CERTIFICATE-----\n"
						+ certEncoded + "\n-----END CERTIFICATE-----");
				log.trace("Certificate file was saved in: /tmp/Certificate.cer");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return engine.validate(signature, criteriaSet);
	}

	private static boolean validateSubjectDN(SignableSAMLObject samlToken, String subjectName)
			throws UnmarshallingException, ValidationException, CertificateException {

		Signature signature = samlToken.getSignature();
		KeyInfo keyInfo = signature.getKeyInfo();
		X509Certificate pubKey = KeyInfoHelper.getCertificates(keyInfo).get(0);

		String issuer = pubKey.getSubjectDN().getName();
		return issuer.equals(subjectName);
	}

	private static boolean validateIssuerDN(SignableSAMLObject samlToken, String issuerName)
			throws UnmarshallingException, ValidationException, CertificateException {

		Signature signature = samlToken.getSignature();
		KeyInfo keyInfo = signature.getKeyInfo();
		X509Certificate pubKey = KeyInfoHelper.getCertificates(keyInfo).get(0);
		String issuer = pubKey.getIssuerDN().getName();
		return issuer.equals(issuerName);
	}

	private static List<STSClaim> getClaims(org.opensaml.saml1.core.Assertion samlAssertion) throws SecurityException,
			ValidationException, ConfigurationException, UnmarshallingException, CertificateException, KeyException {

		ArrayList<STSClaim> claims = new ArrayList<STSClaim>();

		List<org.opensaml.saml1.core.AttributeStatement> attributeStmts = samlAssertion.getAttributeStatements();

		for (org.opensaml.saml1.core.AttributeStatement attributeStmt : attributeStmts) {
			List<org.opensaml.saml1.core.Attribute> attributes = attributeStmt.getAttributes();

			for (org.opensaml.saml1.core.Attribute attribute : attributes) {
				String claimType = attribute.getAttributeNamespace() + "/" + attribute.getAttributeName();
				String claimValue = getValueFrom(attribute.getAttributeValues());
				claims.add(new STSClaim(claimType, claimValue));
			}
		}

		return claims;
	}

	private static String getValueFrom(List<XMLObject> attributeValues) {

		StringBuffer buffer = new StringBuffer();

		for (XMLObject value : attributeValues) {
			if (buffer.length() > 0)
				buffer.append(',');
			buffer.append(value.getDOM().getTextContent());
		}

		return buffer.toString();
	}

	private static Document getDocument(String doc) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder documentbuilder = factory.newDocumentBuilder();
		return documentbuilder.parse(new InputSource(new StringReader(doc)));
	}

	private String getAttrVal(String envelopedToken, String element, String attribute)
			throws ParserConfigurationException, SAXException, IOException {
		Document doc = getDocument(envelopedToken);
		String val = doc.getElementsByTagName(element).item(0).getAttributes().getNamedItem(attribute).getNodeValue();
		return val;
	}

	private String getElementVal(String envelopedToken, String element) throws ParserConfigurationException,
			SAXException, IOException {
		Document doc = getDocument(envelopedToken);
		String val = doc.getElementsByTagName(element).item(0).getTextContent();
		return val;
	}

	public void setIssuerDN(String issuerDN) {
		this.issuerDN = issuerDN;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public void setMaxClockSkew(int maxClockSkew) {
		this.maxClockSkew = maxClockSkew;
	}
}
