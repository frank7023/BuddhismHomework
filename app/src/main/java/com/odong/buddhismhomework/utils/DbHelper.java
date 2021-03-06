package com.odong.buddhismhomework.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.odong.buddhismhomework.R;
import com.odong.buddhismhomework.models.Book;
import com.odong.buddhismhomework.models.Channel;
import com.odong.buddhismhomework.models.Favorite;
import com.odong.buddhismhomework.models.Playlist;
import com.odong.buddhismhomework.models.Video;
import com.odong.buddhismhomework.receivers.ProgressReceiver;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by flamen on 15-2-7.
 */
public class DbHelper extends SQLiteOpenHelper {

    public List<Channel> listChannel() {
        List<Channel> channels = new ArrayList<>();
        try {
            Cursor c = getReadableDatabase().query("channels", new String[]{"cid", "title", "description", "type"}, null, null, null, null, "created ASC");

            while (c.moveToNext()) {
                Channel ch = new Channel();
                ch.setCid(c.getString(c.getColumnIndexOrThrow("cid")));
                ch.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
                ch.setType(c.getString(c.getColumnIndexOrThrow("type")));
                ch.setDescription(c.getString(c.getColumnIndexOrThrow("description")));
                channels.add(ch);
            }
            c.close();
        } catch (SQLiteDatabaseLockedException e) {
            Log.d("数据库", "锁定", e);
        }
        return channels;
    }

    public List<Playlist> listPlaylist(String channel) {
        List<Playlist> playlist = new ArrayList<>();
        try {

            Cursor c = getReadableDatabase().query("playlist", new String[]{"pid", "title", "description"}, "cid = ?", new String[]{channel}, null, null, "created DESC");

            while (c.moveToNext()) {
                Playlist p = new Playlist();
                p.setPid(c.getString(c.getColumnIndexOrThrow("pid")));
                p.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
                p.setDescription(c.getString(c.getColumnIndexOrThrow("description")));
                playlist.add(p);
            }
            c.close();
        } catch (SQLiteDatabaseLockedException e) {
            Log.d("数据库", "锁定", e);
        }
        return playlist;
    }

    public List<Video> listVideo(String playlist) {
        List<Video> videos = new ArrayList<>();
        try {
            Cursor c = getReadableDatabase().query("videos", new String[]{"vid", "title", "description"}, "pid = ?", new String[]{playlist}, null, null, "created DESC");

            while (c.moveToNext()) {
                Video v = new Video();
                v.setVid(c.getString(c.getColumnIndexOrThrow("vid")));
                v.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
                v.setDescription(c.getString(c.getColumnIndexOrThrow("description")));
                videos.add(v);
            }
            c.close();
        } catch (SQLiteDatabaseLockedException e) {
            Log.d("数据库", "锁定", e);
        }
        return videos;
    }


    public Book getBook(int id) {
        Book book = null;
        Cursor c = getReadableDatabase().query("books", new String[]{"id", "name", "title", "author"}, "id = ?", new String[]{Integer.toString(id)}, null, null, null, "1");
        if (c.moveToNext()) {
            book = createBook(c);

        }
        c.close();
        return book;
    }

    public List<Book> searchBook(String keyword) {
        keyword = "%" + keyword + "%";
        List<Book> books = new ArrayList<Book>();
        try {
            Cursor c = getReadableDatabase().query("books", new String[]{"id", "name", "title", "author"}, "name LIKE ? OR title LIKE ? OR author LIKE ?", new String[]{keyword, keyword, keyword}, null, null, "id ASC", "250");
            while (c.moveToNext()) {
                Book d = createBook(c);
                books.add(d);
            }
            c.close();

        } catch (SQLiteDatabaseLockedException e) {
            Log.d("数据库", "锁定", e);
        }
        return books;
    }

    public List<Book> getFavBookList() {
        List<Book> books = new ArrayList<Book>();
        try {
            Cursor c = getReadableDatabase().query("books", new String[]{"id", "name", "title", "author"}, "fav = ?", new String[]{"1"}, null, null, "id ASC");
            while (c.moveToNext()) {
                Book d = createBook(c);
                d.setFav(true);
                books.add(d);
            }
            c.close();
        } catch (SQLiteDatabaseLockedException e) {
            Log.d("数据库", "锁定", e);
        }
        return books;
    }

