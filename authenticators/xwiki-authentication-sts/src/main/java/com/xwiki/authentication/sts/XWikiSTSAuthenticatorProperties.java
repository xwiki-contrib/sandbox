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
package com.xwiki.authentication.sts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;

class XWikiSTSAuthenticatorProperties {
	 static Log log = LogFactory.getLog(XWikiSTSAuthenticator.class);
	 
	 String getAuthURL(XWikiContext context) {
		String url = context.getWiki().Param("xwiki.authentication.sts.authurl");
		log.trace("getAuthUR(): " + url);
		return url;
	}

	 String getIdField(XWikiContext context) {
		String idField = context.getWiki().Param("xwiki.authentication.sts.id_field");
		log.trace("getIdField(): " + idField);
		return idField;
	}

	 String getAuthField(XWikiContext context) {
		String field = context.getWiki().Param("xwiki.authentication.sts.auth_field");
		log.trace("getAuthField(): " + field);
		return field;
	}
	 String getWtrealm(XWikiContext context) {
		String wtrealm = context.getWiki().Param("xwiki.authentication.sts.wtrealm");
		log.trace("getWtrealm(): " + wtrealm);
		return wtrealm;
	}

	 String getWreplyHost(XWikiContext context) {
		String wreply = context.getWiki().Param("xwiki.authentication.sts.wreply_host");
		log.trace("getWreplyHost(): " + wreply);
		return wreply;
	}

	 String getWreplyPage(XWikiContext context) {
		String wreply = context.getWiki().Param("xwiki.authentication.sts.wreply_page");
		log.trace("getWreplyPage(): " + wreply);
		return wreply;
	}

	 String getWctx(XWikiContext context) {
		String wctx = context.getWiki().Param("xwiki.authentication.sts.wctx");
		log.trace("getWctx(): " + wctx);
		return wctx;
	}

	 String getWct(XWikiContext context) {
		String wct = context.getWiki().Param("xwiki.authentication.sts.wct");
		log.trace("getWct(): " + wct);
		return wct;
	}

	 String getWfresh(XWikiContext context) {
		String wfresh = context.getWiki().Param("xwiki.authentication.sts.wfresh");
		log.trace("getWfresh(): " + wfresh);
		return wfresh;
	}
	
	 String getIssuer(XWikiContext context) {
		String val = context.getWiki().Param("xwiki.authentication.sts.issuer");
		log.trace("getIssuer(): " + val);
		return val;
	}	
	 String getEntityId(XWikiContext context) {
		String val = context.getWiki().Param("xwiki.authentication.sts.entity_id");
		log.trace("getEntityId(): " + val);
		return val;
	}		
	 String getIssuerDN(XWikiContext context) {
		String val = context.getWiki().Param("xwiki.authentication.sts.issuer_dn");
		log.trace("getIssuerDN(): " + val);
		return val;
	}	
	 String getSubjectDNs(XWikiContext context) {
		String val = context.getWiki().Param("xwiki.authentication.sts.subject_dns");
		log.trace("getSubjectDNs(): " + val);
		return val;
	}		
	 String getAudienceURIs(XWikiContext context) {
		String val = context.getWiki().Param("xwiki.authentication.sts.audience_uris");
		log.trace("getAudienceURIs(): " + val);
		return val;
	}	
	
	 String getDataFormat(XWikiContext context) {
		String format = context.getWiki().Param("xwiki.authentication.sts.data_format");
		log.trace("getDataFormat(): " + format);
		return format;
	}
	
	 String getUsernameRule(XWikiContext context) {
		String val = context.getWiki().Param("xwiki.authentication.sts.xwiki_username_rule");
		log.trace("getUsernameRule(): " + val);
		return val;
	}	
	 String getFieldMapping(XWikiContext context) {
		String val = context.getWiki().Param("xwiki.authentication.sts.field_mapping");
		log.trace("getFieldMapping(): " + val);
		return val;
	}	
	
	
	

}
