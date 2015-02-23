package com.odong.buddhismhomework.pages.reading;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.gson.Gson;
import com.odong.buddhismhomework.R;
import com.odong.buddhismhomework.models.Book;
import com.odong.buddhismhomework.models.CacheFile;
import com.odong.buddhismhomework.models.Dzj;
import com.odong.buddhismhomework.models.Pager;
import com.odong.buddhismhomework.utils.DwDbHelper;
import com.odong.buddhismhomework.utils.KvHelper;
import com.odong.buddhismhomework.utils.WidgetHelper;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by flamen on 15-2-19.
 */
public class ShowActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        ((TextView) findViewById(R.id.tv_book_content)).setMovementMethod(new ScrollingMovementMethod());


        type = getIntent().getStringExtra("type");
        String file = getIntent().getStringExtra("file");


        KvHelper kh = new KvHelper(this);
        WidgetHelper wh = new WidgetHelper(this);
        wh.initTextViewFont(R.id.tv_book_content);

        try {
            if ("book".equals(type)) {
                book = new Gson().fromJson(file, Book.class);
                getActionBar().setIcon(R.drawable.ic_books);
                setTitle(((Book) book).getName());
                pager = kh.get(((Book) book).getScrollId(), Pager.class, new Pager());


            } else if ("dzj".equals(type)) {
                getActionBar().setIcon(R.drawable.ic_dzj);
                book = new Gson().fromJson(file, Dzj.class);
                setTitle(((Dzj) book).getTitle());
                pager = kh.get(((Dzj) book).getScrollId(), Pager.class, new Pager());
            }

            read(pager.getCur(), false);
        } catch (IOException e) {
            ((TextView) findViewById(R.id.tv_book_content)).setText(R.string.lbl_empty);
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book, menu);
        if ("book".equals(type)) {
            menu.findItem(R.id.action_add_to_favorites).setVisible(false);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        TextView tv = (TextView) findViewById(R.id.tv_book_content);
        pager.setX(tv.getScrollX());
        pager.setY(tv.getScrollY());

        KvHelper kv = new KvHelper(this);

        if (type.equals("book")) {
            kv.set(((Book) book).getScrollId(), pager);
        } else if (type.equals("dzj")) {
            kv.set(((Dzj) book).getScrollId(), pager);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_page_next:
                doPage(true);
                break;
            case R.id.action_page_previous:
                doPage(false);
                break;
            case R.id.action_zoom_in:
                new WidgetHelper(this).zoomTextView(R.id.tv_book_content, false);
                break;
            case R.id.action_zoom_out:
                new WidgetHelper(this).zoomTextView(R.id.tv_book_content, true);
                break;
            case R.id.action_add_to_favorites:
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle(R.string.action_add_to_favorites);
                adb.setMessage(R.string.lbl_are_you_sure);
                adb.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DwDbHelper ddh = new DwDbHelper(ShowActivity.this);
                        ddh.setDzjFav(((Dzj) book).getId(), true);
                        ddh.close();
                        new WidgetHelper(ShowActivity.this).toast(getString(R.string.lbl_success), false);
                    }
                });
                adb.setNegativeButton(android.R.string.no, null);
                adb.create().show();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void doPage(boolean next) {
        WidgetHelper wh = new WidgetHelper(this);
        int p;
        if (next) {
            if (pager.isLast()) {
                wh.toast(getString(R.string.lbl_error_last_page), false);
                return;
            }
            p = pager.getCur() + 1;
        } else {
            if (pager.isFirst()) {
                wh.toast(getString(R.string.lbl_error_first_page), false);
                return;
            }
            p = pager.getCur() - 1;
        }
        try {
            read(p, true);
            pager.setX(0);
            pager.setY(0);
            Log.d("翻页", "" + pager.getCur());
            wh.toast(getString(R.string.lbl_cur_page, pager.getCur() + 1, pager.getSize()), false);
        } catch (IOException e) {
            Log.e("翻页", "previous", e);
        }

    }

    private void read(int page, boolean top) throws IOException {


        TextView tv = (TextView) findViewById(R.id.tv_book_content);

        StringBuilder sb = new StringBuilder();

        BufferedReader br;
        switch (type) {
            case "book":
                br = new BufferedReader(new InputStreamReader(getResources().openRawResource(((Book) book).getFiles().get(0))));
                break;
            case "dzj":
                br = new BufferedReader(new InputStreamReader(new FileInputStream(new CacheFile(this, ((Dzj) book).getName()).getRealFile())));
                break;
            default:
                throw new IllegalArgumentException();
        }
        String line;
        if (pager.isInit()) {
            for (int i = 0, begin = page * Pager.LINES, end = (page + 1) * Pager.LINES; i < end && i < pager.getLen(); i++) {
                line = br.readLine();
                if (i < begin) {
                    continue;
                }
                if (line == null) {
                    break;
                }
                sb.append(line);
                sb.append("\n");
            }
            pager.setCur(page);
        } else {
            int i = 0;
            while ((line = br.readLine()) != null) {
                i++;
                if (i < Pager.LINES) {
                    sb.append(line);
                    sb.append("\n");
                }
            }
            pager.setCur(0);
            pager.setLen(i);
        }

        br.close();

        tv.setText(sb.toString());
        if (top) {
            pager.setX(0);
            pager.setY(0);
        }
        tv.scrollTo(pager.getX(), pager.getY());

        KvHelper kh = new KvHelper(this);
        switch (type) {
            case "book":
                kh.set(((Book) book).getScrollId(), pager);
                break;
            case "dzj":
                kh.set(((Dzj) book).getScrollId(), pager);
                break;
        }
    }


    private String type;
    private Object book;
    private Pager pager;


}