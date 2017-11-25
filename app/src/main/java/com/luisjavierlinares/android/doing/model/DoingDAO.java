package com.luisjavierlinares.android.doing.model;

import android.content.Context;

import com.luisjavierlinares.android.doing.database.DoingDatabase;
import com.luisjavierlinares.android.doing.database.DoingReceiversDatabase;
import com.luisjavierlinares.android.doing.time.AgoDate;
import com.luisjavierlinares.android.doing.utils.LazyList;
import com.luisjavierlinares.android.doing.utils.RandomUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.luisjavierlinares.android.doing.model.Doing.DoingAction;

/**
 * Created by Luis on 25/03/2017.
 */

public class DoingDAO {

    private static final int DATABASE_LOAD_LIMIT = 250000; // load "only" the last 250000 doings :)
    private static final int LOCAL_SEARCH_LIMIT = 1000; // only search the last 1000 doings in the local copy

    private static DoingDAO sDoingDAO;
    private Context mContext;

    private DoingDatabase mDoingDatabase;
    private List<Doing> mDoings;
    private DoingReceiversDatabase mDoingReceiversDatabase;

    public static synchronized DoingDAO get(Context context){
        if (sDoingDAO == null){
            sDoingDAO = new DoingDAO(context);
        }
        return sDoingDAO;
    }

    private DoingDAO(Context context){
        mContext = context.getApplicationContext();
        mDoingDatabase = DoingDatabase.get(mContext);
        mDoingReceiversDatabase = DoingReceiversDatabase.get(mContext);

//      ONLY FOR TESTING
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                addTestDoings();
//            }
//        }).start();
//      END OF ONLY FOR TESTING

        mDoings = mDoingDatabase.getAllLazyDoingsOrderByDate();
        long i = mDoingDatabase.size();
    }

    public void addDoing(Doing doing) {

        if (exists(doing)) {return;}

        mDoings.add(0, doing);
        mDoingDatabase.add(doing);

    }

    public void addReceiver(Doing doing, User receiver) {
        if (doing == null || doing.getId() == null) {return;}

        if (receiver == null || receiver.getFriendCode() == null) {return;}

        mDoingReceiversDatabase.add(doing, receiver);
    }

    public void addReceivers(Doing doing, List<User> receivers){
        for(User receiver:receivers) {
           addReceiver(doing, receiver);
        }
    }

    public void updateDoing(Doing doing) {
        mDoingDatabase.update(doing);
    }

    public void updateOnlyCounts(Doing doing) {
        mDoingDatabase.updateCounts(doing);
    }

    public void updateDoingLikesCount(UUID doingId) {
        if (doingId == null) {return;}

        Doing doing = mDoingDatabase.getDoing(doingId);

        if (doing == null) {return;}

        int likesCount = doing.getLikes().size();
        doing.setLikesCount(likesCount);
        doing.setHasNewLikes(true);

        mDoingDatabase.update(doing);
    }

    public void updateDoingCommentariesCount(UUID doingId) {
        if (doingId == null) {return;}

        Doing doing = mDoingDatabase.getDoing(doingId);

        if (doing == null) {return;}

        int commentariesCount = doing.getCommentaries().size();
        doing.setCommentariesCount(commentariesCount);
        doing.setHasNewCommentaries(true);

        mDoingDatabase.update(doing);
    }

    public List<Doing> getAllDoings() {
        return mDoings;
//        return mDoingDatabase.getAllLazyDoingsOrderByDate();
    }

    public List<Doing> getAllDoingsFromDatabase() {
        return mDoingDatabase.getAllLazyDoingsOrderByDate();
    }

    public List<Doing> getAllDoingsFromUser(User user) {
        LazyList<Doing> mUserDoings = mDoingDatabase.getAllLazyDoingsFromUser(user.getId(), DATABASE_LOAD_LIMIT);
        return mUserDoings;
    }

    public List<Doing> getAllDoingsFromUser(User user, DoingAction doingAction) {
        LazyList<Doing> mUserDoings = mDoingDatabase.getAllLazyDoingsFromUser(user.getId(), doingAction, DATABASE_LOAD_LIMIT);
        return mUserDoings;
    }

