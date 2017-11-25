package com.luisjavierlinares.android.doing.model;

import java.util.List;
import java.util.UUID;

import static com.luisjavierlinares.android.doing.model.Like.*;

/**
 * Created by Luis on 18/05/2017.
 */

public interface Likes {

    public Like get(int position);

    public Like get(UUID likeId);

    public List<Like> getAll();

    public List<Like> getAll(LikeType likeType);

    public void add(Like like);

    public void update();

    public int size();
}
