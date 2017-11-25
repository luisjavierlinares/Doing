package com.luisjavierlinares.android.doing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;

import com.luisjavierlinares.android.doing.model.Commentary;
import com.luisjavierlinares.android.doing.model.Doing;
import com.luisjavierlinares.android.doing.controller.DoingController;
import com.luisjavierlinares.android.doing.model.User;
import com.luisjavierlinares.android.doing.model.User.UserState;
import com.luisjavierlinares.android.doing.time.AgoDate;
import com.luisjavierlinares.android.doing.time.AgoDate.DateScope;

import java.util.Calendar;

import static com.luisjavierlinares.android.doing.model.Doing.DoingAction;
import static com.luisjavierlinares.android.doing.model.Doing.DoingAction.LISTENING;
import static com.luisjavierlinares.android.doing.model.Doing.DoingAction.PLAYING;
import static com.luisjavierlinares.android.doing.model.Doing.DoingAction.READING;
import static com.luisjavierlinares.android.doing.model.Doing.DoingAction.WATCHING;
import static com.luisjavierlinares.android.doing.model.User.UserState.FRIEND_OF_A_FRIEND;
import static com.luisjavierlinares.android.doing.model.User.UserState.ME;
import static com.luisjavierlinares.android.doing.model.User.UserState.UNKNOWN;
import static com.luisjavierlinares.android.doing.time.AgoDate.DateScope.DAYS;
import static com.luisjavierlinares.android.doing.time.AgoDate.DateScope.HOURS;
import static com.luisjavierlinares.android.doing.time.AgoDate.DateScope.MINUTES;
import static com.luisjavierlinares.android.doing.time.AgoDate.DateScope.MONTHS;
import static com.luisjavierlinares.android.doing.time.AgoDate.DateScope.SECONDS;
import static com.luisjavierlinares.android.doing.time.AgoDate.DateScope.YEARS;
import static com.luisjavierlinares.android.doing.time.AgoDate.getAgoDate;

/**
 * Created by Luis on 11/04/2017.
 */

public class DoingUiUtils {

    final static int[] mColorIntArray = {R.color.colorAccent, R.color.colorAccent, R.color.colorAccent};
    final static int[] mIconIntArray = {R.drawable.ic_add_doing_white, R.drawable.ic_add_doing_white, R.drawable.ic_add_friend_white};


    public static String getDoingUserNameString(Context context, Doing doing) {
        return getUserNameString(context, doing.getUser());
    }

    public static String getUserNameMeString(Context context, User user) {
        if (user.isMe()) {
            return context.getString(R.string.me);
        }

        return getUserNameString(context, user);
    }

    public static String getUserNameString(Context context, User user) {
        if (user.isMe()) {
            return context.getString(R.string.I);
        }

        String name = user.getName();
        if ((name == null) || (user.isAFriendOfAFriend())) {
            name = getUserUnkownName(context, user);
        }

        return name;
    }

    public static String getUserUnkownName(Context context, User user){
        String name = user.getFriendName();
        if (name == null) {
            name = context.getString(R.string.unknown_user_name);
        }

//        String friendCode = user.getFriendCode();
//        if (friendCode != null) {
//            name = name.concat(" [").concat(friendCode).concat("]");
//        }
        return name;
    }

    public static String getFriendCodeString(Context context, User user) {
        String friendCode = user.getFriendCode();
        if (friendCode == null) {
            friendCode = context.getString(R.string.no_friend_code);
        }
        return friendCode;
    }

    public static String getDoingVerbString(Context context, Doing doing) {
        String verbText = context.getString(R.string.was);

        AgoDate agoDate = getAgoDate(doing.getDate(), Calendar.getInstance().getTime());

        if (agoDate.isShorterThan(new AgoDate(3, HOURS))) {
            DoingController doingController = DoingController.get(context);
            Doing lastDoing = doingController.getLastDoing(doing.getUser());
            if (doing.getId().equals(lastDoing.getId())) {
                if (doing.getUser().getId().equals(DoingSettings.getMyId(context))) {
                    verbText = context.getString(R.string.am);
                } else {
                    verbText = context.getString(R.string.is);
                }
            }
        }

        return verbText;
    }

