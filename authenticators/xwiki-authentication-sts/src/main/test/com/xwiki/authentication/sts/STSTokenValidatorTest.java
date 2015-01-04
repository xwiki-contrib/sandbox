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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.io.FileUtils;
import com.xwiki.authentication.sts.STSClaim;
import com.xwiki.authentication.sts.STSTokenValidator;

import junit.framework.Assert;
import junit.framework.TestCase;

public class STSTokenValidatorTest extends TestCase {
	private static Log log = LogFactory.getLog(STSTokenValidatorTest.class);
	static File testFile;
	static STSTokenValidator validator;
	static String context;
	static String issuer;
	static String testToken;
	static String entityId;
	static String issuerDN;
	static List<String> subjectDNs;
	static List<URI> audienceUris;
	static boolean validateExpiration;
	static int maxClockSkew=60000;

	public STSTokenValidatorTest(String name) throws Exception {
		super(name);
		validator = new STSTokenValidator();
		subjectDNs = new ArrayList<String>();
		audienceUris = new ArrayList<URI>();

		// Common test settings
		testFile = new File("testToken.xml");
		subjectDNs
				.add("EMAILADDRESS=cisu.help@vraa.gov.lv, CN=IVIS.LVP.STS, OU=VPISD, O=VRAA, L=Riga, ST=Riga, C=LV");
		audienceUris.add(new URI("http://85.254.250.27/xwiki"));
		entityId = "http://www.latvija.lv/trust";
		issuer="http://www.latvija.lv/trust";
		issuerDN = "CN=IVIS Root CA";
		context="jVJD9au1VeqdOX4UjCD4FRXsHGUwTynkP4UJvNAdxK";

	}

	public void testPosValidation() throws Exception {
		// Current settings
		testFile = new File("testToken.xml");
		validator.setSubjectDNs(subjectDNs);
		validator.setAudienceUris(audienceUris);
		testToken = FileUtils.readFileToString(testFile);
		validator.setEntityId(entityId);
		validator.setIssuerDN(issuerDN);
		validator.setIssuer(issuer);
		validator.setContext(context);
		validator.setValidateExpiration(false);
		validator.setMaxClockSkew(maxClockSkew);
		logSettings();
		// Validate token
		List<STSClaim> claims = validator.validate(testToken);
		log.info("Validation passed. Claims: " + claims.size());
		for (int i = 0; i < claims.size(); i++) {
			log.debug("claim " + claims.get(i).getClaimType() + ' ' + claims.get(i).getClaimValues());
		}
		log.info("testPosValidation passed");
	}

	public void testNegBadSignature() throws Exception {
		// Current settings
		File tamperedFile = new File("tamperedToken.xml");
		testToken = FileUtils.readFileToString(tamperedFile);
		// Validate token
		List<STSClaim> claims = null;
		try {
			claims = validator.validate(testToken);
			log.error("testNegBadSignature failed");
		} catch (Exception e) {
			Assert.assertEquals(claims, null);
			Assert.assertEquals("Invalid signature", e.getMessage());
			log.info("testNegBadSignature passed");
		} finally {
		// Renew default settings
		testFile = new File("testToken.xml");
		testToken = FileUtils.readFileToString(tamperedFile);
		}
	}

	public void testNegWrongSubjectDNs() throws Exception {
		// Current settings
		List<String> wrongIssuers = new ArrayList<String>();
		wrongIssuers.add("Wrong Issuer");
		validator.setSubjectDNs(wrongIssuers);
		// Validate token
		List<STSClaim> claims = null;
		try {
			claims = validator.validate(testToken);
			log.error("testNegWrongSubjectDNs failed");
		} catch (STSException e) {
			Assert.assertEquals(claims, null);
			Assert.assertEquals("Wrong token SubjectDN", e.getMessage());
			log.info("testNegWrongSubjectDNs passed");
		}finally {
		validator.setSubjectDNs(subjectDNs);
		}
	}

