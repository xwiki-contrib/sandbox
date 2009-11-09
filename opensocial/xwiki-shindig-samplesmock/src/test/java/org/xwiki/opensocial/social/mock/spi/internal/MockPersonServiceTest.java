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
 */
package org.xwiki.opensocial.social.mock.spi.internal;

import org.apache.shindig.auth.AnonymousSecurityToken;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.social.core.model.NameImpl;
import org.apache.shindig.social.opensocial.model.Name;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.jmock.Mock;
import org.xwiki.bridge.DocumentAccessBridge;
import org.xwiki.component.descriptor.DefaultComponentDescriptor;
import org.xwiki.opensocial.social.mock.model.PersonXW;
import org.xwiki.opensocial.social.mock.spi.MockXWikiComponent;
import org.xwiki.opensocial.social.mock.spi.internal.MockPersonService;
import org.xwiki.test.AbstractXWikiComponentTestCase;

public class MockPersonServiceTest extends AbstractXWikiComponentTestCase
{
    private Mock mockDocumentAccessBridge;

    /**
     * {@inheritDoc}
     * 
     * @see org.xwiki.test.AbstractXWikiComponentTestCase#registerComponents()
     */
    @Override
    protected void registerComponents() throws Exception
    {
        this.mockDocumentAccessBridge = mock(DocumentAccessBridge.class);
        DefaultComponentDescriptor<DocumentAccessBridge> descriptor =
            new DefaultComponentDescriptor<DocumentAccessBridge>();
        descriptor.setRole(DocumentAccessBridge.class);
        getComponentManager().registerComponent(descriptor,
            (DocumentAccessBridge) this.mockDocumentAccessBridge.proxy());
    }

    /**
     * @throws Exception
     */
    public void testGetPersonBasic() throws Exception
    {
        Person expected = new PersonXW();
        expected.setId("XWiki.Julia");
        Name name = new NameImpl();
        name.setGivenName("Julia");
        name.setFamilyName("Doe");
        expected.setName(name);

        String userId = "XWiki.Julia";
        UserId uid = new UserId(UserId.Type.userId, userId);
        SecurityToken token = new AnonymousSecurityToken();

        MockPersonService personService =
            (MockPersonService) getComponentManager().lookup(MockXWikiComponent.class, "MockPersonService");
        Person actual = personService.getPerson(uid, null, token).get();

        assertEquals(expected.getId(), actual.getId());
        assertNotNull(actual.getName());
        assertEquals(expected.getName().getGivenName(), actual.getName().getGivenName());
        assertEquals(expected.getName().getFamilyName(), actual.getName().getFamilyName());

    }

}
