package com.luisjavierlinares.android.doing;

import com.luisjavierlinares.android.doing.model.Doing;

/**
 * Created by Luis on 11/04/2017.
 */

public interface DoingDetailCallbacks {

    public static enum DOING_LIST_TYPE {ALL, RECENT, USER};

    void onDoingSelected(Doing doing);
    void onDoingSelected(Doing doing, DOING_LIST_TYPE List_type);
}
