package ReminderAPI;

import entities.Event;
import entities.User;

/**
 * Convenient for using or implementing the code with interface class
 * 
 * <p>
 * Reminder does not store an actual instance of <code>JobImpl</code> class, but
 * instead allows you to define an instance of one, through the use of a <code>Reminder</code>.
 * </p>
 * 
 * <p>
 * <code>JobImpl</code> have implemented this interface<code>{@link Reminder}</code>
 *  for the convenience.
 * </p>
 * 
 * @see Event
 * @see User
 * @see #sendMail(int)
 * 
 * @author Nguyen Tuan Ngoc
 */
public interface Reminder {

    /**
     * send mail with message before 3 days by organizer.
     * 
     * <p> This selection is for somebody want a long preparation for upcoming event. </p>
     * 
     * @param
     * @return void
     * @throws Exception
     */
    void sendMailBefore1Week(User user, Event event) throws Exception;

    /**
     * send mail with message before 3 days by organizer.
     * 
     * <p> This selection is for somebody want a preparation for upcoming event. </p>
     * 
     * @param
     * @return void
     * @throws Exception
     */
    void sendMailBefore3Days(User user, Event event) throws Exception;

    /**
     * send mail with message at specific time by organizer 
     */
    void sendMail(int eid);

    /**
     * send mail with message before 1 hour by organizer 
     * 
     * <p> This selection is for somebody who wants to have a short preparation </p>
     * 
     * @param
     * @return void
     * @throws Exception
     */
    void sendMailBefore1Hour(User user, Event event) throws Exception;

    /*
     * send mail with message before 30 minutes by organizer 
     */
    void sendMailBefore30Min(User user, Event event) throws Exception;

    /*
     * send mail with message before 15 minutes by organizer 
     */
    void sendMailBefore15Min(User user, Event event) throws Exception;

    /**
     * send mail with message before 10 mintes by organizer.
     * 
     * <p> this selection is for somebody want a short time reminder before certain event. </p>
     * 
     * @param
     * @return void
     * @throws Exception
     */
    void sendMailBefore10Min(User user, Event event) throws Exception;

    /* 
     * send mail with message before 5 minutes by organizer
     */
    void sendMailBefore5Min(User user, Event event) throws Exception;

    
}
