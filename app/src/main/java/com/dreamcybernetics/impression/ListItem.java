package com.dreamcybernetics.impression;

import android.database.Cursor;

import java.util.ArrayList;

public class ListItem {
    public static final byte ITEM_TYPE_POLL = 0;
    public static final byte ITEM_TYPE_QUESTION = 1;
    public static final byte ITEM_TYPE_OPTION = 2;

    private final int _id;
    private String _text;
    private final ArrayList<ListItem> _subItems;

    public ListItem(int id, String text, ArrayList<ListItem> subItems) {
        _id = id;
        _text = text;
        _subItems = subItems;
    }

    public int getId() {
        return _id;
    }

    public String getText() {
        return _text;
    }

    public void setText(String text) {
        _text = text;
    }

    public ArrayList<ListItem> getSubItems() {
        return _subItems;
    }

    public void addSubItem(ListItem subItem) {
        _subItems.add(subItem);
    }

    public static void deleteSubItemsInDatebase(int itemId) {
        String criteria = DBHelper.Polls.COLUMN_NAME_PARENT_ID + "=" + itemId;

        Cursor cursor = DBHelper.select(DBHelper.Polls.TABLE_NAME, new String[] { DBHelper.Polls._ID }, criteria);

        while (cursor.moveToNext()) {
            int tmpID = cursor.getInt(cursor.getColumnIndex(DBHelper.Polls._ID));
            deleteSubItemsInDatebase(tmpID);
        }

        DBHelper.delete(DBHelper.Polls.TABLE_NAME, criteria);
        cursor.close();
    }
}
