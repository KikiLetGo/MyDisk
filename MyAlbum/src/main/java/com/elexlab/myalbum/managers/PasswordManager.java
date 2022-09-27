package com.elexlab.myalbum.managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.elexlab.myalbum.pojos.Password;
import com.elexlab.myalbum.utils.DeviceUtils;
import com.elexlab.myalbum.utils.EasyLog;

/**
 * Created by BruceYoung on 10/14/17.
 */
public class PasswordManager {
    private final static String TAG = PasswordManager.class.getSimpleName();
    private static PasswordManager instance = new PasswordManager();
    public static PasswordManager getInstance(){
        return instance;
    }
    private PasswordManager(){}

   // private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    public String readPassword(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("password",Context.MODE_PRIVATE);
        String password = sharedPreferences.getString("password",null);
        return password;
    }
    private String writePassword(Context context,String password){
        SharedPreferences sharedPreferences = context.getSharedPreferences("password",Context.MODE_PRIVATE);
        sharedPreferences.edit().putString("password",password).apply();
        return password;
    }

    public void updatePassword(final Password newPassword, final Context context){
//        String uniqueId = DeviceUtils.getUniqueId(context);
//        mDatabase.child("password").child("gesture").child(uniqueId).setValue(newPassword.getKey(), new DatabaseReference.CompletionListener() {
//            @Override
//            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                if(databaseError != null){
//                    EasyLog.e(TAG,databaseError.getMessage());
//                    if(dataSourceCallback != null){
//                        dataSourceCallback.onFailure(databaseError.getMessage(),databaseError.getCode());
//                    }
//                    return;
//                }
//                PasswordManager.getInstance().writePassword(context,newPassword.getKey());
//                if (dataSourceCallback != null) {
//                    dataSourceCallback.onSuccess(newPassword);
//                }
//            }
//      });
    }
    public boolean isPasswordSetted(Context context){
        return readPassword(context) != null;
    }
}
