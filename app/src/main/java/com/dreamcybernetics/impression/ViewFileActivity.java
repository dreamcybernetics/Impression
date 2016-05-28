package com.dreamcybernetics.impression;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import java.io.File;

public class ViewFileActivity extends AppCompatActivity {
    public static final String ARG_FILE_PATH = "filepath";

    private String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_file);

        filePath = getIntent().getExtras().getString(ARG_FILE_PATH);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(filePath.substring(filePath.lastIndexOf(File.separator) + 1));
        }

        ((WebView)findViewById(R.id.wvFile)).loadUrl("file:///" + filePath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_file_activity, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.miDelete: {
                deleteFile();

                return true;
            }
            case android.R.id.home: {
                finish();

                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void deleteFile() {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setMessage(getString(R.string.txt_confirm_delete_file));
        dlgBuilder.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if ((new File(filePath)).delete()) {
                    finish();
                }
            }
        });
        dlgBuilder.setNegativeButton(R.string.btn_no, null);
        dlgBuilder.create().show();
    }
}