    public static String getDoingHistoryVerbString(Context context, Doing doing) {
        String verbText = context.getString(R.string.was);
        return verbText;
    }

    public static Drawable getDoingActionDrawable(Context context, Doing doing) {
        DoingAction action = doing.getAction();

        if (action == PLAYING) {
            return ContextCompat.getDrawable(context, R.drawable.action_playing);
        } else if (action == WATCHING) {
            return ContextCompat.getDrawable(context, R.drawable.action_watching);
        } else if (action == LISTENING) {
            return ContextCompat.getDrawable(context, R.drawable.action_listening);
        } else if (action == READING) {
            return ContextCompat.getDrawable(context, R.drawable.action_reading);
        } else {
            return ContextCompat.getDrawable(context, R.drawable.action_enjoying);
        }
    }

    public static int getDoingActionColor(Context context, Doing doing) {
        DoingAction action = doing.getAction();

        if (action == PLAYING) {
            return ContextCompat.getColor(context, R.color.colorPlaying);
        } else if (action == WATCHING) {
            return ContextCompat.getColor(context, R.color.colorWatching);
        } else if (action == LISTENING) {
            return ContextCompat.getColor(context, R.color.colorListening);
        } else if (action == READING) {
            return ContextCompat.getColor(context, R.color.colorReading);
        } else {
            return ContextCompat.getColor(context, R.color.colorEnjoying);
        }
    }

    public static String getDoingAgoDateString(Context context, Doing doing) {
        AgoDate agoDate = getAgoDate(doing.getDate(), Calendar.getInstance().getTime());
        return getAgoDateString(context, agoDate);
    }

    public static String getCommentaryAgoDateString(Context context, Commentary commentary) {
        AgoDate agoDate = getAgoDate(commentary.getDate(), Calendar.getInstance().getTime());
        return getAgoDateString(context, agoDate);
    }

    public static String getAgoDateString(Context context, AgoDate agoDate) {
        int value = agoDate.getValue();
        DateScope scope = agoDate.getScope();
        if (scope == YEARS) {
            return (value + " " + ((value == 1) ? context.getString(R.string.year) : context.getString(R.string.years)));
        } else if (scope == MONTHS) {
            return (value + " " + ((value == 1) ? context.getString(R.string.month) : context.getString(R.string.months)));
        } else if (scope == DAYS) {
            return (value + " " + ((value == 1) ? context.getString(R.string.day) : context.getString(R.string.days)));
        } else if (scope == HOURS) {
            return (value + " " + ((value == 1) ? context.getString(R.string.hour) : context.getString(R.string.hours)));
        } else if (scope == MINUTES) {
//            return (value + " " + ((value == 1) ? context.getString(R.string.minute) : context.getString(R.string.minutes)));
            return (context.getString(R.string.less_than) + " " + "1" + " " + context.getString(R.string.hour));
        } else if (scope == SECONDS) {
//            return (value + " " + ((value == 1) ? context.getString(R.string.second) : context.getString(R.string.seconds)));
            return (context.getString(R.string.less_than) + " " + "1" + " " + context.getString(R.string.hour));
        } else {
            return (value + " " + context.getString(R.string.time));
        }
    }

    public static String getLikesCountKString(Context context, Doing doing) {
        return getKCount(doing.getLikesCount());
    }

    public static String getCommentariesCountKString(Context context, Doing doing) {
        return getKCount(doing.getCommentariesCount());
    }

    public static int getLikesCountStyle(Context context, Doing doing) {
        if ((!doing.hasNewLikes()) || (doing.getLikesCount() == 0)) {
            return Typeface.NORMAL;
        } else {
            return Typeface.BOLD;
        }
    }

