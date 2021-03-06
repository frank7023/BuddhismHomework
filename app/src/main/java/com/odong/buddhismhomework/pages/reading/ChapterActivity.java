package com.odong.buddhismhomework.pages.reading;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.odong.buddhismhomework.R;
import com.odong.buddhismhomework.models.Book;
import com.odong.buddhismhomework.utils.WidgetHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.TOCReference;

/**
 * Created by flamen on 15-3-6.
 */
public class ChapterActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        final Book book = (Book) getIntent().getSerializableExtra("book");

        setTitle(getString(R.string.action_book_chapter) + ": " + book.getTitle());
        chapters = new ArrayList<>();
        links = new ArrayList<>();

        try {
            nl.siegmann.epublib.domain.Book epub = book.toEpub(this);

            logTableOfContents(epub.getTableOfContents().getTocReferences(), 0);
        } catch (IOException e) {
            Log.e("EPUB", "目录", e);
            new WidgetHelper(this).toast(getString(R.string.lbl_error_book_format), false);
        }

        ListView lv = (ListView) findViewById(R.id.lv_items);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, chapters);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ChapterActivity.this, EpubActivity.class);
                intent.putExtra("book", book);
                intent.putExtra("link", links.get(position));
                startActivity(intent);
            }
        });

    }

    private void logTableOfContents(List<TOCReference> references, int depth) {
        if (references == null) {
            return;
        }
        for (TOCReference tr : references) {
            StringBuilder sb = new StringBuilder();
            sb.append("|--");
            for (int i = 0; i < depth; i++) {
                sb.append("--");
            }
            sb.append(tr.getTitle());

            chapters.add(sb.toString());
            links.add(tr.getCompleteHref());
            logTableOfContents(tr.getChildren(), depth + 1);
        }
    }

    private List<String> chapters;
    private List<String> links;


}
