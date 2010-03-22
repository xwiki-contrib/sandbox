package org.xwiki.user.internal;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.observation.EventListener;
import org.xwiki.observation.event.Event;
import org.xwiki.user.RegistrationNotifierConfiguration;
import org.xwiki.user.event.UserCreationEvent;
import org.xwiki.user.event.UserValidationEvent;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.plugin.mailsender.MailSenderPluginApi;

@Component("registrationNotifier")
public class UserRegistrationNotifier implements EventListener
{

    @Requirement
    private Execution execution;

    @Requirement
    private RegistrationNotifierConfiguration configuration;

    public List<Event> getEvents()
    {
        return Arrays.<Event> asList(new UserValidationEvent(), new UserCreationEvent());
    }

    public String getName()
    {
        return "registrationNotifier";
    }

    @SuppressWarnings("unchecked")
    public void onEvent(Event event, Object source, Object data)
    {
        if (this.shouldProcessEvent(event) && configuration.getEmailAddressesToNotify().length > 0) {
            // Prepare velocity context for the mail
            VelocityContext vcontext = new VelocityContext();
            Map<String, String> dataMap = (Map<String, String>) data;
            vcontext.put("firstName", dataMap.get("firstName"));
            vcontext.put("lastName", dataMap.get("lastName"));
            vcontext.put("email", dataMap.get("email"));
            // Iterate over recipients and send emails
            for (String recipient : configuration.getEmailAddressesToNotify()) {
                getMailSenderApi().sendMessageFromTemplate(getAdminEmail(), recipient, null, null, "",
                    configuration.getEmailTemplateDocumentName(), vcontext);
            }
        }
    }

    private MailSenderPluginApi getMailSenderApi()
    {
        return (MailSenderPluginApi) getXWikiContext().getWiki().getPluginApi("mailsender", getXWikiContext());
    }

    private XWikiContext getXWikiContext()
    {
        return (XWikiContext) this.execution.getContext().getProperty("xwikicontext");
    }

    private String getAdminEmail()
    {
        return getXWikiContext().getWiki().getXWikiPreference("admin_email", getXWikiContext());
    }

    private boolean isEmailValidationMode()
    {
        return getXWikiContext().getWiki().getXWikiPreference("use_email_verification", getXWikiContext()).equals("1");
    }

    private boolean shouldProcessEvent(Event event)
    {
        if (this.isEmailValidationMode() && event instanceof UserValidationEvent 
            || !this.isEmailValidationMode() && event instanceof UserCreationEvent) {
            return true;
        }
        return false;
    }
}
