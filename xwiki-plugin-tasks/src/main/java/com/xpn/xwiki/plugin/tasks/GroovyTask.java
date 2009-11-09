package com.xpn.xwiki.plugin.tasks;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.objects.BaseObject;

public class GroovyTask implements Job {
	private static Log logger = LogFactory.getLog(GroovyTask.class);
	
	public void execute(JobExecutionContext context) throws JobExecutionException {
		try {
			JobDataMap data = context.getJobDetail().getJobDataMap();
			XWiki xwiki = (XWiki) data.get("xwiki");
			XWikiContext xwikiContext = (XWikiContext) data.get("context");
			int task = data.getInt("task");
						
			Binding binding = new Binding(data.getWrappedMap());
			GroovyShell shell = new GroovyShell(binding);
			BaseObject object = xwikiContext.getDoc().getObject(TasksPlugin.TASK_CLASS, task);
			shell.evaluate(object.getLargeStringValue("script"));
		} catch (CompilationFailedException e) {
			throw new JobExecutionException(e);
		}
	}
}