    public static int getCommentariesCountStyle(Context context, Doing doing) {
        if ((!doing.hasNewCommentaries()) || (doing.getCommentariesCount() == 0)) {
            return Typeface.NORMAL;
        } else {
            return Typeface.BOLD;
        }
    }

    public static String getKCount(Integer count) {
        Integer countInt = count;

        if (countInt < 1000) {
            return countInt.toString();
        }

        if (countInt > 9999) {
            return "+9k";
        }

        Integer countQuot = countInt / 1000;
        Integer countRem = countInt % 1000;
        if (countRem == 0) {
            return countQuot.toString().concat("k");
        } else {
            return "+".toString().concat(countQuot.toString().concat("k"));
        }

    }

    public static String getFriendshipString(Context context, User user) {
        String friendshipText = new String();

        switch (user.getState()) {
            case ACTIVE:
                String friendName = getUserNameString(context, user);
                String iniText = context.getString(R.string.friendship_active_text_ini);
                String endText = context.getString(R.string.friendship_active_text_end);
                friendshipText = iniText.concat(friendName).concat(" ").concat(endText);
                break;
            case INACTIVE:
                friendName = getUserNameString(context, user);
                iniText = context.getString(R.string.friendship_inactive_text_ini);
                endText = context.getString(R.string.friendship_inactive_text_end);
                friendshipText = iniText.concat(friendName).concat(" ").concat(endText);
                break;
            case INVITED_BY_ME:
                friendName = getUserNameString(context, user);
                iniText = context.getString(R.string.friendship_invited_text_ini);
                endText = context.getString(R.string.friendship_invited_text_end);
                friendshipText = iniText.concat(" ").concat(friendName).concat(" ").concat(endText);
                break;
            case INVITING_ME:
                friendName = getUserNameString(context, user);
                friendshipText = friendName.concat(" ").concat(context.getString(R.string.friendship_inviting_me_text));
                break;
            case IGNORED_BY_ME:
                friendName = getUserNameString(context, user);
                iniText = context.getString(R.string.friendship_ignored_text_ini);
                endText = context.getString(R.string.friendship_ignored_text_end);
                friendshipText = iniText.concat(friendName).concat(" ").concat(endText);
                break;
            default:
        }

        return friendshipText;
    }

    public static Drawable getFriendshipIcon(Context context, User user) {
        Drawable friendshipIcon;

        switch (user.getState()) {
            case ACTIVE:
            case INACTIVE:
                friendshipIcon = ContextCompat.getDrawable(context, R.drawable.ic_friendship_ok);
                break;
            case INVITED_BY_ME:
            case INVITING_ME:
                friendshipIcon = ContextCompat.getDrawable(context, R.drawable.ic_friendship_pause);
                break;
            case IGNORED_BY_ME:
            case IGNORING_ME:
                friendshipIcon = ContextCompat.getDrawable(context, R.drawable.ic_friendship_ko);
                break;
            default:
                friendshipIcon = ContextCompat.getDrawable(context, R.drawable.ic_friendship_pause);
        }

        return friendshipIcon;
    }

    public static int getFriendshipIconColor(Context context, User user) {

        switch (user.getState()) {
            case ACTIVE:
                return ContextCompat.getColor(context, R.color.okColor);
            case INVITED_BY_ME:
            case INVITING_ME:
                return ContextCompat.getColor(context, R.color.warningColor);
            case IGNORED_BY_ME:
            case IGNORING_ME:
                return ContextCompat.getColor(context, R.color.koColor);
            case INACTIVE:
            default:
                return ContextCompat.getColor(context, R.color.inactiveColor);
        }
    }

    public static int getNormalLikeColor(Context context) {
        return ContextCompat.getColor(context, R.color.colorNormalLike);
    }

    public static int getGoldenLikeColor(Context context) {
        return ContextCompat.getColor(context, R.color.colorGoldenLike);
    }

    public static Drawable getNormalFilledLikeDrawable(Context context) {
        return ContextCompat.getDrawable(context, R.drawable.like_filled);
    }

