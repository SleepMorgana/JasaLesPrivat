package developer.aulia.jasalesprivat;

import android.app.Application;
import android.content.res.Configuration;
import android.util.Log;

import java.io.IOException;
import java.util.AbstractCollection;

/**
 * A singleton class used for detecting application on start and on exit for
 * loading and saving lists to database
 * This does not work correctly, onCreate() works but onTerminate() only works for emulator
 */
public class myCalendarApplication extends Application {
    private static myCalendarApplication JasaLesPrivat;

    public myCalendarApplication getInstance() {
        return JasaLesPrivat;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        JasaLesPrivat = this;
        Log.d("ApplicationStatus", "App started");


        // doesn't work cuz I am getting error saying null array
        try {

            // Intialize lists once at the begining of each loading
            EventListHandler.initStaticList();
            EventListHandler.initDynamicList();

            // Load lists from database to EventListHandler
            CalendarObjectList<? extends AbstractCollection<? extends CalendarObject>, ? extends CalendarObject> list0 = CalendarDB.retriveListLocal(0, this);
            CalendarObjectList<? extends AbstractCollection<? extends CalendarObject>, ? extends CalendarObject> list1 = CalendarDB.retriveListLocal(1, this);
            CalendarObjectList<? extends AbstractCollection<? extends CalendarObject>, ? extends CalendarObject> list2 = CalendarDB.retriveListLocal(2, this);
            CalendarObjectList<? extends AbstractCollection<? extends CalendarObject>, ? extends CalendarObject> list3 = CalendarDB.retriveListLocal(3, this);

            EventListHandler.setStaticList((StaticEventList) list0);
            EventListHandler.setDynamicList((DynamicEventList) list1);
            EventListHandler.setDeadlineList((DynamicEventList) list2);
            EventListHandler.setFinishedDynamicList((DynamicEventList) list3);

        } catch (Exception e) {
            Log.e("SHIT", e.getMessage());
            try {

                // for the very first time, initialize the local database
                CalendarDB.initDBLocal(this);

            } catch (IOException ex) {
                Log.e("Errorrrr", e.getMessage());
            }
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

}