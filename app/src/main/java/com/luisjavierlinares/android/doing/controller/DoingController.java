package com.luisjavierlinares.android.doing.controller;

import android.content.Context;
import android.graphics.Bitmap;

import com.luisjavierlinares.android.doing.DoingSettings;
import com.luisjavierlinares.android.doing.events.CommentaryAddedEvent;
import com.luisjavierlinares.android.doing.events.DoingAddedEvent;
import com.luisjavierlinares.android.doing.events.LikeAddedEvent;
import com.luisjavierlinares.android.doing.events.UserAddedEvent;
import com.luisjavierlinares.android.doing.events.UserUpdatedEvent;
import com.luisjavierlinares.android.doing.managers.DoingAccountManager;
import com.luisjavierlinares.android.doing.messaging.MessagingSystem;
import com.luisjavierlinares.android.doing.messaging.MessagingUpdate.UpdateType;
import com.luisjavierlinares.android.doing.model.Commentary;
import com.luisjavierlinares.android.doing.model.CommentaryDAO;
import com.luisjavierlinares.android.doing.model.CommentaryFactory;
import com.luisjavierlinares.android.doing.model.Doing;
import com.luisjavierlinares.android.doing.model.DoingDAO;
import com.luisjavierlinares.android.doing.model.DoingFactory;
import com.luisjavierlinares.android.doing.model.Like;
import com.luisjavierlinares.android.doing.model.LikeDAO;
import com.luisjavierlinares.android.doing.model.LikeFactory;
import com.luisjavierlinares.android.doing.model.User;
import com.luisjavierlinares.android.doing.model.UserDAO;
import com.luisjavierlinares.android.doing.model.UserFactory;
import com.luisjavierlinares.android.doing.services.DoingLocalDataBackup;
import com.luisjavierlinares.android.doing.services.UpdateAndNotifyService;
import com.luisjavierlinares.android.doing.managers.StorageManager;
import com.luisjavierlinares.android.doing.utils.NetworkConnection;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Created by Luis on 12/05/2017.
 */

public class DoingController {

    private static DoingController sDoingController;
    private Context mContext;
    private UserDAO mUserDAO;
    private DoingDAO mDoingDAO;
    private LikeDAO mLikeDAO;
    private CommentaryDAO mCommentaryDAO;
    private MessagingSystem mMessagingSystem;
    private StorageManager mStorageManager;
    private NetworkConnection mNetworkConnection;

    private EventBus mEventBus;

    private Thread mUpdateDbCountsThread;

    public static synchronized DoingController get(Context context) {
        if (sDoingController == null) {
            sDoingController = new DoingController(context);
        }
        return sDoingController;
    }

    private DoingController(Context context) {
        mContext = context;
        mEventBus = EventBus.getDefault();
        init();
    }

    public void init() {
        //  Fist time indicator
        if (DoingSettings.isFirstTime(mContext) == true) {
            setNotifications(true);
            DoingSettings.setFirstTime(mContext, false);
        }

        // initialize catalogs
        mUserDAO = UserDAO.get(mContext);
        mDoingDAO = DoingDAO.get(mContext);
        mLikeDAO = LikeDAO.get(mContext);
        mCommentaryDAO = CommentaryDAO.get(mContext);

        // initialize messaging system
        mMessagingSystem = MessagingSystem.get(mContext);
        mMessagingSystem.addListeners(getMyUser());

        // initialize storage system
        mStorageManager = StorageManager.get(mContext);
        setUsersAvatarDownloader();
        if (DoingSettings.isAvatarUploadPending(mContext)) {
            uploadAvatar(getMyUser());
        }

        // initialize network connection checker
        mNetworkConnection = new NetworkConnection(mContext);

        // initiate update and notification service
        UpdateAndNotifyService.start(mContext);

        // register name to search
        mMessagingSystem.addMyNameToSearch();
    }

    public void start() {
        resetLastNotificationCounters();
        UpdateAndNotifyService.clearAllNotifications(mContext);
        mMessagingSystem.goOnline();
        fixLastDatabaseCountsInBackground(100);
        backupLocalData();
    }

    public void stop() {
        mMessagingSystem.goOffline();
        resetLastNotificationCounters();
    }

    public void setUsersAvatarDownloader() {
        List<User> users = mUserDAO.getAllActiveUsers();

        for (int i = 0; i < users.size(); i++) {
            User thisUser = users.get(i);
            if (avatarHasBeenUpdated(thisUser)) {
                mStorageManager.downloadAvatarFromUser(thisUser);
            }
        }

    }