    public static Drawable getNormalEmptyLikeDrawable(Context context) {
        return ContextCompat.getDrawable(context, R.drawable.like_empty);
    }

    public static int getAvatarWidth(Context context) {
        return 120;
    }

    public static int getAvatarHeight(Context context) {
        return 120;
    }

    public static float getActiveButtonAlpha(Context context) {
        return (float) 1;
    }

    public static float getInactiveButtonAlpha(Context context) {
        return (float) 0.25;
    }

    public static Drawable getUserAvatar(Context context, User user) {
        Bitmap bitmap = DoingController.get(context).loadAvatar(user);
        if (bitmap == null) {
            Drawable drawable = ContextCompat.getDrawable(context, R.drawable.default_avatar_drawable);
            return drawable;
        }
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
        roundedBitmapDrawable.setCircular(true);
        return roundedBitmapDrawable;
    }

    public static Drawable getCommentaryImage(Context context) {
        return context.getResources().getDrawable(R.drawable.bubble_white_normal);
    }

    public static Drawable getInvCommentaryImage(Context context) {
        return context.getResources().getDrawable(R.drawable.bubble_white_inv);
    }

    public static class AddFabAnimator {

        private Context mContext;

        private static final int HOME_TAB_POS = 0;
        private static final int HISTORY_TAB_POS = 1;
        private static final int FRIENDS_TAB_POS = 2;

        FloatingActionButton mAddFab;
        int mCurrentTab;

        public AddFabAnimator(Context context, FloatingActionButton addFab) {
            mContext = context;
            mAddFab = addFab;
            mCurrentTab = HOME_TAB_POS;
        }

        public AddFabAnimator(Context context, FloatingActionButton addFab, int currentTab) {
            mContext = context;
            mAddFab = addFab;
            mCurrentTab = currentTab;
            mAddFab.setImageDrawable(ContextCompat.getDrawable(mContext, mIconIntArray[currentTab]));
        }

        public void onChangeTabAnimation(final int position, final boolean active) {
            int nextTab = position;

            if (((mCurrentTab == HOME_TAB_POS) || (mCurrentTab == HISTORY_TAB_POS)) && (nextTab == FRIENDS_TAB_POS)) {
                animateAddButton(position, active);
            } else if (((mCurrentTab == HOME_TAB_POS) || (mCurrentTab == HISTORY_TAB_POS)) && (nextTab != FRIENDS_TAB_POS)) {
                mAddFab.setImageDrawable(ContextCompat.getDrawable(mContext, mIconIntArray[position]));
            } else if ((mCurrentTab == FRIENDS_TAB_POS) && ((nextTab == HOME_TAB_POS) || (nextTab == HISTORY_TAB_POS))) {
                animateAddButton(position, active);
            }

            mCurrentTab = nextTab;
        }

        private void animateAddButton(final int position, final boolean active) {

            mAddFab.clearAnimation();
            // Scale down animation
            ScaleAnimation shrink = new ScaleAnimation(1f, 0.2f, 1f, 0.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            shrink.setDuration(150);     // animation duration in milliseconds
            shrink.setInterpolator(new DecelerateInterpolator());
            shrink.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mAddFab.setBackgroundTintList(ContextCompat.getColorStateList(mContext, mColorIntArray[position]));
                    if (active) {
                        mAddFab.setAlpha(getActiveButtonAlpha(mContext));
                    } else {
                        mAddFab.setAlpha(getInactiveButtonAlpha(mContext));
                    }
                    mAddFab.setImageDrawable(ContextCompat.getDrawable(mContext, mIconIntArray[position]));

                    ScaleAnimation expand = new ScaleAnimation(0.2f, 1f, 0.2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                    expand.setDuration(100);     // animation duration in milliseconds
                    expand.setInterpolator(new AccelerateInterpolator());
                    mAddFab.startAnimation(expand);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mAddFab.startAnimation(shrink);
        }

    }


}
