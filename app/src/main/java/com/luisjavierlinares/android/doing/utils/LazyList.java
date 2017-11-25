package com.luisjavierlinares.android.doing.utils;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Luis on 05/04/2017.
 */

public class LazyList<T> extends ArrayList<T> {

    private final Cursor mCursor;
    private final ItemFactory<T> mCreator;
    private int mAddedSize;

    public LazyList(Cursor cursor, ItemFactory<T> creator) {
        mCursor = cursor;
        mCreator = creator;
        mAddedSize = 0;
    }

    @Override
    public boolean add(T t) {
        mAddedSize++;
        return super.add(t);
    }

    @Override
    public void add(int index, T element) {
        mAddedSize++;
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        mAddedSize = mAddedSize + c.size();
        return super.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        mAddedSize = mAddedSize + c.size();
        return super.addAll(index, c);
    }

    @Override
    public T remove(int index) {
        return super.remove(index);
    }

    @Override
    public T get(int index) {
        int size = super.size();
        if (index < size) {
            // find item in the collection
            T item = super.get(index);
            if (item == null) {
                item = mCreator.create(mCursor, index);
                set(index, item);
            }
            return item;
        } else {
            // we have to grow the collection
            for (int i = size; i < index; i++) {
                add(null);
                mAddedSize--;
            }
            // create last object, add and return
            T item = mCreator.create(mCursor, index);
            add(item);
            mAddedSize--;
            return item;
        }
    }

    @Override
    public int size() {
        return mCursor.getCount() + mAddedSize;
    }

    public void setAddedSizeLess(int i) {
        if (mAddedSize < i) {
            mAddedSize = 0;
        } else {
            mAddedSize = mAddedSize - i;
        }
    }

    public void closeCursor() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

    public interface ItemFactory<T> {
        T create(Cursor cursor, int index);
    }

}
