package com.savdev.scheduling.quartz;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

@Singleton
@Startup
public class SchedulingStarterService {

    public static final String TRIGGER_NAME = "testTriggerName";
    public static final String JOB_NAME = "testJobName";
    public static final String JOB_GROUP_NAME = "testJobGroupName";

    @Inject
    CronJobConfig cronJobConfig;

    @PostConstruct
    void init(){
        JobDetail job = JobBuilder.newJob(QuartzSchedulerJob.class)
                .withIdentity(JOB_NAME, JOB_GROUP_NAME).build();
        try {

            Trigger trigger = TriggerBuilder
                    .newTrigger()
                    .withIdentity(TRIGGER_NAME, JOB_GROUP_NAME)
                    .withSchedule(
                            CronScheduleBuilder.cronSchedule(
                                    cronJobConfig.cronjobConfiguration()))
                    .build();

            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();
            scheduler.scheduleJob(job, trigger);

        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }
}
