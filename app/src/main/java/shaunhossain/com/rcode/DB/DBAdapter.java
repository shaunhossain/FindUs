package shaunhossain.com.rcode.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {

    private static final String TAG = "DBAdapter";
    
    private static final String DATABASE_NAME = "RCODEDB";
    /****************************************************************/
    private static final String TABLE_GENERATED = "generated";
    private static final String KEY_CODE = "code";
    private static final String KEY_ADDRESS = "address";
    /****************************************************************/

    
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE1 =
        "create table if not exists " + TABLE_GENERATED + " (code VARCHAR primary key, address VARCHAR);";
    

    private final Context context;

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DBAdapter(Context ctx)
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
        
    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
        	try {
                Log.w(TAG, "DB CREATED!");
        		db.execSQL(DATABASE_CREATE1);
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_GENERATED);
            onCreate(db);
        }
    }    

    //---opens the database---
    public DBAdapter open() throws SQLException
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---    
    public void close() 
    {
        DBHelper.close();
    }
    
    //---insert a record into the database---
    public long insertRecord(String code, String address)
    {
        ContentValues initialValues = new ContentValues ();
        initialValues.put(KEY_CODE, code);
        initialValues.put(KEY_ADDRESS, address);
        return db.insert(TABLE_GENERATED, null, initialValues);
    }
    //---deletes a particular record---
    public boolean deleteCode(String code)
    {	
        return db.delete(TABLE_GENERATED, KEY_CODE + "=?", new String[] {code}) > 0;
    }
    //---retrieves all the records---
    public Cursor getAllCodes()
    {
        return db.query(TABLE_GENERATED, new String[] {KEY_CODE,KEY_ADDRESS },  null, null,null,null,null );
    }
//    //--- update records---
//    public boolean updateNoteMainText(String NewMainText, int id)
//    {
//        ContentValues args = new ContentValues();
//        args.put(KEY_MAIN_TEXT, NewMainText);
//        args.put(KEY_TITLE,NewMainText );
//        return db.update(TABLE_NOTES, args, KEY_NOTE_ID + "=?",  new String[] {String.valueOf(id)}) > 0;
//    }

    public boolean existsCode(String code) {
        
        String Query = "Select * from " + TABLE_GENERATED + " where " + KEY_CODE + "=?";
        Cursor cursor = db.rawQuery(Query, new String[]{code});
                if(cursor.getCount()<=0){
                	return false;
                }
            return true;
    }
//    public boolean deleteALL() {
//
//        db.delete(TABLE_EXHIBITIONS, null, null);
//            return true;
//    }

}
