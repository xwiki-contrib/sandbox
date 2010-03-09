package org.xwiki.user;

import org.xwiki.component.annotation.ComponentRole;

@ComponentRole
public interface RegistrationNotifierConfiguration
{
    String[] getEmailAddressesToNotify();
    
    String getEmailTemplateDocumentName();
}
