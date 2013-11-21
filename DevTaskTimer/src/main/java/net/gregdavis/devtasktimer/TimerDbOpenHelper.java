package net.gregdavis.devtasktimer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by greg on 6/19/13.
 */
public class TimerDbOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DevTaskTimer";
    private static final int DATABASE_VERSION = 1;

    public TimerDbOpenHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
        db.execSQL("insert into timers (label, startTime, endTime, overallTime, ticking) values ('w00t', 0, 0, 0, 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(" drop table if exists timers ");
        onCreate(db);
    }

    // Database creation sql statement
    private static final String DATABASE_CREATE =
            " create table timers "
                    + " (_id integer primary key autoincrement, "
                    + " startTime integer, "
                    + " endTime integer, "
                    + " overallTime integer, "
                    + " ticking integer not null, "
                    + " label text); ";


}
