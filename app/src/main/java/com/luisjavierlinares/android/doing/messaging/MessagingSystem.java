package com.luisjavierlinares.android.doing.messaging;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import com.luisjavierlinares.android.doing.DoingSettings;
import com.luisjavierlinares.android.doing.database.MessagingUpdatesDatabase;
import com.luisjavierlinares.android.doing.events.AccountRecoveredEvent;
import com.luisjavierlinares.android.doing.events.DoingReceivedEvent;
import com.luisjavierlinares.android.doing.events.FriendCheckedEvent;
import com.luisjavierlinares.android.doing.events.FriendSearchEvent;
import com.luisjavierlinares.android.doing.events.MyUserCreatedEvent;
import com.luisjavierlinares.android.doing.events.MyUserUpdatedEvent;
import com.luisjavierlinares.android.doing.events.UserNameExistsEvent;
import com.luisjavierlinares.android.doing.events.UserUpdatedEvent;
import com.luisjavierlinares.android.doing.managers.DoingAccountManager;
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
import com.luisjavierlinares.android.doing.model.User.UserState;
import com.luisjavierlinares.android.doing.model.UserDAO;
import com.luisjavierlinares.android.doing.model.UserFactory;
import com.luisjavierlinares.android.doing.managers.StorageManager;
import com.luisjavierlinares.android.doing.services.UpdateAndNotifyService;
import com.luisjavierlinares.android.doing.utils.RandomUtils;
import com.luisjavierlinares.android.doing.utils.TextUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.luisjavierlinares.android.doing.messaging.MessagingUpdate.*;

/**
 * Created by Luis on 09/05/2017.
 */

public class MessagingSystem {

    private static final String DOINGS_ROOT = "doings";
    private static final String USERS_ROOT = "users";
    private static final String LIKES_ROOT = "likes";
    private static final String COMMENTARIES_ROOT = "commentaries";
    private static final String USER_CODES_ROOT = "userCodes";
    private static final String FRIEND_CODES_ROOT = "friendCodes";
    private static final String FRIENDSHIPS_ROOT = "friendships";
    private static final String LOCALDATA_ROOT = "localData";
    private static final String NAMESTOSEARCH_ROOT ="namesToSearch";

    private static MessagingSystem sMessagingSystem;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;
    private MessagingUpdatesDatabase mUdpateTimeDatabase;
    private Context mContext;
    private Double mTimeOffset;
    private Boolean mListening;
    private Boolean mIsForeground;

    private EventBus mEventBus;

    private List<String> mListenedForUserInfo;

    public static synchronized MessagingSystem get(Context context) {
        if (sMessagingSystem == null) {
            sMessagingSystem = new MessagingSystem(context);
        }
        return sMessagingSystem;
    }

    private MessagingSystem(Context context) {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase.setPersistenceEnabled(true);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mContext = context;
        mIsForeground = true;
        mListening = false;
        logIn();
        mListenedForUserInfo = new ArrayList<>();
        mUdpateTimeDatabase = MessagingUpdatesDatabase.get(context);
        listenForLoginStatus();
        listenForTimeOffset();
        mEventBus = EventBus.getDefault();
    }

