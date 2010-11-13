package org.xwiki.tools.reporter.publishers;

import org.xwiki.tools.reporter.Publisher;


public class StdoutPublisher implements Publisher
{
    public void publish(final String subject, final String content)
    {
        System.out.println(subject);
        System.out.println("------------------------------------------------------------------------");
        System.out.println(content);
    }
}
