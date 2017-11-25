package com.luisjavierlinares.android.doing.managers;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.luisjavierlinares.android.doing.DoingSettings;
import com.luisjavierlinares.android.doing.events.UserAddedEvent;
import com.luisjavierlinares.android.doing.messaging.MessagingSystem;
import com.luisjavierlinares.android.doing.messaging.MessagingUpdate.UpdateType;
import com.luisjavierlinares.android.doing.model.User;

import org.greenrobot.eventbus.EventBus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Luis on 13/05/2017.
 */

public class StorageManager {

    public final static int AVATAR_MIN_UPDATE_TIME_IN_HOURS = 12;

    private final static String FIREBASE_STORAGE_REF = "gs://doing-80de7.appspot.com/";
    private final static String FIREBASE_IMG_FOLDER = "img";
    private final static String LOCAL_IMG_FOLDER = "user_images";
    private final static String EXTERNAL_IMG_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath();

    private static StorageManager sStorageManager;
    private Context mContext;
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mStorageReference;

    private EventBus mEventBus;

    public static synchronized StorageManager get(Context context){
        if (sStorageManager == null){
            sStorageManager = new StorageManager(context);
        }
        return sStorageManager;
    }

    private StorageManager(Context context) {
        mContext = context;
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStorageReference = mFirebaseStorage.getReferenceFromUrl(FIREBASE_STORAGE_REF);
        mEventBus = EventBus.getDefault();
    }

    public String saveImage(Bitmap bitmap, String path, String filename) {
        ContextWrapper cw = new ContextWrapper(mContext);
        File directory = cw.getDir(path, Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }
        File myPath = new File(directory, filename + ".png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
        return myPath.getAbsolutePath();
    }

    public String saveImage(Bitmap bitmap, String filename) {
        return saveImage(bitmap, LOCAL_IMG_FOLDER, filename);
    }

    public String saveExternalImage(Bitmap bitmap, String filename) {
        File directory = new File (mContext.getFilesDir().getAbsolutePath());
        if (!directory.exists()) {
            directory.mkdir();
        }
        File myPath = new File(directory, filename + ".png");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
//            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
//                e.printStackTrace();
            }
        }
        return myPath.getAbsolutePath();
    }

    public Bitmap loadImage(String path, String filename) {
        ContextWrapper cw = new ContextWrapper(mContext);
        File directory = cw.getDir(path, Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }

        try {
            File file = new File(directory, filename + ".png");
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                return bitmap;
            }
        }
        catch (FileNotFoundException e)
        {
//            e.printStackTrace();
        }

        return null;
    }

    public Bitmap loadImage (String filename) {
        return loadImage(LOCAL_IMG_FOLDER, filename);
    }

    public Bitmap loadExternalImage(String filename) {
        File directory = new File (EXTERNAL_IMG_FOLDER);
        if (!directory.exists()) {
            directory.mkdir();
        }

        try {
            File file = new File(directory, filename + ".png");
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
                return bitmap;
            }
        }
        catch (FileNotFoundException e)
        {
//            e.printStackTrace();
        }

        return null;
    }

    public void uploadImage(Bitmap bitmap, String filename) {
        String ref = FIREBASE_IMG_FOLDER.concat("/".concat(filename));
        StorageReference storageReference = mStorageReference.child(ref);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        UploadTask uploadTask =storageReference.putBytes(byteArray);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                DoingSettings.setAvatarUploadPending(mContext, true);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                DoingSettings.setAvatarUploadPending(mContext, false);
            }
        });
    }

    public void downloadImage(final String filename) {
        String ref = FIREBASE_IMG_FOLDER.concat("/".concat(filename));
        StorageReference storageReference = mStorageReference.child(ref);

        final long ONE_MEGABYTE = 1024 * 1024;
        storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                saveImage(bitmap, filename);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                // Handle any errors
            }
        });
    }

    public void uploadAvatarImage(Bitmap bitmap, User user) {
        if (user.getUserCode() == null) {
            return;
        }
        uploadImage(bitmap, user.getUserCode());
    }

    public void downloadAvatarFromUser(final User user) {
        String ref = FIREBASE_IMG_FOLDER.concat("/".concat(user.getUserCode()));
        StorageReference storageReference = mStorageReference.child(ref);

        final long ONE_MEGABYTE = 1024 * 1024;
        storageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                saveAvatarImage(bitmap, user);
                MessagingSystem messagingSystem = MessagingSystem.get(mContext);
                Long time = messagingSystem.getEstimatedTime().getTime();
                messagingSystem.setUpdateTime(user, UpdateType.AVATAR, time);
                mEventBus.postSticky(new UserAddedEvent(user));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                // Handle any errors
            }
        });
    }

    public String saveAvatarImage(Bitmap bitmap, User user) {
        String filename = user.getId().toString();
        return saveImage(bitmap, filename);
    }

    public Bitmap loadAvatarImage(User user) {
        String filename = user.getId().toString();
        return loadImage(filename);
    }

    public File loadAvatarFile(User user) {
        String filename = user.getId().toString();
        ContextWrapper cw = new ContextWrapper(mContext);
        File directory = cw.getDir(LOCAL_IMG_FOLDER, Context.MODE_PRIVATE);
        if (!directory.exists()) {
            directory.mkdir();
        }

        File file = new File(directory, filename + ".png");
        return file;
    }
}
