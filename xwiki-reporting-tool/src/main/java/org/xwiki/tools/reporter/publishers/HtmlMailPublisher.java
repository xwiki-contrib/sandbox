package org.xwiki.tools.reporter.publishers;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.xwiki.tools.reporter.Publisher;

import com.xpn.xwiki.plugin.mailsender.MailSenderPlugin;
import com.xpn.xwiki.plugin.mailsender.Mail;
import com.xpn.xwiki.plugin.mailsender.MailConfiguration;


public class HtmlMailPublisher implements Publisher
{
    private final MailSenderPlugin mailPlugin;

    private final List<String> recipients = new ArrayList<String>();

    private final MailConfiguration mailConfig = new MailConfiguration();

    public HtmlMailPublisher()
    {
        this.mailPlugin = new MailSenderPlugin("", "", null);
    }

    public void addRecipient(final String to)
    {
        this.recipients.add(to);
    }

    /**
     * Valid map entries are:
     * port - (int) port number to use for sending. Default: 25
     * host - (String) what computer to send to. Default: localhost
     * from - (String) return email address. Default: i.did.not.edit.the.config@localhost.localdomain
     * smtpUsername - (String) user name to log in to the email server with. Default: <none>
     * smtpPassword - (String) password to log in to the email server with. Default: <none>
     * javamailExtraProperties - (String) additional properties to set for the mailer.
     *     Note: The full list of available properties that we can set is defined here:
     *     http://java.sun.com/products/javamail/javadocs/com/sun/mail/smtp/package-summary.html
     */
    public void setMailConfig(Map<String, String> configMap)
    {
        if (configMap.get("from") == null) {
            this.mailConfig.setFrom("i.did.not.edit.the.config@localhost.localdomain");
        } else {
            this.mailConfig.setFrom(configMap.get("from"));
        }
        if (configMap.get("port") != null) {
            this.mailConfig.setPort(Integer.parseInt(configMap.get("port")));
        }
        if (configMap.get("host") != null) {
            this.mailConfig.setHost(configMap.get("host"));
        }
        if (configMap.get("smtpUsername") != null) {
            this.mailConfig.setSmtpUsername(configMap.get("smtpUsername"));
        }
        if (configMap.get("smtpPassword") != null) {
            this.mailConfig.setSmtpPassword(configMap.get("smtpPassword"));
        }
        if (configMap.get("javamailExtraProperties") != null) {
            this.mailConfig.setExtraProperties(configMap.get("javamailExtraProperties"));
        }
    }

    public void publish(final String subject, final String content)
    {
        final List<Mail> messages = new ArrayList<Mail>();
        for (String recipient : recipients) {
            messages.add(new Mail(this.mailConfig.getFrom(), recipient, null, null, subject, "", content));
        }
        try {
            this.mailPlugin.sendMails(messages, this.mailConfig, null);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
