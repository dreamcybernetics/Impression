package com.dreamcybernetics.impression;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private ArrayList<String> listPollNames;
    private ArrayList<Integer> listPollIDs;

    private TextView tvEmpty;
    private ListView lvPolls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvEmpty = (TextView)findViewById(R.id.tvEmpty);

        lvPolls = (ListView)findViewById(R.id.lvPolls);
        lvPolls.setOnItemClickListener(this);
        registerForContextMenu(lvPolls);

        DBHelper.openDatabase(getApplicationContext());
        loadPolls();
    }

    @Override
    protected void onDestroy() {
        DBHelper.closeDatabase();

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.miAddPoll: {
                addPoll();

                return true;
            }
            case R.id.miViewFiles: {
                startActivity(new Intent(this, ViewFilesListActivity.class));

                return true;
            }
            case R.id.miHelp: {
                startActivity(new Intent(this, AboutActivity.class));

                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.context_menu_modify, menu);

        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.miEdit: {
                editPoll(((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position);

                return true;
            }
            case R.id.miDelete: {
                removePoll(((AdapterView.AdapterContextMenuInfo)item.getMenuInfo()).position);

                return true;
            }
            default: {
                return super.onContextItemSelected(item);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        openPoll(position);
    }

    private void loadPolls() {
        Cursor cursor = DBHelper.select(DBHelper.Polls.TABLE_NAME,
                new String[] { DBHelper.Polls._ID, DBHelper.Polls.COLUMN_NAME_ITEM_NAME },
                DBHelper.Polls.COLUMN_NAME_ITEM_TYPE + "=" + ListItem.ITEM_TYPE_POLL);

        int pollsCount = cursor.getCount();

        listPollIDs = new ArrayList<>(pollsCount);
        listPollNames = new ArrayList<>(pollsCount);

        while (cursor.moveToNext()) {
            int pollID = cursor.getInt(cursor.getColumnIndex(DBHelper.Polls._ID));
            String pollName = cursor.getString(cursor.getColumnIndex(DBHelper.Polls.COLUMN_NAME_ITEM_NAME));

            listPollIDs.add(pollID);
            listPollNames.add(pollName);
        }

        cursor.close();

        lvPolls.setAdapter(new ArrayAdapter<>(this, R.layout.list_item_poll, R.id.tvPollName, listPollNames));

        checkEmpty();
    }

    private void addPoll() {
        final EditText etInput = new EditText(this);
        etInput.setPadding(20, 50, 20, 30);
        etInput.setHint(R.string.hint_poll_name);

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setView(etInput);
        dlgBuilder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newPollName = etInput.getText().toString().trim();
                if (!newPollName.isEmpty()) {
                    int newPollID = DBHelper.insert(DBHelper.Polls.TABLE_NAME,
                            new String[] {
                                    DBHelper.Polls.COLUMN_NAME_PARENT_ID,
                                    DBHelper.Polls.COLUMN_NAME_ITEM_NAME,
                                    DBHelper.Polls.COLUMN_NAME_ITEM_TYPE
                            },
                            new String[] { "0", newPollName, String.valueOf(ListItem.ITEM_TYPE_POLL) });

                    if (newPollID > 0) {
                        listPollIDs.add(newPollID);
                        listPollNames.add(newPollName);

                        ((ArrayAdapter)lvPolls.getAdapter()).notifyDataSetChanged();
                        lvPolls.smoothScrollToPosition(listPollIDs.size() - 1);

                        checkEmpty();
                    }
                }
            }
        });
        dlgBuilder.setNegativeButton(R.string.btn_cancel, null);
        dlgBuilder.create().show();
    }

    private void editPoll(final int position) {
        String pollName = listPollNames.get(position);

        final EditText etInput = new EditText(this);
        etInput.setPadding(20, 50, 20, 30);
        etInput.setHint(R.string.hint_poll_name);
        etInput.setText(pollName);
        etInput.setSelection(pollName.length());

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setView(etInput);
        dlgBuilder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newPollName = etInput.getText().toString().trim();
                if (!newPollName.isEmpty()) {
                    if (DBHelper.update(DBHelper.Polls.TABLE_NAME, new String[] { DBHelper.Polls.COLUMN_NAME_ITEM_NAME },
                            new String[] { newPollName }, DBHelper.Polls._ID + "=" + listPollIDs.get(position)) > 0) {

                        listPollNames.set(position, newPollName);

                        ((ArrayAdapter)lvPolls.getAdapter()).notifyDataSetChanged();
                    }
                }
            }
        });
        dlgBuilder.setNegativeButton(R.string.btn_cancel, null);
        dlgBuilder.create().show();
    }

    private void removePoll(final int position) {
        final int pollID = listPollIDs.get(position);

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setMessage(String.format(Locale.US, getString(R.string.txt_confirm_delete_poll), listPollNames.get(position)));
        dlgBuilder.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (DBHelper.delete(DBHelper.Polls.TABLE_NAME, DBHelper.Polls._ID + "=" + pollID) > 0) {
                    listPollIDs.remove(position);
                    listPollNames.remove(position);

                    ListItem.deleteSubItemsInDatebase(pollID);

                    ((ArrayAdapter)lvPolls.getAdapter()).notifyDataSetChanged();

                    checkEmpty();
                }
            }
        });
        dlgBuilder.setNegativeButton(R.string.btn_no, null);
        dlgBuilder.create().show();
    }

    private void openPoll(int position) {
        Intent intent = new Intent(MainActivity.this, PollActivity.class);

        intent.putExtra(PollActivity.ARG_POLL_ID, listPollIDs.get(position));
        intent.putExtra(PollActivity.ARG_POLL_NAME, listPollNames.get(position));

        startActivity(intent);
    }

    private void checkEmpty() {
        tvEmpty.setVisibility(listPollIDs.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
