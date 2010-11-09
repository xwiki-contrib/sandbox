package org.xwiki.tools.reporter.publishers;

import org.xwiki.tools.reporter.Publisher;


public class StdoutPublisher implements Publisher
{
    public void publish(final String content)
    {
        System.out.println(content);
    }
}
