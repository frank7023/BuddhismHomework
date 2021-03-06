package com.odong.buddhismhomework.pages;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.odong.buddhismhomework.R;
import com.odong.buddhismhomework.models.Homework;
import com.odong.buddhismhomework.utils.WidgetHelper;
import com.odong.buddhismhomework.utils.XmlHelper;
import com.odong.buddhismhomework.utils.YoutubePlayer;

import java.io.IOException;
import java.util.List;

/**
 * Created by flamen on 15-2-8.
 */
public class HomeworkActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homework);

        String type = getIntent().getStringExtra("type");
        getActionBar().setIcon(getResources().getIdentifier("ic_" + type, "drawable", getPackageName()));
        homeworkList = new XmlHelper(this).getHomeworkList(type);

        ((TextView) findViewById(R.id.tv_homework_content)).setMovementMethod(new ScrollingMovementMethod());
        initSpinner();
        initPlayButton();
    }

//    @Override
//    public void onBackPressed() {
//
//        AlertDialog.Builder adb = new AlertDialog.Builder(this);
//        adb.setMessage(R.string.dlg_will_pause);
//        adb.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                HomeworkActivity.this.finish();
//            }
//        });
//        adb.setNegativeButton(android.R.string.no, null);
//        adb.setCancelable(false);
//        adb.create().show();
//
//    }

    private void initSpinner() {

        Spinner spinner = (Spinner) findViewById(R.id.sp_homework);
        ArrayAdapter<Homework> adapter = new ArrayAdapter<Homework>(this,
                android.R.layout.simple_spinner_item, homeworkList);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                initHomework(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                initHomework(0);
            }
        });

    }


    private void initHomework(int index) {

        Homework hw = homeworkList.get(index);

        setTitle(hw.getName());

        TextView tv = (TextView) findViewById(R.id.tv_homework_content);
        try {
            tv.setText(new WidgetHelper(this).readFile(hw.getIncantations().toArray(new Integer[1])));
            tv.scrollTo(0, 0);
        } catch (Resources.NotFoundException | IOException e) {
            Log.e("读取文件", hw.getName(), e);
            tv.setText(R.string.lbl_error_io);
        }

    }

    private void initPlayButton() {
        findViewById(R.id.btn_homework_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Homework hw = (Homework) ((Spinner) findViewById(R.id.sp_homework)).getSelectedItem();
                new YoutubePlayer(HomeworkActivity.this, hw.getVid()).start();

            }
        });
    }

    private List<Homework> homeworkList;

}
