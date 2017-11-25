package com.luisjavierlinares.android.doing.model;

import com.luisjavierlinares.android.doing.utils.LazyList;

import java.util.List;
import java.util.UUID;

/**
 * Created by Luis on 18/05/2017.
 */

public class CommentariesProxy implements Commentaries {

    private UUID mDoingId;
    private CommentaryDAO mCommentaryDAO;
    private List<Commentary> mCommentaries;

    public CommentariesProxy(UUID doingId, CommentaryDAO commentaryDAO) {
        mDoingId = doingId;
        mCommentaryDAO = commentaryDAO;
        mCommentaries = null;
    }

    @Override
    public Commentary get(int position) {
        if (mCommentaries == null) {
            mCommentaries = mCommentaryDAO.getDoingCommentaries(mDoingId);
        }
        return mCommentaries.get(position);
    }

    @Override
    public Commentary get(UUID commentaryId) {
        if (mCommentaries == null) {
            mCommentaries = mCommentaryDAO.getDoingCommentaries(mDoingId);
        }

        for (int i = 0; i < mCommentaries.size(); i++) {
            Commentary thisCommentary = mCommentaries.get(i);
            if (thisCommentary.getId().equals(commentaryId)) {
                return thisCommentary;
            }
        }
        return null;
    }

    @Override
    public List<Commentary> getAll() {
        if (mCommentaries == null) {
            mCommentaries = mCommentaryDAO.getDoingCommentaries(mDoingId);
        }
        return mCommentaries;
    }

    @Override
    public void add(Commentary commentary) {
        if (mCommentaries == null) {
            mCommentaries = mCommentaryDAO.getDoingCommentaries(mDoingId);
        }
        mCommentaries.add(commentary);
    }

    @Override
    public void update() {
        mCommentaries = mCommentaryDAO.getDoingCommentaries(mDoingId);
    }

    @Override
    public int size() {
        if (mCommentaries == null) {
            return 0;
        }
        return mCommentaries.size();
    }
}