    private Boolean avatarHasBeenUpdated(User user) {
        Long localUpdateTime = mMessagingSystem.getUpdateTime(user, UpdateType.AVATAR);
        Long lastUpdate = user.getLastUpdate().getTime();
        Long time = mMessagingSystem.getEstimatedTime().getTime();

        // if the user is updated return false
        if (lastUpdate < localUpdateTime) {
            return false;
        }

        // if the user have been updated in the 12 hours return false
        long timeDiff = time - localUpdateTime;
        long hourDiff = TimeUnit.MILLISECONDS.toHours(timeDiff);

        if (hourDiff < StorageManager.AVATAR_MIN_UPDATE_TIME_IN_HOURS) {
            return false;
        }

        return true;
    }

    public void registerMyUser() {
        User myUser = getMyUser();

        if ((myUser.getUserCode() == null) && (myUser.getFriendName() != null) && (isDeviceOnline())) {
            mMessagingSystem.sendMyUser();
        }
    }

    public void recoverMyUser(String recoveryCode) {
        DoingAccountManager accountManager = DoingAccountManager.get(mContext);
        String userId = accountManager.getOnlineIdFromRecoveryCode(recoveryCode);
        String secret = accountManager.getOnlineSecretFromRecoveryCode(recoveryCode);

        mMessagingSystem.recoverMyUser(userId, secret);
    }

    public User getMyUser() {
        return mUserDAO.getMyUser();
    }

    public User getUser(UUID userId) {
        return mUserDAO.getUser(userId);
    }

    public User getUserWithCode(String userCode) {
        return mUserDAO.getUserWithCode(userCode);
    }

    public User getUserWithFriendCode(String friendCode) {
        return mUserDAO.getUserWithFriendCode(friendCode);
    }

    public List<User> getAllUsers() {
        return mUserDAO.getAllUsers();
    }

    public List<User> getAllOtherUsers() {
        return mUserDAO.getAllOtherUsers();
    }

    public List<User> getAllOtherUsersOrderByState() {
        List<User> users = new ArrayList<>();

        List<User> allUsers = mUserDAO.getAllOtherUsers();

        List<User> usersOk = new ArrayList<>();
        List<User> usersInactive = new ArrayList<>();
        List<User> usersPause = new ArrayList<>();
        List<User> usersInviting = new ArrayList<>();
        List<User> usersKo = new ArrayList<>();

        for (User user : allUsers) {
            switch (user.getState()) {
                case ACTIVE:
                    usersOk.add(user);
                    break;
                case INACTIVE:
                    usersInactive.add(user);
                    break;
                case INVITED_BY_ME:
                    usersPause.add(user);
                    break;
                case INVITING_ME:
                    usersInviting.add(user);
                    break;
                case IGNORED_BY_ME:
                case IGNORING_ME:
                    usersKo.add(user);
                    break;
                default:
                    usersPause.add(user);
            }
        }

        users.addAll(usersInviting);
        users.addAll(usersOk);
        users.addAll(usersPause);
        users.addAll(usersInactive);
        users.addAll(usersKo);

        return users;
    }

    public List<User> getAllActiveFriends() {
        return mUserDAO.getAllActiveUsers();
    }

    public List<User> getInvitingMeFriends() {
        return mUserDAO.getInvitingMeUsers();
    }

    public Doing getDoing(UUID doingId) {
        return mDoingDAO.getDoing(doingId);
    }

    public Doing getLastDoing(User user) {
        return mDoingDAO.getLastDoing(user);
    }

    public List<Doing> getAllDoings() {
        return mDoingDAO.getAllDoings();
    }

    public List<Doing> getAllDoingsFromDataBase() {
        return mDoingDAO.getAllDoingsFromDatabase();
    }

    public List<Doing> getRecentDoings() {
        return mDoingDAO.getRecentDoings();
    }

    public List<Doing> getAllDoingsFromUser(User user) {
        return mDoingDAO.getAllDoingsFromUser(user);
    }

    public List<Doing> getAllDoingsFromUser(User user, Doing.DoingAction doingAction) {
        return mDoingDAO.getAllDoingsFromUser(user, doingAction);
    }

    public List<User> getReceivers(Doing doing) {
        return mDoingDAO.getReceivers(doing);
    }