    private void listenForLoginStatus() {
        mFirebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                } else {
                    // User is signed out
                    logIn();
                }
            }
        });
    }

    private void listenForTimeOffset() {
        DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                mTimeOffset = snapshot.getValue(Double.class);
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });
    }

    public void goOnline() {
        mFirebaseDatabase.goOnline();
        mIsForeground = true;
    }

    public void goOffline() {
        mFirebaseDatabase.goOffline();
        mIsForeground = false;
    }

    public void goOnlineQuickly() {
        mFirebaseDatabase.goOnline();
    }

    public boolean isForeground() {
        return mIsForeground;
    }

    public void reset() {
        mFirebaseDatabase.goOffline();
        mFirebaseDatabase.goOnline();
    }

    public Date getEstimatedTime() {
        if (mTimeOffset != null) {
            double estimatedServerTimeMs = System.currentTimeMillis() + mTimeOffset;
            return new Date(new Double(estimatedServerTimeMs).longValue());
        } else {
            return new Date(System.currentTimeMillis());
        }
    }

    public void logIn() {
        DoingAccountManager doingAccountManager = DoingAccountManager.get(mContext);
        final String email = doingAccountManager.getOnlineId();
        final String password = doingAccountManager.getOnlineSecret();

        // if the user does not have an account yet, do nothing
        if (email == null) {
            return;
        }

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            // Could not sign in
                        }

                    }
                });
    }

    public void sendMyUser() {
        User myUser = UserDAO.get(mContext).getMyUser();

        // if the user already have a userCode do nothing
        if (myUser.getUserCode() != null) {
            return;
        }

        // if we already have a firebase user create the firebase database user
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            createMyUser(firebaseUser);
        }

        final String email;
        final String password;
        DoingAccountManager doingAccountManager = DoingAccountManager.get(mContext);

        email = doingAccountManager.getOrCreateOnlineId();
        password = doingAccountManager.getOrCreateOnlineSecret();

        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = task.getResult().getUser();
                            createMyUser(firebaseUser);
                            UpdateAndNotifyService.start(mContext);
                        }
                    }
                });
    }

    private void createMyUser(FirebaseUser firebaseUser) {
        User myUser = UserDAO.get(mContext).getMyUser();

        // if the user already have a userCode do nothing
        if (myUser.getUserCode() != null) {
            return;
        }

        DatabaseReference databaseReference = mFirebaseDatabase.getReference();
        String friendCode = null;
        Boolean friendCodeNotUsed = false;
        while (friendCodeNotUsed != true) {
            friendCode = RandomUtils.getBase58ReadableRandom(DoingSettings.FRIEND_CODE_SIZE);
            friendCodeNotUsed = !databaseReference.child(FRIEND_CODES_ROOT).child(friendCode).equals(true);
        }

        String userCode = null;
        userCode = firebaseUser.getUid().toString();

        // update user
        Long lastUpdate = getEstimatedTime().getTime();
        myUser.setUserCode(userCode);
        myUser.setFriendCode(friendCode);
        myUser.setLastUpdate(new Date(lastUpdate));

        // add userCode and friendCode to existing userCode and friendCode list
        databaseReference.child(FRIEND_CODES_ROOT).child(friendCode).setValue(true);
        databaseReference.child(USER_CODES_ROOT).child(friendCode).setValue(userCode);

        // add new user to firebase database
        UserMessage userMessage = new UserMessage(myUser.getUserCode(), myUser.getFriendName(),
                myUser.getFriendCode(), myUser.getCreationDate().getTime(), myUser.getLastUpdate().getTime());
        databaseReference.child(USERS_ROOT).child(userCode).setValue(userMessage);


        // update user in local database
        UserDAO.get(mContext).updateUser(myUser);

        // add listeners
        addListeners(myUser);

        // add user name to search
        addMyNameToSearch();

        // if the user have a custom avatar upload it
        StorageManager storageManager = StorageManager.get(mContext);
        Bitmap avatar = storageManager.loadAvatarImage(myUser);
        if (avatar != null) {
            storageManager.uploadAvatarImage(avatar, myUser);
            sendUserUpdateTime(myUser);
        }

        mEventBus.postSticky(new MyUserUpdatedEvent());
    }

    public void recoverMyUser(final String email, final String password) {

        mFirebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            // Could not sign in
                            mEventBus.postSticky(new AccountRecoveredEvent(false));
                        } else {
                            FirebaseUser firebaseUser = task.getResult().getUser();
                            if (firebaseUser != null) {
                                final String myUserCode = firebaseUser.getUid();

                                DoingAccountManager accountManager = DoingAccountManager.get(mContext);
                                accountManager.setOnlineId(email);
                                accountManager.setOnlineSecret(password);

                                DatabaseReference databaseReference = mFirebaseDatabase.getReference();
                                databaseReference.child(USERS_ROOT).child(myUserCode)
                                        .addListenerForSingleValueEvent(newRecoverMyUserEventListener());

                                UpdateAndNotifyService.start(mContext);
                                mEventBus.postSticky(new AccountRecoveredEvent(true));
                                mEventBus.postSticky(new MyUserCreatedEvent());
                            }
                        }

                    }
                });
    }

    public void addMyNameToSearch() {
        User myUser = UserDAO.get(mContext).getMyUser();

        if (myUser == null) return;

        if ((myUser.getFriendName() == null) || (myUser.getUserCode() == null) || (myUser.getFriendCode() == null))
            return;

        NameToSearchMessage nameToSearchMessage = new NameToSearchMessage(myUser.getFriendName(), myUser.getUserCode(), myUser.getFriendCode());
        String compactName = TextUtils.toNoAccentsNoSpacesLowerCase(myUser.getFriendName());

        DatabaseReference databaseReference = mFirebaseDatabase.getReference(NAMESTOSEARCH_ROOT);
        databaseReference.child(compactName).setValue(nameToSearchMessage);
    }

    public void sendDoing(Doing doing, List<User> receivers) {
        User myUser = UserDAO.get(mContext).getMyUser();
        DoingMessage doingMessage = getMessageFromDoing(doing, receivers);

        DatabaseReference databaseReference = mFirebaseDatabase.getReference(DOINGS_ROOT);
        for (User receiver : receivers) {
            if (!receiver.getUserCode().equals(myUser.getUserCode())) {
                databaseReference.child(receiver.getUserCode()).child(doingMessage.getTimestamp().toString()).setValue(doingMessage);
            }
        }
    }

    public void sendLike(Like like, List<User> receivers) {
        User myUser = UserDAO.get(mContext).getMyUser();
        LikeMessage likeMessage = getMessageFromLike(like);

        DatabaseReference databaseReference = mFirebaseDatabase.getReference(LIKES_ROOT);
        for (User receiver : receivers) {
            if (!receiver.getUserCode().equals(myUser.getUserCode())) {
                databaseReference.child(receiver.getUserCode()).child(likeMessage.getTimestamp().toString()).setValue(likeMessage);
            }
        }
    }

    public void sendCommentary(Commentary commentary, List<User> receivers) {
        User myUser = UserDAO.get(mContext).getMyUser();
        CommentaryMessage commentaryMessage = getMessageFromCommentary(commentary);

        DatabaseReference databaseReference = mFirebaseDatabase.getReference(COMMENTARIES_ROOT);
        for (User receiver : receivers) {
            if (!receiver.getUserCode().equals(myUser.getUserCode())) {
                databaseReference.child(receiver.getUserCode()).child(commentaryMessage.getTimestamp().toString()).setValue(commentaryMessage);
            }
        }
    }

    public void sendUserUpdateTime(User user) {
        if (user.getUserCode() == null) {
            return;
        }

        DatabaseReference databaseReferenceUser = mFirebaseDatabase.getReference(USERS_ROOT);
        databaseReferenceUser.child(user.getUserCode()).child("lastUpdate").setValue(user.getLastUpdate().getTime());
    }

    public void sendFriendship(User user, User friend) {
        String userCode = user.getUserCode();
        String friendUserCode = friend.getUserCode();
        String friendStatus = friend.getState().toString();
        DatabaseReference databaseReference = mFirebaseDatabase.getReference(FRIENDSHIPS_ROOT);
        databaseReference.child(friendUserCode).child(userCode).setValue(friendStatus);
    }

    public void activateFriendship(User user, User friend) {
        String userCode = user.getUserCode();
        String friendUserCode = friend.getUserCode();
        String friendStatus = UserState.ACTIVE.toString();
        DatabaseReference databaseReference = mFirebaseDatabase.getReference(FRIENDSHIPS_ROOT);
        databaseReference.child(friendUserCode).child(userCode).setValue(friendStatus);
        databaseReference.child(userCode).child(friendUserCode).setValue(friendStatus);
    }

    private void listenForUserInfo(final User user) {

        //Check that this user is not already being listened
        if (isBeingListenedForUserInfo(user)) {
            return;
        }

        DatabaseReference databaseReference = mFirebaseDatabase.getReference(USERS_ROOT);
        databaseReference.child(user.getUserCode()).child("lastUpdate").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Long lastUpdate = dataSnapshot.getValue(Long.class);
                if (lastUpdate == null) {
                    return;
                }
                user.setLastUpdate(new Date(lastUpdate));
                UserDAO.get(mContext).updateUser(user);
                Date estimatedTime = getEstimatedTime();
                setUpdateTime(user, UpdateType.USER, estimatedTime.getTime());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void addListeners(User user) {

        if (user.getUserCode() == null) {
            return;
        }
        if (mListening) {
            return;
        }

        List<User> friends = UserDAO.get(mContext).getAllOtherUsers();

        for (User friend : friends) {
            addListenersToUser(friend);
        }

        listenForFriendshipStatus(user);
        listenForDoings(user);
        listenForLikes(user);
        listenForCommentaries(user);

        mListening = true;
    }

    public void addListenersToUser(User friend) {
        listenForUserInfo(friend);
    }

    private void listenForFriendshipStatus(final User user) {

        final String userCode = user.getUserCode();

        DatabaseReference databaseReference = mFirebaseDatabase.getReference(FRIENDSHIPS_ROOT);
        databaseReference.child(userCode).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String friendUserCode = dataSnapshot.getKey();
                String friendShipStatusString = dataSnapshot.getValue(String.class);
                UserState friendShipStatus = UserState.valueOf(friendShipStatusString);

                if (friendUserCode == null) {
                    return;
                }

                User friend = UserDAO.get(mContext).getUserWithCode(friendUserCode);

                // if we have already added this user as a friend then friendship is active
                // else we add the user as a pending or active friend
                if (friend.isInvitedByMe()) {
                    friend.activate();
                    UserDAO.get(mContext).updateUser(friend);
                    addListenersToUser(friend);
                    activateFriendship(user, friend);
                    mEventBus.postSticky(new UserUpdatedEvent(friend));
                    return;
                } else if (friend.isUnknown()) {
                    User unknownFriend = UserFactory.get(mContext).getUser(null);
                    unknownFriend.setUserCode(friendUserCode);
                    if (friendShipStatus == UserState.ACTIVE) {
                        unknownFriend.activate();
                    } else {
                        unknownFriend.setAsInvitingMe();
                        int lastCount = DoingSettings.getLastInvitationNotificationCount(mContext);
                        DoingSettings.setLastInvitationNotificationCount(mContext, lastCount + 1);
                    }
                    listenOnceForFriend(unknownFriend);

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String friendUserCode = dataSnapshot.getKey();

                if (friendUserCode == null) {
                    return;
                }

                User friend = UserDAO.get(mContext).getUserWithCode(friendUserCode);
                String userStateString = dataSnapshot.getValue(String.class);

                if (userStateString == null) {
                    return;
                }

                UserState userState = UserState.valueOf(userStateString);

                if (userState == null) {
                    return;
                }

                if (userState == UserState.ACTIVE) {
                    friend.activate();
                    UserDAO.get(mContext).updateUser(friend);
                    addListenersToUser(friend);
                    mEventBus.postSticky(new UserUpdatedEvent(friend));
                    return;
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void listenForDoings(final User user) {
        final String userCode = user.getUserCode();

        DatabaseReference databaseReference = mFirebaseDatabase.getReference(DOINGS_ROOT).child(userCode);
        databaseReference.keepSynced(true);
        databaseReference.orderByKey().addChildEventListener(newDoingChildEventListener());
    }

    private void listenForLikes(final User user) {
        String userCode = user.getUserCode();

        DatabaseReference databaseReference = mFirebaseDatabase.getReference(LIKES_ROOT).child(userCode);
        databaseReference.orderByKey().addChildEventListener(newLikeChildEventListener());
    }

    private void listenForCommentaries(final User user) {
        String userCode = user.getUserCode();

        DatabaseReference databaseReference = mFirebaseDatabase.getReference(COMMENTARIES_ROOT).child(userCode);
        databaseReference.orderByKey().addChildEventListener(newCommentaryChildEventListener());

    }

    public void listenOnceCheckFriend(final String name) {
        String compactName = TextUtils.toNoAccentsNoSpacesLowerCase(name);

        final DatabaseReference databaseReference = mFirebaseDatabase.getReference();
        databaseReference.child(NAMESTOSEARCH_ROOT).child(compactName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    mEventBus.postSticky(new FriendCheckedEvent(true, false, null, null, name));
                    return;
                }

                final NameToSearchMessage foundFriend = dataSnapshot.getValue(NameToSearchMessage.class);
                mEventBus.postSticky(new FriendCheckedEvent(true, true, foundFriend.getFriendCode(), foundFriend.getUserCode(), foundFriend.getUserName()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mEventBus.postSticky(new FriendCheckedEvent(false, false, null, null, name));
            }
        });
    }

    public void listenOnceCheckName(final String name) {
        String compactName = TextUtils.toNoAccentsNoSpacesLowerCase(name);

        DatabaseReference databaseReference = mFirebaseDatabase.getReference(NAMESTOSEARCH_ROOT);
        databaseReference.child(compactName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mEventBus.postSticky(new UserNameExistsEvent(name, true, true));
                } else {
                    mEventBus.postSticky(new UserNameExistsEvent(name, true, false));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mEventBus.postSticky(new UserNameExistsEvent(name, false, false));
            }
        });
    }

    private void listenOnceForFriend(final User friend) {
        final String userCode = friend.getUserCode();
        if (userCode == null) {
            return;
        }

        DatabaseReference databaseReference = mFirebaseDatabase.getReference(USERS_ROOT);
        databaseReference.child(userCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String friendCode = dataSnapshot.child("friendCode").getValue(String.class);
                if (friendCode == null) {
                    return;
                }

                String friendName = dataSnapshot.child("friendName").getValue(String.class);
                if (friendName == null) {
                    return;
                }

                friend.setFriendCode(friendCode);
                friend.setFriendName(friendName);
                UserDAO.get(mContext).addUser(friend);
                addListenersToUser(friend);
                mEventBus.postSticky(new UserUpdatedEvent(friend));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private DoingMessage getMessageFromDoing(Doing doing, List<User> receivers) {
        String doingIdString = doing.getId().toString();
        String userCode = doing.getUser().getUserCode();
        String actionString = doing.getAction().toString();
        String text = doing.getText();

        List<DoingReceiverMessage> doingReceivers = new ArrayList<>();

        for (User receiver : receivers) {
            DoingReceiverMessage doingReceiver = getMessageFromReceiver(receiver);
            doingReceivers.add(doingReceiver);
        }

        Long timestamp = getEstimatedTime().getTime();
        DoingMessage doingMessage = new DoingMessage(doingIdString, userCode, actionString, text, timestamp, doingReceivers);

        return doingMessage;
    }

    private DoingReceiverMessage getMessageFromReceiver(User receiver) {
        return new DoingReceiverMessage(receiver.getUserCode(), receiver.getFriendName(), receiver.getFriendCode());
    }

    private LikeMessage getMessageFromLike(Like like) {
        String likeIdString = like.getId().toString();
        String doingIdString = like.getDoing().getId().toString();
        String senderUserCode = like.getSender().getUserCode();
        String senderFriendName = like.getSender().getFriendName();
        String senderFriendCode = like.getSender().getFriendCode();
        String receiverUserCode = like.getDoing().getUser().getUserCode();
        String likeTypeString = like.getType().toString();

        Long timestamp = getEstimatedTime().getTime();
        LikeMessage likeMessage = new LikeMessage(likeIdString, doingIdString, senderUserCode, senderFriendName,
                senderFriendCode, receiverUserCode, likeTypeString, timestamp);

        return likeMessage;
    }

    private CommentaryMessage getMessageFromCommentary(Commentary commentary) {
        String commentaryIdString = commentary.getId().toString();
        String doingIdString = commentary.getDoing().getId().toString();
        String senderUserCode = commentary.getSender().getUserCode();
        String senderFriendName = commentary.getSender().getFriendName();
        String senderFriendCode = commentary.getSender().getFriendCode();
        String receiverUserCode = commentary.getDoing().getUser().getUserCode();
        String text = commentary.getText();

        Long timestamp = getEstimatedTime().getTime();
        CommentaryMessage commentaryMessage = new CommentaryMessage(commentaryIdString, doingIdString,
                senderUserCode, senderFriendName, senderFriendCode, receiverUserCode, text, timestamp);

        return commentaryMessage;
    }

    private Doing getDoingFromMessage(DoingMessage doingMessage) {
        UUID doingId = UUID.fromString(doingMessage.getId());
        Doing.DoingAction doingAction = Doing.DoingAction.valueOf(doingMessage.getAction());
        String userCode = doingMessage.getUser();
        User user = UserDAO.get(mContext).getUserWithCode(userCode);
        Date date = new Date(doingMessage.getTimestamp());

        Doing doing = DoingFactory.get(mContext).getDoing(doingId, user, doingAction, doingMessage.getText(), date);
        return doing;
    }

    private List<User> getReceiversFromMessage(DoingMessage doingMessage) {
        List<User> receivers = new ArrayList<>();
        List<DoingReceiverMessage> doingReceiverMessages = doingMessage.getReceivers();

        for (DoingReceiverMessage doingReceiverMessage : doingReceiverMessages) {
            User receiver = getReceiverFromMessage(doingReceiverMessage);
            receivers.add(receiver);
        }

        return receivers;
    }

    private User getReceiverFromMessage(DoingReceiverMessage doingReceiverMessage) {
        User receiver = UserFactory.get(mContext).getUser(null, User.UserState.UNKNOWN);
        receiver.setUserCode(doingReceiverMessage.getUserCode());
        receiver.setFriendCode(doingReceiverMessage.getFriendCode());
        receiver.setFriendName(doingReceiverMessage.getFriendName());

        return receiver;
    }

    private Like getLikeFromMessage(LikeMessage likeMessage) {
        UUID likeId = UUID.fromString(likeMessage.getId());
        Like.LikeType likeType = Like.LikeType.valueOf(likeMessage.getType());
        User sender = UserDAO.get(mContext).getUserWithCode(likeMessage.getSender());
        if (sender.getFriendName() == null) {
            sender.setFriendName(likeMessage.getSenderFriendName());
        }
        if (sender.getFriendCode() == null) {
            sender.setFriendCode(likeMessage.getSenderFriendCode());
        }
        UUID doingId = UUID.fromString(likeMessage.getDoing());
        Doing doing = DoingDAO.get(mContext).getDoing(doingId);
        if (doing == null) {
            doing = DoingFactory.get(mContext).getDoing();
            doing.setId(doingId);
        }
        Date date = new Date(likeMessage.getTimestamp());

        Like like = LikeFactory.get(mContext).getLike(likeId, sender, doing, likeType, date);
        return like;
    }

    private Commentary getCommentaryFromMessage(CommentaryMessage commentaryMessage) {
        UUID commentaryId = UUID.fromString(commentaryMessage.getId());
        User sender = UserDAO.get(mContext).getUserWithCode(commentaryMessage.getSender());
        if (sender.getFriendName() == null) {
            sender.setFriendName(commentaryMessage.getSenderFriendName());
        }
        if (sender.getFriendCode() == null) {
            sender.setFriendCode(commentaryMessage.getSenderFriendCode());
        }
        UUID doingId = UUID.fromString(commentaryMessage.getDoing());
        Doing doing = DoingDAO.get(mContext).getDoing(doingId);
        if (doing == null) {
            doing = DoingFactory.get(mContext).getDoing();
            doing.setId(doingId);
        }
        String text = commentaryMessage.getText();
        Date date = new Date(commentaryMessage.getTimestamp());

        Commentary commentary = CommentaryFactory.get(mContext).getCommentary(commentaryId, sender, doing, text, date);
        return commentary;
    }

    public void setUpdateTime(User user, UpdateType type, Long updateTime) {
        String id = user.getUserCode() + type.toString();
        MessagingUpdate messagingUpdate = new MessagingUpdate(id, type, updateTime);
        if (mUdpateTimeDatabase.get(messagingUpdate.getId()) == null) {
            mUdpateTimeDatabase.add(messagingUpdate);
        } else {
            mUdpateTimeDatabase.update(messagingUpdate);
        }
    }

    public Long getUpdateTime(User user, UpdateType type) {
        String id = user.getUserCode() + type.toString();
        MessagingUpdate messagingUpdate = mUdpateTimeDatabase.get(id);

        if (messagingUpdate == null) {
            return Long.valueOf(0);
        }

        if (messagingUpdate.getLastUpdate() == null) {
            return Long.valueOf(0);
        }

        return messagingUpdate.getLastUpdate();
    }

    private Boolean isBeingListenedForUserInfo(User user) {
        String userCode = user.getUserCode();

        if (userCode == null) {
            return true;
        }

        for (int i = 0; i < mListenedForUserInfo.size(); i++) {
            String listenedUserCode = mListenedForUserInfo.get(i);
            if (userCode.equals(listenedUserCode)) {
                return true;
            }
        }

        return false;
    }

    public void setSynced(User user, boolean sync) {
        String userCode = user.getUserCode();

        DatabaseReference databaseReference = mFirebaseDatabase.getReference(DOINGS_ROOT).child(userCode);
        databaseReference.keepSynced(sync);

        databaseReference = mFirebaseDatabase.getReference(LIKES_ROOT).child(userCode);
        databaseReference.keepSynced(true);

        databaseReference = mFirebaseDatabase.getReference(COMMENTARIES_ROOT).child(userCode);
        databaseReference.keepSynced(true);
    }

    public void listenOnceForDoingsReceived(final User user) {
        String userCode = user.getUserCode();

        DatabaseReference databaseReference = mFirebaseDatabase.getReference(DOINGS_ROOT).child(userCode);
        databaseReference.keepSynced(true);
        databaseReference.orderByKey().addListenerForSingleValueEvent(newDoingsValueEventListener());
    }

    public void listenOnceForLikesReceived(final User user) {
        String userCode = user.getUserCode();

        DatabaseReference databaseReference = mFirebaseDatabase.getReference(LIKES_ROOT).child(userCode);
        databaseReference.keepSynced(true);
        databaseReference.orderByKey().addListenerForSingleValueEvent(newLikesValueEventListener());
    }

    public void listenOnceForCommentariesReceived(final User user) {
        String userCode = user.getUserCode();

        DatabaseReference databaseReference = mFirebaseDatabase.getReference(COMMENTARIES_ROOT).child(userCode);
        databaseReference.keepSynced(true);
        databaseReference.orderByKey().addListenerForSingleValueEvent(newComentariesValueEventListener());
    }

    private ChildEventListener newDoingChildEventListener() {
        ChildEventListener doingChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if (isForeground()) {
                    downloadDoing(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        return doingChildEventListener;
    }

    private ChildEventListener newLikeChildEventListener() {
        ChildEventListener likeChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final DataSnapshot thisSnapshot = dataSnapshot;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isForeground()) {
                            downloadLike(thisSnapshot);
                        }
                    }
                });
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        return likeChildEventListener;
    }

    private ChildEventListener newCommentaryChildEventListener() {
        ChildEventListener commentaryChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final DataSnapshot thisSnapshot = dataSnapshot;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (isForeground()) {
                            downloadCommentary(thisSnapshot);
                        }
                    }
                });
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        return commentaryChildEventListener;
    }

    private ValueEventListener newDoingsValueEventListener() {
        ValueEventListener doingValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final DataSnapshot thisSnapshot = dataSnapshot;
                downloadDoings(thisSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        return doingValueEventListener;
    }

    private ValueEventListener newLikesValueEventListener() {
        ValueEventListener likeValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final DataSnapshot thisSnapshot = dataSnapshot;
                downloadLikes(thisSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        return likeValueEventListener;
    }

    private ValueEventListener newComentariesValueEventListener() {
        ValueEventListener comentaryValueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final DataSnapshot thisSnapshot = dataSnapshot;
                downloadCommentaries(thisSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        return comentaryValueEventListener;
    }

    private void downloadDoing(DataSnapshot dataSnapshot) {
        final DoingMessage doingMessage = dataSnapshot.getValue(DoingMessage.class);
        if (doingMessage == null) {
            return;
        }

        Doing doing = getDoingFromMessage(doingMessage);
        List<User> receivers = getReceiversFromMessage(doingMessage);

        boolean saved = saveDoing(doing, receivers);
        if (saved) {
            dataSnapshot.getRef().removeValue();
        }
    }

    private void downloadLike(DataSnapshot dataSnapshot) {
        final LikeMessage likeMessage = dataSnapshot.getValue(LikeMessage.class);
        if (likeMessage == null) {
            return;
        }

        Like like = getLikeFromMessage(likeMessage);
        boolean saved = saveLike(like);
        if (saved) {
            dataSnapshot.getRef().removeValue();
        }
    }

    private void downloadCommentary(DataSnapshot dataSnapshot) {
        final CommentaryMessage commentaryMessage = dataSnapshot.getValue(CommentaryMessage.class);
        if (commentaryMessage == null) {
            return;
        }

        Commentary commentary = getCommentaryFromMessage(commentaryMessage);
        boolean saved = saveCommentary(commentary);
        if (saved) {
            dataSnapshot.getRef().removeValue();
        }
    }

    private void downloadDoings(DataSnapshot dataSnapshot) {
        for (DataSnapshot thisSnapshot : dataSnapshot.getChildren()) {
            final DoingMessage doingMessage = thisSnapshot.getValue(DoingMessage.class);
            if (doingMessage == null) {
                return;
            }

            Doing doing = getDoingFromMessage(doingMessage);
            List<User> receivers = getReceiversFromMessage(doingMessage);

            boolean saved = saveDoing(doing, receivers);
            if (saved) {
                dataSnapshot.getRef().removeValue();
            }
        }
    }

    private void downloadLikes(DataSnapshot dataSnapshot) {
        for (DataSnapshot thisSnapshot : dataSnapshot.getChildren()) {
            final LikeMessage likeMessage = thisSnapshot.getValue(LikeMessage.class);
            if (likeMessage == null) {
                return;
            }

            Like like = getLikeFromMessage(likeMessage);

            boolean saved = saveLike(like);
            if (saved) {
                dataSnapshot.getRef().removeValue();
            }
        }
    }

    private void downloadCommentaries(DataSnapshot dataSnapshot) {
        Map<String, Object> childrenToRemove = new HashMap<>();

        for (DataSnapshot thisSnapshot : dataSnapshot.getChildren()) {
            final CommentaryMessage commentaryMessage = thisSnapshot.getValue(CommentaryMessage.class);
            if (commentaryMessage == null) {
                return;
            }

            Commentary commentary = getCommentaryFromMessage(commentaryMessage);

            boolean saved = saveCommentary(commentary);
            if (saved) {
                childrenToRemove.put(thisSnapshot.getKey(), null);
            }
        }

        dataSnapshot.getRef().updateChildren(childrenToRemove);
    }

    private Boolean saveDoing(Doing doing, List<User> receivers) {
        if ((DoingDAO.get(mContext).exists(doing)) || (doing.getUser().isInactive())) {
            return true;
        }

        DoingDAO.get(mContext).addDoing(doing);
        DoingDAO.get(mContext).addReceivers(doing, receivers);
        DoingDAO.get(mContext).updateDoingLikesCount(doing.getId());
        DoingDAO.get(mContext).updateDoingCommentariesCount(doing.getId());

        int lastCount = DoingSettings.getLastDoingNotificationCount(mContext);
        DoingSettings.setLastDoingNotificationCount(mContext, lastCount + 1);
        mEventBus.postSticky(new DoingReceivedEvent(doing));

        if (DoingDAO.get(mContext).exists(doing)) {
            return true;
        } else {
            return false;
        }
    }

    private Boolean saveLike(Like like) {
        if (LikeDAO.get(mContext).exists(like)) {
            return true;
        }

        // if we receive a like before his doing we only save it to the database
        if (!DoingDAO.get(mContext).exists(like.getDoing())) {
            LikeDAO.get(mContext).addLike(like);
        }
        // else we also update the doing
        else {
            Boolean isANewLike = like.getDoing().addLike(like);
            if (!isANewLike) {
                return true;
            }
            Doing doing = like.getDoing();
            LikeDAO.get(mContext).addLike(like);
            DoingDAO.get(mContext).updateDoingLikesCount(doing.getId());
        }

        // Only notify likes to me
        if (UserDAO.get(mContext).isMe(like.getDoing().getUser())) {
            int lastCount = DoingSettings.getLastLikeNotificationCount(mContext);
            DoingSettings.setLastLikeNotificationCount(mContext, lastCount + 1);
        }

        if (LikeDAO.get(mContext).exists(like)) {
            return true;
        } else {
            return false;
        }
    }

    private Boolean saveCommentary(Commentary commentary) {
        if (CommentaryDAO.get(mContext).exists(commentary)) {
            return true;
        }

        // if we receive the commentary before the doing we only save it to the database
        if (!DoingDAO.get(mContext).exists(commentary.getDoing())) {
            CommentaryDAO.get(mContext).addCommentary(commentary);
            // else we also update the doing
        } else {
            Doing doing = commentary.getDoing();
            CommentaryDAO.get(mContext).addCommentary(commentary);
            DoingDAO.get(mContext).updateDoingCommentariesCount(doing.getId());

            // Only notify commentaries to my doings o to doings i have already commented
            if ((UserDAO.get(mContext).isMe(commentary.getDoing().getUser())) ||
                    CommentaryDAO.get(mContext).hasACommentary(UserDAO.get(mContext).getMyUser(), commentary.getDoing())) {
                int lastCount = DoingSettings.getLastCommentaryNotificationCount(mContext);
                DoingSettings.setLastCommentaryNotificationCount(mContext, lastCount + 1);
            }
        }

        if (CommentaryDAO.get(mContext).exists(commentary)) {
            return true;
        } else {
            return false;
        }
    }

    public void backupLocalData(User user) {
        String userCode = user.getUserCode();

        if (userCode == null) {return;}

        DatabaseReference databaseReference = mFirebaseDatabase.getReference(LOCALDATA_ROOT);

        List<User> friends = UserDAO.get(mContext).getAllOtherUsers();
        for (User friend : friends) {
            databaseReference.child(userCode).child("friends").child(friend.getUserCode()).child("name").setValue(friend.getName());
            databaseReference.child(userCode).child("friends").child(friend.getUserCode()).child("state").setValue(friend.getState());
        }
    }

    private ValueEventListener newRecoverMyUserEventListener() {
        ValueEventListener recoverMyUserEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserMessage userMessage = dataSnapshot.getValue(UserMessage.class);

                User myUser = UserDAO.get(mContext).getMyUser();
                myUser.setUserCode(userMessage.getUserCode());
                myUser.setFriendName(userMessage.getFriendName());
                myUser.setFriendCode(userMessage.getFriendCode());
                Long lastUpdate = getEstimatedTime().getTime();
                myUser.setLastUpdate(new Date(lastUpdate));

                // Update user in local datase
                UserDAO.get(mContext).updateUser(myUser);

                // add listeners
                addListeners(myUser);

                // Recover my user Avatar
                StorageManager storageManager = StorageManager.get(mContext);
                storageManager.downloadAvatarFromUser(myUser);

                // recoverMyFriends
                recoverMyFriends(myUser);

                mEventBus.postSticky(new MyUserUpdatedEvent());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        return recoverMyUserEventListener;
    }

    private void recoverMyFriends(User myUser) {
        String userCode = myUser.getUserCode();
        DatabaseReference databaseReference = mFirebaseDatabase.getReference(FRIENDSHIPS_ROOT).child(userCode);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot thisSnapshot : dataSnapshot.getChildren()) {
                    String friendCode = thisSnapshot.getKey();
                    String friendStatusString = thisSnapshot.getValue(String.class);
                    UserState friendStatus;
                    if (friendStatusString.equals("ACTIVE")) {
                        friendStatus = UserState.ACTIVE;
                    } else {
                        friendStatus = UserState.INVITING_ME;
                    }

                    DatabaseReference databaseReference = mFirebaseDatabase.getReference(USERS_ROOT);
                    databaseReference.child(friendCode).addListenerForSingleValueEvent(newRecoverMyFriendEventListener(friendStatus));

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private ValueEventListener newRecoverMyFriendEventListener(final User.UserState userState) {
        ValueEventListener recoverMyFriendEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserMessage userMessage = dataSnapshot.getValue(UserMessage.class);

                if (userMessage == null) return;

                User myFriend = UserFactory.get(mContext).getUser();
                myFriend.setId(UUID.randomUUID());
                myFriend.setUserCode(userMessage.getUserCode());
                myFriend.setFriendName(userMessage.getFriendName());
                myFriend.setFriendCode(userMessage.getFriendCode());
                myFriend.setCreationDate(new Date(userMessage.getCreationDate()));
                myFriend.setLastUpdate(new Date(userMessage.getLastUpdate()));
                myFriend.setState(userState);

                // Add user in local database
                UserDAO.get(mContext).addUser(myFriend);

                // Add listener to friend
                addListenersToUser(myFriend);

                mEventBus.postSticky(new UserUpdatedEvent(myFriend));

                // Recover friend local data
                User myUser = UserDAO.get(mContext).getMyUser();
                DatabaseReference databaseReference = mFirebaseDatabase.getReference(LOCALDATA_ROOT);
                databaseReference.child(myUser.getUserCode())
                        .child("friends")
                        .child(myFriend.getUserCode())
                        .addListenerForSingleValueEvent(newRecoverMyFriendLocalDataEventListener());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        return recoverMyFriendEventListener;
    }

    private ValueEventListener newRecoverMyFriendLocalDataEventListener() {
        ValueEventListener recoverMyFriendLocalDataEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String friendUserCode = dataSnapshot.getKey();
                if (friendUserCode == null) {return;}

                User friend = UserDAO.get(mContext).getUserWithCode(friendUserCode);
                if (friend.isUnknown()) { return;}

                String name = dataSnapshot.child("name").getValue(String.class);
                if (name != null) {
                    friend.setName(name);
                }

                String state = dataSnapshot.child("state").getValue(String.class);
                if (state != null) {
                    switch (state) {
                        case "ACTIVE":
                            friend.activate();
                            break;
                        case "INACTIVE":
                            friend.deactivate();
                            break;
                        case "IGNORED_BY_ME":
                            if (!friend.isActive()) {
                                friend.setAsIgnored();
                            }
                            break;
                    }
                }

                UserDAO.get(mContext).updateUser(friend);
                mEventBus.postSticky(new UserUpdatedEvent(friend));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        return recoverMyFriendLocalDataEventListener;
    }

    public void searchForOnlineUsers(String str, final int limit){
        if (str.isEmpty()) {
            mEventBus.postSticky(new FriendSearchEvent(new ArrayList<User>(), limit));
            return;
        }

        final String compactStr = TextUtils.toNoAccentsNoSpacesLowerCase(str);
        DatabaseReference databaseReference = mFirebaseDatabase.getReference(NAMESTOSEARCH_ROOT);
        Query query = databaseReference.orderByKey()
                .startAt(compactStr)
                .limitToFirst(limit);

        query.keepSynced(true);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> usersFound = new ArrayList<User>();
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
                    String compactStrFound = postSnapshot.getKey();
                    if (!compactStrFound.startsWith(compactStr)) continue;

                    NameToSearchMessage userFound = postSnapshot.getValue(NameToSearchMessage.class);
                    User user = UserFactory.get(mContext).getUser(userFound.getUserName());
                    user.setFriendName(userFound.getUserName());
                    user.setUserCode(userFound.getUserCode());
                    user.setFriendCode(userFound.getFriendCode());
                    usersFound.add(user);
                }

                mEventBus.postSticky(new FriendSearchEvent(usersFound, limit));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
