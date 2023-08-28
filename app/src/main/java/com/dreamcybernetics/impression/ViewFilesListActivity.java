package com.dreamcybernetics.impression;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

    private String filesDir;
    private String[] filesList;

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
                    new String[] { android.Manifest.permission.READ_EXTERNAL_STORAGE },
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
        intent.putExtra(ViewFileActivity.ARG_FILE_PATH, filesDir + File.separator + filesList[position]);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_READ_FILE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadFilesList();
            } else {
                Toast.makeText(this, R.string.msg_err_load_dir, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadFilesList() {
        filesDir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getString(R.string.app_name);
        filesList = (new File(filesDir)).list();

        if (filesList == null) {
            return;
        }

        //reverse
        for (int i = 0; i < filesList.length / 2; i++) {
            String temp = filesList[i];
            filesList[i] = filesList[filesList.length - i - 1];
            filesList[filesList.length - i - 1] = temp;
        }

        ListView lvFiles = findViewById(R.id.lvFiles);
        lvFiles.setOnItemClickListener(this);
        lvFiles.setAdapter(new ArrayAdapter<>(this, R.layout.list_item_view_files, R.id.tvFileName, filesList));
    }
}
