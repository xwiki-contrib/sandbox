package com.xpn.xwiki.plugin.tasks;

import java.text.ParseException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.api.Api;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.classes.BaseClass;
import com.xpn.xwiki.plugin.XWikiDefaultPlugin;
import com.xpn.xwiki.plugin.XWikiPluginInterface;

public class TasksPlugin extends XWikiDefaultPlugin implements XWikiPluginInterface {
	private static Log logger = LogFactory.getLog(TasksPlugin.class);
	
	protected static Scheduler scheduler;
	private JobDataMap data = new JobDataMap();
	
	protected static final String TASK_CLASS = "XWiki.Task";
	protected static final String TASK_NAME = "Task";
	protected static final String TASK_WEB = "XWiki";
	
	public TasksPlugin(String name, String className, XWikiContext context) {
		super(name, className, context);
		init(context);
	}

	protected BaseClass getTaskClass(XWikiContext context) throws XWikiException {
		XWikiDocument doc;
		XWiki xwiki = context.getWiki();
		boolean needsUpdate = false;

		try {
			doc = xwiki.getDocument(TASK_CLASS, context);
		} catch (Exception e) {
			doc = new XWikiDocument();
			doc.setWeb(TASK_WEB);
			doc.setName(TASK_NAME);
			needsUpdate = true;
		}

		BaseClass bclass = doc.getxWikiClass();
		bclass.setName(TASK_CLASS);
		needsUpdate |= bclass.addTextField("taskName", "Task Name", 30);
		needsUpdate |= bclass.addTextField("taskClass", "Task Class", 30);
		needsUpdate |= bclass.addTextField("status", "Status", 30);
		needsUpdate |= bclass.addTextField("cron", "Cron Expression", 30);
		needsUpdate |= bclass.addTextAreaField("script", "Groovy Script", 45, 10);

		if (needsUpdate)
			xwiki.saveDocument(doc, context);
		return bclass;
	}
	
	private static synchronized Scheduler getSchedulerInstance() throws SchedulerException {
		if (scheduler == null) {
			scheduler = StdSchedulerFactory.getDefaultScheduler();
		}
		return scheduler;
	}
	
	private void setStatusListener() throws SchedulerException {
		StatusListener listener = new StatusListener();
		scheduler.addSchedulerListener(listener);
		scheduler.addGlobalJobListener(listener);
	}

	public void init(XWikiContext context) {
		try {
			getTaskClass(context);
			scheduler = getSchedulerInstance();
			setStatusListener();
			scheduler.start();
		} catch (XWikiException e) {
			e.printStackTrace();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
				
	public void pauseTask(String taskName) {
		try {
			scheduler.pauseJob(taskName, Scheduler.DEFAULT_GROUP);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	public void resumeTask(String taskName) {
		try {
			scheduler.resumeJob(taskName, Scheduler.DEFAULT_GROUP);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	public boolean scheduleTask(BaseObject object, XWikiContext context) {
		boolean scheduled = true;
		try {
			String task = String.valueOf(object.getNumber());
			
			JobDetail job = new JobDetail(task, Scheduler.DEFAULT_GROUP, Class.forName(object.getStringValue("taskClass")), true, false, true);
			Trigger trigger = new CronTrigger(task, Scheduler.DEFAULT_GROUP, task, Scheduler.DEFAULT_GROUP, object.getStringValue("cron"));
			
			data.put("task", object.getNumber());
			data.put("context", context);
			data.put("xwiki", context.getWiki());
			job.setJobDataMap(data);
			
			scheduler.addJob(job, true);
			int state = scheduler.getTriggerState(task, Scheduler.DEFAULT_GROUP);
			switch (state) {
				case Trigger.STATE_PAUSED:
					object.setStringValue("status", "Paused");
					break;
				case Trigger.STATE_NORMAL:
					if (getTrigger(task).compareTo(trigger) != 0)
						logger.debug("Reschedule Task : " + object.getStringValue("taskName"));
						scheduler.rescheduleJob(trigger.getName(), trigger.getGroup(), trigger);
					object.setStringValue("status", "Scheduled");
					break;
				case Trigger.STATE_NONE:
					logger.debug("Schedule Task : " + object.getStringValue("taskName"));
					scheduler.scheduleJob(trigger);
					logger.info("XWiki Task Status :"+ object.getStringValue("status"));
					if(object.getStringValue("status").equals("Paused")) {
						scheduler.pauseJob(task, Scheduler.DEFAULT_GROUP);
						object.setStringValue("status", "Paused");
					} else {
						object.setStringValue("status", "Scheduled");
					}
					break;
				default:
					logger.debug("Schedule Task : " + object.getStringValue("taskName"));
					scheduler.scheduleJob(trigger);
					object.setStringValue("status", "Scheduled");
					break;
			}
		} catch (SchedulerException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return scheduled;
	}
	
	public void unscheduleTask(String taskName) {
		try {
			scheduler.deleteJob(taskName, Scheduler.DEFAULT_GROUP);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}
	
	public Trigger getTrigger(String task) {
		Trigger trigger = null;
		try {
			trigger = scheduler.getTrigger(task, Scheduler.DEFAULT_GROUP);
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
		return trigger;
	}
	
	public Date getNextFireTime(String task) {
		return getTrigger(task).getNextFireTime();
	}
	
	public Api getPluginApi(XWikiPluginInterface plugin, XWikiContext context) {
        return new TasksPluginApi((TasksPlugin) plugin, context);
    }
	
	public String getName() {
        return "tasks";
    }
	
	public void flushCache() {
    }
	
	public void virtualInit(XWikiContext context) {
		init(context);
    }	
}