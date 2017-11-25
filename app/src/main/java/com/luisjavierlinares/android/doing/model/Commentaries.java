package com.luisjavierlinares.android.doing.model;

import java.util.List;
import java.util.UUID;

/**
 * Created by Luis on 18/05/2017.
 */

public interface Commentaries {

    public Commentary get(int position);

    public Commentary get(UUID commentaryId);

    public List<Commentary> getAll();

    public void add(Commentary commentary);

    public void update();

    public int size();
}