    public List<Favorite> listFavorite() {
        List<Favorite> favorites = new ArrayList<>();

        Cursor c = getReadableDatabase().query("favorites", new String[]{"id", "tid", "type", "extra", "title"}, null, null, null, null, "id DESC");
        while (c.moveToNext()) {
            Favorite f = new Favorite();
            f.setId(c.getInt(c.getColumnIndex("id")));
            f.setTid(c.getInt(c.getColumnIndex("tid")));
            f.setType(c.getString(c.getColumnIndex("type")));
            f.setTitle(c.getString(c.getColumnIndex("title")));
            f.setExtra(c.getString(c.getColumnIndex("extra")));
            favorites.add(f);
        }
        c.close();
        return favorites;
    }

    public void addWwwFavorite(String type, String title, String url) {
        addFavorite(type, url.hashCode(), title, url);
    }

    public void addDdcFavorite(String title, String url) {
        addWwwFavorite("ddc", title, url);
    }

    public void addCbetaFavorite(String title, String url) {
        addWwwFavorite("cbeta", title, url);
    }

    public void addDzjFavorite(int id, String title) {
        addFavorite("dzj", id, title, null);
    }

    public void delFavorite(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("favorites", "id = ?", new String[]{Integer.toString(id)});
    }


    public List<Book> getBookList() {
        List<Book> books = new ArrayList<Book>();
        Cursor c = getReadableDatabase().query("books", new String[]{"id", "name", "title", "author", "fav"}, null, null, null, null, "id ASC");
        while (c.moveToNext()) {
            Book d = createBook(c);
            d.setFav(c.getInt(c.getColumnIndexOrThrow("fav")) == 1);
            books.add(d);
        }
        c.close();
        return books;
    }

