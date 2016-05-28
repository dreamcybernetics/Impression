package com.dreamcybernetics.impression;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

public class ViewFilesListActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private static final int REQUEST_PERMISSION_READ_FILE = 1;

    String dir;
    String[] files;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_files_list);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            loadFilesList();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] { android.Manifest.permission.WRITE_EXTERNAL_STORAGE },
                    REQUEST_PERMISSION_READ_FILE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, ViewFileActivity.class);
        intent.putExtra(ViewFileActivity.ARG_FILE_PATH, dir + File.separator + files[position]);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_READ_FILE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadFilesList();
            } else {
                Toast.makeText(this, R.string.msg_err_load_dir, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadFilesList() {
        dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getString(R.string.app_name);
        files = (new File(dir)).list();

        if (files == null) {
            return;
        }

        //reverse
        for (int i = 0; i < files.length / 2; i++) {
            String temp = files[i];
            files[i] = files[files.length - i - 1];
            files[files.length - i - 1] = temp;
        }

        ListView lvFiles = (ListView)findViewById(R.id.lvFiles);
        lvFiles.setOnItemClickListener(this);
        lvFiles.setAdapter(new ArrayAdapter<>(this, R.layout.list_item_view_files, R.id.tvFileName, files));
    }
}
