package org.ebur.debitum.database;

import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomOpenHelper;
import androidx.room.RoomOpenHelper.Delegate;
import androidx.room.RoomOpenHelper.ValidationResult;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.room.util.TableInfo.Column;
import androidx.room.util.TableInfo.ForeignKey;
import androidx.room.util.TableInfo.Index;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Callback;
import androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile TransactionDao _transactionDao;

  private volatile PersonDao _personDao;

  private volatile ImageDao _imageDao;

  @Override
  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(5) {
      @Override
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `txn` (`id_transaction` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `amount` INTEGER NOT NULL, `id_person` INTEGER NOT NULL, `description` TEXT, `is_monetary` INTEGER NOT NULL, `timestamp` INTEGER, `timestamp_returned` INTEGER, FOREIGN KEY(`id_person`) REFERENCES `person`(`id_person`) ON UPDATE NO ACTION ON DELETE NO ACTION )");
        _db.execSQL("CREATE INDEX IF NOT EXISTS `index_txn_id_person` ON `txn` (`id_person`)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `person` (`id_person` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT, `note` TEXT, `linked_contact_uri` TEXT)");
        _db.execSQL("CREATE TABLE IF NOT EXISTS `image` (`id_transaction` INTEGER NOT NULL, `filename` TEXT NOT NULL, PRIMARY KEY(`id_transaction`, `filename`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '5461cc14ef7179fe0aac281f4e83feb7')");
      }

      @Override
      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `txn`");
        _db.execSQL("DROP TABLE IF EXISTS `person`");
        _db.execSQL("DROP TABLE IF EXISTS `image`");
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onDestructiveMigration(_db);
          }
        }
      }

      @Override
      protected void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      @Override
      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        _db.execSQL("PRAGMA foreign_keys = ON");
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      @Override
      public void onPreMigrate(SupportSQLiteDatabase _db) {
        DBUtil.dropFtsSyncTriggers(_db);
      }

      @Override
      public void onPostMigrate(SupportSQLiteDatabase _db) {
      }

      @Override
      protected RoomOpenHelper.ValidationResult onValidateSchema(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsTxn = new HashMap<String, TableInfo.Column>(7);
        _columnsTxn.put("id_transaction", new TableInfo.Column("id_transaction", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTxn.put("amount", new TableInfo.Column("amount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTxn.put("id_person", new TableInfo.Column("id_person", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTxn.put("description", new TableInfo.Column("description", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTxn.put("is_monetary", new TableInfo.Column("is_monetary", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTxn.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsTxn.put("timestamp_returned", new TableInfo.Column("timestamp_returned", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysTxn = new HashSet<TableInfo.ForeignKey>(1);
        _foreignKeysTxn.add(new TableInfo.ForeignKey("person", "NO ACTION", "NO ACTION",Arrays.asList("id_person"), Arrays.asList("id_person")));
        final HashSet<TableInfo.Index> _indicesTxn = new HashSet<TableInfo.Index>(1);
        _indicesTxn.add(new TableInfo.Index("index_txn_id_person", false, Arrays.asList("id_person")));
        final TableInfo _infoTxn = new TableInfo("txn", _columnsTxn, _foreignKeysTxn, _indicesTxn);
        final TableInfo _existingTxn = TableInfo.read(_db, "txn");
        if (! _infoTxn.equals(_existingTxn)) {
          return new RoomOpenHelper.ValidationResult(false, "txn(org.ebur.debitum.database.Transaction).\n"
                  + " Expected:\n" + _infoTxn + "\n"
                  + " Found:\n" + _existingTxn);
        }
        final HashMap<String, TableInfo.Column> _columnsPerson = new HashMap<String, TableInfo.Column>(4);
        _columnsPerson.put("id_person", new TableInfo.Column("id_person", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerson.put("name", new TableInfo.Column("name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerson.put("note", new TableInfo.Column("note", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPerson.put("linked_contact_uri", new TableInfo.Column("linked_contact_uri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPerson = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPerson = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPerson = new TableInfo("person", _columnsPerson, _foreignKeysPerson, _indicesPerson);
        final TableInfo _existingPerson = TableInfo.read(_db, "person");
        if (! _infoPerson.equals(_existingPerson)) {
          return new RoomOpenHelper.ValidationResult(false, "person(org.ebur.debitum.database.Person).\n"
                  + " Expected:\n" + _infoPerson + "\n"
                  + " Found:\n" + _existingPerson);
        }
        final HashMap<String, TableInfo.Column> _columnsImage = new HashMap<String, TableInfo.Column>(2);
        _columnsImage.put("id_transaction", new TableInfo.Column("id_transaction", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsImage.put("filename", new TableInfo.Column("filename", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysImage = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesImage = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoImage = new TableInfo("image", _columnsImage, _foreignKeysImage, _indicesImage);
        final TableInfo _existingImage = TableInfo.read(_db, "image");
        if (! _infoImage.equals(_existingImage)) {
          return new RoomOpenHelper.ValidationResult(false, "image(org.ebur.debitum.database.ColorUtils).\n"
                  + " Expected:\n" + _infoImage + "\n"
                  + " Found:\n" + _existingImage);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "5461cc14ef7179fe0aac281f4e83feb7", "76139f8dad5237fe0a994ae69e29cf36");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "txn","person","image");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    boolean _supportsDeferForeignKeys = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP;
    try {
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = FALSE");
      }
      super.beginTransaction();
      if (_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA defer_foreign_keys = TRUE");
      }
      _db.execSQL("DELETE FROM `txn`");
      _db.execSQL("DELETE FROM `person`");
      _db.execSQL("DELETE FROM `image`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      if (!_supportsDeferForeignKeys) {
        _db.execSQL("PRAGMA foreign_keys = TRUE");
      }
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(TransactionDao.class, TransactionDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PersonDao.class, PersonDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ImageDao.class, ImageDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  public TransactionDao transactionDao() {
    if (_transactionDao != null) {
      return _transactionDao;
    } else {
      synchronized(this) {
        if(_transactionDao == null) {
          _transactionDao = new TransactionDao_Impl(this);
        }
        return _transactionDao;
      }
    }
  }

  @Override
  public PersonDao personDao() {
    if (_personDao != null) {
      return _personDao;
    } else {
      synchronized(this) {
        if(_personDao == null) {
          _personDao = new PersonDao_Impl(this);
        }
        return _personDao;
      }
    }
  }

  @Override
  public ImageDao imageDao() {
    if (_imageDao != null) {
      return _imageDao;
    } else {
      synchronized(this) {
        if(_imageDao == null) {
          _imageDao = new ImageDao_Impl(this);
        }
        return _imageDao;
      }
    }
  }
}
