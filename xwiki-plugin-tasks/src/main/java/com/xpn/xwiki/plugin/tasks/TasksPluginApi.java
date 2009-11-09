package com.xpn.xwiki.plugin.tasks;

import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.api.Context;
import com.xpn.xwiki.api.Document;
import com.xpn.xwiki.api.Object;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

public class TasksPluginApi extends Api {
	private static Log logger = LogFactory.getLog(TasksPluginApi.class);

	private TasksPlugin plugin;

	public TasksPluginApi(TasksPlugin plugin, XWikiContext context) {
		super(context);
		init(plugin, context);
	}

	private void init(TasksPlugin plugin, XWikiContext context) {
		setPlugin(plugin);
	}

	public void pauseTask(String number, Document document) {
		pauseTask(document.getObject("XWiki.Task", Integer.valueOf(number).intValue()), document);
	}

	public void pauseTask(Object object, Document document) {
		pauseTask(object.getXWikiObject(), document.getDocument());
	}

	public void pauseTask(BaseObject object, XWikiDocument document) {
		try {
			plugin.pauseTask(String.valueOf(object.getNumber()));
			saveStatus("Paused", object, document);
			logger.debug("Pause Task : " + object.getStringValue("taskName"));
		} catch (XWikiException e) {
			e.printStackTrace();
		}
	}

	public void scheduleTask(BaseObject object, XWikiContext context) {
		plugin.scheduleTask(object, context);
	}

	public void scheduleTasks(Document document, Context context) {
		try {
			Vector objects = document.getObjects(TasksPlugin.TASK_CLASS);
			for (Iterator iterator = objects.iterator(); iterator.hasNext();) {
				Object object = (Object) iterator.next();
				scheduleTask(object.getXWikiObject(), context.getContext());
			}
			saveDocument(document.getDocument());
		} catch (XWikiException e) {
			e.printStackTrace();
		}
	}

	public void resumeTask(String number, Document document) {
		resumeTask(document.getObject("XWiki.Task", Integer.valueOf(number).intValue()), document);
	}

	public void resumeTask(Object object, Document document) {
		resumeTask(object.getXWikiObject(), document.getDocument());
	}

	public void resumeTask(BaseObject object, XWikiDocument document) {
		try {
			plugin.resumeTask(String.valueOf(object.getNumber()));
			saveStatus("Scheduled", object, document);
			logger.debug("Resume Task : " + object.getStringValue("taskName"));
		} catch (XWikiException e) {
			e.printStackTrace();
		}
	}

	public void unscheduleTask(String number, Document document) {
		unscheduleTask(document.getObject("XWiki.Task", Integer.valueOf(number).intValue()), document);
	}

	public void unscheduleTask(Object object, Document document) {
		unscheduleTask(object.getXWikiObject(), document.getDocument());
	}

	public void unscheduleTask(BaseObject object, XWikiDocument document) {
		try {
			Vector objects = document.getObjects(TasksPlugin.TASK_CLASS);
			objects.set(object.getNumber(), null);
			document.addObjectsToRemove(object);
			context.getWiki().saveDocument(document, context);
			plugin.unscheduleTask(String.valueOf(object.getNumber()));
			logger.debug("Delete Task : " + object.getStringValue("taskName"));
		} catch (XWikiException e) {
			e.printStackTrace();
		}
	}

	public Date getNextFireTime(Object object) {
		return plugin.getNextFireTime(String.valueOf(object.getNumber()));
	}

	public TasksPlugin getPlugin() {
		return plugin;
	}

	private void saveStatus(String status, BaseObject object, XWikiDocument document) throws XWikiException {
		object.setStringValue("status", status);
		saveDocument(document);
	}

	private void saveDocument(XWikiDocument document) throws XWikiException {
		context.getWiki().saveDocument(document, context);
	}

	public void setPlugin(TasksPlugin plugin) {
		this.plugin = plugin;
	}
}