    public List<User> getReceiversNotSender(Doing doing) {
        List<User> receivers = mDoingDAO.getReceivers(doing);
        List<User> receiversNotSender = new ArrayList<>();
        User sender = doing.getUser();

        for (User receiver : receivers) {
            if (!sender.getFriendCode().equals(receiver.getFriendCode())) {
                receiversNotSender.add(receiver);
            }
        }

        return receiversNotSender;
    }

    public List<User> getFriendSuggestions(int maxSuggestions) {
        List<User> lastReceivers = mDoingDAO.getLastReceivers(1000);
        List<User> friendSuggestions = new ArrayList<>();

        for (User receiver : lastReceivers) {
            if (!receiver.isAFriendOfAFriend()) {
                continue;
            }

            boolean alreadyAdded = false;
            for (int i = 0; i < friendSuggestions.size() && !alreadyAdded; i++) {
                User friendSuggestion = friendSuggestions.get(i);
                if (receiver.getUserCode().equals(friendSuggestion.getUserCode())) {
                    alreadyAdded = true;
                }
            }

            if ((!alreadyAdded) && (friendSuggestions.size() < maxSuggestions)) {
                friendSuggestions.add(receiver);
            }

        }

        return friendSuggestions;
    }

    public void setMyUser(User user) {
        mUserDAO.setCurrentUser(user);
    }

    public void addFriend(String name, String userCode, String friendCode, String friendName) {
        Date estimatedTime = mMessagingSystem.getEstimatedTime();
        User friend = mUserDAO.getUserWithCode(userCode);
        // if the friend is unknown we create a new friend
        if (friend.isUnknown()) {
            friend = UserFactory.get(mContext).getUser(name);
            friend.setUserCode(userCode);
            friend.setFriendCode(friendCode);
            friend.setFriendName(friendName);
            friend.setAsInvited();
            friend.setCreationDate(estimatedTime);
            friend.setLastUpdate(estimatedTime);
            addFriend(friend);
        }
        // else if is a friend that has invited us we activate the friendship
        else if (friend.isInvitingMe()) {
            friend.setName(name);
            friend.setFriendCode(friendCode);
            activateFriendship(friend);
        }
    }

    public void addFriend(User friend) {
        mUserDAO.addUser(friend);

        mMessagingSystem.sendFriendship(getMyUser(), friend);
        mMessagingSystem.addListenersToUser(friend);

        mEventBus.postSticky(new UserAddedEvent(friend));
    }

    private void activateFriendship(User friend) {
        friend.activate();
        mUserDAO.updateUser(friend);

        mMessagingSystem.activateFriendship(getMyUser(), friend);
        mMessagingSystem.addListenersToUser(friend);

        mStorageManager.downloadAvatarFromUser(friend);

        mEventBus.postSticky(new UserAddedEvent(friend));
    }

    public void addDoing(User user, Doing.DoingAction action, String text) {
        Date date = MessagingSystem.get(mContext).getEstimatedTime();
        Doing doing = DoingFactory.get(mContext).getDoing(user, action, text, date);
        List<User> receivers = mUserDAO.getAllActiveUsers();
        addDoing(doing, receivers);
    }

    public void addDoing(User user, Doing.DoingAction action, String text, List<User> receivers) {
        Date date = MessagingSystem.get(mContext).getEstimatedTime();
        Doing doing = DoingFactory.get(mContext).getDoing(user, action, text, date);
        addDoing(doing, receivers);
    }

    public void addDoing(Doing doing, List<User> receivers) {
        receivers.add(getMyUser());

        mDoingDAO.addDoing(doing);
        mDoingDAO.addReceivers(doing, receivers);

        mMessagingSystem.sendDoing(doing, receivers);
        mEventBus.postSticky(new DoingAddedEvent(doing));
    }

    public void addLike(User user, Doing doing) {
        Date estimatedTime = mMessagingSystem.getEstimatedTime();
        Like like = LikeFactory.get(mContext).getLike(user, doing, estimatedTime);

        if (mLikeDAO.exists(like)) {
            if ((!doing.isLikedByMe()) && (isMe(like.getSender()))) {
                doing.setLikedByMe(true);
                mDoingDAO.updateDoing(doing);
            }
            return;
        }

        Boolean isANewLike = doing.addLike(like);
        if (!isANewLike) {
            return;
        }
        if (isMe(like.getSender())) {
            doing.setLikedByMe(true);
        }

        List<User> receivers = mDoingDAO.getReceivers(doing);

        mDoingDAO.updateDoing(doing);
        mLikeDAO.addLike(like);
        mMessagingSystem.sendLike(like, receivers);
        mEventBus.postSticky(new LikeAddedEvent(like));
    }

