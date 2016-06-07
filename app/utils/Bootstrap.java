package utils;

import models.pontorh.Period;
import play.jobs.Job;
import play.jobs.OnApplicationStart;
import play.test.Fixtures;

@OnApplicationStart
public class Bootstrap extends Job {

    public void doJob() {
        Fixtures.deleteAllModels();
        // Load default date if the database is empty
        if (Period.count() == 0) {
//            Fixtures.loadModels("data.yml");
        }
    }

}