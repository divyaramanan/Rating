package divya.example.com.rateprofessor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Message;
import android.util.Log;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Divya on 3/12/2015.
 */
public class DatabaseAdapter {

    DatabaseHelper dbhelp;

    public DatabaseAdapter(Context context)
    {
           dbhelp = new DatabaseHelper(context);
    }

    public long insertData(int professorId,String firstName,String lastName,String office,String phone,String email,String averageRating,String totalRating)
    {
        SQLiteDatabase sqLiteDatabase = dbhelp.getWritableDatabase();
        ContentValues contentToBeInserted = new ContentValues();
        contentToBeInserted.put("id",professorId);
        contentToBeInserted.put("firstName",firstName);
        contentToBeInserted.put("lastname",lastName);
        contentToBeInserted.put("office",office);
        contentToBeInserted.put("phone",phone);
        contentToBeInserted.put("email",email);
        contentToBeInserted.put("averagerating",averageRating);
        contentToBeInserted.put("totalrating",totalRating);
        long result = sqLiteDatabase.insert(dbhelp.TABLE_NAME,null,contentToBeInserted);
        return result;

    }

    public long insertComment(int professorId,List<String> commentDates,List<String> Comments)
    {
        long result = 0;
        try {
            SQLiteDatabase sqLiteDatabase = dbhelp.getWritableDatabase();
            ContentValues contentToBeInserted = new ContentValues();
            for(int i=0;i<commentDates.size();i++) {
                contentToBeInserted.put("id", professorId);
                contentToBeInserted.put("commentDate", commentDates.get(i));
                contentToBeInserted.put("Comments", Comments.get(i));
                result = sqLiteDatabase.insert(dbhelp.TABLE_COMMENTS, null, contentToBeInserted);
            }

        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public long insertList(int professorId,List<String> firstNames,List<String> lastNames){
        long result = 0;
        SQLiteDatabase sqLiteDatabase = dbhelp.getWritableDatabase();
        ContentValues contentToBeInserted = new ContentValues();
        for(int i=0;i<firstNames.size();i++) {
            contentToBeInserted.put("id", professorId);
            contentToBeInserted.put("firstName", firstNames.get(i));
            contentToBeInserted.put("lastName", lastNames.get(i));
            result = sqLiteDatabase.insert(dbhelp.TABLE_INSTRUCTORS, null, contentToBeInserted);
        }
        return result;
    }
    public Cursor selectList(){
        SQLiteDatabase sqLiteDatabase = dbhelp.getWritableDatabase();
        String[] columns = {"firstName","lastName"};
        Cursor cursor = sqLiteDatabase.query(dbhelp.TABLE_INSTRUCTORS,columns,null,null,null,null,null);
        return cursor;
    }

    public long deleteEntireList(){
        SQLiteDatabase sqLiteDatabase = dbhelp.getWritableDatabase();
        long del = sqLiteDatabase.delete(dbhelp.TABLE_INSTRUCTORS,null,null);
        return del;
    }

    public long deleteAllData(){
        SQLiteDatabase sqLiteDatabase = dbhelp.getWritableDatabase();
        long del = sqLiteDatabase.delete(dbhelp.TABLE_COMMENTS,null,null);
       return del;
    }

    public void getCount(int professorId){
        SQLiteDatabase sqLiteDatabase = dbhelp.getWritableDatabase();
       Cursor cursor = sqLiteDatabase.rawQuery(" SELECT count(*) from UserComments where id ="+professorId,null);
        while(cursor.moveToNext())
        {
            int count = cursor.getInt(0);

        }

    }

    public void deleteInstructorComments(int professorId){
        SQLiteDatabase sqLiteDatabase = dbhelp.getWritableDatabase();
        String where = "id="+professorId;
        long del = sqLiteDatabase.delete(dbhelp.TABLE_COMMENTS,where,null);

    }
    public Cursor selectData(String email){
        SQLiteDatabase sqLiteDatabase = dbhelp.getWritableDatabase();
        String[] columns = {"email"};
        String where = "email="+"'"+email+"'";
        Cursor cursor = sqLiteDatabase.query(dbhelp.TABLE_NAME,columns,where,null,null,null,null);
        return cursor;
    }

    public Cursor selectAllData(int professorId){
        SQLiteDatabase sqLiteDatabase = dbhelp.getWritableDatabase();
        String[] columns = {"firstname","lastname","office","phone","email","averagerating","totalrating"};
        String where = "id="+professorId;
        Cursor cursor = sqLiteDatabase.query(dbhelp.TABLE_NAME,columns,where,null,null,null,null);
        return cursor;
    }

    public Cursor selectComments(int professorId){
        try {
            SQLiteDatabase sqLiteDatabase = dbhelp.getWritableDatabase();
            String[] columns = {"id","commentDate","Comments"};
            String where = "id=" + professorId;
            Cursor cursor = sqLiteDatabase.query(dbhelp.TABLE_COMMENTS, columns, where, null, null, null, null);
            return cursor;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public long updateData(String email,Double average,int total){
        SQLiteDatabase sqLiteDatabase = dbhelp.getWritableDatabase();
        ContentValues contentToBeUpdated = new ContentValues();
        contentToBeUpdated.put("averagerating",average);
        contentToBeUpdated.put("totalrating",total);
        String where = "email="+"'"+email+"'";
        long update = sqLiteDatabase.update(dbhelp.TABLE_NAME,contentToBeUpdated,where,null);
        return update;
    }

    public long updateComment(int professorId,String commentDate,String Comments){
        SQLiteDatabase sqLiteDatabase = dbhelp.getWritableDatabase();
        ContentValues contentToBeUpdated = new ContentValues();
        contentToBeUpdated.put("commentDate",commentDate);
        contentToBeUpdated.put("Comments",Comments);
         String where = "id="+professorId;
        long update = sqLiteDatabase.update(dbhelp.TABLE_COMMENTS,contentToBeUpdated,where,null);
        return update;
    }

    static class DatabaseHelper extends SQLiteOpenHelper{
        private static final String DB_NAME="rating";
        private static final String TABLE_NAME="details";
        private static final String TABLE_COMMENTS="UserComment";
        private static final String TABLE_INSTRUCTORS="Instructors";
        private static final int version = 22;
        private static final String CREATE_TABLE_DETAILS = "CREATE TABLE "+TABLE_NAME+"(id INT PRIMARY KEY,firstname VARCHAR(255),lastname VARCHAR(255),office VARCHAR(255),phone VARCHAR(255),email VARCHAR(255),averagerating DOUBLE,totalrating INT);";
        private static final String DROP_TABLE_DETAILS = "DROP TABLE IF EXISTS "+TABLE_NAME;
        private static final String CREATE_TABLE_COMMENTS = "CREATE TABLE "+TABLE_COMMENTS+"(id INT,commentDate VARCHAR(255),Comments VARCHAR(255));";
        private static final String DROP_TABLE_COMMENTS = "DROP TABLE IF EXISTS "+TABLE_COMMENTS;
        private static final String CREATE_TABLE_LIST="CREATE TABLE "+TABLE_INSTRUCTORS+"(id INT,firstName VARCHAR(255),lastName VARCHAR(255));";
        private static final String DROP_TABLE_LIST = "DROP TABLE IF EXISTS "+TABLE_INSTRUCTORS;
        private Context context;
        public DatabaseHelper(Context context) {
            super(context,DB_NAME,null,version);
            this.context=context;

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_LIST);

            db.execSQL(CREATE_TABLE_COMMENTS);

            db.execSQL(CREATE_TABLE_DETAILS);

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
           try{
            db.execSQL(DROP_TABLE_DETAILS);
            db.execSQL(DROP_TABLE_COMMENTS);
            db.execSQL(DROP_TABLE_LIST);

            onCreate(db);
           }
           catch (Exception e)
           {
               e.printStackTrace();
           }
        }
    }
}
