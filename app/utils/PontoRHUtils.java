package utils;

import models.pontorh.Period;
import models.pontorh.WorkDay;
import org.apache.commons.collections.FastTreeMap;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

public class PontoRHUtils {

    /**
     * Get the last time registred (if the user has been registred before),
     * to be seen when the user register the out
     *
     * @param wrkDay list of WorkDays (will receive the WorkDays of the current day)
     * @return String with formatted time
     */
    public static String lastTimeIn(WorkDay wrkDay) {
        return (wrkDay != null && !CollectionUtils.isEmpty(wrkDay.periods)
                && wrkDay.periods.get(wrkDay.periods.size() - 1).dateOut == null) ?
                new SimpleDateFormat("HH:mm").format(wrkDay.periods.get(wrkDay.periods.size() - 1).dateIn) : null;
    }

    /**
     * Put the WorkDays of the list parameter in a tree and complete the tree with the days of MONTH
     * I'll be used to create the tables in the view, to show all the days of the MONTH
     * The keys of the tree will be the date of each day
     *
     * @param d  the date that will be used to get the MONTH (could be any day of the MONTH)
     * @param lp list that contains all the WorkDays of the MONTH
     * @return the tree "populated"
     */
    public static TreeMap<LocalDate, WorkDay> putInATreeOfMonth(Date d, List<WorkDay> lp) {
        TreeMap<LocalDate, WorkDay> wrkDsTree = new FastTreeMap();

        //Date in start of MONTH
        LocalDate ld = DateTimeUtils.dateToLocalDate(d).with(TemporalAdjusters.firstDayOfMonth());
        //Variavel criada para nao usar o proprio ld, para evitar ficar incrementando o ld
        //pq se o ld é fevereiro, por exemplo, que tem 28-29 dias, ao ir adicionando um dia a LD, se chega em março
        // e marco tem 31 dias. O laço vai ate ld.lengthOfMonth()
        LocalDate lAux = ld;

        for (WorkDay pAux : lp) {
            //key of tree will be the date of the WorkDay
            wrkDsTree.put(DateTimeUtils.dateToLocalDate(pAux.date), pAux);
        }

        int a = ld.lengthOfMonth();

        //Fullfill the rest of the tree with the remaining day (that not contain WorkDays)
        for (int i = 0; i < ld.lengthOfMonth(); i++) {
            //Add a day if it has' been yet
            wrkDsTree.putIfAbsent(lAux, null);
            lAux = lAux.plusDays(1);
        }

        return wrkDsTree;

    }

    /**
     * Put the WorkDays of the list parameter in a tree and complete the tree with the days of WEEK
     * I'll be used to create the tables in the view, to show all the days of the WEEK
     * The keys of the tree will be the date of each day
     *
     * @param d  the date that will be used to get the WEEK (could be any day of the month)
     * @param lp list that contains all the WorkDays of the WEEK
     * @return the tree "populated"
     */
    public static TreeMap<LocalDate, WorkDay> putInATreeOfWeek(Date d, List<WorkDay> lp) {
        TreeMap<LocalDate, WorkDay> wrkDsTree = new FastTreeMap();
        //Date in start of WEEK
        LocalDate ld = DateTimeUtils.dateToLocalDate(d).with(DayOfWeek.MONDAY);
        //OBS: ALTERAR AQUI CASO PRECISE LIMITAR A EXIBIÇÃO APENAS AS DA SEMANA DO MÊS
//        Month month = DateTimeUtils.dateToLocalDate(d).getMonth();

        for (WorkDay wd : lp) {
            for (Period p : wd.periods) {
                p.getPeriodTime();
            }
        }

        for (WorkDay pAux : lp) {
            //key of tree will be the date of the WorkDay
            wrkDsTree.put(DateTimeUtils.dateToLocalDate(pAux.date), pAux);
        }

        //Fullfill the rest of the tree with the remaining day (that not contain WorkDays)
        for (int i = 0; i < 7; i++) {
            //OBS: ALTERAR AQUI CASO PRECISE LIMITAR A EXIBIÇÃO APENAS AS DA SEMANA DO MÊS (2)
//            if (ld.getMonth() == month) {
            //Add a day if it has' been yet
            wrkDsTree.putIfAbsent(ld, null);
//            }
            ld = ld.plusDays(1);
        }

        return wrkDsTree;

    }

}
