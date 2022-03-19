package Utilities;

import java.util.InputMismatchException;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import entities.Event;
import entities.User;
import repositories.DataRepository;


public class EventReminderJob implements Job {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(EventReminderJob.class);

    public static final String EVENTNAME = "EVENTNAME";
    public static final String COUNT = "COUNT";
    public static final String REPO_USED = "REPO";
    public static final String EVENT_ID = "eventID";

    private String flag = "new object";

    private DataRepository repository;

    public void execute(JobExecutionContext context) throws JobExecutionException {


        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        Object eventIdObject = dataMap.get(EVENT_ID); //3
        repository = (DataRepository) dataMap.get(REPO_USED);
        //fetch parameters from JobDataMap
		String eventName = dataMap.getString(EVENTNAME);
        
		int count = dataMap.getInt(COUNT);
        JobKey jobKey = context.getJobDetail().getKey();
		System.out.println(jobKey+": "+ eventName+"-"+count+": flag="+flag);
        count++;
        //add next counter to JobDataMap
		dataMap.put(COUNT, count);
        flag= "object changed";

        LOGGER.info("send mail to each participant in the event: ");

        int eid = ((Number) eventIdObject).intValue();
            Event e = repository.findEventByID(eid);
            if(e == null) throw new InputMismatchException("the eid does not exist");
            for(String name : e.participantsList) {
                System.err.println("name is"+name);
                User user = repository.findUserByName(name);
                MailHelper.sendMail("reminder", "Upcoming event! "+ user.username + e.eventName + "excited?", new String[] {user.email});
            }

    }
}
