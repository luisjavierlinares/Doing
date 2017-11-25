package com.luisjavierlinares.android.doing.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Luis on 18/05/2017.
 */

public class LikesProxy implements Likes {

    private UUID mDoingId;
    private LikeDAO mLikeDAO;
    private List<Like> mLikes;

    public LikesProxy(UUID doingId, LikeDAO likeDAO) {
        mDoingId = doingId;
        mLikeDAO = likeDAO;
        mLikes = null;
    }

    @Override
    public Like get(int position) {
        if (mLikes == null) {
            mLikes = mLikeDAO.getDoingLikes(mDoingId);
        }
        return mLikes.get(position);
    }

    @Override
    public Like get(UUID likeId) {
        if (mLikes == null) {
            mLikes = mLikeDAO.getDoingLikes(mDoingId);
        }
        for (int i = 0; i < mLikes.size(); i++) {
            Like thisLike = mLikes.get(i);
            if (thisLike.getId().equals(likeId)) {
                return thisLike;
            }
        }
        return null;
    }

    @Override
    public List<Like> getAll() {
//        if (mLikes == null) {
            mLikes = mLikeDAO.getDoingLikes(mDoingId);
//        }
        return mLikes;
    }

    @Override
    public List<Like> getAll(Like.LikeType likeType) {
        if (mLikes == null) {
            mLikes = mLikeDAO.getDoingLikes(mDoingId);
        }
        List<Like> thisTypeList = new ArrayList<>();
        for (int i = 0; i < mLikes.size(); i++) {
            Like thisLike = mLikes.get(i);
            if (thisLike.getType() == likeType) {
                thisTypeList.add(thisLike);
            }
        }
        return thisTypeList;
    }

    @Override
    public void add(Like like) {
        if (mLikes == null) {
            mLikes = mLikeDAO.getDoingLikes(mDoingId);
        }
        mLikes.add(like);
    }

    @Override
    public void update() {
        mLikes = mLikeDAO.getDoingLikes(mDoingId);
    }

    @Override
    public int size() {
        if (mLikes == null) {
            return 0;
        }
        return mLikes.size();
    }
}
