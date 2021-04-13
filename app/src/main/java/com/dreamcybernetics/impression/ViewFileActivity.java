package com.dreamcybernetics.impression;

import android.app.AlertDialog;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.util.Scanner;

public class ViewFileActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_OPEN_FILE = 1;

    public static final String ARG_FILE_PATH = "filepath";

    private String filePath = null;

    private MenuItem miDelete;

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_file);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        webView = findViewById(R.id.webView);
        WebSettings settings = webView.getSettings();
        settings.setDefaultTextEncodingName("utf-8");

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            filePath = extras.getString(ARG_FILE_PATH, null);
        }

        if (filePath == null) {
            openFilePrompt();
        } else {
            webView.loadUrl("file:///" + filePath);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_view_file_activity, menu);

        miDelete = menu.findItem(R.id.miDelete);
        if (filePath == null) {
            miDelete.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.miDelete) {
            deleteFile();
            return true;
        } else if (id == R.id.miOpenFile) {
            openFilePrompt();
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_OPEN_FILE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    if (inputStream != null) {
                        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
                        String htmlText = s.hasNext() ? s.next() : "";

                        String base64 = Base64.encodeToString(htmlText.getBytes(), Base64.DEFAULT);
                        webView.loadData(base64, "text/html; charset=utf-8", "base64");

                        if (filePath != null) {
                            filePath = null;
                            miDelete.setVisible(false);
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(this, R.string.msg_err_load_dir, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void openFilePrompt() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/html");
        try {
            startActivityForResult(intent, REQUEST_CODE_OPEN_FILE);
        } catch (Exception e) {
            finish();
        }
    }

    private void deleteFile() {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setMessage(getString(R.string.txt_confirm_delete_file));
        dlgBuilder.setPositiveButton(R.string.btn_yes, (dialog, which) -> {
            if ((new File(filePath)).delete()) {
                finish();
            }
        });
        dlgBuilder.setNegativeButton(R.string.btn_no, null);
        dlgBuilder.create().show();
    }
}
