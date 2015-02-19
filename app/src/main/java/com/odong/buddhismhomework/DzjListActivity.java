package com.odong.buddhismhomework;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.gson.Gson;
import com.odong.buddhismhomework.models.Dzj;
import com.odong.buddhismhomework.utils.DwDbHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by flamen on 15-2-19.
 */
public class DzjListActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        getActionBar().setIcon(R.drawable.ic_dzj);

        String type = getIntent().getStringExtra("type");
        if (type == null) {
            initTypesView();
        } else {
            initBooksView(type);
            setTitle(type);
        }
    }

    private void initTypesView() {
        final List<String> types = new DwDbHelper(this).getDzjTypeList();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                types);

        ListView lv = (ListView) findViewById(R.id.lv_items);
        lv.setAdapter(adapter);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DzjListActivity.this, DzjListActivity.class);
                intent.putExtra("type", types.get(position));
                startActivity(intent);
            }
        });
    }

    private void initBooksView(String type) {
        List<Map<String, String>> items = new ArrayList<Map<String, String>>();
        final List<Dzj> books = new DwDbHelper(this).getDzjList(type);
        for (Dzj d : books) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("title", d.getTitle());
            map.put("details", d.getAuthor());
            items.add(map);
        }
        ListAdapter adapter = new SimpleAdapter(this,
                items,
                android.R.layout.two_line_list_item,
                new String[]{"title", "details"},
                new int[]{android.R.id.text1, android.R.id.text2});

        ListView lv = (ListView) findViewById(R.id.lv_items);
        lv.setAdapter(adapter);


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(DzjListActivity.this, PlayerActivity.class);
                intent.putExtra("book", new Gson().toJson(books.get(position)));
                startActivity(intent);
            }
        });
    }

}