    public void addCommentary(User sender, String text, Doing doing) {
        Date estimatedTime = mMessagingSystem.getEstimatedTime();
        Commentary commentary = CommentaryFactory.get(mContext).getCommentary(sender, doing, text, estimatedTime);
        addCommentary(commentary, doing);
    }

    public void addCommentary(Commentary commentary, Doing doing) {
        if (mCommentaryDAO.exists(commentary)) {
            return;
        }

        List<User> receivers = mDoingDAO.getReceivers(doing);

        doing.addCommentary(commentary);
        mDoingDAO.updateDoing(doing);
        mCommentaryDAO.addCommentary(commentary);
        mMessagingSystem.sendCommentary(commentary, receivers);
        mEventBus.postSticky(new CommentaryAddedEvent(commentary));
    }

    public void updateUser(User user) {
        mUserDAO.updateUser(user);
        mDoingDAO.reloadDoings();
        mEventBus.postSticky(new UserUpdatedEvent(user));
    }

    public void updateDoing(Doing doing) {
        if (mDoingDAO.exists(doing)) {
            mDoingDAO.updateDoing(doing);
        }
    }

    public void updateOnlyDatabaseCounts(Doing doing) {
        if (mDoingDAO.exists(doing)) {
            mDoingDAO.updateOnlyCounts(doing);
        }
    }

    public void updateAvatar(User user, Bitmap avatar) {
        mStorageManager.saveAvatarImage(avatar, user);
        mStorageManager.uploadAvatarImage(avatar, user);
        Date lastUpdate = mMessagingSystem.getEstimatedTime();
        user.setLastUpdate(lastUpdate);
        mMessagingSystem.sendUserUpdateTime(user);
        mUserDAO.updateUser(user);
        mEventBus.postSticky(new UserUpdatedEvent(user));
    }

    public Bitmap loadAvatar(User user) {
        return mStorageManager.loadAvatarImage(user);
    }

    public void uploadAvatar(User user) {
        Bitmap avatar = loadAvatar(user);
        if (avatar != null) {
            mStorageManager.uploadAvatarImage(avatar, user);
        }
    }

    public void ignoreFriend(User friend) {
        friend.setAsIgnored();
        mUserDAO.updateUser(friend);

        mEventBus.postSticky(new UserUpdatedEvent(friend));
    }

    public void reconsiderIgnoredFriend(User friend) {
        if (!friend.isIgnoredByMe()) {
            return;
        }

        friend.setAsInvitingMe();
        mUserDAO.updateUser(friend);

        mEventBus.postSticky(new UserUpdatedEvent(friend));
    }

    public void updateCounts(Doing doing) {
        if (doing == null) {
            return;
        }
        Doing loadedDoing = mDoingDAO.getDoing(doing.getId());

        if (loadedDoing.getLikesCount() >= doing.getLikesCount()) {
            doing.setLikesCount(loadedDoing.getLikesCount());
            doing.setHasNewLikes(loadedDoing.hasNewLikes());
        }

        if (loadedDoing.getCommentariesCount() >= doing.getCommentariesCount()) {
            doing.setCommentariesCount(loadedDoing.getCommentariesCount());
            doing.setHasNewCommentaries(loadedDoing.hasNewCommentaries());
        }

        if (!doing.isLikedByMe() && loadedDoing.isLikedByMe()) {
            doing.setLikedByMe(loadedDoing.isLikedByMe());
        }
    }

    public void updateHasNew(Doing doing) {
        Doing loadedDoing = mDoingDAO.getDoing(doing.getId());
        doing.setHasNewLikes(loadedDoing.hasNewLikes());
        doing.setHasNewCommentaries(loadedDoing.hasNewCommentaries());
    }

    private void fixLastDatabaseCountsInBackground(final int scope) {
        if ((mUpdateDbCountsThread != null) && (mUpdateDbCountsThread.isAlive())) {
            return;
        }

        mUpdateDbCountsThread = new Thread(new Runnable() {
            @Override
            public void run() {
                fixLastDatabaseCounts(scope);
            }
        });
        mUpdateDbCountsThread.setPriority(Thread.MIN_PRIORITY);
        mUpdateDbCountsThread.start();
    }

