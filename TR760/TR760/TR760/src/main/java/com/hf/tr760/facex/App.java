package com.hf.tr760.facex;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.github.yuweiguocn.library.greendao.MigrationHelper;
import com.hf.tr760.facex.greendao.gen.DaoMaster;
import com.hf.tr760.facex.greendao.gen.DaoSession;
import com.hf.tr760.facex.greendao.gen.UserDao;

import org.greenrobot.greendao.database.Database;

/**
 * @author tx
 * @date 2024/3/19 21:21
 * @target this class will do...
 */
public class App extends Application {
    DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        daoSession = getDaoSession();
    }

    public UserDao getUserDao() {
        if (daoSession != null) {
            return daoSession.getUserDao();
        }
        return null;
    }


    public DaoSession getDaoSession() {
        OnepassOpenHelper helper = new OnepassOpenHelper(this, "facex_db", null);
        Database db = helper.getWritableDb();
        DaoSession daoSession = new DaoMaster(db).newSession();
        return daoSession;
    }

    public static class OnepassOpenHelper extends DaoMaster.OpenHelper {

        public OnepassOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
            super(context, name, factory);
        }

        @Override
        public void onUpgrade(Database db, int oldVersion, int newVersion) {
            MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {

                @Override
                public void onCreateAllTables(Database db, boolean ifNotExists) {
                    DaoMaster.createAllTables(db, ifNotExists);
                }

                @Override
                public void onDropAllTables(Database db, boolean ifExists) {
                    DaoMaster.dropAllTables(db, ifExists);
                }
            }, UserDao.class);// 修改beanDao对象
        }
    }
}
