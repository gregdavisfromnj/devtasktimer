package net.gregdavis.devtasktimer;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.AbsoluteChronometer;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Calendar;

/**
 * Created by greg on 6/10/13.
 */
public class TimersActivity extends Activity {

    private static final int ID_ROW_OFFSET = 1000;
    private static final int ID_TIMER_OFFSET = 2000;
    private static final int ID_LABEL_OFFSET = 3000;
    private static final int ID_START_OFFSET = 4000;
    private static final int ID_STOP_OFFSET = 5000;
    private static final int ID_RESET_OFFSET = 6000;
    private static final int ID_DELETE_OFFSET = 7000;

    static final int NEW_TIMER_REQUEST = 0;

    private SQLiteDatabase db;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_timers);


        if (db == null) {
            db = new TimerDbOpenHelper(this).getWritableDatabase();
        }

        Cursor result = db.rawQuery(" select * from timers ", null);


        long id, startTime, overallTime;
        boolean ticking;
        TableLayout timers = (TableLayout) findViewById(R.id.timeTableBody);

        timers.setColumnStretchable(0, true);
        AbsoluteChronometer cron;

        while (result.moveToNext()) {
            id = result.getLong(result.getColumnIndex("_id"));
            startTime = result.getLong(result.getColumnIndex("startTime"));
            overallTime = result.getLong(result.getColumnIndex("overallTime"));
            ticking = 1 == result.getLong(result.getColumnIndex("ticking"));

            cron = addTimerRow(timers, id);
            if (cron == null) {
                // that was weird, keep going...
                continue;
            }

            if (ticking) {
                // we should be ticking so start ticking!
                cron.setBase(startTime - overallTime);
                cron.start();
                updateNotifications();
            } else {
                // don't tick, but setup the chronometer state
                startTime = System.currentTimeMillis();
                db.execSQL(" update timers set startTime = '" + startTime + "' where _id = '" + Long.toString(id) + "' ");
                cron.setBase(startTime - overallTime);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state){
        super.onSaveInstanceState(state);
    }



    public class StartClickListener implements View.OnClickListener {
        
        public void onClick (View btn) {
            long id = btn.getId() - ID_START_OFFSET;

            if (db == null) {
                db = new TimerDbOpenHelper(btn.getContext()).getWritableDatabase();
            }

            Cursor result = db.query("timers",
                                        new String[] {"ticking", "overallTime"},
                                        "_id = ?", new String[]{ Long.toString(id)},
                                        null,
                                        null,
                                        null);
            result.moveToNext();
            boolean ticking = result.getLong(result.getColumnIndex("ticking")) == 1;

            if (!ticking) {
                long startTime = System.currentTimeMillis();
                long overallTime = result.getLong(result.getColumnIndex("overallTime"));

                db.execSQL(" update timers set startTime = '" + Long.toString(startTime) + "', ticking = '1' where _id = '" + Long.toString(id) + "' ");

                AbsoluteChronometer tickingCron = (AbsoluteChronometer)(findViewById((int)(id + ID_TIMER_OFFSET)));
                tickingCron.setBase(startTime - overallTime);
                tickingCron.start();

                // and we're ticking
                updateNotifications();
            }
        }
    }
    public class StopClickListener implements View.OnClickListener {

        public void onClick(View btn) {
            long id = btn.getId() - ID_STOP_OFFSET;

            if (db == null) {
                db = new TimerDbOpenHelper(btn.getContext()).getWritableDatabase();
            }

            Cursor result = db.rawQuery(" select ticking, startTime, overallTime from timers where _id = '" + Long.toString(id) + "'", null);
            result.moveToNext();
            boolean ticking = result.getLong(result.getColumnIndex("ticking")) == 1;
            long startTime = result.getLong((result.getColumnIndex("startTime")));
            long overallTime = result.getLong((result.getColumnIndex("overallTime")));
            long endTime = System.currentTimeMillis();

            if (ticking) {
                AbsoluteChronometer tickingCron = (AbsoluteChronometer)(findViewById((int)id + ID_TIMER_OFFSET));
                tickingCron.stop();

                // get the stop time, and accumulate the overall run time,
                // will allow for paused time to be disregarded
                overallTime = overallTime + (endTime - startTime);

                db.execSQL(" update timers set "
                        + "startTime = '" + Long.toString(System.currentTimeMillis()) + "', "
                        + "ticking = '0', "
                        + "overallTime = '" + Long.toString(overallTime) + "' "
                        + "where _id = '" + Long.toString(id) + "' ");

                updateNotifications();
            }
        }
    }

        public class ResetClickListener implements View.OnClickListener {

            public void onClick(View btn) {
                long id = btn.getId() - ID_RESET_OFFSET;

                if (db == null) {
                    db = new TimerDbOpenHelper(btn.getContext()).getWritableDatabase();
                }

                Cursor result = db.rawQuery(" select ticking from timers where _id = '" + Long.toString(id) + "'", null);
                result.moveToNext();
                boolean ticking = result.getLong(result.getColumnIndex("ticking")) == 1;

                AbsoluteChronometer cron = (AbsoluteChronometer)(findViewById((int)id + ID_TIMER_OFFSET));

                if (ticking) {
                    cron.stop();
                }

                cron.setBase(System.currentTimeMillis());

                db.execSQL(" update timers set startTime = '0', "
                            + " endTime = '0', "
                            + " overallTime = '0', "
                            + " ticking = '0' where _id = '" + Long.toString(id) + "' ");

                updateNotifications();
            }
        }

    public class DeleteClickListener implements View.OnClickListener {

        public void onClick(View btn) {
            long id = btn.getId() - ID_DELETE_OFFSET;

            if (db == null) {
                db = new TimerDbOpenHelper(btn.getContext()).getWritableDatabase();
            }

            int deleted = db.delete("timers", "_id = ?", new String[]{Long.toString(id)});
            TableLayout timers = (TableLayout) findViewById(R.id.timeTableBody);

            timers.removeView(findViewById((int)id + ID_ROW_OFFSET));

            updateNotifications();
        }
    }

    public void onNewTimerClick(View btn) {


        String timerName = getString(R.string.editTimerNameBox) + " " + Calendar.getInstance().getTimeInMillis() + " ";

        if (db == null) {
            db = new TimerDbOpenHelper(this).getWritableDatabase();
        }

        ContentValues values = new ContentValues(5);
        values.put("label", timerName);
        values.put("startTime", 0);
        values.put("endTime", 0);
        values.put("overallTime", 0);
        values.put("ticking", 0);

        long id = db.insert("timers", null, values);

        Intent intent = new Intent(this, EditTimerActivity.class);
        intent.putExtra("ID", id);
        startActivityForResult(intent, NEW_TIMER_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_TIMER_REQUEST) {
            if (resultCode == RESULT_OK) {
                long id = data.getLongExtra("ID", 0);
                String label = data.getStringExtra("TimerName");

                if (db == null) {
                    db = new TimerDbOpenHelper(this).getWritableDatabase();
                }
                ContentValues values = new ContentValues(1);
                values.put("label", label);
                db.update("timers", values, "_id=?", new String[] { Long.toString(id) } );

                TableLayout timers = (TableLayout) findViewById(R.id.timeTableBody);
                addTimerRow(timers, id);
            }
        }
    }

    protected AbsoluteChronometer addTimerRow(TableLayout timers, long id) {


        if (db == null) {
            db = new TimerDbOpenHelper(this).getWritableDatabase();
        }
        Cursor result = db.rawQuery(" select label from timers where _id='" + id + "'" , null);
        result.moveToNext();
        String label = result.getString(result.getColumnIndex("label"));


        TableRow newRow = new TableRow(this);
        newRow.setId((int)id + ID_ROW_OFFSET);

        TableRow.LayoutParams rowLayoutParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        newRow.setLayoutParams(rowLayoutParams);

        AbsoluteChronometer realCron = new AbsoluteChronometer(this);
        TableRow.LayoutParams cronLayout = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        realCron.setTextSize(12);
        realCron.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        realCron.setId((int)id + ID_TIMER_OFFSET);
        realCron.setLayoutParams(cronLayout);

        TextView timerLabel = new TextView(this);
        TableRow.LayoutParams labelLayout = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        timerLabel.setTextSize(12);
        timerLabel.setText(label);
        timerLabel.setId((int)id + ID_LABEL_OFFSET);
        timerLabel.setLayoutParams(labelLayout);

        Button startBtn = new Button(this);
        startBtn.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        startBtn.setText(R.string.startBtnText);
        startBtn.setId((int)id + ID_START_OFFSET);
        startBtn.setOnClickListener(new StartClickListener());

        Button stopBtn = new Button(this);
        stopBtn.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        stopBtn.setText(R.string.stopBtnText);
        stopBtn.setId((int)id + ID_STOP_OFFSET);
        stopBtn.setOnClickListener(new StopClickListener());

        Button resetBtn = new Button(this);
        resetBtn.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        resetBtn.setText(R.string.resetBtnText);
        resetBtn.setId((int)id + ID_RESET_OFFSET);
        resetBtn.setOnClickListener(new ResetClickListener());

        Button deleteBtn = new Button(this);
        deleteBtn.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));
        deleteBtn.setText(R.string.deleteBtnText);
        deleteBtn.setId((int)id + ID_DELETE_OFFSET);
        deleteBtn.setOnClickListener(new DeleteClickListener());

        newRow.addView(timerLabel);
        newRow.addView(realCron);
        newRow.addView(startBtn);
        newRow.addView(stopBtn);
        newRow.addView(resetBtn);
        newRow.addView(deleteBtn);
        timers.addView(newRow);

        return realCron;
    }


    protected void updateNotifications() {

        if (db == null) {
            db = new TimerDbOpenHelper(this).getWritableDatabase();
        }

        long numTickers = DatabaseUtils.queryNumEntries(db, "timers", "ticking = 1");

        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        int notificationId = 001;

        if (numTickers > 0) {
            NotificationCompat.Builder notification = new NotificationCompat.Builder(this);
            notification.setSmallIcon(R.drawable.ic_launcher);
            notification.setContentTitle(getString(R.string.app_name));
            notification.setContentText(getString(R.string.timing_notification));
            notification.setOngoing(true);

            // pretty much boilerplate to navigate back to MainActivity
            Intent resultIntent = new Intent(this, TimersActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(TimersActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            Bundle state = new Bundle();
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT, state);
            notification.setContentIntent(resultPendingIntent);

            // only will have one notification, so use #1
            manager.notify(notificationId, notification.build());

        } else {
            manager.cancel(notificationId);
        }
    }

/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.timers, menu);
        return true;
    }
    */
}
