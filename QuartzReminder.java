package Utilities;

import org.quartz.Trigger;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import com.cronutils.builder.CronBuilder;
import com.cronutils.model.Cron;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.field.expression.FieldExpressionFactory;
import com.cronutils.model.field.value.SpecialChar;

import static com.cronutils.model.field.expression.FieldExpressionFactory.*;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.SchedulerMetaData;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ReminderAPI.Reminder;
import entities.Event;
import entities.User;
import repositories.DataRepository;

public class QuartzReminder implements Reminder {

    /**
     * Case insensitive Logger constant used to pops up in the console.
     * <p> This logger works as it use the JobImpl as a place to show Logger in console window. </p>
     * 
     * {@link org.slf4j.Logger;}
     * 
     * @see Logger
     * @see QuartzReminder
     */
    private static final Logger logger = LoggerFactory.getLogger(QuartzReminder.class);
    
    SchedulerFactory schedulerFactory = new StdSchedulerFactory("src/main/resources/quartz.properties");  
    private Scheduler scheduler;

    /**
     * it use {@literal PostgresAdapter} to instantiate on DataRepository which is an interface class
     * 
     * @see DataRepository
     */
    private DataRepository repo; // instantiate this at constructor
    
    /**
     * <p>Constructor</p>
     * @param repo
     * @throws Exception
     */
    public QuartzReminder(DataRepository repo) throws Exception {
        if(repo == null)
            throw new Exception("no reop");
        this.repo = repo;
        scheduler= schedulerFactory.getScheduler();
        scheduler.start();

    }

    /**
     * <p>Using for shutting down scheduler</p>
     * @throws SchedulerException
     */
    public void shutdown() throws SchedulerException {
        scheduler.shutdown(true);
    }

    /**
     * <p>Helper function for building the materials for designing the scheduler.</p>
     * 
     * @param eid
     * @param startAt
     * @throws SchedulerException
     */
    public void sendNotification(int eid, Instant startAt) throws SchedulerException {
        // collect all user that needs to be sent a reminder
        // only an outline
        scheduler = getScheduler();

        JobDetail jobDetail = buildJobDetail(eid);
        Trigger trigger = buildJobTrigger(jobDetail, startAt);
        scheduler.scheduleJob(jobDetail, trigger);
        logger.error("job scheduled");
    
        // try {
        //     // run for 292 billion years
        //     Thread.sleep(Long.MAX_VALUE);
        //     // executing...
        // } catch(Exception e) {
        //     logger.info("Interrupted");
        //     Thread.currentThread().interrupt();
        //     e.printStackTrace();
        // }
        // logger.info("Sleep finished");

        // scheduler.shutdown(true);
    }   

    /**
     * <p>helper function for unscheduling the running event.</p>
     * 
     * @param scheduler
     * @return void
     * @throws SchedulerException
     */
    public boolean UnScheduler(Scheduler scheduler) throws SchedulerException {
        return scheduler.unscheduleJob(buildTriggerKey("myTrigger", "myTriggerGroup"));
    }

    /**
     * <p>Helper function for building the trigger key.</p>
     * 
     * @param myTrigger
     * @param myTriggerGroup
     * @return void
     */
    public TriggerKey buildTriggerKey(String myTrigger, String myTriggerGroup) {
        return TriggerKey.triggerKey(myTrigger, myTriggerGroup);
    }

    /**
     * <p>Implemented function on the basis of the job builder and the job data map between the key and assign the value for the job.
     *      {@link JobDataMap}
     * </p>
     * <p>Build the job by mapping the key value from the job to the implemented function.</p>
     * <code>buildJobDetail</code> is used to instantiate {@link JobDetail}s.
     * <p>
     * Use this helper method <code>buildJobDetail</code> to implement 
     * <code>{@link org.quartz.JobDetail}</code> return
     * <code>{@link org.quartz.Job}</code>, and which is implemented by
     * <code>{@link org.quartz.JobBuilder}</code>.
     * </p>
     * 
     * <p>
     * Return the description given to the <code>JobDetail</code> instance by its
     * creator (if any).
     * </p>
	 * @param 
	 * @return JobDetail <code>{@link JobDetail}</code>.
     */
    private JobDetail buildJobDetail(int eventID) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(EventReminderJob.COUNT, 1);
        jobDataMap.put(EventReminderJob.EVENTNAME, "vc123");
        jobDataMap.put(EventReminderJob.REPO_USED,repo);
        jobDataMap.put(EventReminderJob.EVENT_ID, eventID);

