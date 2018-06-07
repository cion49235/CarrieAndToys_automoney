package kr.co.inno.autocash;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Autoapp_DBopenHelper extends SQLiteOpenHelper {
	public Autoapp_DBopenHelper(Context context) {
		super(context, "autoapp.db", null, 1);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
			String createtable = "create table auto_app_list ( _id integer primary key autoincrement,"+
			"aai_seq integer, aai_title text, aai_link_url text, aai_status text);";
			db.execSQL(createtable);

	}		                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               		
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("drop table if exits auto_app_list");
		onCreate(db);
	}
}