    public List<Doing> getRecentDoings() {
        // by default we get the last doing of each user if this was added
        // less than three hours ago
        AgoDate agoDate = new AgoDate(3, AgoDate.DateScope.HOURS);
        return getRecentDoings(agoDate);
    }

    public List<Doing> getRecentDoings(AgoDate agoDate) {
        UserDAO usersCatalog = UserDAO.get(mContext);
        List<User> users = usersCatalog.getAllUsers();
        List<Doing> recentDoings = new ArrayList<>();

        for (User user: users) {
            // get the last doing of the user from the database
            Doing doing = mDoingDatabase.getLastDoing(user);
            if (doing != null) {
                // check if the last doing was added before agoDate
                AgoDate doingAgoDate = AgoDate.getAgoDate(doing.getDate());
                if (doingAgoDate.isShorterThan(agoDate)){
                   recentDoings.add(doing);
                }
            }
        }

        if (recentDoings.size() > 1) {
            Collections.sort(recentDoings, new Comparator<Doing>() {
                @Override
                public int compare(Doing o1, Doing o2) {
                    return o2.getDate().compareTo(o1.getDate());
                }
            });
        }

        return recentDoings;
    }

    public List<Doing> getOldDoings(long sizeLimit) {
        return mDoingDatabase.getOldDoings(sizeLimit);
    }

    public Doing getDoing(UUID id) {
        return mDoingDatabase.getDoing(id);
    }

    public Doing getLastDoing(User user) {
        return mDoingDatabase.getLastDoing(user);
    }

    public List<User> getReceivers(Doing doing) {
        List<User> receivers = new ArrayList<>();

        if (doing == null || doing.getId() == null) {return receivers;}

        receivers = mDoingReceiversDatabase.getReceiversFromDoing(doing.getId());

        return receivers;
    }

    public List<User> getOtherReceivers(Doing doing) {
        List<User> receivers = getReceivers(doing);

        User myUser = UserDAO.get(mContext).getMyUser();
        for (User receiver:receivers) {
            if (receiver.getUserCode() == myUser.getUserCode()) {
                receivers.remove(receiver);
            }
        }

        return receivers;
    }

    public List<User> getLastReceivers(int sizeLimit) {
        return mDoingReceiversDatabase.getReceivers(sizeLimit);
    }

    public void removeDoing(Doing doing) {
        mDoingDatabase.remove(doing);
    }

    public Boolean exists(Doing doing) {
        if (doing == null) {return false;}

        if (doing.getId() == null) {return false;}

        Doing thisDoing = mDoingDatabase.getDoing(doing.getId());

        if (thisDoing == null) {return false;}

        return true;
    }

    public long getSize() {
        return mDoingDatabase.size();
    }

    public void reloadDoings() {
        mDoings = mDoingDatabase.getAllLazyDoingsOrderByDate(DATABASE_LOAD_LIMIT);
    }

    private void addTestDoings() {
        UserDAO usersCatalog = UserDAO.get(mContext);
        for(int i = 0; i < 500000; i++) {
            User user = usersCatalog.getUserInPosition(RandomUtils.getRandomNumberBetween(0, usersCatalog.getSize() - 1));
            DoingAction action = DoingAction.PLAYING;
            String text = "El increible juego de los enanos de mundo subterraneo Vol. " + Integer.toString(i);
            if (i%3 == 0) {
                action = DoingAction.WATCHING;
                text = "Acacias 38";
            }
            if (i%4 == 0) {
                action = DoingAction.READING;
                text = "El señor de los anillos - El retorno del rey";
            }
            if (i%5 == 0) {
                action = DoingAction.LISTENING;
                text = "Queen";
            }
            if (i%6 == 0) {
                action = DoingAction.ENJOYING;
                text = "Un día en la playa";
            }
            Doing doing = DoingFactory.get(mContext).getDoing(user, action, text);
//            mDoings.add(doing);
            mDoingDatabase.add(doing);
            for (int j = 0; j < 1000; j++) {
                Commentary commentary = CommentaryFactory.get(mContext).getCommentary(user, doing, "bla bla bla bla");
                doing.addCommentary(commentary);
                CommentaryDAO.get(mContext).addCommentary(commentary);
                DoingDAO.get(mContext).updateDoing(doing);
            }
        }

    }

}