        return JobBuilder.newJob(EventReminderJob.class)
            .withIdentity(((Integer)eventID).toString())
            .requestRecovery(false)
            .withDescription("Send Email for Upcoming Event")
            .usingJobData(jobDataMap)
            .storeDurably()
            .build();
    }

    /**
     * <code>Trigger</code> is used to instantiate {@link Trigger}s.
     * <p>
     * Return the description given to the <code>Job</code> instance by its
     * creator (if any).
     * </p>
     * 
     * @see JobDetail <code>{@link JobDetail}</code>.
     * @see Instant <code>{@link java.time.instant}</code>.
     * @return Trigger <code>{@link Trigger}</code>.
     */
    private Trigger buildJobTrigger(JobDetail jobDetail, Instant startAt) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "eventID?????")
                .withDescription("Send Email Trigger")
                .startAt(Date.from(Instant.from(startAt)))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }

    /**
     * <code>TriggerBuilder</code> is used to instantiate {@link Trigger}s.
     * <p>
     * Return the description given to the <code>Job</code> instance by its
     * creator (if any).
     * </p>
     * 
     * <pre>
     *      Trigger trigger = buildJobTrigger(jobDetail, cron);
     * </pre>
     * 
     * @see JobDetail <code>{@link JobDetail}</code>.
     * @see Cron <code>{@link com.cronutils.model.Cron}</code>.
     * @return Trigger <code>{@link Trigger}</code>.
     */
    private Trigger buildJobTrigger(JobDetail jobDetail, Cron cron) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(),"group2")
                .withDescription("triggerDescription")
                .withSchedule(CronScheduleBuilder.cronSchedule(cron.asString()))
                .build();
    }

    /**
     * <p>Helper function for scheduler</p>
     * 
     * @return void
     * @throws SchedulerException
     */
    public Scheduler getScheduler() throws SchedulerException {
        return schedulerFactory.getScheduler();
    }

    /**
     * <p>Helper function for counting the number of scheduling event(job) using <code>SchedulerMetaData</code> </p>
     *
     * @return an instance of SchedulerMetaData by {@code SchedulerMetaData}
     * @throws SchedulerException
     */
    public SchedulerMetaData getSchedulerMetaData() throws SchedulerException {
        return getScheduler().getMetaData();
    }

    /**
     * <p>Sample builder make the builder of the cron{@code Cron} much more easier</p>
     * 
     * <p>Client code can then use the DSL to write code such as this:{@link CronDefinition}</p>
     * <pre>
     *      CronDefinition cronDefinition = defineOwnCronDefinition();
     * </pre>
     * 
     * @return cronDefinition sample by simply calling {@code CronDefinition}
     */
    private static CronDefinition defineOwnCronDefinition() {
        // define your own cron: arbitrary fields are allowed and last field can be optional
        return CronDefinitionBuilder.defineCron().withSeconds().and().withMinutes().and().withHours().and().withDayOfMonth()
            .supportsHash().supportsL().supportsW().and().withMonth().and().withDayOfWeek().withIntMapping(7, 0) 
            // we
            // support
            // non-standard
            // non-zero-based
            // numbers!
            .supportsHash().supportsL().supportsW().and().withYear().optional().and().instance();
      }

      /**
     * Returns an Instant that is offset by a number of days from now.
     *
     * @param offsetInDays integer number of days to offset by
     * @return an Instant offset by {@code offsetInDays} days
     */
    public static Instant getInstantDaysOffsetFromNow(long offsetInDays) {
        return Instant.now().plus(Duration.ofDays(offsetInDays));
    }


    /**
     * Returns an Instant that is offset by a number of days before now.
     *
     * @param offsetInDays integer number of days to offset by
     * @return an Instant offset by {@code offsetInDays} days
     */
    public static Instant getInstantDaysOffsetBeforeNow(long offsetInDays) {
        return Instant.now().minus(Duration.ofDays(offsetInDays));
    }

    /**
     * <p> get current time by putting the region underlying type is {@code String}you want to set the time zone of that place.</p>
     * 
     * @param region
     * @return the date time of the zone, which is the passing argument is region type {@code String}. Also return type is {@link Date}
     */
    public static ZonedDateTime getCurrentTimeByZoneId(String region) {
        ZoneId zone = ZoneId.of(region);
        ZonedDateTime date = ZonedDateTime.now(zone);
        return date;
    }

    public ZonedDateTime convertZonedDateTime(ZonedDateTime sourceDate, String destZone) {

        ZoneId destZoneId = ZoneId.of(destZone);
        ZonedDateTime destDate = sourceDate.withZoneSameInstant(destZoneId);

        return destDate;
    }

    /**
     * Formats a datetime stamp from an {@code instant} using a formatting pattern.
     *
     * <p>Note: a formatting pattern containing 'a' (for the period; AM/PM) is treated differently at noon/midday.
     * Using that pattern with a datetime whose time falls on "12:00 PM" will cause it to be formatted as "12:00 NOON".</p>
     *
     * @param instant  the instant to be formatted
     * @param timeZone the time zone to compute local datetime
     * @param pattern  formatting pattern, see Oracle docs for DateTimeFormatter for pattern table
     * @return the formatted datetime stamp string
     */
    public static String formatInstant(Instant instant, String timeZone, String pattern) {
        if (instant == null || timeZone == null || pattern == null) {
            return "";
        }
        ZonedDateTime zonedDateTime = getCurrentTimeByZoneId(timeZone);
        String processedPattern = pattern;
        if (zonedDateTime.getHour() == 12 && zonedDateTime.getMinute() == 0) {
            processedPattern = pattern.replace("a", "'NOON'");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(processedPattern);
        return zonedDateTime.format(formatter);
    }

    /**
     * 
     * <p>Return type {@link Cron}.</p>
     * <p>Since this must require the Instant instance as the passing argument</p>
     * <p>This function is made followed the basic constructor builder that {@literal Cron}.</p>
     * 
     * @param starttime
     * @return cron builder sample with the format is UTC is the time zone and also include some basic features that the time of the calendar should have.
     * @throws Exception
     */
    private Cron CronBuilder(Instant starttime) throws Exception {
        
        ZonedDateTime reminder = starttime.atZone(ZoneId.of("UTC"));
        int year = reminder.getYear();
        int month = reminder.getMonthValue();
        // DayOfWeek dow = reminder.getDayOfWeek();
        // int DoW = dow.getValue();
        int DoM = reminder.getDayOfMonth();
        int hour = reminder.getHour();
        int min = reminder.getMinute();
        int sec = reminder.getSecond();

        CronDefinition cronDefinition = defineOwnCronDefinition();
        return CronBuilder.cron(cronDefinition).withYear(FieldExpressionFactory.on(year))
                    .withDoM(between(SpecialChar.L, DoM))
                    .withMonth(on(month))
                    .withDoW(questionMark())
                    .withHour(on(hour))
                    .withMinute(on(min))
                    .withSecond(on(sec))
                    .instance();
    }

    @Override
    public void sendMailBefore5Min(User user, Event event) throws Exception {

        logger.info("preparing to fire alarm for reminding clients");
        Instant schedTime = Instant.from(event.date).minusSeconds(5*60);
        sendNotification(event.eventID, schedTime);

    }

    @Override
    public void sendMailBefore10Min(User user, Event event) throws Exception {

        logger.info("preparing to fire alarm for reminding clients");
        Instant schedTime = Instant.from(event.date).minus(10, ChronoUnit.MINUTES);
        sendNotification(event.eventID, schedTime);
    }

    @Override
    public void sendMailBefore15Min(User user, Event event) throws Exception {

        logger.info("preparing to fire alarm for reminding clients");
        Instant schedTime = Instant.from(event.date).minus(15, ChronoUnit.MINUTES);
        sendNotification(event.eventID, schedTime);
    }

    @Override
    public void sendMailBefore30Min(User user, Event event) throws Exception {

        logger.info("preparing to fire alarm for reminding clients");
        Instant schedTime = Instant.from(event.date).minus(30, ChronoUnit.MINUTES);
        sendNotification(event.eventID, schedTime);
    }

    @Override
    public void sendMailBefore1Hour(User user, Event event) throws Exception {

        logger.info("preparing to fire alarm for reminding clients");
        Instant schedTime = Instant.from(event.date).minus(1, ChronoUnit.HOURS);
        sendNotification(event.eventID, schedTime);
    }

    /**
     *
     * <p> We use this function to help for sending mail with specific time, since this function is quite simple for sending mail to person 
     *     using event ID to pass into function then the function itself will send to people with gmail address </p>
     * 
     * @param eid event identification
     * @return void
     * @throws none
     */
    @Override
    public void sendMail(int eid) {

        try {
            Event e = repo.findEventByID(eid);
            
            logger.info("----------Initializing-----------");

            scheduler = getScheduler();

            logger.info("----------Initialization Complete-------------");

            logger.info("----------Scheduling jobs------------");

            JobDetail jobDetail = buildJobDetail(eid);
            Cron cron = CronBuilder(e.date);
            Trigger trigger = buildJobTrigger(jobDetail, cron);

            scheduler.scheduleJob(jobDetail, trigger);

            scheduler.start();

            logger.info("--------Started scheduler----------");

            try {
                // run for 292 billion years
                Thread.sleep(Long.MAX_VALUE);
                // executing...
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
                ex.printStackTrace();
            }

            logger.info("--------Shutting down--------------");

            scheduler.shutdown(true); 

            logger.info("--------Shutdown complete-----------");

        } catch (SchedulerException e) {
            logger.error("Scheduler throw exception", e);
        } catch (Throwable e) {
            logger.error("Fails", e);
        }
        
    }

    @Override
    public void sendMailBefore3Days(User user, Event event) throws Exception {

        logger.info("preparing to fire alarm for reminding clients");
        Instant schedTime = Instant.from(event.date).minus(3, ChronoUnit.DAYS);
        sendNotification(event.eventID, schedTime);
    }

    @Override
    public void sendMailBefore1Week(User user, Event event) throws Exception {
        
        logger.info("preparing to fire alarm for reminding clients");
        Instant schedTime = Instant.from(event.date).minus(1, ChronoUnit.WEEKS);
        sendNotification(event.eventID, schedTime);
    }

}