	public void testNegWrongAudience() throws Exception {
		// Current settings
		List<URI> wrongAudienceUris = new ArrayList<URI>();
		wrongAudienceUris.add(new URI("http://Wrong/Audience"));
		validator.setAudienceUris(wrongAudienceUris);
		// Validate token
		List<STSClaim> claims = null;
		try {
			claims = validator.validate(testToken);
			log.error("testNegWrongAudience failed");
		} catch (STSException e) {
			Assert.assertEquals(claims, null);
			Assert.assertEquals("The token applies to an untrusted audience: http://85.254.250.27/xwiki",
					e.getMessage());
			log.info("testNegWrongAudience passed");
		}finally {
		validator.setAudienceUris(audienceUris);
		}
	}

	public void testNegWrongEntityId() throws Exception {
		// Current settings
		validator.setEntityId("WrongEntityId");
		// Validate token
		List<STSClaim> claims = null;
		try {
			claims = validator.validate(testToken);
			log.error("testNegWrongEntityId failed");
		} catch (STSException e) {
			Assert.assertEquals(claims, null);
			Assert.assertEquals("Invalid signature", e.getMessage());
			log.info("testNegWrongEntityId passed");
		}finally {
		validator.setEntityId(entityId);
		}
	}

	public void testNegWrongIssuerDN() throws Exception {
		// Current settings
		validator.setIssuerDN("WrongIssuerDN");
		// Validate token
		List<STSClaim> claims = null;
		try {
			claims = validator.validate(testToken);
			log.error("testNegWrongIssuerDN failed");
		} catch (STSException e) {
			Assert.assertEquals(claims, null);
			Assert.assertEquals("Wrong token IssuerDN", e.getMessage());
			log.info("testNegWrongIssuerDN passed");
		}finally {
		validator.setIssuerDN(issuerDN);
		}
	}
	
	public void testNegWrongDate() throws Exception {
		// Current settings
		validator.setValidateExpiration(true);
		// Validate token
		List<STSClaim> claims = null;
		try {
			claims = validator.validate(testToken);
			log.error("testNegWrongDate failed");
		} catch (STSException e) {
			Assert.assertEquals(claims, null);
			Assert.assertEquals("Token Created or Expires elements have been expired", e.getMessage());
			log.info("testNegWrongDate passed");
		}finally {
		validator.setValidateExpiration(false);
		}
	}
	public void testNegWrongContext() throws Exception {
		// Current settings
		validator.setContext("WrongContext");
		// Validate token
		List<STSClaim> claims = null;
		try {
			claims = validator.validate(testToken);
			log.error("testNegWrongContext failed");
		} catch (STSException e) {
			Assert.assertEquals(claims, null);
			Assert.assertEquals("Wrong token Context. Suspected: WrongContext got: jVJD9au1VeqdOX4UjCD4FRXsHGUwTynkP4UJvNAdxK", e.getMessage());
			log.info("testNegWrongContext passed");
		}finally {
		validator.setContext(context);
		}
	}
	public void testNegWrongIssuer() throws Exception {
		// Current settings
		validator.setIssuer("WrongIssuer");
		// Validate token
		List<STSClaim> claims = null;
		try {
			claims = validator.validate(testToken);
			log.error("testNegWrongIssuer failed");
		} catch (STSException e) {
			Assert.assertEquals(claims, null);
			Assert.assertEquals("Wrong token Issuer", e.getMessage());
			log.info("testNegWrongIssuer passed");
		}finally {
		validator.setIssuer(issuer);
		}
	}

	public void logSettings() {
		log.info("Test file: " + testFile.getAbsolutePath());
		log.info("Context: "+ context);
		log.info("Issuer: "+issuer);
		log.info("EntityID: " + entityId);
		log.info("IssuerDN: " + issuerDN);
		log.debug("============= Test token ===========\n" + testToken + "\n=============");
		log.info("AudienceUris: " + audienceUris);
		log.info("TrustedSubjectDNs: " + subjectDNs);
	}

}