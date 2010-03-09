package org.xwiki.user.internal;

import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.user.RegistrationNotifierConfiguration;


@Component
public class DefaultRegistrationNotifierConfiguration implements RegistrationNotifierConfiguration
{
    /**
     * Common prefix for all registration notifier property keys.
     */
    private static final String KEY_PREFIX = "registrationNotifier.";

    /**
     * Default email template for the notification email.
     */
    private static final String DEFAULT_MAIL_TEMPLATE = "XWiki.RegistrationNotificationMail";

    @Requirement
    private ConfigurationSource configurationSource;

    /**
     * {@inheritDoc}
     */
    public String[] getEmailAddressesToNotify()
    {
        String addressesAsString = this.configurationSource.getProperty(KEY_PREFIX + "emailAddresses", new String());
        if (StringUtils.isBlank(addressesAsString)) {
            return new String[0];
        }
        return addressesAsString.split(",");
    }

    /**
     * {@inheritDoc}
     */
    public String getEmailTemplateDocumentName()
    {
        return this.configurationSource.getProperty(KEY_PREFIX + "emailTemplate", DEFAULT_MAIL_TEMPLATE);
    }

}