    private void fixLastDatabaseCounts(int scope) {
        List<Doing> doings = mDoingDAO.getAllDoings();
        for (int i = 0; i < scope || i < doings.size(); i++) {
            fixDatabaseCounts(doings.get(i));
        }
    }

    public void fixDatabaseCounts(Doing doing) {
        if (doing == null) {
            return;
        }

//      Update doing likes counter if necessary
        fixLikesCount(doing);

//      Update doing commentaries counter if necessary
        fixCommentariesCount(doing);
    }

    private void fixLikesCount(Doing doing) {
        //Check if the count and the actual number of likes is not coherent
        int likesCount = doing.getLikesCount();
        List<Like> allLikes = doing.getLikes();
        int likesSize = allLikes.size();

        if (likesCount < likesSize) {
            int likesActualCount = likesSize;
            // Check if the count saved on the database is higher,
            // if that is the case update the counter with that value
            Doing loadedDoing = mDoingDAO.getDoing(doing.getId());
            if ((loadedDoing != null) && (loadedDoing.getLikesCount() > likesActualCount)) {
                likesActualCount = loadedDoing.getLikesCount();
            }
            doing.setLikesCount(likesActualCount);
            updateOnlyDatabaseCounts(doing);
            mEventBus.postSticky(new LikeAddedEvent(null));
        }

        // Update liked by me boolean
        if (!doing.isLikedByMe()) {
            for (Like like : allLikes) {
                if (isMe(like.getSender())) {
                    doing.setLikedByMe(true);
                    updateOnlyDatabaseCounts(doing);
                    mEventBus.postSticky(new LikeAddedEvent(null));
                }
            }
        }
    }

    private void fixCommentariesCount(Doing doing) {
        // Check if the count and the actual number of commentaries is not coherent
        int commentariesCount = doing.getCommentariesCount();
        int commentariesSize = doing.getCommentaries().size();

        if (commentariesCount < commentariesSize) {
            int commentariesActualCount = commentariesSize;
            // Check if the count saved on the database is higher,
            // if that is the case update the counter with that value
            Doing loadedDoing = mDoingDAO.getDoing(doing.getId());
            if ((loadedDoing != null) && (loadedDoing.getCommentariesCount() > commentariesActualCount)) {
                commentariesActualCount = loadedDoing.getCommentariesCount();
            }
            doing.setCommentariesCount(commentariesActualCount);
            updateOnlyDatabaseCounts(doing);
            mEventBus.postSticky(new CommentaryAddedEvent(null));

        }
    }

    public boolean exists(Doing doing) {
        return mDoingDAO.exists(doing);
    }

    public boolean exists(Like like) {
        return mLikeDAO.exists(like);
    }

    public boolean exists(Commentary commentary) {
        return mCommentaryDAO.exists(commentary);
    }

    public boolean existsUserWithCompactName(String compactName) {
        return mUserDAO.existsUserWithCompactName(compactName);
    }

    public void checkFriend(String name) {
        mMessagingSystem.listenOnceCheckFriend(name);
    }

    public void checkIfNameExists(String name) {
        mMessagingSystem.listenOnceCheckName(name);
    }

    public void searchForOnlineUsers(String str) {
        mMessagingSystem.searchForOnlineUsers(str, 5);
    }

    public void resetLastNotificationCounters() {
        DoingSettings.setLastDoingNotificationCount(mContext, 0);
        DoingSettings.setLastLikeNotificationCount(mContext, 0);
        DoingSettings.setLastCommentaryNotificationCount(mContext, 0);
        DoingSettings.setLastInvitationNotificationCount(mContext, 0);
    }

    public void setNotifications(Boolean isOn) {
        DoingSettings.setNotificationOn(mContext, isOn);
    }

    public Boolean isMe(User user) {
        return mUserDAO.isMe(user);
    }

    public Boolean isDeviceOnline() {
        return mNetworkConnection.isOnline();
    }

    public Boolean isMyUserRegistered() {
        return getMyUser().getUserCode() != null;
    }

    public Boolean hasPendingInvitations() {
        return !(getInvitingMeFriends().isEmpty());
    }

    private void backupLocalData() {
        DoingLocalDataBackup localBackup = new DoingLocalDataBackup(mContext);
        localBackup.start();
    }

}
