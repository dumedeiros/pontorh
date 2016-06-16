package models.pontorh;

import play.db.jpa.Model;
import utils.DateTimeUtils;
import utils.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A WorkDay represents a day of work (Obviously)
 * <p/>
 * A WorkDay can have many periods, indicating that the user is allowed to leave many times
 */


@Entity
public class WorkDay extends Model {

    public Date date;
    /**
     * A message the user can comment about any event
     */

    @Column(columnDefinition = "TEXT")
    public String message;
    @Transient
    public long registredTime;

    @ManyToOne(cascade = CascadeType.ALL)
    public User user;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "workDay")
    public List<Period> periods;


    public WorkDay(Date date, User user) {
        this.date = date;
        this.message = "";
        this.user = user;
        this.periods = new ArrayList<Period>();
    }


    /**
     * Get the time of the whole WorkDay
     * (calculated over it's periods)
     *
     * @return the whole WorkDay Time
     */
    public long getWdTime() {
        long t = 0;
        for (Period p : periods) {
            t += p.getPeriodTime();
        }
        registredTime = t;
        return t;
    }

    /**
     * Get the full time of a set of WorkDays
     * (Adding the full time of each WorkDay in the set)
     *
     * @param wds
     * @return
     */
    public static long getWdsTime(List<WorkDay> wds) {
        long l = 0;
        for (WorkDay wd : wds) {
            l += wd.getWdTime();
        }
        return l;
    }


    /**
     * Get the WorkDay of the especified date
     *
     * @param d the especified date
     * @param u the user
     * @return
     */
    public static final WorkDay getOfDay(Date d, User u) {
        return findOneWithDates(DateTimeUtils.getStartOfDay(d), DateTimeUtils.getEndOfDay(d), u);
    }

    /**
     * Get a list of WorkDay in a week given any day of this week
     *
     * @param d the especified date
     * @param u the user
     * @return a list of WorkDay in a week
     */
    public static final List<WorkDay> getOfWeek(Date d, User u) {
        return findWithDates(DateTimeUtils.getStartOfWeek(d), DateTimeUtils.getEndOfWeek(d), u);
    }

    /**
     * Get a list of WorkDay in a month given any day of this month
     *
     * @param d the especified date
     * @param u the user
     * @return list of WorkDay in a month
     */
    public static final List<WorkDay> getOfMonth(Date d, User u) {
        return findWithDates(DateTimeUtils.getStartOfMonth(d), DateTimeUtils.getEndOfMonth(d), u);
    }


    public static final List<WorkDay> findWithDates(Date firstDate, Date lastDate, User u) {
        return find("user = ?1 and date between ?2 and ?3",
                u, firstDate, lastDate).fetch();
        //  Another way to do the same
        //  return find("from Ponto p where p.date >= ?1 and p.date <=?2",
        //      firstDate, lastDate).fetch();
    }

    public static final WorkDay findOneWithDates(Date firstDate, Date lastDate, User u) {
        return find("user = ?1 and date between ?2 and ?3",
                u, firstDate, lastDate).first();
        //  Another way to do the same
        //  return find("from Ponto p where p.date >= ?1 and p.date <=?2",
        //      firstDate, lastDate).fetch();
    }

    public boolean isDataOutMissing() {
        return !this.periods.isEmpty() && getLastPeriod().dateOut == null;
    }

    public Period getLastPeriod() {
        return this.periods.get(this.periods.size() - 1);
    }

    //TODO colocar workDays pontos de paradas de mensagens multiplas
    public void addMessage(String msg) {
        if (!StringUtils.isEmpty(msg)) {
            this.message += msg.concat("\n");
            this.save();
        }
    }

    @Override
    public String toString() {
        return date.toString().substring(0, 11) + registredTime;
    }
}
