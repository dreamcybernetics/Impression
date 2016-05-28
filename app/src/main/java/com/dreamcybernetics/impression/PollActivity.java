package com.dreamcybernetics.impression;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PollActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {
    public static final String ARG_POLL_ID = "pollid";
    public static final String ARG_POLL_NAME = "pollname";

    private static final int REQUEST_PERMISSION_SAVE_FILE = 1;

    private ArrayList<ListItem> listQuestions;
    private LinearLayout viewGroupQuestions;

    private View selectedItemView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        viewGroupQuestions = (LinearLayout)findViewById(R.id.llContent);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        DBHelper.openDatabase(getApplicationContext());
        loadPoll();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_poll_activity, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.miAddQuestion: {
                addQuestion();

                return true;
            }
            case R.id.miReset: {
                resetAll();

                return true;
            }
            case R.id.miSave: {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    save();
                } else {
                    ActivityCompat.requestPermissions(this,
                            new String[] { android.Manifest.permission.WRITE_EXTERNAL_STORAGE },
                            REQUEST_PERMISSION_SAVE_FILE);
                }

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
                if (selectedItemView instanceof CheckBox) {
                    editOption();
                } else {
                    editQuestion();
                }

                return true;
            }
            case R.id.miDelete: {
                if (selectedItemView instanceof CheckBox) {
                    removeOption();
                } else {
                    removeQuestion();
                }

                return true;
            }
            default: {
                return super.onContextItemSelected(item);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.ivAddOption: {
                addOption((ViewGroup)v.getParent().getParent());

                break;
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.tvQuestion: {
                selectedItemView = (View)v.getParent();

                break;
            }
            case R.id.cbOption: {
                selectedItemView = v;

                break;
            }
        }

        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_SAVE_FILE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                save();
            } else {
                Toast.makeText(this, R.string.msg_err_create_dir, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadPoll() {
        int pollID = getIntent().getExtras().getInt(ARG_POLL_ID);
        String pollName = getIntent().getExtras().getString(ARG_POLL_NAME);

        ((TextView)findViewById(R.id.tvPollName)).setText(pollName);

        Cursor questonsCursor = DBHelper.select(DBHelper.Polls.TABLE_NAME,
                new String[] { DBHelper.Polls._ID, DBHelper.Polls.COLUMN_NAME_ITEM_NAME },
                DBHelper.Polls.COLUMN_NAME_PARENT_ID + "=" + pollID);

        listQuestions = new ArrayList<>(questonsCursor.getCount());
        viewGroupQuestions.removeAllViews();

        while (questonsCursor.moveToNext()) {
            int questID = questonsCursor.getInt(questonsCursor.getColumnIndex(DBHelper.Polls._ID));
            String questText = questonsCursor.getString(questonsCursor.getColumnIndex(DBHelper.Polls.COLUMN_NAME_ITEM_NAME));

            View questionView = LayoutInflater.from(this).inflate(R.layout.list_item_question, viewGroupQuestions, false);

            TextView tvQuestion = (TextView)questionView.findViewById(R.id.tvQuestion);
            tvQuestion.setText(questText);
            registerForContextMenu(tvQuestion);
            tvQuestion.setOnLongClickListener(this);

            questionView.findViewById(R.id.ivAddOption).setOnClickListener(this);
            LinearLayout viewGroupOptions = (LinearLayout)questionView.findViewById(R.id.llContent);

            Cursor optionsCursor = DBHelper.select(DBHelper.Polls.TABLE_NAME,
                    new String[] { DBHelper.Polls._ID, DBHelper.Polls.COLUMN_NAME_ITEM_NAME },
                    DBHelper.Polls.COLUMN_NAME_PARENT_ID + "=" + questID);

            ArrayList<ListItem> optionsList = new ArrayList<>(optionsCursor.getCount());

            while (optionsCursor.moveToNext()) {
                int optionID = optionsCursor.getInt(optionsCursor.getColumnIndex(DBHelper.Polls._ID));
                String optionText = optionsCursor.getString(optionsCursor.getColumnIndex(DBHelper.Polls.COLUMN_NAME_ITEM_NAME));

                CheckBox optionView = (CheckBox)LayoutInflater.from(this).inflate(R.layout.list_item_option, viewGroupOptions, false);
                optionView.setText(optionText);
                registerForContextMenu(optionView);
                optionView.setOnLongClickListener(this);

                optionsList.add(new ListItem(optionID, optionText, null));
                viewGroupOptions.addView(optionView);
            }

            optionsCursor.close();

            listQuestions.add(new ListItem(questID, questText, optionsList));
            viewGroupQuestions.addView(questionView);
        }

        questonsCursor.close();
    }

    private void addQuestion() {
        final EditText etInput = new EditText(this);
        etInput.setPadding(20, 50, 20, 30);
        etInput.setHint(R.string.hint_new_question);

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setView(etInput);
        dlgBuilder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newQuestion = etInput.getText().toString().trim();
                if (newQuestion.isEmpty()) {
                    return;
                }

                int pollID = getIntent().getExtras().getInt(ARG_POLL_ID);
                int newQuestionID = DBHelper.insert(DBHelper.Polls.TABLE_NAME,
                        new String[] {
                                DBHelper.Polls.COLUMN_NAME_PARENT_ID,
                                DBHelper.Polls.COLUMN_NAME_ITEM_NAME,
                                DBHelper.Polls.COLUMN_NAME_ITEM_TYPE
                        },
                        new String[] { String.valueOf(pollID), newQuestion, String.valueOf(ListItem.ITEM_TYPE_QUESTION) });

                if (newQuestionID > 0) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(etInput.getWindowToken(), 0);

                    listQuestions.add(new ListItem(newQuestionID, newQuestion, new ArrayList<ListItem>()));

                    View questionView = LayoutInflater.from(PollActivity.this).inflate(R.layout.list_item_question, viewGroupQuestions, false);

                    TextView tvQuestion = (TextView)questionView.findViewById(R.id.tvQuestion);
                    tvQuestion.setText(newQuestion);
                    registerForContextMenu(tvQuestion);
                    tvQuestion.setOnLongClickListener(PollActivity.this);

                    questionView.findViewById(R.id.ivAddOption).setOnClickListener(PollActivity.this);
                    viewGroupQuestions.addView(questionView);
                }
            }
        });
        dlgBuilder.setNegativeButton(R.string.btn_cancel, null);
        dlgBuilder.create().show();
    }

    private void addOption(ViewGroup questionView) {
        EditText etOption = (EditText)questionView.findViewById(R.id.etOption);

        String newOptionText = etOption.getText().toString().trim();
        if (newOptionText.isEmpty()) {
            return;
        }

        int questionPosition = viewGroupQuestions.indexOfChild(questionView);
        ListItem question = listQuestions.get(questionPosition);

        int newOptionID = DBHelper.insert(DBHelper.Polls.TABLE_NAME,
                new String[] {
                        DBHelper.Polls.COLUMN_NAME_PARENT_ID,
                        DBHelper.Polls.COLUMN_NAME_ITEM_NAME,
                        DBHelper.Polls.COLUMN_NAME_ITEM_TYPE
                },
                new String[] { String.valueOf(question.getId()), newOptionText, String.valueOf(ListItem.ITEM_TYPE_OPTION) });

        if (newOptionID > 0) {
            etOption.setText("");

            LinearLayout viewGroupOptions = (LinearLayout)questionView.findViewById(R.id.llContent);

            CheckBox optionView = (CheckBox)LayoutInflater.from(this).inflate(R.layout.list_item_option, viewGroupOptions, false);
            optionView.setText(newOptionText);
            registerForContextMenu(optionView);
            optionView.setOnLongClickListener(this);

            listQuestions.get(questionPosition).addSubItem(new ListItem(newOptionID, newOptionText, null));
            viewGroupOptions.addView(optionView);
        }
    }

    private void removeQuestion() {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setMessage(getString(R.string.txt_confirm_delete_question));
        dlgBuilder.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int questionPosition = viewGroupQuestions.indexOfChild(selectedItemView);
                if (questionPosition >= 0) {
                    int questionID = listQuestions.get(questionPosition).getId();

                    if (DBHelper.delete(DBHelper.Polls.TABLE_NAME, DBHelper.Polls._ID + "=" + questionID) > 0) {
                        listQuestions.remove(questionPosition);

                        ListItem.deleteSubItemsInDatebase(questionID);

                        viewGroupQuestions.removeViewAt(questionPosition);
                    }
                }
            }
        });
        dlgBuilder.setNegativeButton(R.string.btn_no, null);
        dlgBuilder.create().show();
    }

    private void removeOption() {
        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setMessage(getString(R.string.txt_confirm_delete_option));
        dlgBuilder.setPositiveButton(R.string.btn_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int questionPosition = 0; questionPosition < viewGroupQuestions.getChildCount(); questionPosition++) {
                    LinearLayout viewGroupOptions = (LinearLayout)viewGroupQuestions.getChildAt(questionPosition).findViewById(R.id.llContent);

                    int optionPosition = viewGroupOptions.indexOfChild(selectedItemView);
                    if (optionPosition >= 0) {
                        int optionID = listQuestions.get(questionPosition).getSubItems().get(optionPosition).getId();

                        if (DBHelper.delete(DBHelper.Polls.TABLE_NAME, DBHelper.Polls._ID + "=" + optionID) > 0) {
                            listQuestions.get(questionPosition).getSubItems().remove(optionPosition);
                            viewGroupOptions.removeViewAt(optionPosition);
                        }

                        break;
                    }
                }
            }
        });
        dlgBuilder.setNegativeButton(R.string.btn_no, null);
        dlgBuilder.create().show();
    }

    private void editQuestion() {
        int questionPosition = viewGroupQuestions.indexOfChild(selectedItemView);
        if (questionPosition < 0) {
            return;
        }

        final ListItem selectedItem = listQuestions.get(questionPosition);

        final EditText etInput = new EditText(this);
        etInput.setPadding(20, 50, 20, 30);
        etInput.setHint(R.string.hint_question);
        etInput.setText(selectedItem.getText());
        etInput.setSelection(selectedItem.getText().length());

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setView(etInput);
        dlgBuilder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newQuestion = etInput.getText().toString().trim();
                if (!newQuestion.isEmpty()) {
                    if (DBHelper.update(DBHelper.Polls.TABLE_NAME, new String[] { DBHelper.Polls.COLUMN_NAME_ITEM_NAME },
                            new String[] { newQuestion }, DBHelper.Polls._ID + "=" + selectedItem.getId()) > 0) {

                        selectedItem.setText(newQuestion);

                        ((TextView)viewGroupQuestions.findViewById(R.id.tvQuestion)).setText(newQuestion);
                    }
                }
            }
        });
        dlgBuilder.setNegativeButton(R.string.btn_cancel, null);
        dlgBuilder.create().show();
    }

    private void editOption() {
        for (int questionPosition = 0; questionPosition < viewGroupQuestions.getChildCount(); questionPosition++) {
            LinearLayout viewGroupOptions = (LinearLayout)viewGroupQuestions.getChildAt(questionPosition).findViewById(R.id.llContent);

            int optionPosition = viewGroupOptions.indexOfChild(selectedItemView);
            if (optionPosition >= 0) {
                final ListItem selectedItem = listQuestions.get(questionPosition).getSubItems().get(optionPosition);

                final EditText etInput = new EditText(this);
                etInput.setPadding(20, 50, 20, 30);
                etInput.setHint(R.string.hint_option);
                etInput.setText(selectedItem.getText());
                etInput.setSelection(selectedItem.getText().length());

                AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
                dlgBuilder.setView(etInput);
                dlgBuilder.setPositiveButton(R.string.btn_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newOption = etInput.getText().toString().trim();
                        if (!newOption.isEmpty()) {
                            if (DBHelper.update(DBHelper.Polls.TABLE_NAME, new String[] { DBHelper.Polls.COLUMN_NAME_ITEM_NAME },
                                    new String[] { newOption }, DBHelper.Polls._ID + "=" + selectedItem.getId()) > 0) {

                                selectedItem.setText(newOption);

                                ((CheckBox)selectedItemView).setText(newOption);
                            }
                        }
                    }
                });
                dlgBuilder.setNegativeButton(R.string.btn_cancel, null);
                dlgBuilder.create().show();

                break;
            }
        }
    }

    private void resetAll() {
        for (int questionPosition = 0; questionPosition < viewGroupQuestions.getChildCount(); questionPosition++) {
            LinearLayout viewGroupOptions = (LinearLayout)viewGroupQuestions.getChildAt(questionPosition).findViewById(R.id.llContent);

            for (int optionPosition = 0; optionPosition < viewGroupOptions.getChildCount(); optionPosition++) {
                ((CheckBox)viewGroupOptions.getChildAt(optionPosition)).setChecked(false);
            }

            ((EditText)viewGroupQuestions.getChildAt(questionPosition).findViewById(R.id.etOption)).setText("");
        }
    }

    private void save() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_save, null);

        final CheckBox cbDateTime = (CheckBox)dialogView.findViewById(R.id.cbDateTime);
        final EditText etName = (EditText)dialogView.findViewById(R.id.etName);
        final EditText etNotes = (EditText)dialogView.findViewById(R.id.etNotes);

        AlertDialog.OnClickListener dlgBtnClick = new AlertDialog.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == AlertDialog.BUTTON_POSITIVE) {
                    String pollName = getIntent().getExtras().getString(ARG_POLL_NAME, "");

                    String dateTime = "";
                    if (cbDateTime.isChecked()) {
                        Date nowDate = new Date();
                        dateTime = DateFormat.getDateFormat(getApplicationContext()).format(nowDate) + " - " + DateFormat.getTimeFormat(getApplicationContext()).format(nowDate);
                    }

                    String name = etName.getText().toString().trim();
                    String notes = etNotes.getText().toString().trim();

                    String htmlMainBody = getHTMLTemplate(R.raw.html_main_body);
                    String htmlQuestion = getHTMLTemplate(R.raw.html_question);
                    String htmlOptionUnchecked = getHTMLTemplate(R.raw.html_option_unchecked);
                    String htmlOptionChecked = getHTMLTemplate(R.raw.html_option_checked);
                    String htmlOptionOther = getHTMLTemplate(R.raw.html_option_other);

                    StringBuilder questions = new StringBuilder();
                    for (int questionPosition = 0; questionPosition < viewGroupQuestions.getChildCount(); questionPosition++) {
                        LinearLayout questionView = (LinearLayout)viewGroupQuestions.getChildAt(questionPosition);

                        TextView tvQuestion = (TextView)questionView.findViewById(R.id.tvQuestion);

                        LinearLayout viewGroupOptions = (LinearLayout)questionView.findViewById(R.id.llContent);

                        StringBuilder options = new StringBuilder();
                        for (int optionPosition = 0; optionPosition < viewGroupOptions.getChildCount(); optionPosition++) {
                            CheckBox cbOption = (CheckBox)viewGroupOptions.getChildAt(optionPosition);

                            options.append(cbOption.isChecked() ? htmlOptionChecked.replace("[OPTXT]", cbOption.getText()) : htmlOptionUnchecked.replace("[OPTXT]", cbOption.getText()));
                        }

                        EditText etOption = (EditText)questionView.findViewById(R.id.etOption);

                        options.append(htmlOptionOther.replace("[OPTXT]", etOption.getText().toString().trim()));

                        questions.append(htmlQuestion.replace("[QTXT]", tvQuestion.getText()).replace("[OPTIONS]", options.toString()));
                    }

                    String fileBody = htmlMainBody.replace("[TITLE]", pollName).replace("[DATETIME]", dateTime).replace("[NAME]", name).replace("[NOTES]", notes).replace("[QUESTIONS]", questions.toString());

                    String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date()) + ".htm";
                    String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getString(R.string.app_name);
                    File file = new File(dir, fileName);

                    try {
                        File saveDir = new File(dir);
                        if (!saveDir.isDirectory()) {
                            if (!saveDir.mkdirs()) {
                                Toast.makeText(PollActivity.this, R.string.msg_err_create_dir, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        FileOutputStream outputStream = new FileOutputStream(file, false);
                        outputStream.write(fileBody.getBytes());
                        outputStream.close();
                        Toast.makeText(PollActivity.this, R.string.msg_done, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(PollActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setTitle(R.string.title_save_as);
        dlgBuilder.setView(dialogView);
        dlgBuilder.setPositiveButton(R.string.btn_ok, dlgBtnClick);
        dlgBuilder.setNegativeButton(R.string.btn_cancel, null);
        dlgBuilder.create().show();
    }

    private String getHTMLTemplate(int resID) {
        InputStream inputStream = getResources().openRawResource(resID);
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        StringBuilder fileBody = new StringBuilder();
        String readString;

        try {
            while ((readString = bufferedReader.readLine()) != null) {
                fileBody.append(readString);
            }

            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
        } catch (IOException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return fileBody.toString();
    }
}
