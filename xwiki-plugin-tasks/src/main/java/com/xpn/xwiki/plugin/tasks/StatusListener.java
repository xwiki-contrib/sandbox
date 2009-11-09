/**
 * ===================================================================
 *
 * Copyright (c) 2003 Ludovic Dubost, All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details, published at
 * http://www.gnu.org/copyleft/gpl.html or in gpl.txt in the
 * root folder of this distribution.
 *
 * Created by
 * User: Timothée Peignier
 * Date: 16 aug. 2005
 * 
 */
package com.xpn.xwiki.plugin.tasks;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;

public class StatusListener implements SchedulerListener, JobListener {
	private static Log log = LogFactory.getLog(StatusListener.class);

	public void jobScheduled(Trigger trigger) {
		log.info("Task '" + trigger.getJobName() + "' scheduled");
	}

	public void jobUnscheduled(String name, String group) {
		log.info("Task '" + name + "' unscheduled");
	}

	public void triggerFinalized(Trigger trigger) {
	}

	public void triggersPaused(String trigger, String group) {
	}

	public void triggersResumed(String trigger, String group) {
	}

	public void jobsPaused(String name, String group) {
		log.info("Task '" + name + "' paused");
	}

	public void jobsResumed(String name, String group) {
		log.info("Task '" + name + "' resumed");
	}
	
	public void schedulerError(String message, SchedulerException error) {
		log.error(message, error);
	}

	public void schedulerShutdown() {
		log.warn("Scheduler is shutting down");
	}
	
	public String getName() {
		return "StatusListener";
	}

	public void jobToBeExecuted(JobExecutionContext context) {
		log.info("Task '" + context.getJobDetail().getName() + "' is about to be executed");
	}

	public void jobExecutionVetoed(JobExecutionContext context) {
	}

	public void jobWasExecuted(JobExecutionContext context, JobExecutionException e) {
		log.info("Task '" + context.getJobDetail().getName() + "' executed : " + e);
	}
}
