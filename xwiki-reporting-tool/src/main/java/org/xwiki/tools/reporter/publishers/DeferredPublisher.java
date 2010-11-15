package org.xwiki.tools.reporter.publishers;

import java.util.List;
import java.util.ArrayList;

import org.xwiki.tools.reporter.Publisher;


public class DeferredPublisher implements Publisher
{
    private final List<String[]> subjectsAndContents = new ArrayList<String[]>();

    private final Publisher publisher;

    public DeferredPublisher(final Publisher publisher)
    {
        this.publisher = publisher;
    }

    public void publish(final String subject, final String content)
    {
        this.subjectsAndContents.add(new String[] {subject, content});
    }

    public void run()
    {
        if (subjectsAndContents.size() < 2) {
            if (subjectsAndContents.size() < 1) {
                return;
            }
            this.publisher.publish(subjectsAndContents.get(0)[0], subjectsAndContents.get(0)[1]);
            return;
        }

        final StringBuilder subjectBuilder = new StringBuilder();
        final StringBuilder contentBuilder = new StringBuilder();

        for (String[] subjectAndContent : subjectsAndContents) {
            subjectBuilder.append(subjectAndContent[0]);
            subjectBuilder.append(" ");
            contentBuilder.append(subjectAndContent[1]);
            contentBuilder.append("\n");
        }
        this.publisher.publish(subjectBuilder.toString().trim(), contentBuilder.toString().trim());
    }
}
