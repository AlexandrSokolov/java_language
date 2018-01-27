package com.savdev.scheduling.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.inject.Inject;

public class QuartzSchedulerJob implements Job {

    @Inject
    MainService mainService;

    @Override
    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        mainService.start();
    }
}
