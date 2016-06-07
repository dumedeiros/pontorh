package models.pontorh;


import play.db.jpa.Model;
import utils.DateTimeUtils;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.Duration;
import java.util.Date;

/**
 * A Period is a time interval of a WorkDay
 * It's represented by a time of Entrance and a time of Exit
 */

@Entity
public class Period extends Model {

    public Date dateIn;
    public Date dateOut;

    @ManyToOne(cascade = CascadeType.ALL)
    public WorkDay workDay;

    public Period(Date dateIn, Date dateOut, WorkDay workDay) {
        this.dateIn = dateIn;
        this.dateOut = dateOut;
        this.workDay = workDay;
    }

    /**
     * Get the time interval between the entrance and exit
     *
     * @return
     */
    public long getPeriodTime() {
        long l = (dateIn != null && dateOut != null) ?
                Duration.between(DateTimeUtils.dateToLocalDateTime(dateIn), DateTimeUtils.dateToLocalDateTime(dateOut)).toMillis() :
                0;
        return l;
    }

    @Override
    public String toString() {
        return dateIn.toString().substring(11, 16);
//        return  date.toString().substring(11, 20) + (isIn ? "ENT" : "SAI");
    }
}
