package com.luisjavierlinares.android.doing.model;

import android.content.Context;

import com.luisjavierlinares.android.doing.DoingSettings;
import com.luisjavierlinares.android.doing.database.UserDatabase;
import com.luisjavierlinares.android.doing.utils.TextUtils;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.luisjavierlinares.android.doing.model.User.UserState;


/**
 * Created by Luis on 29/03/2017.
 */

public class UserDAO {

    private static UserDAO sUserDAO;
    private Context mContext;

    private UserDatabase mUserDatabase;
    private static User mMyUser;
    private List<User> mUsers;

    public static synchronized UserDAO get(Context context){
        if (sUserDAO == null){
            sUserDAO = new UserDAO(context);
        }
        return sUserDAO;
    }

    private UserDAO(Context context){
        mContext = context.getApplicationContext();
        mUserDatabase = UserDatabase.get(mContext);
        mMyUser = mUserDatabase.getUser(DoingSettings.getMyId(mContext));

//      ONLY FOR TESTING
//        addTestUsers();
//      END OF ONLY FOR TESTING

        mUsers = mUserDatabase.getUsersOrderByName();


    }

    public void addUser(User user){
        if (!exists(user)) {
            mUserDatabase.add(user);
            mUsers.add(user);
        }
    }

    public void updateUser(User user){
        mUserDatabase.update(user);
        for (int i = 0; i < mUsers.size(); i++) {
            User thisUser = mUsers.get(i);

            if (thisUser.getId().equals(user.getId())) {
                thisUser.setName(user.getName());
                thisUser.setUserCode(user.getUserCode());
                thisUser.setState(user.getState());
            }

            if (isMe(user)) {
                if ((user.getFriendName() != null) && (!user.getFriendName().isEmpty())) {
                    mMyUser.setFriendName(user.getFriendName());
                }
                if ((user.getUserCode() != null)  && (!user.getUserCode().isEmpty())) {
                    mMyUser.setUserCode(user.getUserCode());
                }
            }

        }
    }

    public void setCurrentUser(User user) {
        mMyUser = user;
    }

    public User getMyUser() {
        if (mMyUser == null) {
            mMyUser = mUserDatabase.getUser(DoingSettings.getMyId(mContext));
        }
        return mMyUser;
    }

    public User getUser(UUID id) {
        User user = mUserDatabase.getUser(id);
        if (user == null) {
            user = UserFactory.get(mContext).getUser(UUID.randomUUID(), null, UserState.UNKNOWN);
        }
        return user;
    }

    public User getUserWithCode(String userCode) {
        User user = mUserDatabase.getUserWithCode(userCode);
        if (user == null) {
            user = UserFactory.get(mContext).getUser(UUID.randomUUID(), null, UserState.UNKNOWN);
            user.setUserCode(userCode);
//            user.setFriendCode(userCode.substring(0, 5));
        }
        return user;
    }

    public User getUserWithFriendCode(String friendCode) {
        User user = mUserDatabase.getUserWithFriendCode(friendCode);
        if (user == null) {
            user = UserFactory.get(mContext).getUser(UUID.randomUUID(), null, UserState.UNKNOWN);
            user.setUserCode(friendCode);
        }
        return user;
    }

    //  ONLY FOR TESTING
    public User getUserInPosition(int position) {
        return mUsers.get(position);
    }

    public List<User> getAllUsers() {
        return mUsers;
    }

    public List<User> getAllOtherUsers() {
        List<User> otherUsers = new ArrayList<>();

        for(int i = 0; i < mUsers.size(); i++) {
            User thisUser = mUsers.get(i);
            if (!isMe(thisUser)) {
                otherUsers.add(thisUser);
            }
        }
        return otherUsers;
    }

    public List<User> getAllActiveUsers() {
        List<User> otherUsers = new ArrayList<>();

        for(int i = 0; i < mUsers.size(); i++) {
            User thisUser = mUsers.get(i);
            if (thisUser.isActive())  {
                otherUsers.add(thisUser);
            }
        }
        return otherUsers;
    }

    public List<User> getInvitingMeUsers() {
        List<User> invitedUsers = new ArrayList<>();

        for(int i = 0; i < mUsers.size(); i++) {
            User thisUser = mUsers.get(i);
            if (thisUser.isInvitingMe())  {
                invitedUsers.add(thisUser);
            }
        }
        return invitedUsers;
    }

    public void removeUser(User user) {
        mUserDatabase.remove(user);
    }

    public boolean isMe(User user) {
        if (user == null) {return false;}

        if (user.getId() == null) {return false;}

        if (user.getId().equals(mMyUser.getId())) {return true;
        }
        return false;
    }

    public Boolean exists(User user) {
        if (user == null) {return false;}

        if (user.getUserCode() == null) {return false;}

        User thisUser = mUserDatabase.getUserWithCode(user.getUserCode());

        if (thisUser == null) {return  false;}

        return true;
    }

    public Boolean existsUserWithCompactName(String compactName) {
        for(int i = 0; i < mUsers.size(); i++) {
           User thisUser = mUsers.get(i);
           if (thisUser.getFriendName() == null) continue;
           String thisCompactName = TextUtils.toNoAccentsNoSpacesLowerCase(thisUser.getFriendName());
            if (thisCompactName.equals(compactName)) {
                return true;
            }
        }
        return false;
    }

    //  ONLY FOR TESTING
    private void addTestUsers() {

        List<User> users = mUserDatabase.getUsers();
        if (users.size() > 1) {return;}

        User user = UserFactory.get(mContext).getUser(UUID.randomUUID(), "Nuria", UserState.ACTIVE);
//        mUsers.add(user);
        mUserDatabase.add(user);
        user = UserFactory.get(mContext).getUser(UUID.randomUUID(), "Jorge", UserState.ACTIVE);
//        mUsers.add(user);
        mUserDatabase.add(user);
        user = UserFactory.get(mContext).getUser(UUID.randomUUID(), "Beatriz", UserState.ACTIVE);
//        mUsers.add(user);
        mUserDatabase.add(user);
        user = UserFactory.get(mContext).getUser(UUID.randomUUID(), "José María Marin Guillem", UserState.ACTIVE);
//        mUsers.add(user);
        mUserDatabase.add(user);
    }

    public int getSize() {
        return mUsers.size();
    }
}
