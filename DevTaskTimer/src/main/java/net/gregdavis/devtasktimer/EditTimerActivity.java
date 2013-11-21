package net.gregdavis.devtasktimer;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class EditTimerActivity extends Activity {

    private long _timerId = 0;

    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_timer);


        _timerId = getIntent().getLongExtra("ID", 0);
/*
        if (_timerId == 0) {
            finish();
        }

        if (db == null) {
            db = new TimerDbOpenHelper(this).getWritableDatabase();
        }
        Cursor result = db.rawQuery(" select * from timers where _id='" + _timerId + "'" , null);
        result.moveToNext();

        String timerLabel = result.getString(result.getColumnIndex("label"));

        TextView nameBox = (TextView)findViewById(R.id.editTimerNameBox);
        nameBox.setText(timerLabel);

        */
    }

    public void onOk(View btn) {
        /*
        if (db == null) {
            db = new TimerDbOpenHelper(this).getWritableDatabase();
        }
        ContentValues values = new ContentValues(1);
        TextView nameBox = (TextView)findViewById(R.id.editTimerNameBox);
        values.put("label", nameBox.getText().toString());
        db.update("timers", values, "_id=?", new String[] { Long.toString(_timerId) } );

*/
        TextView nameBox = (TextView)findViewById(R.id.editTimerNameBox);

        Intent data = new Intent();
        data.putExtra("TimerName", nameBox.getText().toString());
        data.putExtra("ID", _timerId);
        setResult(RESULT_OK, data);
        finish();
    }

    public void onCancel(View btn) {
        setResult(
            RESULT_CANCELED,
            new Intent().putExtra("ID", _timerId)
        );
        finish();
    }


    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_timer, menu);
        return true;
    }
*/
}
