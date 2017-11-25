package com.luisjavierlinares.android.doing.model;

import android.content.Context;

import com.luisjavierlinares.android.doing.database.CommentaryDatabase;

import java.util.List;
import java.util.UUID;

/**
 * Created by Luis on 09/04/2017.
 */

public class CommentaryDAO {

    private static CommentaryDAO sCommentaryDAO;
    private Context mContext;

    private CommentaryDatabase mCommentaryDatabase;

    public static synchronized CommentaryDAO get(Context context){
        if (sCommentaryDAO == null){
            sCommentaryDAO = new CommentaryDAO(context);
        }
        return sCommentaryDAO;
    }

    private CommentaryDAO(Context context) {
        mContext = context.getApplicationContext();
        mCommentaryDatabase = CommentaryDatabase.get(mContext);
    }

    public void addCommentary(Commentary commentary) {
        mCommentaryDatabase.add(commentary);
    }

    public List<Commentary> getAllCommentaries() {
        return mCommentaryDatabase.getAllCommentaries();
    }

    public List<Commentary> getDoingCommentaries(UUID doing_id) {
        return mCommentaryDatabase.getDoingCommentariesOrderByDate(doing_id);
    }

    public Commentary getCommentary(UUID id) {
        return mCommentaryDatabase.getCommentary(id);
    }

    public Boolean hasACommentary(User user, Doing doing) {
        return  mCommentaryDatabase.hasACommentary(user, doing);
    }

    public int countCommentaries(Doing doing) {
        return ((Long)mCommentaryDatabase.countCommentaries(doing)).intValue();
    }

    public void removeCommentary(Commentary commentary) {
        mCommentaryDatabase.remove(commentary);
    }

    public Boolean exists(Commentary commentary) {
        if (commentary == null) {return false;}

        if (commentary.getId() == null) {return false;}

        Commentary thisCommentary = mCommentaryDatabase.getCommentary(commentary.getId());

        if (thisCommentary == null) {return  false;}

        return true;
    }
}
