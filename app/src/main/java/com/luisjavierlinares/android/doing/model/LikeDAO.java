package com.luisjavierlinares.android.doing.model;

import android.content.Context;

import com.luisjavierlinares.android.doing.database.LikeDatabase;

import java.util.List;
import java.util.UUID;

/**
 * Created by Luis on 09/04/2017.
 */

public class LikeDAO {

    private static LikeDAO sLikeDAO;
    private Context mContext;

    private LikeDatabase mLikeDatabase;

    public static synchronized LikeDAO get(Context context){
        if (sLikeDAO == null){
            sLikeDAO = new LikeDAO(context);
        }
        return sLikeDAO;
    }

    private LikeDAO(Context context) {
        mContext = context.getApplicationContext();
        mLikeDatabase = LikeDatabase.get(mContext);
    }

    public void addLike(Like like) {
        mLikeDatabase.add(like);
    }

    public List<Like> getAllLikes() {
        return mLikeDatabase.getAllLikes();
    }

    public List<Like> getDoingLikes(UUID doingId) {
        return mLikeDatabase.getDoingLikes(doingId);
    }

    public Like getLike(UUID id) {
        return mLikeDatabase.getLike(id);
    }

    public int countLikes(Doing doing) {
        return ((Long)mLikeDatabase.countLikes(doing)).intValue();
    }

    public void removeLike(Like like) {
        mLikeDatabase.remove(like);
    }

    public Boolean exists(Like like) {
        if (like == null) {return false;}

        if (like.getId() == null) {return false;}

        Like thisLike = mLikeDatabase.getLike(like.getId());

        if (thisLike == null) {return  false;}

        return true;
    }

}