    public void loadSql(File file) {
        SQLiteDatabase db = getWritableDatabase();

        try {
            db.beginTransaction();

            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                if (line.endsWith(";")) {
                    db.execSQL(sb.toString());
                    sb = new StringBuilder();
                } else {
                    sb.append("\n");
                }
            }
            br.close();


            db.setTransactionSuccessful();
        } catch (IOException e) {
            Log.d("加载", "SQL", e);
        } finally {
            db.endTransaction();
        }
    }


    public interface LogCallback {
        void call(String message, Date created);
    }

    public void clearLog(int days) {
        getWritableDatabase().delete("logs", "created < ?", new String[]{new Timestamp(new Date().getTime() - days * 1000 * 60 * 60 * 24).toString()});
    }

    public void listLog(int size, LogCallback callback) {
        Cursor c = getReadableDatabase().query("logs", new String[]{"message", "created"}, null, null, null, null, "id DESC", Integer.toString(size));
        while (c.moveToNext()) {
            callback.call(c.getString(c.getColumnIndexOrThrow("message")), Timestamp.valueOf(c.getString(c.getColumnIndexOrThrow("created"))));
        }
        c.close();
    }

    public void addLog(String message) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("message", message);
        db.insert("logs", null, cv);

    }

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        onUpgrade(db, 0, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion + 1; i <= newVersion; i++) {
            install(db, i);
        }

    }


    public void index(ProgressReceiver.Callback callback) throws IOException, JSONException {

        final SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {

            Log.d("db", "加载书籍");
            db.delete("books", "1=1", null);
            callback.run(5);
            load(R.raw.cbeta201405, new JsonCallback() {
                @Override
                public void run(JSONObject jo) throws JSONException {
                    ContentValues cv = new ContentValues();
                    cv.put("title", jo.getString("title"));
                    cv.put("size", jo.getLong("size"));
                    cv.put("name", jo.getString("name"));
                    cv.put("author", jo.getString("author"));
                    db.insert("books", null, cv);
                }
            });
            callback.run(30);

            Log.d("db", "加载经典");
            load(R.raw.favorites, new JsonCallback() {
                @Override
                public void run(JSONObject jo) throws JSONException {

                    ContentValues cv = new ContentValues();
                    cv.put("fav", 1);
                    db.update("books", cv, "name = ?", new String[]{jo.getString("name")});
                }
            });
            callback.run(40);

            Log.d("db", "加载视频");
            db.delete("channels", "1=1", null);
            db.delete("playlist", "1=1", null);
            db.delete("videos", "1=1", null);
            load(R.raw.videos, new JsonCallback() {
                @Override
                public void run(JSONObject joc) throws JSONException {


                    ContentValues cvc = new ContentValues();
                    String cid = joc.getString("id");
                    cvc.put("cid", cid);
                    cvc.put("type", joc.getString("type"));
                    cvc.put("title", joc.getString("title"));
                    cvc.put("description", joc.getString("description"));
                    db.insert("channels", null, cvc);
                    Log.d("channel", joc.getString("title") + " " + cid);

                    JSONArray jap = joc.getJSONArray("playlist");
                    for (int ip = 0; ip < jap.length(); ip++) {
                        JSONObject jop = jap.getJSONObject(ip);
                        String pid = jop.getString("id");
                        ContentValues cvp = new ContentValues();
                        cvp.put("cid", cid);
                        cvp.put("pid", pid);
                        cvp.put("title", jop.getString("title"));
                        cvp.put("description", jop.getString("description"));
                        db.insert("playlist", null, cvp);

                        JSONArray jav = jop.getJSONArray("videos");
                        for (int iv = 0; iv < jav.length(); iv++) {
                            JSONObject jov = jav.getJSONObject(iv);
                            ContentValues cvv = new ContentValues();
                            cvv.put("pid", pid);
                            cvv.put("vid", jov.getString("id"));
                            cvv.put("title", jov.getString("title"));
                            cvv.put("description", jov.getString("description"));
                            db.insert("videos", null, cvv);

                        }
                    }
                }
            });
            callback.run(95);
            db.setTransactionSuccessful();
            callback.run(100);
        } finally {
            db.endTransaction();
        }

    }

    interface JsonCallback {
        void run(JSONObject jo) throws JSONException;
    }

    private void load(int rid, JsonCallback callback) throws IOException, JSONException {

        InputStream is = context.getResources().openRawResource(rid);
        Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        Writer writer = new StringWriter();
        int n;
        char[] buf = new char[1024];
        while ((n = reader.read(buf)) != -1) {
            writer.write(buf, 0, n);
        }
        JSONArray ja = new JSONArray(writer.toString());
        for (int i = 0; i < ja.length(); i++) {
            callback.run(ja.getJSONObject(i));
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (int i = oldVersion; i > newVersion; i--) {
            uninstall(db, i);
        }
    }

    private void addFavorite(String type, int tid, String title, String extra) {

        SQLiteDatabase db = getWritableDatabase();

        Cursor cur = db.query(
                "favorites",
                new String[]{"title"},
                "tid = ? AND type = ?",
                new String[]{Integer.toString(tid), type},
                null,
                null, null);
        boolean exists = cur.moveToNext();
        cur.close();

        ContentValues cv = new ContentValues();

        cv.put("title", title);
        cv.put("extra", extra);
        if (exists) {
            db.update("favorites", cv, "tid = ? AND type = ?", new String[]{Integer.toString(tid), type});
        } else {
            cv.put("type", type);
            cv.put("tid", tid);
            db.insert("favorites", null, cv);
        }

    }

    private Book createBook(Cursor c) {
        Book d = new Book();
        d.setId(c.getInt(c.getColumnIndexOrThrow("id")));
        d.setName(c.getString(c.getColumnIndexOrThrow("name")));
        d.setAuthor(c.getString(c.getColumnIndexOrThrow("author")));
        d.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
        return d;
    }

    private void uninstall(SQLiteDatabase db, int version) {
        switch (version) {
            case 4:
                drop_index("ddc_url");
                drop_table("ddc");
                break;
            case 2:
                for (String s : new String[]{
                        drop_index("books_title"),
                        drop_index("books_name"),
                        drop_index("books_type"),
                        drop_index("books_author"),
                        drop_table("books")
                }) {
                    db.execSQL(s);
                }

                break;
            case 1:
                for (String s : new String[]{
                        "logs",
                        "settings"}) {
                    db.execSQL(drop_table(s));
                }
                break;
        }
    }

    private void install(SQLiteDatabase db, int version) {

        switch (version) {
            case 6:
                db.execSQL("ALTER TABLE books ADD COLUMN size INTEGER(8) NOT NULL DEFAULT 0");
                break;
            case 5:
                for (String s : new String[]{
                        "CREATE TABLE IF NOT EXISTS favorites(id INTEGER PRIMARY KEY AUTOINCREMENT, title VARCHAR(255) NOT NULL, type VARCHAR(16) NOT NULL, tid INTEGER NOT NULL, extra VARCHAR(500), created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)",
                        "CREATE INDEX IF NOT EXISTS favorites_type ON favorites(type)",

                        "DROP TABLE IF EXISTS books",
                        "CREATE TABLE IF NOT EXISTS books(id INTEGER PRIMARY KEY AUTOINCREMENT, title VARCHAR(255) NOT NULL, name VARCHAR(255) NOT NULL, author VARCHAR(255) NOT NULL, fav INTEGER(1) NOT NULL DEFAULT 0, created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)",
                        "CREATE INDEX IF NOT EXISTS books_name ON books(name)",
                        "CREATE INDEX IF NOT EXISTS books_title ON books(title)",
                        "CREATE INDEX IF NOT EXISTS books_author ON books(author)",

                        "DROP INDEX IF EXISTS ddc_url",
                        "DROP TABLE IF EXISTS ddc",

                        "CREATE TABLE IF NOT EXISTS channels(id INTEGER PRIMARY KEY AUTOINCREMENT, cid VARCHAR(64) NOT NULL, type VARCHAR(8) NOT NULL, title VARCHAR(255) NOT NULL, description VARCHAR(1000), created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)",
                        "CREATE TABLE IF NOT EXISTS playlist(id INTEGER PRIMARY KEY AUTOINCREMENT, cid VARCHAR(64) NOT NULL, pid VARCHAR(64) NOT NULL, title VARCHAR(255) NOT NULL, description VARCHAR(1000), created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)",
                        "CREATE TABLE IF NOT EXISTS videos(id INTEGER PRIMARY KEY AUTOINCREMENT, vid VARCHAR(64) NOT NULL, pid VARCHAR(64) NOT NULL, title VARCHAR(255) NOT NULL, description VARCHAR(1000), created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)",

                        "CREATE INDEX IF NOT EXISTS channels_cid ON channels(cid)",
                        "CREATE INDEX IF NOT EXISTS channels_type ON channels(type)",
                        "CREATE INDEX IF NOT EXISTS playlist_cid ON playlist(cid)",
                        "CREATE INDEX IF NOT EXISTS playlist_pid ON playlist(pid)",
                        "CREATE INDEX IF NOT EXISTS playlist_title ON playlist(title)",
                        "CREATE INDEX IF NOT EXISTS videos_vid ON videos(vid)",
                        "CREATE INDEX IF NOT EXISTS videos_title ON videos(title)",
                        "CREATE INDEX IF NOT EXISTS videos_pid ON videos(pid)",
                }) {
                    db.execSQL(s);
                }
                break;
            case 4:
                for (String s : new String[]{
                        "CREATE TABLE IF NOT EXISTS ddc(title VARCHAR(255) NOT NULL, url VARCHAR(255)  PRIMARY KEY, created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)",
                        "CREATE INDEX IF NOT EXISTS ddc_url ON ddc(url)",
                }) {
                    db.execSQL(s);
                }
                break;
            case 3:
                db.execSQL("ALTER TABLE books ADD COLUMN fav INTEGER(1) NOT NULL DEFAULT 0");
                break;
            case 2:
                for (String s : new String[]{
                        "CREATE TABLE IF NOT EXISTS books(id INTEGER PRIMARY KEY AUTOINCREMENT, title VARCHAR(255) NOT NULL, name VARCHAR(255) NOT NULL, author VARCHAR(255) NOT NULL, type VARCHAR(255) NOT NULL, created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)",
                        "CREATE INDEX IF NOT EXISTS books_name ON books(name)",
                        "CREATE INDEX IF NOT EXISTS books_title ON books(title)",
                        "CREATE INDEX IF NOT EXISTS books_author ON books(author)",
                        "CREATE INDEX IF NOT EXISTS books_type ON books(type)",
                }) {
                    db.execSQL(s);
                }
                break;
            case 1:
                for (String s : new String[]{
                        "CREATE TABLE IF NOT EXISTS logs(id INTEGER PRIMARY KEY AUTOINCREMENT, message VARCHAR(255) NOT NULL, created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)",
                        "CREATE TABLE IF NOT EXISTS settings(`key` VARCHAR(32) PRIMARY KEY, val TEXT NOT NULL)"}) {
                    db.execSQL(s);
                }
                break;

        }

    }

    private String drop_table(String name) {
        return "DROP TABLE IF EXISTS " + name;
    }

    private String drop_index(String name) {
        return "DROP INDEX IF EXISTS " + name;
    }

    private Context context;
    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "BuddhismHomework.db";


}
