package org.kohaerenzstiftung.andtroin;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.kohaerenzstiftung.Language;
import org.kohaerenzstiftung.UnknownIso3LanguageException;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObservable;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database {
	public class DetailsKeyValueX extends DetailsKeyValue {
		private LinkedList<Integer> mOld = new LinkedList<Integer>();

		public LinkedList<Integer> getmOld() {
			return mOld;
		}

		public DetailsKeyValueX(int mId, String mKey, String mValue) {
			super(mId, mKey, mValue);
		}

		public void addId(int id) {
			Integer wrapped;
			if (!this.mOld.contains((wrapped = new Integer(id)))) {
				mOld.add(wrapped);
			}
		}
	}

	public class DetailsKeyX extends DetailsKey {

		@Override
		public boolean equals(Object o) {
			DetailsKeyX other = (DetailsKeyX) o;
			if (!other.getmValue().equals(this.getmValue())) {
				return false;
			}
			if (!other.getmLanguage().equals(this.getmLanguage())) {
				return false;
			}
			return true;
		}

		public DetailsKeyX(String value, LinkedList<DetailsKeyValue> values,
				int mId, String language) {
			super(value, null, mId, language);
			this.addValues(values);
		}

		public void addValue(DetailsKeyValue value) {
			DetailsKeyValueX theValue = null;
			for (DetailsKeyValue val : this.getmValues()) {
				if (val.getmValue().equals(value.getmValue())) {
					theValue = (DetailsKeyValueX) val;
					break;
				}
			}
			if (theValue == null) {
				theValue = new DetailsKeyValueX(-1, value.getmKey(),
						value.getmValue());
				this.getmValues().add(theValue);
			}
			theValue.addId(value.getmId());
		}

		public void addValues(LinkedList<DetailsKeyValue> values) {
			for (DetailsKeyValue val : values) {
				addValue(val);
			}
		}
	}

	public class InvalidJsonDetailException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6257218768346845919L;

		public InvalidJsonDetailException(String string) {
			super("invalid json detail " + string);
		}
	}

	public class NotInTransactionException extends Exception {
		/**
		 * 
		 */
		private static final long serialVersionUID = 174590693165557397L;
	}

	@SuppressWarnings("unused")
	private static final int SERIES_INCOMPLETE = 0;
	@SuppressWarnings("unused")
	private static final int SERIES_COMPLETE = 1;

	private static final int ENTRY_FAILED = 0;
	public static final int ENTRY_PENDING = 1;
	private static final int ENTRY_SCHEDULED = 2;
	private static final int ENTRY_SWAP = 3;
	private static final int ENTRY_FAILED_UP = 4;
	private static final int ENTRY_OK = 5;
	private static final int ENTRY_INVALID = 6;

	static private DataSetObservable mObservable = new DataSetObservable();
	static boolean mInTrx = false;
	private SQLiteDatabase mDb = null;
	private DatabaseHelper mDatabaseHelper = null;
	private int mTrxCount = 0;
	private boolean mOpen = false;
	//private Context mContext;

	public boolean isMOpen() {
		return this.mOpen;
	}

	private class DatabaseHelper extends SQLiteOpenHelper {
		private static final int CURRENT_VERSION = 30;

		public DatabaseHelper(Context context) {
			super(context, "andtroin.db", null, CURRENT_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table lists ("
					+ "_id integer primary key autoincrement, "
					+ "name varchar(255) not null, "
					+ "sourceLanguage varchar(255) not null, "
					+ "targetLanguage varchar(255) not null, "
					+ "unique (name)" + ");");
			db.execSQL("create table entries ("
					+ "_id integer primary key autoincrement, "
					+ "list_id integer not null, " + "status int default "
					+ Database.ENTRY_PENDING + ", " + "score int default 0, "
					+ "foreign key(list_id) references lists(_id) "
					+ "on update cascade on delete cascade" + ");");
			db.execSQL("create table series_content ("
					+ "list_id integer not null, "
					+ "entry_id integer not null, "
					+ "series_id integer not null, "
					+ "level integer not null, "
					+ "status integer not null, "
					+ "persistent_status integer not null, "
					+ "unique (entry_id), "
					+ "foreign key(list_id) references list(_id) on update cascade on delete cascade, "
					+ "foreign key(entry_id) references entries(_id) on update cascade on delete cascade, "
					+ "foreign key(series_id) references series(_id) on update cascade on delete cascade"
					+ ");");
			db.execSQL("create table series ("
					+ "_id integer primary key autoincrement, "
					+ "list_id integer not null, "
					+ "schedule_id integer not null, "
					+ "foreign key(list_id) references list(_id) on update cascade on delete cascade, "
					+ "foreign key(schedule_id) references schedules(_id) on update cascade on delete cascade"
					+ ");");

			db.execSQL("create table schedules ("
					+ "_id integer primary key autoincrement, "
					+ "interval integer not null, "
					+ "next_timeout int not null, "
					+ "list_id integer not null, "
					+ "series_size integer not null, "
					+ "unique (list_id), "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade, "
					+ "check (series_size > 0)" + ");");

			db.execSQL("create table denominations ("
					+ "_id integer primary key autoincrement, "
					+ "list_id integer not null, "
					+ "side varchar(255) not null, "
					+ "value varchar(255) not null, "
					+ "entry_id integer not null, "
					+ "foreign key(entry_id) references entries(_id) on update cascade on delete cascade, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade"
					+ ");");

			db.execSQL("create table examples ("
					+ "_id integer primary key autoincrement, "
					+ "list_id integer not null, "
					+ "value varchar(255) not null, "
					+ "translation varchar(255) not null, "
					+ "denomination_id integer not null, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade,"
					+ "foreign key(denomination_id) references denominations(_id) on update cascade on delete cascade"
					+ ");");

			db.execSQL("create table details ("
					+ "list_id integer not null, "
					+ "value_id integer not null, "
					+ "denomination_id integer not null, "
					+ "foreign key(denomination_id) references denominations(_id) on update cascade on delete cascade, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade, "
					+ "foreign key(value_id) references details_key_values(_id) on update cascade on delete cascade "
					+ ");");

			db.execSQL("create table details_keys ("
					+ "_id integer primary key autoincrement, "
					+ "side varchar(255) not null, "
					+ "value varchar(255) not null, "
					+ "list_id integer not null, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade"
					+ ");");

			db.execSQL("create table details_key_values ("
					+ "_id integer primary key autoincrement, "
					+ "details_keys_id integer not null, "
					+ "list_id integer not null, "
					+ "value varchar(255) not null, "
					+ "foreign key(details_keys_id) references details_keys(_id) on update cascade on delete cascade, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade"
					+ ");");

			db.execSQL("create table categories ("
					+ "_id integer primary key autoincrement, "
					+ "list_id integer not null, "
					+ "source_language varchar(255) not null, "
					+ "target_language varchar(255) not null, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade"
					+ ");");

			db.execSQL("create table entry_categories ("
					+ "entry_id integer not null, "
					+ "list_id integer not null, "
					+ "category_id integer not null, "
					+ "foreign key(entry_id) references entries(_id) on update cascade on delete cascade, "
					+ "foreign key(category_id) references categories(_id) on update cascade on delete cascade, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade"
					+ ");");

			db.execSQL("create table entry_pairs ("
					+ "source varchar(255) not null, "
					+ "target varchar(255) not null, "
					+ "entry_id integer not null, "
					+ "list_id integer not null, "
					+ "foreign key(entry_id) references entries(_id) on update cascade on delete cascade,"
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade"
					+ ");");

			db.execSQL("create index idx_entry_id on entry_pairs(entry_id);");
			db.execSQL("create index idx_source on entry_pairs(list_id, source, target);");
			db.execSQL("create index idx_target on entry_pairs(list_id, target, source);");

			doUpgradeStep(db, 1, CURRENT_VERSION);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			doUpgradeStep(db, oldVersion, newVersion);
		}

		private void doUpgradeStep(SQLiteDatabase db, int oldVersion,
				int endVersion) {
			if (oldVersion >= endVersion) {
				return;
			}
			switch (oldVersion) {
			case 1:
				// upgrade to version 2
				doVersion1toVersion2(db);
				break;
			case 2:
				// upgrade to version 3
				doVersion2toVersion3(db);
				break;
			case 3:
				// upgrade to version 4
				doVersion3toVersion4(db);
				break;
			case 4:
				// upgrade to version 5
				doVersion4toVersion5(db);
				break;
			case 5:
				// upgrade to version 6
				doVersion5toVersion6(db);
				break;
			case 6:
				// upgrade to version 7
				doVersion6toVersion7(db);
				break;
			case 7:
				// upgrade to version 8
				doVersion7toVersion8(db);
				break;
			case 8:
				// upgrade to version 9
				doVersion8toVersion9(db);
				break;
			case 9:
				// upgrade to version 10
				doVersion9toVersion10(db);
				break;
			case 10:
				// upgrade to version 11
				doVersion10toVersion11(db);
				break;
			case 11:
				// upgrade to version 12
				doVersion11toVersion12(db);
				break;
			case 12:
				// upgrade to version 13
				doVersion12toVersion13(db);
				break;
			case 13:
				// upgrade to version 14
				doVersion13toVersion14(db);
				break;
			case 14:
				// upgrade to version 15
				doVersion14toVersion15(db);
				break;
			case 15:
				// upgrade to version 16
				doVersion15toVersion16(db);
				break;
			case 16:
				// upgrade to version 17
				doVersion16toVersion17(db);
				break;
			case 17:
				// upgrade to version 18
				doVersion17toVersion18(db);
				break;
			case 18:
				// upgrade to version 19
				doVersion18toVersion19(db);
				break;
			case 19:
				break;
			case 20:
				// upgrade to version 21
				doVersion19toVersion21(db);
				break;
			case 21:
				// upgrade to version 22
				doVersion21toVersion22(db);
				break;
			case 22:
				// upgrade to version 23
				doVersion22toVersion23(db);
				break;
			case 23:
				// upgrade to version 24
				doVersion23toVersion24(db);
				break;
			case 24:
				// upgrade to version 25
				doVersion24toVersion25(db);
				break;
			case 25:
				// upgrade to version 26
				doVersion25toVersion26(db);
				break;
			case 26:
				// upgrade to version 27
				doVersion26toVersion27(db);
				break;
			case 27:
				// upgrade to version 28
				doVersion27toVersion28(db);
				break;
			case 28:
				// upgrade to version 28
				doVersion28toVersion29(db);
				break;
			case 29:
				// upgrade to version 28
				doVersion29toVersion30(db);
				break;
			}
			doUpgradeStep(db, oldVersion + 1, endVersion);
		}
		
		private void doVersion29toVersion30(SQLiteDatabase db) {
			db.execSQL("create table words (" +
					"language varchar(255) not null," +
					"value varchar(255) not null" +
			");");
			db.execSQL("create index i18 on words (language, value);");
		}
		
		private void doVersion28toVersion29(SQLiteDatabase db) {
			db.execSQL("create index i16 on denominations (_id, entry_id, list_id, side);");
			db.execSQL("create index i17 on denomination_forms (denomination_id, value);");			
		}


		private void doVersion27toVersion28(SQLiteDatabase db) {
			Cursor cursor = db.rawQuery("select _id " +
					"from entries where "
					+ "_id not in (select entry_id from entry_pairs)", null);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				int id = cursor.getInt(0);
				Entry entry = getEntry(id);
				List list = getList(entry.getmListId());
				doDeleteEntry(id, true, 
						list.getmSourceLanguage().getmIso3Language(),
						list.getmTargetLanguage().getmIso3Language());
			}
			cursor.close();
		}

		private void doVersion26toVersion27(SQLiteDatabase db) {
			doVersion25toVersion26(db);
		}

		private void doVersion25toVersion26(SQLiteDatabase db) {
			Cursor cursor = db.rawQuery("select form_attribute_id " +
					"from denomination_form_attributes where "
					+ "form_attribute_id not in (select _id from form_attributes)", null);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
				int id = cursor.getInt(0);
				db.delete("denomination_form_attributes", "form_attribute_id = ?",
						new String[]{Integer.toString(id)});
			}
			cursor.close();
		}

		private void doVersion24toVersion25(SQLiteDatabase db) {
			Cursor cursor = db.rawQuery("select list_id, _id from entries "
					+ "where _id not in (select entry_id from denominations)",
					null);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				int id = cursor.getInt(1);
				int listId = cursor.getInt(0);
				List list = getList(listId);
				doDeleteEntry(id, true, list.getmSourceLanguage()
						.getmIso3Language(), list.getmTargetLanguage()
						.getmIso3Language());
			}
		}

		private void doVersion23toVersion24(SQLiteDatabase db) {
			db.execSQL("delete from denominations where entry_id not in "
					+ "(select entry_id from entry_pairs)");
			db.execSQL("delete from denomination_forms where denomination_id not in "
					+ "(select _id from denominations)");
		}

		private void doVersion22toVersion23(SQLiteDatabase db) {
			db.execSQL("delete from denominations where entry_id not in "
					+ "(select _id from entries)");
			db.execSQL("delete from denomination_forms where denomination_id not in "
					+ "(select _id from denominations)");
		}

		private void doVersion21toVersion22(SQLiteDatabase db) {
			db.execSQL("create table denominations_backup ("
					+ "_id integer primary key autoincrement, "
					+ "list_id integer not null, "
					+ "side varchar(255) not null, "
					+ "entry_id integer not null, "
					+ "foreign key(entry_id) references entries(_id) on update cascade on delete cascade, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade"
					+ ");");
			db.execSQL("insert into denominations_backup (_id, list_id, side, entry_id) "
					+ "select _id, list_id, side, entry_id from denominations");
			db.execSQL("drop table denominations");
			db.execSQL("create table denominations ("
					+ "_id integer primary key autoincrement, "
					+ "list_id integer not null, "
					+ "side varchar(255) not null, "
					+ "entry_id integer not null, "
					+ "foreign key(entry_id) references entries(_id) on update cascade on delete cascade, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade"
					+ ");");
			db.execSQL("create index i14 on denominations (entry_id, side);");
			db.execSQL("create index i15 on denominations (list_id, side);");
			db.execSQL("insert into denominations (_id, list_id, side, entry_id) "
					+ "select _id, list_id, side, entry_id from denominations_backup");
			db.execSQL("drop table denominations_backup;");
		}

		private void doVersion19toVersion21(SQLiteDatabase db) {
			db.execSQL("insert into denomination_forms(denomination_id, value, list_id) "
					+ "select _id, value, list_id from denominations;");
		}

		private void doVersion18toVersion19(SQLiteDatabase db) {
			db.execSQL("drop table entry_categories_count");
			db.execSQL("alter table categories add column count integer not null default 0;");
			db.execSQL("create index i13 on categories(list_id, count);");
			Cursor cursor = db.query("entry_categories", new String[] {
					"category_id", "count(*) as c" }, null, null,
					"category_id", null, null);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				int categoryId = cursor.getInt(0);
				int count = cursor.getInt(1);
				ContentValues values = new ContentValues();
				values.put("count", count);
				db.update("categories", values, "_id = ?",
						new String[] { Integer.toString(categoryId) });
			}
			cursor.close();
		}

		private void doVersion17toVersion18(SQLiteDatabase db) {
			db.execSQL("drop table entry_categories_count");
			db.execSQL("create table entry_categories_count ("
					+ "_id integer primary key autoincrement, "
					+ "category_id integer, "
					+ "count integer, "
					+ "list_id integer not null, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade, "
					+ "foreign key(category_id) references categories(_id) on update cascade on delete cascade"
					+ ");");
			db.execSQL("create index i12 on entry_categories_count(list_id, count);");
			Cursor cursor = db.query("entry_categories",
					new String[] { "category_id, list_id, count(*) as c" },
					null, null, "category_id, list_id", null, null);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				int categoryId = cursor.getInt(0);
				int listId = cursor.getInt(1);
				int count = cursor.getInt(2);
				ContentValues values = new ContentValues();
				values.put("category_id", categoryId);
				values.put("list_id", listId);
				values.put("count", count);
				db.insert("entry_categories_count", null, values);
			}
			cursor.close();

		}

		private void doVersion16toVersion17(SQLiteDatabase db) {
			db.execSQL("create table entry_categories_count ("
					+ "_id integer primary key autoincrement, "
					+ "category_id integer, "
					+ "count integer, "
					+ "list_id integer not null, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade, "
					+ "foreign key(category_id) references categories(_id) on update cascade on delete cascade"
					+ ");");
			db.execSQL("create index i11 on entry_categories_count(list_id, count);");
			Cursor cursor = db.query("entry_categories",
					new String[] { "category_id, list_id, count(*) as c" },
					null, null, "category_id, list_id", null, null);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				int categoryId = cursor.getInt(0);
				int listId = cursor.getInt(1);
				int count = cursor.getInt(2);
				ContentValues values = new ContentValues();
				values.put("category_id", categoryId);
				values.put("list_id", listId);
				values.put("count", count);
				db.insert("entry_categories_count", null, values);
			}
			cursor.close();
		}

		private void doVersion15toVersion16(SQLiteDatabase db) {
			db.delete("proposed_peers", null, null);
			db.delete("no_peers", null, null);
		}

		private void doVersion14toVersion15(SQLiteDatabase db) {
			db.execSQL("drop table proposed_peers");
			db.execSQL("drop table no_peers");
			db.execSQL("create table proposed_peers ("
					+ "_id integer primary key autoincrement, "
					+ "entry1_id integer, "
					+ "entry2_id integer, "
					+ "list_id integer not null, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade, "
					+ "foreign key(entry1_id) references entries(_id) on update cascade on delete cascade, "
					+ "foreign key(entry2_id) references entries(_id) on update cascade on delete cascade"
					+ ");");
			db.execSQL("create table no_peers ("
					+ "_id integer primary key autoincrement, "
					+ "entry1_id integer, "
					+ "entry2_id integer, "
					+ "list_id integer not null, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade, "
					+ "foreign key(entry1_id) references entries(_id) on update cascade on delete cascade, "
					+ "foreign key(entry2_id) references entries(_id) on update cascade on delete cascade"
					+ ");");
		}

		private void doVersion13toVersion14(SQLiteDatabase db) {
			db.execSQL("create table proposed_peers ("
					+ "entry1_id integer, "
					+ "entry2_id integer, "
					+ "list_id integer not null, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade, "
					+ "foreign key(entry1_id) references entries(_id) on update cascade on delete cascade, "
					+ "foreign key(entry2_id) references entries(_id) on update cascade on delete cascade"
					+ ");");
		}

		private void doVersion12toVersion13(SQLiteDatabase db) {
			db.execSQL("create table no_peers ("
					+ "entry1_id integer, "
					+ "entry2_id integer, "
					+ "list_id integer not null, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade, "
					+ "foreign key(entry1_id) references entries(_id) on update cascade on delete cascade, "
					+ "foreign key(entry2_id) references entries(_id) on update cascade on delete cascade"
					+ ");");
		}

		private void doVersion11toVersion12(SQLiteDatabase db) {
			db.execSQL("drop index series1");
			db.execSQL("drop index idx_entry_id");
			db.execSQL("drop index entry_categories1");
			db.execSQL("drop index examples1");
			db.execSQL("drop index series_content1");
			db.execSQL("drop index series_content4");
			db.execSQL("drop index entries2");
			db.execSQL("drop index categories2");
			db.execSQL("drop index entries1");
			db.execSQL("drop index schedules1");
			db.execSQL("drop index details_keys1");
			db.execSQL("drop index lists1");
			db.execSQL("drop index series2");

			db.execSQL("create index i1 on details_key_values_new(details_keys_id, value);");
			db.execSQL("create index i2 on details_keys_new(language, value);");
			db.execSQL("create index i3 on entry_pairs(source, list_id);");
			db.execSQL("create index i4 on entry_pairs(target, list_id);");
			db.execSQL("create index i5 on form_attributes(list_id, value, weight);");
			db.execSQL("create index i6 on form_attributes(list_id, side, weight, _id);");
			db.execSQL("create index i7 on series_content(status, series_id, level);");
			db.execSQL("create index i8 on entries(list_id, status, score);");
			db.execSQL("create index i9 on schedules(next_timeout);");
			db.execSQL("create index i10 on details_new(value_id, denomination_id);");
		}

		private void doVersion10toVersion11(SQLiteDatabase db) {
			db.execSQL("alter table schedules add column foreign_language integer not null default 0;");
		}

		private void doVersion9toVersion10(SQLiteDatabase db) {
			LinkedList<DetailsKeyX> allDetailsKeys = new LinkedList<DetailsKeyX>();
			LinkedList<Integer> detailsKeysToDelete = new LinkedList<Integer>();
			Cursor cursor = db.query("details_keys_new",
					new String[] { "_id" }, null, null, null, null, null);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				int keyId = cursor.getInt(0);
				DetailsKey detailsKey = doGetDetailsKey(db, keyId);
				detailsKeysToDelete.add(new Integer(keyId));
				DetailsKeyX detailsKeyX = new DetailsKeyX(
						detailsKey.getmValue(), detailsKey.getmValues(), -1,
						detailsKey.getmLanguage());
				if (allDetailsKeys.contains(detailsKeyX)) {
					continue;
				}
				allDetailsKeys.add(detailsKeyX);
				Cursor cursor2 = db.query(
						"details_keys_new",
						new String[] { "_id" },
						"_id != ? and value = ? and language = ?",
						new String[] { Integer.toString(keyId),
								detailsKey.getmValue(),
								detailsKey.getmLanguage() }, null, null, null);
				for (cursor2.moveToFirst(); !cursor2.isAfterLast(); cursor2
						.moveToNext()) {
					int keyId2 = cursor2.getInt(0);
					DetailsKey detailsKey2 = doGetDetailsKey(db, keyId2);
					detailsKeyX.addValues(detailsKey2.getmValues());
				}
				cursor2.close();
			}
			cursor.close();

			for (DetailsKeyX dkx : allDetailsKeys) {
				DetailsKeyValue theDkv = null;
				int dkid = doPutDetailsKey(db, dkx, dkx.getmLanguage());
				DetailsKey dk = doGetDetailsKey(db, dkid);
				for (DetailsKeyValue dkv : dkx.getmValues()) {
					DetailsKeyValueX dkvx = (DetailsKeyValueX) dkv;
					String value = dkvx.getmValue();
					for (DetailsKeyValue dkv2 : dk.getmValues()) {
						if (dkv2.getmValue().equals(value)) {
							theDkv = dkv2;
							break;
						}
					}
					int newId = theDkv.getmId();
					ContentValues values = new ContentValues();
					for (Integer oldId : dkvx.getmOld()) {
						values.put("value_id", newId);
						db.update("details_new", values, "value_id = ?",
								new String[] { oldId.toString() });
						db.delete("details_key_values_new", "_id = ?",
								new String[] { oldId.toString() });
					}
				}
			}
			for (Integer d : detailsKeysToDelete) {
				db.delete("details_keys_new", "_id = ?",
						new String[] { d.toString() });
			}
		}

		private void doVersion8toVersion9(SQLiteDatabase db) {
			db.delete("details_new", null, null);
			db.delete("details_keys_new", null, null);
			db.delete("details_key_values_new", null, null);
			Cursor cursor = getDetailsKeys(db);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				int id = cursor.getInt(0);
				String side = cursor.getString(1);
				String value = cursor.getString(2);
				int listId = cursor.getInt(3);

				convertDetailsKey(id, side, value, listId, db);
			}
			cursor.close();
		}

		private void convertDetailsKey(int detailsKeyId, String side,
				String key, int listId, SQLiteDatabase db) {
			String language = getLanguage(listId, side, db);
			ContentValues values = new ContentValues();
			values.put("value", key);
			values.put("language", language);
			db.insert("details_keys_new", null, values);
			int keyId = getLastInsertId(db);
			Cursor cursor = getDetailsKeyValues(detailsKeyId, db);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				int id = cursor.getInt(0);
				String value = cursor.getString(1);
				convertDetailsKeyValue(keyId, id, value, listId, db);
			}
			cursor.close();
		}

		private void convertDetailsKeyValue(int newKeyId, int oldValueId,
				String value, int listId, SQLiteDatabase db) {
			ContentValues values = new ContentValues();
			values.put("value", value);
			values.put("details_keys_id", newKeyId);
			db.insert("details_key_values_new", null, values);
			int valueId = getLastInsertId(db);
			Cursor cursor = getDetails(oldValueId, db);
			for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor
					.moveToNext()) {
				int denominationId = cursor.getInt(0);
				values = new ContentValues();
				values.put("denomination_id", denominationId);
				values.put("value_id", valueId);
				values.put("list_id", listId);
				db.insert("details_new", null, values);
			}
			cursor.close();
		}

		private Cursor getDetails(int oldValueId, SQLiteDatabase db) {
			return db.query("details", new String[] { "denomination_id" },
					"value_id = ?",
					new String[] { Integer.toString(oldValueId) }, null, null,
					null);
		}

		private String getLanguage(int listId, String side, SQLiteDatabase db) {
			String column = side + "Language";
			Cursor cursor = db.query("lists", new String[] { column },
					"_id = ?", new String[] { Integer.toString(listId) }, null,
					null, null);
			cursor.moveToFirst();
			String result = cursor.getString(0);
			cursor.close();
			return result;
		}

		private Cursor getDetailsKeyValues(int detailsKeyId, SQLiteDatabase db) {
			return db.query("details_key_values",
					new String[] { "_id", "value" }, "details_keys_id = ?",
					new String[] { Integer.toString(detailsKeyId) }, null,
					null, null);
		}

		private Cursor getDetailsKeys(SQLiteDatabase db) {
			return db.query("details_keys", new String[] { "_id", "side",
					"value", "list_id" }, null, null, null, null, null);
		}

		private void doVersion7toVersion8(SQLiteDatabase db) {
			db.execSQL("create table details_new ("
					+ "list_id integer not null, "
					+ "value_id integer not null, "
					+ "denomination_id integer not null, "
					+ "foreign key(denomination_id) references denominations(_id) on update cascade on delete cascade, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade, "
					+ "foreign key(value_id) references details_key_values_new(_id) on update cascade on delete cascade "
					+ ");");

			db.execSQL("create table details_keys_new ("
					+ "_id integer primary key autoincrement, "
					+ "language varchar(255) not null, "
					+ "value varchar(255) not null " + ");");

			db.execSQL("create table details_key_values_new ("
					+ "_id integer primary key autoincrement, "
					+ "details_keys_id integer not null, "
					+ "value varchar(255) not null, "
					+ "foreign key(details_keys_id) references details_keys_new(_id) on update cascade on delete cascade "
					+ ");");
		}

		private void doVersion6toVersion7(SQLiteDatabase db) {
			db.execSQL("update form_attributes set weight = _id;");
		}

		private void doVersion5toVersion6(SQLiteDatabase db) {
			db.execSQL("alter table form_attributes add column weight integer not null default 0;");
			db.execSQL("update form_attributes set weight = _id;");
		}

		private void doVersion4toVersion5(SQLiteDatabase db) {
			db.execSQL("create table denomination_forms ("
					+ "_id integer primary key autoincrement, "
					+ "denomination_id integer not null, "
					+ "value varchar(255) not null, "
					+ "list_id integer not null, "
					+ "foreign key(denomination_id) references denominations(_id) on update cascade on delete cascade, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade"
					+ ");");
			db.execSQL("create table form_attributes ("
					+ "_id integer primary key autoincrement, "
					+ "list_id integer not null, "
					+ "value varchar(255) not null, "
					+ "side varchar(255) not null, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade"
					+ ");");
			db.execSQL("create table denomination_form_attributes ("
					+ "denomination_form_id integer not null, "
					+ "form_attribute_id integer not null, "
					+ "list_id integer not null, "
					+ "foreign key(denomination_form_id) references denomination_forms(_id) on update cascade on delete cascade, "
					+ "foreign key(form_attribute_id) references form_attributes(_id) on update cascade on delete cascade, "
					+ "foreign key(list_id) references lists(_id) on update cascade on delete cascade"
					+ ");");
		}

		private void doVersion3toVersion4(SQLiteDatabase db) {
			db.execSQL("drop index idx_source");
			db.execSQL("drop index idx_target");
			db.execSQL("create index idx_source on entry_pairs(list_id, source, target);");
			db.execSQL("create index idx_target on entry_pairs(list_id, target, source);");
			db.execSQL("create index idx_source1 on entry_pairs(list_id, entry_id, source);");
			db.execSQL("create index idx_target1 on entry_pairs(list_id, entry_id, target);");
		}

		private void doVersion2toVersion3(SQLiteDatabase db) {
			db.execSQL("drop index idx_source");
			db.execSQL("drop index idx_target");
			db.execSQL("drop index entry_pairs1");
			db.execSQL("drop index entry_pairs2");
			db.execSQL("create index idx_source on entry_pairs(list_id, source, target, entry_id);");
			db.execSQL("create index idx_target on entry_pairs(list_id, target, source, entry_id);");
		}

		private void doVersion1toVersion2(SQLiteDatabase db) {
			db.execSQL("create index idx_categories1 on categories "
					+ "(list_id, source_language, target_language);");
			db.execSQL("create index categories2 on categories (_id);");

			db.execSQL("create index entry_categories1 on entry_categories (entry_id);");

			db.execSQL("create index entries1 on entries (_id);");
			db.execSQL("create index entries2 on entries (list_id, status);");

			db.execSQL("create index schedules1 on schedules (_id);");
			db.execSQL("create index schedules2 on schedules (next_timeout);");
			db.execSQL("create index schedules3 on schedules (list_id);");

			db.execSQL("create index details_keys1 on details_keys (_id);");
			db.execSQL("create index details_keys2 on details_keys (list_id, side, value);");

			db.execSQL("create index details_key_values1 on details_key_values (details_keys_id, value);");

			db.execSQL("create index details1 on details (value_id, denomination_id);");

			db.execSQL("create index lists1 on lists (_id);");
			db.execSQL("create index lists2 on lists (name);");

			db.execSQL("create index series1 on series (schedule_id);");
			db.execSQL("create index series2 on series (_id);");

			db.execSQL("create index entry_pairs1 on entry_pairs (list_id, source);");
			db.execSQL("create index entry_pairs2 on entry_pairs (list_id, target);");

			db.execSQL("create index denominations1 on denominations (entry_id, side);");
			db.execSQL("create index denominations2 on denominations (list_id, side);");

			db.execSQL("create index examples1 on examples (denomination_id);");

			db.execSQL("create index series_content1 on series_content (series_id, status);");
			db.execSQL("create index series_content2 on series_content (series_id, level);");
			db.execSQL("create index series_content3 on series_content (series_id, persistent_status);");
			db.execSQL("create index series_content4 on series_content (entry_id);");
		}

	}

	public Database(Context context) {
		super();
		//this.mContext = context;
		this.mDatabaseHelper = new DatabaseHelper(context);
	}

	public boolean listExists(String name) {
		int count = 0;
		Cursor cursor;
		cursor = this.mDb.query("lists", new String[] { "_id" }, "name = ?",
				new String[] { name }, null, null, null);
		count = cursor.getCount();
		cursor.close();
		boolean result = count > 0;
		return result;
	}

	private int getLastInsertId() {
		return getLastInsertId(this.mDb);
	}

	static private int getLastInsertId(SQLiteDatabase db) {
		Cursor cursor = db.rawQuery("select last_insert_rowid();", null);
		cursor.moveToFirst();
		int result = cursor.getInt(0);
		cursor.close();
		return result;
	}

	private void startTransaction() {
		this.mDb.beginTransaction();
		this.mTrxCount++;
	}

	private void doImportListFromBufferedReader(List list,
			BufferedReader bufferedReader, Updatable updatable)
			throws IOException {
		String source;
		LinkedList<EntryPair> entryPairList = new LinkedList<EntryPair>();
		while ((source = bufferedReader.readLine()) != null) {
			String target = bufferedReader.readLine();
			EntryPair entryPair = new EntryPair(-1, -1, source, target);
			entryPairList.add(entryPair);
		}
		this.doImportListFromEntryPairList(list, entryPairList, updatable);
	}

	public void importListFromBufferedReader(List list,
			BufferedReader bufferedReader, Updatable updatable) {
		this.startTransaction();
		try {
			this.doImportListFromBufferedReader(list, bufferedReader, updatable);
			try {
				this.commit();
			} catch (NotInTransactionException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			try {
				this.rollback();
			} catch (NotInTransactionException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	public void importFromJsonStrings(Context context, List list,
			String jsonStrings, Updatable updatable) throws Exception {
		Exception ex = null;
		this.startTransaction();
		int listId = list.getmId();
		if (listId == -1) {
			listId = putList(list);
			list.setmId(listId);
		}
		list = getList(listId);
		try {
			doImportFromJsonStrings(context, list, jsonStrings, updatable);
		} catch (Exception e) {
			ex = e;
		}
		if (ex == null) {
			try {
				this.commit();
			} catch (NotInTransactionException e) {
				e.printStackTrace();
			}
		} else {
			try {
				this.rollback();
			} catch (NotInTransactionException e) {
				e.printStackTrace();
			}
		}
		if (ex != null) {
			throw ex;
		}
	}

	private void doImportFromJsonStrings(Context context, List list,
			String jsonStrings, Updatable updatable) throws JSONException,
			InvalidJsonDetailException {
		JSONTokener tokener = new JSONTokener(jsonStrings);

		int length = ((Integer) tokener.nextValue()).intValue();

		updatable.update(length);
		int processed = 0;
		int onePercent = length / 100;
		if (onePercent < 1) {
			onePercent = 1;
		}
		int progress = processed / onePercent;
		updatable.update(processed);

		for (int i = 0; i < length; i++) {
			JSONObject entryObject = null;
			try {
				entryObject = (JSONObject) tokener.nextValue();
			} catch (JSONException e) {
				break;
			}
			importJsonEntry(context, entryObject, list);
			processed++;
			int newProgress = processed / onePercent;
			if (newProgress > progress) {
				updatable.update(processed);
			}
		}
	}

	private void importJsonEntry(Context context, JSONObject entryObject,
			List list) throws JSONException, InvalidJsonDetailException {
		JSONArray jsonCategories = entryObject.getJSONArray("categories");
		JSONArray jsonSourceDenominations = entryObject
				.getJSONArray("sourceDenominations");
		JSONArray jsonTargetDenominations = entryObject
				.getJSONArray("targetDenominations");
		int score = 0;
		if (entryObject.has("score")) {
			score = entryObject.getInt("score");
		}
		int status = 0;
		if (entryObject.has("status")) {
			status = entryObject.getInt("status");
		}

		Entry entry = new Entry(-1, list.getmId(), null, null, null, score,
				status, list.getmSourceLanguage().getmIso3Language(), list
						.getmTargetLanguage().getmIso3Language());
		LinkedList<Category> categories = entry.getmCategories();
		LinkedList<Denomination> sourceDenominations = entry
				.getmSourceDenominations();
		LinkedList<Denomination> targetDenominations = entry
				.getmTargetDenominations();

		int length = jsonCategories.length();
		for (int i = 0; i < length; i++) {
			JSONObject jsonCategory = jsonCategories.getJSONObject(i);
			String source = jsonCategory.getString("source");
			String target = jsonCategory.getString("target");
			Category category = getCategory(source, target,
					list.getmId());
			categories.add(category);
		}
		length = jsonSourceDenominations.length();
		for (int i = 0; i < length; i++) {
			JSONObject jsonDemonination = jsonSourceDenominations
					.getJSONObject(i);
			Denomination denomination = buildDenomination(context,
					jsonDemonination, list, true, list.getmSourceLanguage()
							.getmIso3Language());
			sourceDenominations.add(denomination);
		}
		length = jsonTargetDenominations.length();
		for (int i = 0; i < length; i++) {
			JSONObject jsonDemonination = jsonTargetDenominations
					.getJSONObject(i);
			Denomination denomination = buildDenomination(context,
					jsonDemonination, list, false, list.getmTargetLanguage()
							.getmIso3Language());
			targetDenominations.add(denomination);
		}
		if (entry.isValid()) {
			doPutEntry(entry, list.getmId());
		}
	}

	private Category getCategory(String source, String target,
			int listId) {
		Cursor cursor = mDb.query("categories", new String[] { "_id" },
				"list_id = ? and source_language = ? and target_language = ?",
				new String[] { Integer.toString(listId), source, target },
				null, null, null);
		cursor.moveToFirst();
		int id = -1;
		if (!cursor.isAfterLast()) {
			id = cursor.getInt(0);
		}
		cursor.close();
		if (id == -1) {
			putCategory(new Category(-1, source, target, listId), listId);
			return getCategory(source, target, listId);
		}
		return new Category(id, source, target, listId);
	}

	private Denomination buildDenomination(Context context,
			JSONObject jsonDenomination, List list, boolean source,
			String language) throws JSONException, InvalidJsonDetailException {
		String value = jsonDenomination.getString("value");

		LinkedList<DetailsKeyValue> details = new LinkedList<DetailsKeyValue>();
		JSONObject jsonDetails = jsonDenomination.getJSONObject("details");
		JSONArray names = jsonDetails.names();
		int length = (names == null) ? 0 : names.length();
		for (int i = 0; i < length; i++) {
			String jsonDetailsKey = names.getString(i);
			String jsonDetailsKeyValue = jsonDetails.getString(jsonDetailsKey);
			DetailsKeyValue detail = getDetail(context, jsonDetailsKey,
					jsonDetailsKeyValue, list, source, language);
			details.add(detail);
		}

		LinkedList<Example> examples = new LinkedList<Example>();
		JSONArray jsonExamples = jsonDenomination.getJSONArray("examples");
		length = jsonExamples.length();
		for (int i = 0; i < length; i++) {
			JSONObject jsonExample = jsonExamples.getJSONObject(i);
			Example example = buildExample(jsonExample);
			examples.add(example);
		}

		LinkedList<DenominationForm> forms = new LinkedList<DenominationForm>();

		if (jsonDenomination.has("forms")) {
			JSONArray jsonForms = jsonDenomination.getJSONArray("forms");
			length = jsonForms.length();
			for (int i = 0; i < length; i++) {
				JSONObject jsonForm = jsonForms.getJSONObject(i);
				DenominationForm form = buildForm(jsonForm, source);
				forms.add(form);
			}
		}

		return new Denomination(-1, -1, -1, value, language, examples, details,
				forms);
	}

	private DenominationForm buildForm(JSONObject jsonForm, boolean source)
			throws JSONException {
		String value = jsonForm.getString("value");
		JSONArray jsonAttributes = jsonForm.getJSONArray("attributes");
		int length = jsonAttributes.length();
		LinkedList<FormAttribute> attributes = new LinkedList<FormAttribute>();
		for (int i = 0; i < length; i++) {
			JSONObject jsonAttribute = jsonAttributes.getJSONObject(i);
			FormAttribute attribute = buildAttribute(jsonAttribute, source);
			attributes.add(attribute);
		}
		return new DenominationForm(-1, value, -1, attributes);
	}

	private FormAttribute buildAttribute(JSONObject jsonAttribute,
			boolean source) throws JSONException {
		int weight = jsonAttribute.getInt("weight");
		String value = jsonAttribute.getString("value");
		return new FormAttribute(-1, source, value, weight);
	}

	private Example buildExample(JSONObject jsonExample) throws JSONException {
		String value = jsonExample.getString("value");
		String translation = jsonExample.getString("translation");
		return new Example(-1, -1, -1, value, translation);
	}

	private DetailsKeyValue getDetail(Context context, String key,
			String value, List list, boolean source, String language)
			throws JSONException, InvalidJsonDetailException {
		int id = getDetailId(key, value, source, list.getmId(),
				language);
		return new DetailsKeyValue(id, key, value);
	}

	private int getDetailId(String key, String value,
			boolean source, int listId, String language) {
		int detailsKeyId = getDetailsKeyId(language, key);
		Cursor cursor = mDb.query("details_key_values_new",
				new String[] { "_id" }, "details_keys_id = ? and value = ?",
				new String[] { Integer.toString(detailsKeyId), value }, null,
				null, null);
		cursor.moveToFirst();
		int result = -1;
		if (!cursor.isAfterLast()) {
			result = cursor.getInt(0);
		}
		cursor.close();
		if (result != -1) {
			return result;
		}
		putDetailsKeyValue(new DetailsKeyValue(-1, key, value), detailsKeyId);
		return getDetailId(key, value, source, listId, language);
	}

	private void putDetailsKeyValue(DetailsKeyValue detailsKeyValue,
			int detailsKeyId) {
		putDetailsKeyValue(this.mDb, detailsKeyValue, detailsKeyId);
	}

	private int getDetailsKeyId(String language, String key) {
		Cursor cursor = mDb.query("details_keys_new", new String[] { "_id" },
				"language = ? and value = ?", new String[] { language, key },
				null, null, null);
		cursor.moveToFirst();
		int result = -1;
		if (!cursor.isAfterLast()) {
			result = cursor.getInt(0);
		}
		cursor.close();
		if (result != -1) {
			return result;
		}
		putDetailsKey(new DetailsKey(key, null, -1, language), language);
		return getDetailsKeyId(language, key);
	}

	private void commit() throws NotInTransactionException {
		if (this.mTrxCount < 1) {
			throw new NotInTransactionException();
		}
		this.mDb.setTransactionSuccessful();
		this.mDb.endTransaction();
		this.mTrxCount--;
		if (this.mTrxCount < 1) {
			Database.mObservable.notifyChanged();
		}
	}

	private void rollback() throws NotInTransactionException {
		if (this.mTrxCount < 1) {
			throw new NotInTransactionException();
		}
		this.mDb.endTransaction();
		this.mTrxCount--;
	}

	private void open() {
		for (;;) {
			try {
				this.mDb = this.mDatabaseHelper.getWritableDatabase();
				this.mDb.setLockingEnabled(false);
				break;
			} catch (Exception e) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		this.mOpen = true;
		// this.mRandom = new Random();
	}

	public void open(Context context) {
		this.open();
	}

	public void close() {
		this.mDb.close();
		this.mOpen = false;
	}

	private void doDeleteList(int listId) {
		this.mDb.delete("entries", "list_id = ?",
				new String[] { Integer.toString(listId) });
		this.mDb.delete("series_content", "list_id = ?",
				new String[] { Integer.toString(listId) });
		this.mDb.delete("series", "list_id = ?",
				new String[] { Integer.toString(listId) });
		this.mDb.delete("schedules", "list_id = ?",
				new String[] { Integer.toString(listId) });
		this.mDb.delete("denominations", "list_id = ?",
				new String[] { Integer.toString(listId) });
		this.mDb.delete("examples", "list_id = ?",
				new String[] { Integer.toString(listId) });
		this.mDb.delete("details_new", "list_id = ?",
				new String[] { Integer.toString(listId) });
		this.mDb.delete("entry_pairs", "list_id = ?",
				new String[] { Integer.toString(listId) });
		this.mDb.delete("lists", "_id = ?",
				new String[] { Integer.toString(listId) });
		this.mDb.delete("entry_categories", "list_id = ?",
				new String[] { Integer.toString(listId) });
		/*
		 * 
		 * NOTE: don't have to decrement count in categories here because
		 * categories will also be deleted...
		 */
		this.mDb.delete("categories", "list_id = ?",
				new String[] { Integer.toString(listId) });
		this.mDb.delete("denomination_forms", "list_id = ?",
				new String[] { Integer.toString(listId) });
		this.mDb.delete("form_attributes", "list_id = ?",
				new String[] { Integer.toString(listId) });
		this.mDb.delete("denomination_form_attributes", "list_id = ?",
				new String[] { Integer.toString(listId) });
		this.mDb.delete("no_peers", "list_id = ?",
				new String[] { Integer.toString(listId) });
		this.mDb.delete("proposed_peers", "list_id = ?",
				new String[] { Integer.toString(listId) });
	}

	public void deleteList(int id) {
		this.startTransaction();
		this.doDeleteList(id);
		try {
			this.commit();
		} catch (NotInTransactionException e) {
			e.printStackTrace();
		}
	}

	public Cursor getLists() {
		Cursor cursor = null;
		cursor = this.mDb.query("lists", new String[] { "name", "_id" }, null,
				null, null, null, "name asc");
		return cursor;
	}

	public String getSourceLanguage(int listId) {
		String result;
		Cursor cursor = this.mDb.query("lists",
				new String[] { "sourceLanguage" }, "_id = ?",
				new String[] { Integer.toString(listId) }, null, null, null);
		cursor.moveToFirst();
		result = cursor.getString(0);
		cursor.close();
		return result;
	}

	public String getTargetLanguage(int listId) {
		String result;
		Cursor cursor = this.mDb.query("lists",
				new String[] { "targetLanguage" }, "_id = ?",
				new String[] { Integer.toString(listId) }, null, null, null);
		cursor.moveToFirst();
		result = cursor.getString(0);
		cursor.close();
		return result;
	}

	public int getScheduleCount() {
		int result;
		Cursor cursor = this.mDb.query("schedules",
				new String[] { "count(*)" }, null, null, null, null, null);
		cursor.moveToFirst();
		result = cursor.getInt(0);
		cursor.close();
		return result;
	}

	public Schedule getScheduleByPosition(int position) {
		Schedule result;
		Cursor cursor = this.mDb.query("schedules", new String[] { "_id",
				"interval", "list_id", "series_size", "foreign_language" },
				null, null, null, null, "_id", Integer.toString(position)
						+ ", 1");
		cursor.moveToFirst();
		int interval = cursor.getInt(1);
		int seriesSize = cursor.getInt(3);
		int id = cursor.getInt(0);
		int listId = cursor.getInt(2);
		boolean sourceForeign = (cursor.getInt(4) != 0);
		cursor.close();
		result = new Schedule(id, interval, listId, seriesSize, sourceForeign);
		return result;
	}

	public String getListNameById(int id) {
		String result;
		Cursor cursor = this.mDb.query("lists", new String[] { "name" },
				"_id = ?", new String[] { Integer.toString(id) }, null, null,
				null, null);
		cursor.moveToFirst();
		result = cursor.getString(0);
		cursor.close();
		return result;
	}

	private ContentValues getContentValues(Schedule schedule) {
		ContentValues result = new ContentValues();
		int interval = schedule.getmInterval();
		int listId = schedule.getmListId();
		int seriesSize = schedule.getmSeriesSize();
		int id = schedule.getmId();
		boolean sourceForeign = schedule.ismSourceForeign();
		int foreignLanguage = sourceForeign ? 1 : 0;
		result.put("interval", interval);
		result.put("list_id", listId);
		result.put("series_size", seriesSize);
		result.put("foreign_language", foreignLanguage);
		result.put("next_timeout", (System.currentTimeMillis() / 1000));
		if (id != -1) {
			result.put("_id", id);
		}
		return result;
	}

	private void doDeleteSchedule(int scheduleId) {
		Schedule schedule = this.getSchedule(scheduleId);
		this.mDb.delete("schedules", "_id = ?",
				new String[] { Integer.toString(scheduleId) });
		int listId = schedule.getmListId();
		Cursor cursor = this.getSeriesBySchedule(scheduleId);
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int id = cursor.getInt(0);
			this.deleteSeries(id, listId);
		}
		cursor.close();
	}

	private Cursor getSeriesBySchedule(int scheduleId) {
		Cursor result = this.mDb
				.query("series", new String[] { "_id" }, "schedule_id = ?",
						new String[] { Integer.toString(scheduleId) }, null,
						null, null);
		return result;
	}

	private void deleteSeries(int seriesId, int listId) {
		this.startTransaction();
		this.doDeleteSeries(seriesId, listId);
		try {
			this.commit();
		} catch (NotInTransactionException e) {
			e.printStackTrace();
		}
	}

	private void doDeleteSeries(int seriesId, int listId) {
		ContentValues contentValues = new ContentValues();
		contentValues.put("status", ENTRY_PENDING);
		this.mDb.update("entries", contentValues, "list_id = ?",
				new String[] { Integer.toString(listId) });
		this.mDb.delete("series_content", "series_id = ?",
				new String[] { Integer.toString(seriesId) });
		this.mDb.delete("series", "_id = ?",
				new String[] { Integer.toString(seriesId) });

	}

	public void deleteSchedule(int id) {
		this.startTransaction();
		this.doDeleteSchedule(id);
		try {
			this.commit();
		} catch (NotInTransactionException e) {
			e.printStackTrace();
		}
	}

	public List getList(int id) {
		Cursor cursor = this.mDb.query("lists", new String[] { "_id", "name",
				"sourceLanguage", "targetLanguage" }, "_id = ?",
				new String[] { Integer.toString(id) }, null, null, null);
		cursor.moveToFirst();

		String name = cursor.getString(1);
		String sourceLanguageIso3 = cursor.getString(2);
		Language sourceLanguage = null;
		try {
			sourceLanguage = Language.getLanguageByIso3(sourceLanguageIso3);
		} catch (UnknownIso3LanguageException e) {
			// HELP!
			e.printStackTrace();
		}
		String targetLanguageIso3 = cursor.getString(3);
		Language targetLanguage = null;
		try {
			targetLanguage = Language.getLanguageByIso3(targetLanguageIso3);
		} catch (UnknownIso3LanguageException e) {
			// HELP!
			e.printStackTrace();
		}
		cursor.close();
		return new List(id, name, sourceLanguage, targetLanguage);
	}

	public EntryPair getEntryPairByPosition(long listId, boolean sortBySource,
			int position) {
		String orderBy = sortBySource ? "list_id, source, target"
				: "list_id, target, source";
		Cursor cursor = this.mDb.query("entry_pairs", new String[] { "source",
				"target", "entry_id" }, "list_id = ?",
				new String[] { Integer.toString((int) listId) }, null, null,
				orderBy, "" + position + ", 1");
		cursor.moveToFirst();
		String source = cursor.getString(0);
		String target = cursor.getString(1);
		int entryId = cursor.getInt(2);
		cursor.close();
		EntryPair result = new EntryPair(entryId, (int) listId, source, target);
		return result;
	}

	public int getEntryPairCount(long listId) {
		Cursor cursor = this.mDb.query("entry_pairs",
				new String[] { "count(*)" }, "list_id = ?",
				new String[] { Integer.toString((int) listId) }, null, null,
				null);
		cursor.moveToFirst();
		int result = cursor.getInt(0);
		cursor.close();
		return result;
	}

	private ContentValues getContentValues(List list) {
		String name = list.getmName();
		Language sourceLanguage = list.getmSourceLanguage();
		Language targetLanguage = list.getmTargetLanguage();
		ContentValues result = new ContentValues();
		result.put("name", name);
		String sourceLanguageString = sourceLanguage.getmIso3Language();
		String targetLanguageString = targetLanguage.getmIso3Language();
		result.put("sourceLanguage", sourceLanguageString);
		result.put("targetLanguage", targetLanguageString);
		return result;
	}

	public int putList(List list) {
		this.startTransaction();
		ContentValues contentValues = this.getContentValues(list);
		this.mDb.insert("lists", null, contentValues);
		int result = this.getLastInsertId();
		try {
			this.commit();
		} catch (NotInTransactionException e) {
			e.printStackTrace();
		}
		return result;
	}

	private int doPutEntry(Entry entry, int listId) {
		int result = entry.getmId();
		if (result != -1) {
			if (!entryExists(result)) {
				// I don't know if this can possibly happen
				return result;
			}
			this.deleteEntry(result, false, entry.getmSourceLanguage(),
					entry.getmTargetLanguage());
		}

		ContentValues contentValues = this.getContentValues(entry, listId);
		if (result == -1) {
			this.mDb.insert("entries", null, contentValues);
			result = this.getLastInsertId();
		}

		entry.setmId(result);
		LinkedList<Denomination> sourceDenominations = entry
				.getmSourceDenominations();
		this.putDenominations(result, listId, sourceDenominations, true);
		LinkedList<Denomination> targetDenominations = entry
				.getmTargetDenominations();
		this.putDenominations(result, listId, targetDenominations, false);

		LinkedList<Category> categories = entry.getmCategories();
		this.putEntryCategories(result, listId, categories);

		putEntryPairs(entry, result, listId);
		return result;
	}

	private boolean entryExists(int id) {
		Cursor cursor = mDb.query("entries", new String[] { "count(*)" },
				"_id = ?", new String[] { Integer.toString(id) }, null, null,
				null);
		cursor.moveToFirst();
		boolean result = cursor.getInt(0) > 0;
		cursor.close();
		return result;
	}

	private void putEntryCategories(int entryId, int listId,
			LinkedList<Category> categories) {
		for (Category category : categories) {
			putEntryCategory(entryId, listId, category);
		}
	}

	private void putEntryCategory(int entryId, int listId, Category category) {
		ContentValues values = getContentValues(entryId, listId, category);
		mDb.insert("entry_categories", null, values);
		mDb.rawQuery("update categories set count = count + 1 "
				+ "where _id = ?",
				new String[] { Integer.toString(category.getmId()) });
	}

	private ContentValues getContentValues(int entryId, int listId,
			Category category) {
		ContentValues result = new ContentValues();
		result.put("category_id", category.getmId());
		result.put("entry_id", entryId);
		result.put("list_id", entryId);
		return result;
	}

	private void putEntryPairs(Entry entry, int entryId, int listId) {
		LinkedList<Denomination> sourceDenominations = entry
				.getmSourceDenominations();
		LinkedList<Denomination> targetDenominations = entry
				.getmTargetDenominations();
		for (Denomination sourceDenomination : sourceDenominations) {
			for (Denomination targetDenomination : targetDenominations) {
				EntryPair entryPair = new EntryPair(entryId, listId,
						sourceDenomination.getmValue(),
						targetDenomination.getmValue());
				putEntryPair(entryPair, entryId, listId);
			}
		}
	}

	private void putEntryPair(EntryPair entryPair, int entryId, int listId) {
		ContentValues contentValues = getContentValues(entryPair, entryId,
				listId);
		this.mDb.insert("entry_pairs", null, contentValues);
	}

	private ContentValues getContentValues(EntryPair entryPair, int entryId,
			int listId) {
		String source = entryPair.getmSource();
		String target = entryPair.getmTarget();
		ContentValues result = new ContentValues();
		result.put("entry_id", entryId);
		result.put("list_id", listId);
		result.put("source", source);
		result.put("target", target);
		return result;
	}

	private void putDenominations(int entryId, int listId,
			LinkedList<Denomination> denominations, boolean source) {
		this.startTransaction();
		this.doPutDenominations(entryId, listId, denominations, source);
		try {
			this.commit();
		} catch (NotInTransactionException e) {
			e.printStackTrace();
		}
	}

	private void doPutDenominations(int entryId, int listId,
			LinkedList<Denomination> denominations, boolean source) {
		for (Denomination denomination : denominations) {
			this.putDenomination(denomination, entryId, listId, source);
		}
	}

	private void putDenomination(Denomination denomination, int entryId,
			int listId, boolean source) {
		int denominationId = denomination.getmId();
		ContentValues contentValues = this.getContentValues(denomination,
				entryId, listId, source);
		this.mDb.insert("denominations", null, contentValues);
		if (denominationId == -1) {
			denominationId = this.getLastInsertId();
		}
		putDenominationForm(denominationId, listId, new DenominationForm(-1,
				denomination.getmValue(), denominationId, null));
		denomination.setmId(denominationId);
		LinkedList<DetailsKeyValue> details = denomination.getmDetails();
		for (DetailsKeyValue detail : details) {
			this.putDetail(denominationId, detail, source, listId);
		}
		LinkedList<Example> examples = denomination.getmExamples();
		for (Example example : examples) {
			this.putExample(denominationId, listId, example);
		}
		for (DenominationForm form : denomination.getmForms()) {
			this.putDenominationForm(denominationId, listId, form);
		}
		
		wordsCheck(denomination.getmLanguage(), denomination.getmValue());
		for (DenominationForm form : denomination.getmForms()) {
			wordsCheck(denomination.getmLanguage(), form.getmValue());
		}
	}

	private void wordsCheck(String language, String value) {
		String[] tokens = value.split("\\s+");
		for (String token : tokens) {
			wordCheck(language, token);
		}
	}

	private void wordCheck(String language, String token) {
		if (!haveWord(language, token)) {
			ContentValues values = new ContentValues();
			values.put("language", language);
			values.put("value", token);
			this.mDb.insert("words", null, values );
		}
	}

	private boolean haveWord(String language, String value) {
		Cursor cursor = this.mDb.query("words", new String[] {"language"},
				"language = ? and value = ?",
				new String[] {language, value}, null, null, null);
		cursor.moveToFirst();
		boolean result;
		if (cursor.isAfterLast()) {
			result = false;
		} else {
			result = true;
		}
		cursor.close();
		return result;
	}

	private void putDenominationForm(int denominationId, int listId,
			DenominationForm form) {
		ContentValues contentValues = this.getContentValues(form,
				denominationId, listId);
		this.mDb.insert("denomination_forms", null, contentValues);
		int id;
		if ((id = form.getmId()) == -1) {
			id = getLastInsertId();
		}
		for (FormAttribute attribute : form.getmFormAttributes()) {
			int weight = attribute.getmWeight();
			String value = attribute.getmValue();
			int attributeId = attribute.getmId();
			if (attributeId == -1) {
				attributeId = getAttributeId(listId, value, weight);
				if (attributeId == -1) {
					insertFormKey(attribute, listId);
					attributeId = getLastInsertId();
				}
				attribute.setmId(attributeId);
			}
			ContentValues values = this.getContentValues(attribute,
					denominationId, id, listId);
			this.mDb.insert("denomination_form_attributes", null, values);
		}
	}

	private int getAttributeId(int listId, String value, int weight) {
		Cursor cursor = this.mDb.query(
				"form_attributes",
				new String[] { "_id" },
				"list_id = ? and value = ? and weight = ?",
				new String[] { Integer.toString(listId), value,
						Integer.toString(weight) }, null, null, null);
		cursor.moveToFirst();
		int result = !cursor.isAfterLast() ? cursor.getInt(0) : -1;
		cursor.close();
		return result;
	}

	private ContentValues getContentValues(FormAttribute attribute,
			int denominationId, int formId, int listId) {
		ContentValues result = new ContentValues();
		result.put("denomination_form_id", formId);
		result.put("form_attribute_id", attribute.getmId());
		result.put("list_id", listId);
		return result;
	}

	private ContentValues getContentValues(DenominationForm form,
			int denominationId, int listId) {
		ContentValues result = new ContentValues();
		result.put("denomination_id", denominationId);
		result.put("value", form.getmValue());
		result.put("list_id", listId);
		if (form.getmId() != -1) {
			result.put("_id", form.getmId());
		}
		return result;
	}

	private void putExample(int denominationId, int listId, Example example) {
		ContentValues contentValues = this.getContentValues(example,
				denominationId, listId);
		this.mDb.insert("examples", null, contentValues);
	}

	private ContentValues getContentValues(Example example, int denominationId,
			int listId) {
		ContentValues result = new ContentValues();

		int id = example.getmId();
		String translation = example.getmTranslation();
		String value = example.getmValue();

		result.put("denomination_id", denominationId);
		result.put("translation", translation);
		result.put("value", value);
		result.put("list_id", listId);
		if (id != -1) {
			result.put("_id", id);
		}
		return result;
	}

	private void putDetail(int denominationId, DetailsKeyValue detail,
			boolean source, int listId) {
		ContentValues contentValues = getContentValues(denominationId, listId,
				detail);
		this.mDb.insert("details_new", null, contentValues);
	}

	private ContentValues getContentValues(int denominationId, int listId,
			DetailsKeyValue detail) {
		int valueId = detail.getmId();

		ContentValues result = new ContentValues();

		result.put("denomination_id", denominationId);
		result.put("value_id", valueId);
		result.put("list_id", listId);
		return result;
	}

	private ContentValues getContentValues(Denomination denomination,
			int entryId, int listId, boolean source) {
		int id = denomination.getmId();
		ContentValues result = new ContentValues();
		result.put("entry_id", entryId);
		result.put("list_id", listId);
		String side = source ? "source" : "target";
		result.put("side", side);
		if (id != -1) {
			result.put("_id", id);
		}
		return result;
	}

	private ContentValues getContentValues(Entry entry, int listId) {
		int id = entry.getmId();
		int score = entry.getmScore();
		int status = entry.getmStatus();
		ContentValues result = new ContentValues();
		result.put("list_id", listId);
		result.put("score", score);
		result.put("status", status);
		if (id != -1) {
			result.put("_id", id);
		}
		return result;
	}


	private void doDeleteEntry(int id, boolean forever, String sourceLanguage,
			String targetLanguage) {
		if (forever) {
			this.deleteSeriesContentByEntryId(id);
		}
		this.deleteDenominationsByEntryId(id, sourceLanguage, targetLanguage);
		this.deleteEntryCategoriesbyEntryId(id);
		deleteEntryPairs(id);
		if (forever) {
			this.mDb.delete("entries", "_id = ?",
					new String[] { Integer.toString(id) });
			this.mDb.delete("no_peers", "entry1_id = ?",
					new String[] { Integer.toString(id) });
			this.mDb.delete("no_peers", "entry2_id = ?",
					new String[] { Integer.toString(id) });
			this.mDb.delete("proposed_peers", "entry1_id = ?",
					new String[] { Integer.toString(id) });
			this.mDb.delete("proposed_peers", "entry2_id = ?",
					new String[] { Integer.toString(id) });
		}
	}

	private void deleteEntryCategoriesbyEntryId(int id) {
		LinkedList<Category> categories = this.getCategories(id);
		for (Category category : categories) {
			this.mDb.rawQuery("update categories "
					+ "set count = count - 1 where _id = ?",
					new String[] { Integer.toString(category.getmId()) });
		}
		this.mDb.delete("entry_categories", "entry_id = ?",
				new String[] { Integer.toString(id) });
	}

	private void deleteEntryPairs(int id) {
		this.mDb.delete("entry_pairs", "entry_id = ?",
				new String[] { Integer.toString(id) });
	}

	private void deleteDenominationsByEntryId(int id, String sourceLanguage,
			String targetLanguage) {
		this.startTransaction();
		this.doDeleteDenominationsByEntryId(id, sourceLanguage, targetLanguage);
		try {
			this.commit();
		} catch (NotInTransactionException e) {
			e.printStackTrace();
		}
	}

	private void doDeleteDenominationsByEntryId(int id, String sourceLanguage,
			String targetLanguage) {
		LinkedList<Denomination> denominations = this.getDenominations(id,
				"source", sourceLanguage);
		for (Denomination denomination : denominations) {
			int denominationId = denomination.getmId();
			deleteDetailsByDenominationId(denominationId);
			deleteExamplesByDenominationId(denominationId);
			deleteDenominationFormsByDenominationId(denominationId);
		}
		denominations = this.getDenominations(id, "target", targetLanguage);
		for (Denomination denomination : denominations) {
			int denominationId = denomination.getmId();
			deleteDetailsByDenominationId(denominationId);
			deleteExamplesByDenominationId(denominationId);
			deleteDenominationFormsByDenominationId(denominationId);
		}
		this.mDb.delete("denominations", "entry_id = ?",
				new String[] { Integer.toString(id) });
	}

	private void deleteDenominationFormsByDenominationId(int denominationId) {
		Cursor cursor = this.mDb.query("denomination_forms",
				new String[] { "_id" }, "denomination_id = ?",
				new String[] { Integer.toString(denominationId) }, null, null,
				null);
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int id = cursor.getInt(0);
			this.mDb.delete("denomination_form_attributes",
					"denomination_form_id = ?",
					new String[] { Integer.toString(id)});
		}
		cursor.close();
		this.mDb.delete("denomination_forms", "denomination_id = ?",
				new String[] { Integer.toString(denominationId) });
	}

	private void deleteExamplesByDenominationId(int denominationId) {
		this.mDb.delete("examples", "denomination_id = ?",
				new String[] { Integer.toString(denominationId) });
	}

	private void deleteDetailsByDenominationId(int denominationId) {
		this.mDb.delete("details_new", "denomination_id = ?",
				new String[] { Integer.toString(denominationId) });
	}

	private void deleteSeriesContentByEntryId(int id) {
		this.mDb.delete("series_content", "entry_id = ?",
				new String[] { Integer.toString(id) });

	}

	public Entry getEntry(int entryId) {
		int listId = this.getListIdForEntry(entryId);
		List list = getList(listId);
		Cursor cursor = mDb.query("entries",
				new String[] { "score", "status" }, "_id = ?",
				new String[] { Integer.toString(entryId) }, null, null, null);
		cursor.moveToFirst();
		int score = cursor.getInt(0);
		int status = cursor.getInt(1);
		cursor.close();
		LinkedList<Denomination> sourceDenominations = this
				.getSourceDenominations(entryId, list.getmSourceLanguage()
						.getmIso3Language());
		LinkedList<Denomination> targetDenominations = this
				.getTargetDenominations(entryId, list.getmTargetLanguage()
						.getmIso3Language());
		LinkedList<Category> categories = this.getCategories(entryId);
		return new Entry(entryId, listId, sourceDenominations,
				targetDenominations, categories, score, status, list
						.getmSourceLanguage().getmIso3Language(), list
						.getmTargetLanguage().getmIso3Language());
	}

	private LinkedList<Category> getCategories(int entryId) {
		LinkedList<Category> result = new LinkedList<Category>();
		Cursor cursor = mDb
				.rawQuery(
						"select c._id, c.source_language, c.target_language, c.list_id "
								+ "from categories c, entry_categories ec where c._id = ec.category_id "
								+ "and ec.entry_id = ?",
						new String[] { Integer.toString(entryId) });
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int listId = cursor.getInt(3);
			String sourceLanguage = cursor.getString(1);
			int id = cursor.getInt(0);
			String targetLanguage = cursor.getString(2);
			Category category = new Category(id, sourceLanguage,
					targetLanguage, listId);
			result.add(category);
		}
		cursor.close();
		return result;
	}

	private LinkedList<Denomination> getSourceDenominations(int entryId,
			String language) {
		return this.getDenominations(entryId, "source", language);
	}

	private LinkedList<Denomination> getTargetDenominations(int entryId,
			String language) {
		return this.getDenominations(entryId, "target", language);
	}

	private LinkedList<Denomination> getDenominations(int entryId, String side,
			String language) {
		LinkedList<Denomination> result = new LinkedList<Denomination>();
		Cursor cursor = this.mDb.query("denominations", new String[] { "_id",
				"list_id" }, "entry_id = ? and side = ?", new String[] {
				Integer.toString(entryId), side }, null, null, null);
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int id = cursor.getInt(0);
			int listId = cursor.getInt(1);
			String value = getDenominationValue(id);
			LinkedList<DetailsKeyValue> details = getDetails(id);
			LinkedList<Example> examples = getDenominationExamples(id);
			LinkedList<DenominationForm> denominationForms = getDenominationForms(
					id, false);
			Denomination denomination = new Denomination(id, entryId, listId,
					value, language, examples, details, denominationForms);
			result.add(denomination);
		}
		cursor.close();
		return result;
	}

	private String getDenominationValue(int denominationId) {
		LinkedList<DenominationForm> forms = getDenominationForms(
				denominationId, true);
		String result = null;
		for (DenominationForm form : forms) {
			if (form.getmFormAttributes().size() < 1) {
				result = form.getmValue();
				break;
			}
		}
		return result;
	}

	private LinkedList<DenominationForm> getDenominationForms(
			int denominationId, boolean infinitive) {
		LinkedList<DenominationForm> result = new LinkedList<DenominationForm>();
		Cursor cursor = this.mDb.query("denomination_forms", new String[] {
				"_id", "value" }, "denomination_id = ?",
				new String[] { Integer.toString(denominationId) }, null, null,
				null);
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int id = cursor.getInt(0);
			String value = cursor.getString(1);
			LinkedList<FormAttribute> formAttributes = null;
			formAttributes = getFormAttributes(id);
			if ((!infinitive) && (formAttributes.size() < 1)) {
				continue;
			}
			DenominationForm form = new DenominationForm(id, value,
					denominationId, formAttributes);
			result.add(form);
		}
		cursor.close();
		Collections.sort(result, DenominationForm.comparator);
		return result;
	}

	private LinkedList<FormAttribute> getFormAttributes(int denominationFormId) {
		LinkedList<FormAttribute> result = new LinkedList<FormAttribute>();
		Cursor cursor = this.mDb.query("denomination_form_attributes",
				new String[] { "form_attribute_id" },
				"denomination_form_id = ?",
				new String[] { Integer.toString(denominationFormId) }, null,
				null, null);
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int id = cursor.getInt(0);
			FormAttribute formAttribute = getFormAttribute(id);
			result.add(formAttribute);
		}
		cursor.close();
		return result;
	}

	private FormAttribute getFormAttribute(int id) {
		Cursor cursor = this.mDb.query("form_attributes", new String[] {
				"side", "value", "weight" }, "_id = ?",
				new String[] { Integer.toString(id) }, null, null, null);
		cursor.moveToFirst();
		String value = cursor.getString(1);
		String side = cursor.getString(0);
		int weight = cursor.getInt(2);
		boolean source = side.equals("source");
		FormAttribute result = new FormAttribute(id, source, value, weight);
		cursor.close();
		return result;
	}

	/*private void log(String logMe) {
		Activity.log(logMe, mContext);
	}*/

	private LinkedList<Example> getDenominationExamples(int denominationId) {
		LinkedList<Example> result = new LinkedList<Example>();
		Cursor cursor = this.mDb.query("examples", new String[] { "value",
				"translation", "_id", "list_id" }, "denomination_id = ?",
				new String[] { Integer.toString(denominationId) }, null, null,
				null);
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			String value = cursor.getString(0);
			String translation = cursor.getString(1);
			int exampleId = cursor.getInt(2);
			int listId = cursor.getInt(3);
			Example example = new Example(exampleId, denominationId, listId,
					value, translation);
			result.add(example);
		}
		cursor.close();
		return result;
	}

	private int getListIdForEntry(int entryId) {
		Cursor cursor = this.mDb.query("entries", new String[] { "list_id" },
				"_id = ?", new String[] { Integer.toString(entryId) }, null,
				null, null);
		cursor.moveToFirst();
		int result = cursor.getInt(0);
		cursor.close();
		return result;
	}

	public void deleteEntry(int id, boolean forever, String sourceLanguage,
			String targetLanguage) {
		this.startTransaction();
		this.doDeleteEntry(id, forever, sourceLanguage, targetLanguage);
		try {
			this.commit();
		} catch (NotInTransactionException e) {
			e.printStackTrace();
		}
	}

	public void putSchedule(Schedule schedule) {
		ContentValues values = this.getContentValues(schedule);
		this.mDb.insert("schedules", null, values);
	}

	public Cursor searchDenomination(int id, String s, boolean bySource) {
		String side = bySource ? "source" : "target";
		String string = DatabaseUtils.sqlEscapeString("%" + s + "%");
		String selection = "d.list_id = ? and like(" + string +
				", df.value) and side = '" + side + "'";
		/*
		 * Cursor result = this.mDb.rawQuery(
		 * "select distinct d.entry_id as _id, df.value as entry " +
		 * "from denominations d, denomination_forms df where " +
		 * "d._id = df.denomination_id and " + selection, new String[] {
		 * Integer.toString(id) });
		 */
		Cursor result = this.mDb.rawQuery(
				"select distinct 42 as _id, df.value as entry "
						+ "from denominations d, denomination_forms df where "
						+ "d._id = df.denomination_id and " + selection,
				new String[] { Integer.toString(id) });
		return result;
	}

	public int getPositionForEntryPair(int id, String string, boolean bySource) {
		String select = bySource ? "source" : "target";
		String selection = "list_id = ? and " + select + " < ?";
		Cursor cursor = this.mDb.query("entry_pairs",
				new String[] { "count(*) as count" }, selection, new String[] {
						Integer.toString(id), string }, null, null, select);
		cursor.moveToFirst();
		int result = cursor.getInt(0);
		cursor.close();
		return result;
	}

	public boolean entriesScheduled() {
		boolean result = getNextEntry() != null;
		return result;
	}

	public ScheduledEntry getNextEntry() {
		Entry entry = null;
		int seriesId = -1;
		int scheduleId = -1;
		int level = -1;
		Cursor seriesCursor = this.mDb.query("series", new String[] { "_id",
				"schedule_id" }, null, null, null, null, "random()");
		boolean sourceForeign = false;
		for (seriesCursor.moveToFirst(); !(seriesCursor.isAfterLast()); seriesCursor
				.moveToNext()) {
			if (entry != null) {
				break;
			}
			seriesId = seriesCursor.getInt(0);
			scheduleId = seriesCursor.getInt(1);
			if (seriesComplete(seriesId)) {
				Cursor cursor = this.mDb.query(
						"series_content",
						new String[] { "min(level)" },
						"(status = ? or status = ?) and series_id = ?",
						new String[] { Integer.toString(ENTRY_FAILED),
								Integer.toString(ENTRY_FAILED_UP),
								Integer.toString(seriesId) }, null, null, null);
				cursor.moveToFirst();
				if (!cursor.isAfterLast()) {
					level = cursor.getInt(0);
				} else {
					level = -1;
				}
				cursor.close();
			} else {
				level = 0;
			}
			if (level != -1) {
				entry = getRandomFailedEntry(seriesId, level);
			}
		}
		seriesCursor.close();
		if (entry != null) {
			Schedule schedule = getSchedule(scheduleId);
			sourceForeign = schedule.ismSourceForeign();
			return new ScheduledEntry(entry, sourceForeign, seriesId, level);
		} else {
			return null;
		}
	}

	private Entry getRandomFailedEntry(int seriesId, int level) {
		Entry result = null;
		Cursor cursor = null;
		if ((level == 0) && (!seriesComplete(seriesId))) {
			cursor = this.mDb.query(
					"series_content",
					new String[] { "entry_id" },
					"series_id = ? and level = ? and status = ?",
					new String[] { Integer.toString(seriesId),
							Integer.toString(level),
							Integer.toString(ENTRY_FAILED) }, null, null,
					"random()", "1");
		} else {
			cursor = this.mDb
					.query("series_content",
							new String[] { "entry_id" },
							"series_id = ? and level = ? and (status = ? or status = ?)",
							new String[] { Integer.toString(seriesId),
									Integer.toString(level),
									Integer.toString(ENTRY_FAILED),
									Integer.toString(ENTRY_FAILED_UP) }, null,
							null, "random()", "1");
		}
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			int entryId = cursor.getInt(0);
			result = getEntry(entryId);
		}
		cursor.close();
		return result;
	}

	private void collectSeries(int id) {
		this.startTransaction();
		Cursor cursor = this.mDb.query("series_content",
				new String[] { "entry_id" },
				"series_id = ? and persistent_status = ?", new String[] {
						Integer.toString(id), Integer.toString(ENTRY_FAILED) },
				null, null, null);
		ContentValues contentValues = new ContentValues();
		contentValues.put("status", ENTRY_FAILED);
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int entryId = cursor.getInt(0);
			this.mDb.update("entries", contentValues, "_id = ?",
					new String[] { Integer.toString(entryId) });
		}
		cursor.close();
		cursor = this.mDb.query("series_content", new String[] { "entry_id" },
				"series_id = ? and persistent_status = ?", new String[] {
						Integer.toString(id), Integer.toString(ENTRY_OK) },
				null, null, null);
		contentValues = new ContentValues();
		contentValues.put("status", ENTRY_SWAP);
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int entryId = cursor.getInt(0);
			this.mDb.update("entries", contentValues, "_id = ?",
					new String[] { Integer.toString(entryId) });
			mDb.execSQL("update entries set score = score - 1 where _id = "
					+ entryId);
		}
		cursor.close();

		this.mDb.delete("series_content", "series_id = ?",
				new String[] { Integer.toString(id) });
		this.mDb.delete("series", "_id = ?",
				new String[] { Integer.toString(id) });
		try {
			this.commit();
		} catch (NotInTransactionException e) {
			e.printStackTrace();
		}
	}

	public void handleCorrectAnswer(int entryId) {

		this.startTransaction();
		int status = getEntryStatusInSeries(entryId);
		if (status != ENTRY_INVALID) {
			int seriesId = getSeriesIdFromEntryId(entryId);
			if (status == ENTRY_FAILED_UP) {
				moveEntryUp(entryId);
				int level = getLevelForEntry(entryId);
				if ((level > 1) || (seriesComplete(seriesId))) {
					int countAtLevel = getCountAtLevel(seriesId, (level - 1));
					if (countAtLevel < 1) {
						ContentValues contentValues = new ContentValues();
						contentValues.put("level", (level - 1));
						this.mDb.update("series_content", contentValues,
								"series_id = ? and level = ?",
								new String[] { Integer.toString(seriesId),
										Integer.toString(level) });
					}
				}
			} else {
				setEntryOk(entryId);
				int level = getLevelForEntry(entryId);
				int count = getCountAtLevel(seriesId, (level + 1));
				if (count < 1) {
					int failedCount = getFailedCountAtLevel(seriesId, level);
					if (failedCount < 1) {
						if (level < 1) {
							if (seriesComplete(seriesId)) {
								collectSeries(seriesId);
							}
						} else {
							moveEntriesDown(seriesId, level);
						}
					}
				}
			}
		}
		try {
			this.commit();
		} catch (NotInTransactionException e) {
			e.printStackTrace();
		}
	}

	private boolean seriesComplete(int seriesId) {
		Cursor cursor = this.mDb.query("series",
				new String[] { "schedule_id" }, "_id = ?",
				new String[] { Integer.toString(seriesId) }, null, null, null);
		cursor.moveToFirst();
		int scheduleId = cursor.getInt(0);
		cursor.close();

		Schedule schedule = getSchedule(scheduleId);

		return seriesComplete(seriesId, schedule);
	}

	private boolean seriesComplete(int seriesId, Schedule schedule) {
		int listId = schedule.getmListId();

		Cursor cursor = this.mDb.query("series_content",
				new String[] { "count(*) as count" }, "series_id = ?",
				new String[] { Integer.toString(seriesId) }, null, null, null);
		cursor.moveToFirst();
		int scheduled = cursor.getInt(0);
		cursor.close();

		int maxEntries = schedule.getmSeriesSize();

		cursor = this.mDb.query(
				"entries",
				new String[] { "count(*) as count" },
				"list_id = ? and status != ?",
				new String[] { Integer.toString(listId),
						Integer.toString(ENTRY_SCHEDULED) }, null, null, null);
		cursor.moveToFirst();
		int available = cursor.getInt(0);
		cursor.close();

		// Series series = new Series(seriesId, maxEntries, scheduled,
		// lowestFailedLevel, scheduleId);
		return scheduled >= maxEntries || (available < 1);
	}

	private void moveEntriesDown(int seriesId, int level) {
		ContentValues contentValues = new ContentValues();
		contentValues.put("level", 0);
		contentValues.put("status", ENTRY_FAILED);
		this.mDb.update(
				"series_content",
				contentValues,
				"series_id = ?",
				new String[] { Integer.toString(seriesId) });

		/*contentValues = new ContentValues();
		contentValues.put("status", ENTRY_FAILED);
		this.mDb.update(
				"series_content",
				contentValues,
				"series_id = ? and level = ?",
				new String[] { Integer.toString(seriesId),
						Integer.toString((level - 1)) });*/
	}

	private int getFailedCountAtLevel(int seriesId, int level) {
		Cursor cursor = this.mDb.query(
				"series_content",
				new String[] { "count(*) as count" },
				"series_id = ? and level = ? and (status = ? or status = ?)",
				new String[] { Integer.toString(seriesId),
						Integer.toString(level),
						Integer.toString(ENTRY_FAILED),
						Integer.toString(ENTRY_FAILED_UP) }, null, null, null);
		cursor.moveToFirst();
		int result = cursor.getInt(0);
		cursor.close();
		return result;
	}

	private int getLevelForEntry(int entryId) {
		Cursor cursor = this.mDb.query("series_content",
				new String[] { "level" }, "entry_id = ?",
				new String[] { Integer.toString(entryId) }, null, null, null);
		cursor.moveToFirst();
		int result = cursor.getInt(0);
		cursor.close();
		return result;
	}

	private int getCountAtLevel(int seriesId, int level) {
		Cursor cursor = this.mDb.query(
				"series_content",
				new String[] { "count(*) as count" },
				"series_id = ? and level = ?",
				new String[] { Integer.toString(seriesId),
						Integer.toString(level) }, null, null, null);
		cursor.moveToFirst();
		int result = cursor.getInt(0);
		cursor.close();
		return result;
	}

	private int getSeriesIdFromEntryId(int entryId) {
		Cursor cursor = this.mDb.query("series_content",
				new String[] { "series_id" }, "entry_id = ?",
				new String[] { Integer.toString(entryId) }, null, null, null);
		cursor.moveToFirst();
		int result = cursor.getInt(0);
		cursor.close();
		return result;
	}

	private void setEntryOk(int entryId) {
		ContentValues contentValues = new ContentValues();
		contentValues.put("status", ENTRY_OK);
		this.mDb.update("series_content", contentValues, "entry_id = ?",
				new String[] { Integer.toString(entryId) });
	}

	private void moveEntryUp(int entryId) {
		int level = getLevelForEntry(entryId);
		ContentValues contentValues = new ContentValues();
		contentValues.put("level", (level + 1));
		contentValues.put("status", ENTRY_FAILED);
		this.mDb.update("series_content", contentValues, "entry_id = ?",
				new String[] { Integer.toString(entryId) });
	}

	private int getEntryStatusInSeries(int entryId) {
		Cursor cursor = this.mDb.query("series_content",
				new String[] { "status" }, "entry_id = ?",
				new String[] { Integer.toString(entryId) }, null, null, null);
		cursor.moveToFirst();
		int result;
		if (cursor.isAfterLast()) {
			result = ENTRY_INVALID;
		} else {
			result = cursor.getInt(0);
		}
		cursor.close();
		return result;
	}

	public void handleWrongAnswer(int entryId) {
		this.startTransaction();
		ContentValues contentValues = new ContentValues();
		contentValues.put("status", ENTRY_FAILED_UP);
		contentValues.put("persistent_status", ENTRY_FAILED);
		this.mDb.update("series_content", contentValues, "entry_id = ?",
				new String[] { Integer.toString(entryId) });
		mDb.execSQL("update entries set score = score + 1 where _id = "
				+ entryId);
		try {
			this.commit();
		} catch (NotInTransactionException e) {
			e.printStackTrace();
		}
	}

	public void scheduleEntries() {
		this.startTransaction();
		Cursor schedulesCursor = this.mDb.query("schedules",
				new String[] { "_id" }, "next_timeout <= strftime('%s','now')",
				null, null, null, "random()");
		for (schedulesCursor.moveToFirst(); !(schedulesCursor.isAfterLast()); schedulesCursor
				.moveToNext()) {
			int scheduleId = schedulesCursor.getInt(0);
			Schedule schedule = getSchedule(scheduleId);
			boolean scheduled = scheduleEntry(schedule);
			if (scheduled) {
				int interval = schedule.getmInterval();
				/*
				 * this.mDb.execSQL(
				 * "update schedules set next_timeout = strftime('%s','now') + "
				 * + interval + " where _id = " + scheduleId);
				 */
				this.mDb.execSQL("update schedules set next_timeout = next_timeout + "
						+ interval + " where _id = " + scheduleId);
			}
		}
		schedulesCursor.close();
		try {
			this.commit();
		} catch (NotInTransactionException e) {
			e.printStackTrace();
		}
	}

	private Schedule getSchedule(int scheduleId) {
		Cursor cursor = this.mDb
				.query("schedules", new String[] { "interval", "list_id",
						"series_size", "foreign_language" }, "_id = ?",
						new String[] { Integer.toString(scheduleId) }, null,
						null, null);
		cursor.moveToFirst();
		int interval = cursor.getInt(0);
		int listId = cursor.getInt(1);
		int seriesSize = cursor.getInt(2);
		boolean sourceForeign = (cursor.getInt(3) != 0);
		cursor.close();
		return new Schedule(scheduleId, interval, listId, seriesSize,
				sourceForeign);
	}

	private boolean scheduleEntry(Schedule schedule) {
		return scheduleEntry(schedule, false);
	}

	private boolean scheduleEntry(Schedule schedule, boolean second) {
		int listId = schedule.getmListId();
		int entryId = -1;
		Cursor cursor = null;
		if (!second) {
			cursor = this.mDb.query(
					"entries",
					new String[] { "_id" },
					"list_id = ? and status = ?",
					new String[] { Integer.toString(listId),
							Integer.toString(ENTRY_FAILED) }, null, null,
					"random()", "1");
			cursor.moveToFirst();
			if (!cursor.isAfterLast()) {
				entryId = cursor.getInt(0);
			}
			cursor.close();
		}
		if (entryId == -1) {
			cursor = this.mDb.query(
					"entries",
					new String[] { "_id" },
					"list_id = ? and status = ?",
					new String[] { Integer.toString(listId),
							Integer.toString(ENTRY_PENDING) }, null, null,
					"score desc, random()", "1");
			cursor.moveToFirst();
			if (!cursor.isAfterLast()) {
				entryId = cursor.getInt(0);
			}
			cursor.close();
		}
		if (entryId == -1) {
			if (!second) {
				ContentValues contentValues = new ContentValues();
				contentValues.put("status", ENTRY_PENDING);
				this.mDb.update(
						"entries",
						contentValues,
						"list_id = ? and status = ?",
						new String[] { Integer.toString(listId),
								Integer.toString(ENTRY_SWAP) });
				return scheduleEntry(schedule, true);
			}
		}

		if (entryId != -1) {
			doScheduleEntry(entryId, schedule);
			return true;
		}

		return false;
	}

	private void doScheduleEntry(int entryId, Schedule schedule) {
		int scheduleId = schedule.getmId();
		int seriesId = -1;
		Cursor seriesCursor = this.mDb
				.query("series", new String[] { "_id" }, "schedule_id = ?",
						new String[] { Integer.toString(scheduleId) }, null,
						null, null);
		for (seriesCursor.moveToFirst(); !(seriesCursor.isAfterLast()); seriesCursor
				.moveToNext()) {
			seriesId = seriesCursor.getInt(0);
			if (!seriesComplete(seriesId, schedule)) {
				break;
			} else {
				seriesId = -1;
			}
		}
		seriesCursor.close();
		if (seriesId == -1) {
			seriesId = createSeries(schedule);
		}
		doScheduleEntry(entryId, schedule, seriesId);

	}

	private void doScheduleEntry(int entryId, Schedule schedule, int seriesId) {
		int listId = schedule.getmListId();
		ContentValues contentValues = new ContentValues();
		contentValues.put("list_id", listId);
		contentValues.put("entry_id", entryId);
		contentValues.put("series_id", seriesId);
		contentValues.put("level", 0);
		contentValues.put("status", ENTRY_FAILED);
		contentValues.put("persistent_status", ENTRY_OK);
		this.mDb.insert("series_content", null, contentValues);
		contentValues = new ContentValues();
		contentValues.put("status", ENTRY_SCHEDULED);
		this.mDb.update("entries", contentValues, "_id = ?",
				new String[] { Integer.toString(entryId) });
	}

	private int createSeries(Schedule schedule) {
		int listId = schedule.getmListId();
		int scheduleId = schedule.getmId();
		ContentValues contentValues = new ContentValues();
		contentValues.put("list_id", listId);
		contentValues.put("schedule_id", scheduleId);
		this.mDb.insert("series", null, contentValues);
		return getLastInsertId();
	}

	public int getDetailsKeysCount(String language) {
		Cursor cursor = this.mDb.query("details_keys_new",
				new String[] { "count(*) as count" }, "language = ?",
				new String[] { language }, null, null, null);
		cursor.moveToFirst();
		int result = cursor.getInt(0);
		cursor.close();
		return result;
	}

	public DetailsKey getDetailsKeyByPosition(String language, int position) {
		Cursor cursor = this.mDb.query("details_keys_new", new String[] {
				"_id", "value" }, "language = ?", new String[] { language },
				null, null, "value", position + ", 1");
		cursor.moveToFirst();
		int id = cursor.getInt(0);
		String value = cursor.getString(1);
		cursor.close();
		LinkedList<DetailsKeyValue> values = getDetailsKeyValues(this.mDb, id,
				value);
		DetailsKey result = new DetailsKey(value, values, id, language);
		return result;
	}

	private static LinkedList<DetailsKeyValue> getDetailsKeyValues(
			SQLiteDatabase db, int keyId, String key) {
		Cursor cursor = db.query("details_key_values_new", new String[] {
				"_id", "value" }, "details_keys_id = ?",
				new String[] { Integer.toString(keyId) }, null, null, "value");
		LinkedList<DetailsKeyValue> result = new LinkedList<DetailsKeyValue>();
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int valId = cursor.getInt(0);
			String value = cursor.getString(1);
			result.add(new DetailsKeyValue(valId, key, value));
		}
		cursor.close();
		return result;
	}

	public void putDetailsKey(DetailsKey detailsKey, String language) {
		this.startTransaction();
		this.doPutDetailsKey(detailsKey, language);
		try {
			this.commit();
		} catch (NotInTransactionException e) {
			e.printStackTrace();
		}
	}

	private int doPutDetailsKey(DetailsKey detailsKey, String language) {
		return doPutDetailsKey(this.mDb, detailsKey, language);
	}

	private static int doPutDetailsKey(SQLiteDatabase db,
			DetailsKey detailsKey, String language) {
		int entryId = detailsKey.getmId();
		if (entryId != -1) {
			updateDetailsKey(db, detailsKey, language);
		} else {
			entryId = insertDetailsKey(db, detailsKey, language);
		}
		return entryId;
	}

	private DetailsKey doGetDetailsKey(int id) {
		return doGetDetailsKey(this.mDb, id);
	}

	static private DetailsKey doGetDetailsKey(SQLiteDatabase db, int id) {
		Cursor cursor = db.query("details_keys_new", new String[] { "value",
				"language" }, "_id = ?", new String[] { Integer.toString(id) },
				null, null, null);
		cursor.moveToFirst();
		String value = cursor.getString(0);
		String language = cursor.getString(1);
		cursor.close();
		LinkedList<DetailsKeyValue> values = getDetailsKeyValues(db, id, value);
		DetailsKey result = new DetailsKey(value, values, id, language);
		return result;
	}

	private static int insertDetailsKey(SQLiteDatabase db,
			DetailsKey detailsKey, String language) {
		ContentValues contentValues = getContentValues(detailsKey, language);
		db.insert("details_keys_new", null, contentValues);
		int id = getLastInsertId(db);
		for (DetailsKeyValue detailsKeyValue : detailsKey.getmValues()) {
			putDetailsKeyValue(db, detailsKeyValue, id);
		}
		return id;
	}

	private static ContentValues getContentValues(DetailsKey detailsKey,
			String language) {
		String value = detailsKey.getmValue();
		int id = detailsKey.getmId();
		ContentValues result = new ContentValues();
		result.put("value", value);
		result.put("language", language);
		if (id != -1) {
			result.put("_id", id);
		}
		return result;
	}

	static private void putDetailsKeyValue(SQLiteDatabase db,
			DetailsKeyValue detailsKeyValue, int detailsKeyId) {
		int id = detailsKeyValue.getmId();
		if (id != -1) {
			updateDetailsKeyValue(db, detailsKeyValue, detailsKeyId);
		} else {
			insertDetailsKeyValue(db, detailsKeyValue, detailsKeyId);
		}

	}

	static private void insertDetailsKeyValue(SQLiteDatabase db,
			DetailsKeyValue detailsKeyValue, int detailsKeyId) {
		ContentValues contentValues = getContentValues(detailsKeyValue,
				detailsKeyId);
		db.insert("details_key_values_new", null, contentValues);
	}

	private static ContentValues getContentValues(
			DetailsKeyValue detailsKeyValue, int detailsKeyId) {
		String value = detailsKeyValue.getmValue();
		int id = detailsKeyValue.getmId();
		ContentValues result = new ContentValues();
		result.put("details_keys_id", detailsKeyId);
		result.put("value", value);
		if (id != -1) {
			result.put("_id", id);
		}
		return result;
	}

	static private void updateDetailsKey(SQLiteDatabase db,
			DetailsKey detailsKey, String language) {
		ContentValues contentValues = getContentValues(detailsKey, language);
		int id = detailsKey.getmId();
		db.update("details_keys_new", contentValues, "_id = ?",
				new String[] { Integer.toString(id) });
		LinkedList<DetailsKeyValue> detailsKeyValuesNew = detailsKey
				.getmValues();
		LinkedList<DetailsKeyValue> detailsKeyValuesOld = getDetailsKeyValues(
				db, id, detailsKey.getmValue());
		for (DetailsKeyValue detailsKeyValue : detailsKeyValuesOld) {
			if (detailsKeyValuesNew.contains(detailsKeyValue)) {
				continue;
			}
			deleteDetailsKeyValue(db, detailsKeyValue.getmId());
		}
		for (DetailsKeyValue detailsKeyValue : detailsKeyValuesNew) {
			putDetailsKeyValue(db, detailsKeyValue, id);
		}
	}

	static private void deleteDetailsKeyValue(SQLiteDatabase db, int id) {
		db.delete("details_key_values_new", "_id = ?",
				new String[] { Integer.toString(id) });
		db.delete("details_new", "value_id = ?",
				new String[] { Integer.toString(id) });
	}

	static private void updateDetailsKeyValue(SQLiteDatabase db,
			DetailsKeyValue detailsKeyValue, int detailsKeyId) {
		ContentValues contentValues = getContentValues(detailsKeyValue,
				detailsKeyId);
		int id = detailsKeyValue.getmId();
		db.update("details_key_values_new", contentValues, "_id = ?",
				new String[] { Integer.toString(id) });
	}

	public void deleteDetailsKey(int id) {
		this.startTransaction();
		this.doDeleteDetailsKey(id);
		try {
			this.commit();
		} catch (NotInTransactionException e) {
			e.printStackTrace();
		}
	}

	private void doDeleteDetailsKey(int id) {
		DetailsKey detailsKey = getDetailsKey(id);
		LinkedList<DetailsKeyValue> detailsKeyValues = detailsKey.getmValues();
		this.mDb.delete("details_keys_new", "_id = ?",
				new String[] { Integer.toString(id) });

		for (DetailsKeyValue detailsKeyValue : detailsKeyValues) {
			deleteDetailsKeyValue(this.mDb, detailsKeyValue.getmId());
		}
	}

	public DetailsKey getDetailsKey(int id) {
		DetailsKey result = doGetDetailsKey(id);
		return result;
	}

	private LinkedList<DetailsKeyValue> getDetails(int denominationId) {
		LinkedList<DetailsKeyValue> result = new LinkedList<DetailsKeyValue>();

		Cursor cursor = this.mDb
				.rawQuery(
						"select dv.value, dk.value, dv._id, d.list_id "
								+ "from details_keys_new dk, details_key_values_new dv, details_new d "
								+ "where dv.details_keys_id = dk._id "
								+ "and d.value_id = dv._id "
								+ "and d.denomination_id = ?",
						new String[] { Integer.toString(denominationId) });

		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			String value = cursor.getString(0);
			String key = cursor.getString(1);
			int detailId = cursor.getInt(2);
			DetailsKeyValue detailsKeyValue = new DetailsKeyValue(detailId,
					key, value);
			result.add(detailsKeyValue);
		}

		cursor.close();
		return result;
	}

	public Cursor getDetailsKeys(Database database, String language) {
		Cursor result = this.mDb.query("details_keys_new", new String[] {
				"value", "_id" }, "language = ?", new String[] { language },
				null, null, "value");
		return result;
	}

	public Cursor getDetailsKeyValues(Database mDatabase, int keyId) {
		Cursor result = this.mDb.query("details_key_values_new", new String[] {
				"value", "_id" }, "details_keys_id = ?",
				new String[] { Integer.toString(keyId) }, null, null, "value");
		return result;
	}

	public boolean listIsScheduled(int listId) {
		Cursor cursor = this.mDb.query("schedules",
				new String[] { "count(*)" }, "list_id = ?",
				new String[] { Integer.toString(listId) }, null, null, null);
		cursor.moveToFirst();
		boolean result = cursor.getInt(0) > 0;
		cursor.close();
		return result;
	}

	public void deleteScheduleByListId(int listId) {
		Cursor cursor = this.mDb.query("schedules", new String[] { "_id" },
				"list_id = ?", new String[] { Integer.toString(listId) }, null,
				null, null);
		cursor.moveToFirst();
		int id = cursor.getInt(0);
		cursor.close();
		deleteSchedule(id);
	}

	public void putEntries(ArrayList<Entry> entries, int listId) {
		this.startTransaction();
		for (Entry entry : entries) {
			this.doPutEntry(entry, listId);
		}
		try {
			this.commit();
		} catch (NotInTransactionException e) {
			e.printStackTrace();
		}
	}

	public int getCategoriesCountByListId(int listId) {
		int result;
		Cursor cursor = this.mDb.query("categories",
				new String[] { "count(*)" }, "list_id = ?",
				new String[] { Integer.toString(listId) }, null, null, null);
		cursor.moveToFirst();
		result = cursor.getInt(0);
		cursor.close();
		return result;
	}

	public Category getCategoryByPosition(int listId, int pos) {
		Cursor cursor = this.mDb.query("categories", new String[] { "_id",
				"source_language", "target_language", "list_id" },
				"list_id = ?", new String[] { Integer.toString(listId) }, null,
				null, "count desc, _id asc", pos + ", 1");
		cursor.moveToFirst();
		int id = cursor.getInt(0);
		String sourceLanguage = cursor.getString(1);
		String targetLanguage = cursor.getString(2);
		listId = cursor.getInt(3);
		cursor.close();
		return new Category(id, sourceLanguage, targetLanguage, listId);
	}

	public void putCategory(Category category, int listId) {
		this.startTransaction();
		this.doPutCategory(category, listId);
		try {
			this.commit();
		} catch (NotInTransactionException e) {
			e.printStackTrace();
		}
	}

	private void doPutCategory(Category category, int listId) {
		int id = category.getmId();
		if (id == -1) {
			insertCategory(category, listId);
		} else {
			updateCategory(category, listId);
		}
	}

	private void updateCategory(Category category, int listId) {
		ContentValues values = getContentValues(category, listId);
		mDb.update("categories", values, "_id = ?",
				new String[] { Integer.toString(category.getmId()) });
	}

	private ContentValues getContentValues(Category category, int listId) {
		ContentValues result = new ContentValues();
		int id = category.getmId();
		if (id != -1) {
			result.put("_id", id);
		}
		String sourceLanguage = category.getmSourceLanguage();
		result.put("source_language", sourceLanguage);
		String targetLanguage = category.getmTargetLanguage();
		result.put("target_language", targetLanguage);
		result.put("list_id", listId);
		return result;
	}

	private void insertCategory(Category category, int listId) {
		ContentValues values = getContentValues(category, listId);
		mDb.insert("categories", null, values);
	}

	public void deleteCategory(int categoryId) {
		this.startTransaction();
		this.doDeleteCategory(categoryId);
		try {
			this.commit();
		} catch (NotInTransactionException e) {
			e.printStackTrace();
		}
	}

	private void doDeleteCategory(int categoryId) {
		mDb.delete("entry_categories", "category_id = ?",
				new String[] { Integer.toString(categoryId) });
		mDb.delete("categories", "_id = ?",
				new String[] { Integer.toString(categoryId) });
	}

	/*
	 * public void exportList(int listId, String filePath, Updatable updatable)
	 * throws Exception { BufferedWriter writer = new BufferedWriter(new
	 * FileWriter(filePath, false)); IOException throwMe = null; try {
	 * exportListToWriter(listId, writer, updatable); } catch (IOException e) {
	 * throwMe = e; e.printStackTrace(); } writer.close(); if (throwMe != null)
	 * { new File(filePath).delete(); throw throwMe; } }
	 */
	public void exportList(int listId, String filePath, Updatable updatable)
			throws IOException {
		DataOutputStream os = new DataOutputStream(new FileOutputStream(
				filePath));
		exportListToDataOutputStream(listId, os, updatable);
		os.close();
	}

	private void exportListToDataOutputStream(int listId, DataOutputStream os,
			Updatable updatable) throws IOException {
		Cursor cursor = mDb.query("entries", new String[] { "count(*)" },
				"list_id = ?", new String[] { Integer.toString(listId) }, null,
				null, null);
		cursor.moveToFirst();
		int count = cursor.getInt(0);
		updatable.update(count);
		cursor.close();
		cursor = this.mDb.query("entries", new String[] { "_id" },
				"list_id = ?", new String[] { Integer.toString(listId) }, null,
				null, null);
		int onePercent = count / 100;
		if (onePercent < 1) {
			onePercent = 1;
		}
		os.writeInt(count);
		exportToOsFromCursor(cursor, onePercent, os, updatable);
		cursor.close();
	}

	/*
	 * private void exportListToWriter(int listId, BufferedWriter writer,
	 * Updatable updatable) throws Exception { Cursor cursor =
	 * mDb.query("entries", new String[] { "count(*)" }, "list_id = ?", new
	 * String[] { Integer.toString(listId) }, null, null, null);
	 * cursor.moveToFirst(); int count = cursor.getInt(0);
	 * updatable.update(count); cursor.close(); cursor =
	 * this.mDb.query("entries", new String[] { "_id" }, "list_id = ?", new
	 * String[] { Integer.toString(listId) }, null, null, null); int onePercent
	 * = count / 100; if (onePercent < 1) { onePercent = 1; } writer.write(count
	 * + "\n");
	 * 
	 * exportFromCursor(cursor, onePercent, writer, updatable); cursor.close();
	 * }
	 */

	/*
	 * private void exportFromCursor(Cursor cursor, int onePercent,
	 * BufferedWriter writer, Updatable updatable) throws Exception { int
	 * progress = 0; int processed = 0; for (cursor.moveToFirst();
	 * !cursor.isAfterLast(); cursor.moveToNext()) { int id = cursor.getInt(0);
	 * Entry entry = getEntry(id); JSONObject jsonEntry = entry2Json(entry);
	 * writer.write(jsonEntry.toString(4)); processed++; int newProgress =
	 * processed / onePercent; if (newProgress > progress) { progress =
	 * newProgress; updatable.update(processed); } } }
	 */

	private void exportToOsFromCursor(Cursor cursor, int onePercent,
			DataOutputStream os, Updatable updatable) throws IOException {
		int progress = 0;
		int processed = 0;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int id = cursor.getInt(0);
			Entry entry;
			try {
				entry = getEntry(id);
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			exportEntryToOs(entry, os);
			processed++;
			int newProgress = processed / onePercent;
			if (newProgress > progress) {
				progress = newProgress;
				updatable.update(processed);
			}
		}
	}

	private void exportEntryToOs(Entry entry, DataOutputStream os)
			throws IOException {
		exportEntryCategoriesToOs(entry.getmCategories(), os);
		os.writeInt(entry.getmScore());
		exportEntryDenominationsToOs(entry.getmSourceDenominations(), os);
		exportEntryDenominationsToOs(entry.getmTargetDenominations(), os);
		os.writeInt(entry.getmStatus());
	}

	private Entry readEntryFromDataInputStream(DataInputStream is,
			List list) throws IOException {
		String sourceLanguage = list.getmSourceLanguage().getmIso3Language();
		String targetLanguage = list.getmTargetLanguage().getmIso3Language();
		LinkedList<Category> categories = readCategoriesFromIs(is, list.getmId());
		int score = is.readInt();
		//TODO
		score = 0;
		LinkedList<Denomination> sourceDenominations =
				readDenominations(is, list.getmSourceLanguage().getmIso3Language(), true, list.getmId());
		LinkedList<Denomination> targetDenominations =
				readDenominations(is, list.getmTargetLanguage().getmIso3Language(), false, list.getmId());
		int status = is.readInt();
		return new Entry(-1, -1,
				sourceDenominations, targetDenominations,
				categories, score, status,
				sourceLanguage, targetLanguage);
	}



	private void exportEntryDenominationsToOs(
			LinkedList<Denomination> denominations, DataOutputStream os)
			throws IOException {
		os.writeInt(denominations.size());
		for (Denomination denomination : denominations) {
			exportEntryDenominationToOs(denomination, os);
		}
	}
	
	private LinkedList<Denomination> readDenominations(DataInputStream is,
			String language, boolean source, int listId) throws IOException {
		int size = is.readInt();
		LinkedList<Denomination> result = new LinkedList<Denomination>();
		for (int i = 0; i < size; i++) {
			Denomination denomination = readDenomination(is, language, source, listId);
			result.add(denomination);
		}
		return result;
	}


	private void exportStringToOs(String string, DataOutputStream os)
			throws IOException {
		char[] value = string.toCharArray();
		os.writeInt(value.length);
		for (int i = 0; i < value.length; i++) {
			os.writeChar(value[i]);
		}
	}
	
	private String readString(DataInputStream is) throws IOException {
		int length = is.readInt();
		char[] array = new char[length];
		for (int i = 0; i < length; i++) {
			array[i] = is.readChar();
		}
		return new String(array);
	}

	private void exportEntryDenominationToOs(Denomination denomination,
			DataOutputStream os) throws IOException {
		exportDenominationDetailsToOs(denomination.getmDetails(), os);
		exportDenominationExamplesToOs(denomination.getmExamples(), os);
		exportDenominationFormsToOs(denomination.getmForms(), os);
		exportStringToOs(denomination.getmValue(), os);
	}
	
	private Denomination readDenomination(DataInputStream is,
			String language, boolean source, int listId) throws IOException {
		LinkedList<DetailsKeyValue> details =
				readDetails(is, source, listId, language);
		LinkedList<Example> examples =
				readExamples(is);
		LinkedList<DenominationForm> forms =
				readForms(is, source, listId);
		String value = readString(is);
		return new Denomination(-1, -1, -1, value,
				language, examples, details, forms);
	}



	private void exportDenominationFormsToOs(
			LinkedList<DenominationForm> forms, DataOutputStream os)
			throws IOException {
		os.writeInt(forms.size());
		for (DenominationForm form : forms) {
			exportDenominationFormToOs(form, os);
		}
	}
	
	private LinkedList<DenominationForm> readForms(DataInputStream is, boolean source, int listId) throws IOException {
		int length = is.readInt();
		LinkedList<DenominationForm> result = new LinkedList<DenominationForm>();
		for (int i = 0; i < length; i++) {
			DenominationForm form = readForm(is, source, listId);
			result.add(form);
		}
		return result;
	}

	private DenominationForm readForm(DataInputStream is, boolean source, int listId) throws IOException {
		String value = readString(is);
		LinkedList<FormAttribute> formAttributes =
				readFormAttributes(is, source, listId);
		return new DenominationForm(-1, value, -1, formAttributes);
	}

	private void exportDenominationFormToOs(DenominationForm form,
			DataOutputStream os) throws IOException {
		exportStringToOs(form.getmValue(), os);
		exportFormAttributesToOs(form.getmFormAttributes(), os);
	}

	private void exportFormAttributesToOs(LinkedList<FormAttribute> attributes,
			DataOutputStream os) throws IOException {
		os.writeInt(attributes.size());
		for (FormAttribute attribute : attributes) {
			exportFormAttributeToOs(attribute, os);
		}
	}
	
	private LinkedList<FormAttribute> readFormAttributes(DataInputStream is, boolean source,
			int listId) throws IOException {
		int length = is.readInt();
		LinkedList<FormAttribute> result = new LinkedList<FormAttribute>();
		for (int i = 0; i < length; i++) {
			FormAttribute formAttribute = readFormAttribute(is, source, listId);
			result.add(formAttribute);
		}
		return result;
	}

	private FormAttribute readFormAttribute(DataInputStream is, boolean source, int listId) throws IOException {
		String value = readString(is);
		return getFormAttribute(listId, value, source);
	}

	private FormAttribute getFormAttribute(int listId, String value, boolean source) {
		String side = source ? "source" : "target";
		Cursor cursor = this.mDb.query("form_attributes", new String[]{"_id", "weight"},
				"list_id = ? and value = ? and side = ?",
				new String[]{Integer.toString(listId), value, side},
				null, null, null);
		cursor.moveToFirst();
		if (cursor.isAfterLast()) {
			cursor.close();
			putFormKey(new FormAttribute(-1, source, value, 0), listId);
			return getFormAttribute(listId, value, source);
		}
		int id = cursor.getInt(0);
		int weight = cursor.getInt(1);
		cursor.close();
		return new FormAttribute(id, source, value, weight);
	}

	private void exportFormAttributeToOs(FormAttribute attribute,
			DataOutputStream os) throws IOException {
		exportStringToOs(attribute.getmValue(), os);
	}

	private void exportDenominationExamplesToOs(LinkedList<Example> examples,
			DataOutputStream os) throws IOException {
		os.writeInt(examples.size());
		for (Example example : examples) {
			exportDenominationExampleToOs(example, os);
		}
	}
	
	private LinkedList<Example> readExamples(DataInputStream is) throws IOException {
		LinkedList<Example> result = new LinkedList<Example>();
		int length = is.readInt();
		for (int i = 0; i < length; i++) {
			Example example = readExample(is);
			result.add(example);
		}
		return result;
	}


	private Example readExample(DataInputStream is) throws IOException {
		String translation = readString(is);
		String value = readString(is);
		return new Example(-1, -1, -1, value, translation);
	}

	private void exportDenominationExampleToOs(Example example,
			DataOutputStream os) throws IOException {
		exportStringToOs(example.getmTranslation(), os);
		exportStringToOs(example.getmValue(), os);
	}

	private void exportDenominationDetailsToOs(
			LinkedList<DetailsKeyValue> details, DataOutputStream os)
			throws IOException {
		os.writeInt(details.size());
		for (DetailsKeyValue detail : details) {
			exportDenominationDetailToOs(detail, os);
		}
	}
	
	private LinkedList<DetailsKeyValue> readDetails(DataInputStream is,
			boolean source, int listId, String language) throws IOException {
		LinkedList<DetailsKeyValue> result = new LinkedList<DetailsKeyValue>();
		int length = is.readInt();
		for (int i = 0; i < length; i++) {
			DetailsKeyValue detail = readDetail(is, source, listId, language);
			result.add(detail);
		}
		return result;
	}

	private DetailsKeyValue readDetail(DataInputStream is, boolean source,
			int listId, String language) throws IOException {
		String key = readString(is);
		String value = readString(is);
		int id = getDetailId(key, value, source, listId, language);
		return new DetailsKeyValue(id, key, value);
	}

	private void exportDenominationDetailToOs(DetailsKeyValue detail,
			DataOutputStream os) throws IOException {
		exportStringToOs(detail.getmKey(), os);
		exportStringToOs(detail.getmValue(), os);
	}

	private void exportEntryCategoriesToOs(LinkedList<Category> categories,
			DataOutputStream os) throws IOException {
		os.writeInt(categories.size());
		for (Category category : categories) {
			exportEntryCategoryToOs(category, os);
		}
	}
	
	private LinkedList<Category> readCategoriesFromIs(DataInputStream is, int listId)
			throws IOException {
		LinkedList<Category> result = new LinkedList<Category>();
		int length = is.readInt();
		for (int i = 0; i < length; i++) {
			Category category = readCategory(is, listId);
			result.add(category);
		}
		return result;
	}

	private Category readCategory(DataInputStream is, int listId) throws IOException {
		String source = readString(is);
		String target = readString(is);
		return getCategory(source, target, listId);
	}

	private void exportEntryCategoryToOs(Category category, DataOutputStream os)
			throws IOException {
		exportStringToOs(category.getmSourceLanguage(), os);
		exportStringToOs(category.getmTargetLanguage(), os);
	}

	private void proposePeers(int listId) {
		EntryPeers peers = findPeers();// doFindPeers(listId);
		if (peers != null) {
			if (!isNoPeers(peers)) {
				ContentValues values = getContentValues(peers, listId);
				this.mDb.insert("proposed_peers", null, values);
			}
		}
	}

	private boolean isNoPeers(EntryPeers peers) {
		Cursor cursor = this.mDb.query(
				"no_peers",
				new String[] { "count(*)" },
				"entry1_id = ? and entry2_id = ?",
				new String[] { Integer.toString(peers.getmId1()),
						Integer.toString(peers.getmId2()) }, null, null, null);
		cursor.moveToFirst();
		boolean result = (cursor.getInt(0) > 0);
		cursor.close();
		return result;
	}

	public void proposePeers() {
		this.startTransaction();

		Cursor cursor = this.mDb.query("lists", new String[] { "_id" }, null,
				null, null, null, "random()", "1");

		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			int listId = cursor.getInt(0);
			proposePeers(listId);
		}
		cursor.close();
		try {
			this.commit();
		} catch (NotInTransactionException e) {
			e.printStackTrace();
		}
	}

	private ContentValues getContentValues(EntryPeers peers, int listId) {
		ContentValues result = new ContentValues();
		result.put("entry1_id", peers.getmId1());
		result.put("entry2_id", peers.getmId2());
		result.put("list_id", listId);
		return result;
	}

	/*
	 * private EntryPeers findPeersById(int listId, int random, boolean
	 * tryAgain) { String column = (random == 1) ? "source" : "target";
	 * 
	 * /*Cursor cursor = this.mDb.rawQuery("select e2.entry_id from " +
	 * "entry_pairs e1, entry_pairs e2 where " +
	 * "e2.list_id = ? and e2.entry_id > ? and " +
	 * "e1.list_id = e2.list_id and e1.entry_id = ? and " + "e2." + column +
	 * " = e1." + column + " order by random() limit 1", new
	 * String[]{Integer.toString(listId), Integer.toString(id1),
	 * Integer.toString(id1)});
	 * 
	 * Cursor cursor = this.mDb.rawQuery("select d2.entry_id from " +
	 * "denomination_forms df2, denomination_forms df1, " +
	 * "denominations d2, denominations d1 " +
	 * "where df2.denomination_id = d2._id " +
	 * "and df1.denomination_id = d1._id " + "and df1.value = df2.value " +
	 * "and d2.entry_id <> d1.entry_id " + "and d1.entry_id = ? and " +
	 * "d1.list_id = d2.list_id and " + "d1.side = d2.side", null);
	 * 
	 * cursor.moveToFirst(); EntryPeers result = null; if
	 * (!cursor.isAfterLast()) { int id2 = cursor.getInt(0); result = new
	 * EntryPeers(id1, id2); } cursor.close(); if ((result == null)&&(tryAgain))
	 * { result = findPeersById(listId, id1, ((random + 1) % 2), false); }
	 * return result; }
	 */

	private EntryPeers findPeers() {
		
		int randomId = getRandomEntryId();
		
		if (randomId == -1) {
			return null;
		}

		Cursor cursor = this.mDb.rawQuery(
				"select d1.entry_id, d2.entry_id from "
						+ "denomination_forms df2, denomination_forms df1, "
						+ "denominations d2, denominations d1 "
						+ "where df2.denomination_id = d2._id "
						+ "and df1.denomination_id = d1._id "
						+ "and df1.value = df2.value "
						+ "and d2.entry_id <> ? "
						+ "and d1.entry_id = ? and "
						+ "d1.list_id = d2.list_id and " + "d1.side = d2.side",
				new String[]{Integer.toString(randomId), Integer.toString(randomId)});

		cursor.moveToFirst();
		EntryPeers result = null;
		if (!cursor.isAfterLast()) {
			int id1 = cursor.getInt(0);
			int id2 = cursor.getInt(1);
			result = new EntryPeers(id1, id2);
		}
		cursor.close();
		return result;
	}

	private int getRandomEntryId() {
		Cursor cursor = this.mDb.query("entries", new String[]{"_id"},
				null, null, null, null, "random()", "1");
		cursor.moveToFirst();
		int result = -1;
		if (!cursor.isAfterLast()) {
			result = cursor.getInt(0);
		}
		cursor.close();
		return result;
	}

	public LinkedList<String> getInfinitives(int listId, String string,
			boolean source) {
		LinkedList<String> result = doGetInfinitives(listId, string, source);
		return result;
	}

	private LinkedList<String> doGetInfinitives(int listId, String string,
			boolean source) {
		LinkedList<String> result = new LinkedList<String>();
		String side = source ? "source" : "target";
		Cursor cursor = this.mDb.rawQuery("select d._id "
				+ "from denominations d, denomination_forms df "
				+ "where d._id = df.denomination_id and "
				+ "d.side = ? and d.list_id = ? and df.value = ?",
				new String[] { side, Integer.toString(listId), string });
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			int denominationId = cursor.getInt(0);
			String infinitive = getDenominationValue(denominationId);
			if (!result.contains(infinitive)) {
				result.add(getDenominationValue(denominationId));
			}
		}
		cursor.close();
		return result;
	}

	public int getFormKeysCount(int listId, boolean source) {
		int result = doGetFormKeysCount(listId, source);
		return result;
	}
	
	public Cursor getFormAttributes(int listId, boolean source, String string) {
		Cursor result;
		result = doGetFormAttributes(listId, source, string);
		return result;
	}

	private int doGetFormKeysCount(int listId, boolean source) {
		String side = source ? "source" : "target";
		Cursor cursor = this.mDb.query("form_attributes",
				new String[] { "count(*)" }, "list_id = ? and side = ?",
				new String[] { Integer.toString(listId), side }, null, null,
				null);
		cursor.moveToFirst();
		int result = cursor.getInt(0);
		cursor.close();
		return result;
	}
	
	private Cursor doGetFormAttributes(int listId, boolean source, String value) {
		String side = source ? "source" : "target";
		String escaped = DatabaseUtils.sqlEscapeString("%" + value + "%");
		String likeSelection = "like(" + escaped + ", value)";
		Cursor result = this.mDb.query("form_attributes",
				new String[] { "_id", "value" }, "list_id = ? and side = ? and " + likeSelection,
				new String[] { Integer.toString(listId), side }, null, null,
				null);
		return result;
	}

	public FormAttribute getFormKeyByPosition(int listId, boolean source,
			int position) {
		FormAttribute result = doGetFormKeyByPosition(listId, source, position);
		return result;
	}

	private FormAttribute doGetFormKeyByPosition(int listId, boolean source,
			int position) {
		String side = source ? "source" : "target";
		Cursor cursor = this.mDb.query("form_attributes", new String[] { "_id",
				"list_id", "value", "weight" }, "list_id = ? and side = ?",
				new String[] { Integer.toString(listId), side }, null, null,
				"weight, _id", Integer.toString(position) + ", 1");
		cursor.moveToFirst();
		int id = cursor.getInt(0);
		String value = cursor.getString(2);
		int weight = cursor.getInt(3);
		cursor.close();
		return new FormAttribute(id, source, value, weight);
	}

	public void putFormKey(FormAttribute formAttribute, int listId) {
		doPutFormKey(formAttribute, listId);
	}

	private void doPutFormKey(FormAttribute formAttribute, int listId) {
		if (formAttribute.getmId() == -1) {
			insertFormKey(formAttribute, listId);
		} else {
			updateFormKey(formAttribute, listId);
		}
	}

	private void updateFormKey(FormAttribute formAttribute, int listId) {
		int id = formAttribute.getmId();
		ContentValues contentValues = getContentValues(formAttribute, listId);
		this.mDb.update("form_attributes", contentValues, "_id = ?",
				new String[] { Integer.toString(id) });
	}

	private ContentValues getContentValues(FormAttribute formAttribute,
			int listId) {
		String value = formAttribute.getmValue();
		String side = formAttribute.ismSource() ? "source" : "target";
		int id = formAttribute.getmId();
		int weight = formAttribute.getmWeight();
		ContentValues result = new ContentValues();
		result.put("list_id", listId);
		result.put("value", value);
		result.put("side", side);
		result.put("weight", weight);
		if (id != -1) {
			result.put("_id", id);
		}
		return result;
	}

	private void insertFormKey(FormAttribute formAttribute, int listId) {
		ContentValues contentValues = getContentValues(formAttribute, listId);
		this.mDb.insert("form_attributes", null, contentValues);
	}

	public FormAttribute getFormKey(int id) {
		FormAttribute result = doGetFormKey(id);
		return result;
	}

	private FormAttribute doGetFormKey(int id) {
		Cursor cursor = this.mDb.query("form_attributes", new String[] { "_id",
				"side", "value", "weight" }, "_id = ?",
				new String[] { Integer.toString(id) }, null, null, null);
		cursor.moveToFirst();
		String side = cursor.getString(1);
		String value = cursor.getString(2);
		int weight = cursor.getInt(3);
		cursor.close();
		return new FormAttribute(id, "source".equals(side), value, weight);
	}

	public void deleteFormKey(int id) {
		this.mDb.delete("form_attributes", "_id = ?",
				new String[] { Integer.toString(id) });
		this.mDb.delete("denomination_form_attributes",
				"form_attribute_id = ?", new String[] { Integer.toString(id) });
	}

	private DetailsKey doGetDetailsKey(DetailsKeyValue detailsKeyValue) {
		Cursor cursor = mDb.query("details_key_values_new",
				new String[] { "details_keys_id" }, "_id = ?",
				new String[] { Integer.toString(detailsKeyValue.getmId())},
				null, null, null);
		cursor.moveToFirst();
		int id = -1;
		if (!cursor.isAfterLast()) {
			id = cursor.getInt(0);
		}
		cursor.close();
		return doGetDetailsKey(id);
	}

	public DetailsKey getDetailsKey(DetailsKeyValue detailsKeyValue) {
		DetailsKey result = doGetDetailsKey(detailsKeyValue);
		return result;
	}

	public boolean peersPending() {
		boolean result = false;
		Cursor cursor = this.mDb.query("proposed_peers",
				new String[] { "count(*)" }, null, null, null, null, null);
		cursor.moveToFirst();
		result = (cursor.getInt(0) > 0);
		cursor.close();
		return result;
	}

	public EntryPeers getProposedEntryPeers() {
		EntryPeers result = null;
		Cursor cursor = this.mDb.query("proposed_peers", new String[] {
				"entry1_id", "entry2_id" }, null, null, null, null, "random()",
				"1");
		cursor.moveToFirst();
		if (!cursor.isAfterLast()) {
			int id1 = cursor.getInt(0);
			int id2 = cursor.getInt(1);
			result = new EntryPeers(id1, id2);
		}
		cursor.close();
		return result;
	}

	public void doNotmergeEntries(int id1, int id2, int listId) {
		int tmp = id1;
		id1 = (id2 < id2) ? id2 : id1;
		if (id1 != tmp) {
			id2 = tmp;
		}
		this.startTransaction();
		this.mDb.delete("proposed_peers", "(entry1_id = ? and entry2_id = ?)",
				new String[] { Integer.toString(id1), Integer.toString(id2) });
		EntryPeers entryPeers = new EntryPeers(id1, id2);
		this.mDb.insert("no_peers", null, getContentValues(entryPeers, listId));
		try {
			this.commit();
		} catch (NotInTransactionException e) {
			e.printStackTrace();
		}
	}

	public boolean isFormKeyReferenced(int id) {
		Cursor cursor = this.mDb.query("denomination_form_attributes",
				new String[] { "denomination_form_id" },
				"form_attribute_id = ?", new String[] { Integer.toString(id) },
				null, null, null, "1");
		cursor.moveToFirst();
		boolean result = false;
		if (!cursor.isAfterLast()) {
			result = true;
		}
		cursor.close();
		return result;
	}

	public boolean isDetailsKeyReferenced(int id) {
		Cursor cursor = this.mDb.query("details_key_values_new",
				new String[] { "details_keys_id" }, "details_keys_id = ?",
				new String[] { Integer.toString(id) }, null, null, null, "1");
		cursor.moveToFirst();
		boolean result = false;
		if (!cursor.isAfterLast()) {
			result = true;
		}
		cursor.close();
		return result;
	}

	public boolean isCategoryKeyReferenced(int id) {
		Cursor cursor = this.mDb.query("entry_categories",
				new String[] { "category_id" }, "category_id = ?",
				new String[] { Integer.toString(id) }, null, null, null, "1");
		cursor.moveToFirst();
		boolean result = false;
		if (!cursor.isAfterLast()) {
			result = true;
		}
		cursor.close();
		return result;
	}

	public boolean isDetailsKeyValueReferenced(int id) {
		Cursor cursor = this.mDb.query("details_new",
				new String[] { "value_id" }, "value_id = ?",
				new String[] { Integer.toString(id) }, null, null, null, "1");
		cursor.moveToFirst();
		boolean result = false;
		if (!cursor.isAfterLast()) {
			result = true;
		}
		cursor.close();
		return result;
	}

	public void importListFromDataInputStream(List list,
			DataInputStream dataInputStream, Updatable updatable) {
		this.startTransaction();
		try {
			this.doImportListFromDataInputStream(list, dataInputStream,
					updatable);
			try {
				this.commit();
			} catch (NotInTransactionException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			try {
				this.rollback();
			} catch (NotInTransactionException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}
	}

	private void doImportListFromEntryPairList(List list,
			LinkedList<EntryPair> entryPairList, Updatable updatable) {
		int listId = list.getmId();
		if (listId == -1) {
			listId = putList(list);
		}
		list = getList(listId);
		int processed = 0;
		int size = entryPairList.size();
		updatable.update(size);
		int onePercent = size / 100;
		if (onePercent < 1) {
			onePercent = 1;
		}

		int progress = processed;
		updatable.update(progress);
		for (EntryPair entryPair : entryPairList) {
			Entry entry = new Entry(-1, listId, null, null, null, 0,
					ENTRY_PENDING,
					list.getmSourceLanguage().getmIso3Language(), list
							.getmTargetLanguage().getmIso3Language());
			entry.addSourceDenomination(entryPair.getmSource());
			entry.addTargetDenomination(entryPair.getmTarget());
			try {
				this.doPutEntry(entry, listId);
			} catch (SQLiteConstraintException e) {
			}
			processed++;
			int newProgress = processed / onePercent;
			if (newProgress > progress) {
				progress = newProgress;
				updatable.update(processed);
			}
		}
	}

	private void doImportListFromDataInputStream(List list, DataInputStream is,
			Updatable updatable) throws IOException {
		int listId = list.getmId();
		if (listId == -1) {
			listId = putList(list);
		}
		list = getList(listId);
		int processed = 0;
		int size = is.readInt();
		updatable.update(size);
		int onePercent = size / 100;
		if (onePercent < 1) {
			onePercent = 1;
		}
		int progress = processed;
		for (int i = 0; i < size; i++) {
			Entry entry;
			try {
				entry = readEntryFromDataInputStream(is, list);
				this.doPutEntry(entry, listId);
			} catch (Exception e) {
				e.printStackTrace();
			}
			processed++;
			int newProgress = processed / onePercent;
			if (newProgress > progress) {
				progress = newProgress;
				updatable.update(processed);
			}
		}
	}

	public LinkedList<String> findWords(int listId, String w, boolean bySource) {
		LinkedList<String> result = new LinkedList<String>();
		List list = getList(listId);
		String word = DatabaseUtils.sqlEscapeString(w + "%");
		String language = bySource ? list.getmSourceLanguage().getmIso3Language() :
			list.getmTargetLanguage().getmIso3Language();
		String selection = "like(" + word + ", value) and language = ?";
		Cursor cursor = this.mDb.query("words", new String[]{"value"},
				selection, new String[]{language}, null,
				null, null);
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			result.add(cursor.getString(0));
		}
		cursor.close();
		return result;
	}
}