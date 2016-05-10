package com.framgia.photoshare.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.framgia.photoshare.activities.MainActivity;

/**
 * Created by avishek on 12/11/15.
 */
public class SessionManager {
    SharedPreferences pref;

    SharedPreferences.Editor editor;

    Context _context;

    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "PHOTOSHARE";

    private static final String IS_LOGIN = "IsLoggedIn";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(boolean status) {
        editor.putBoolean(IS_LOGIN, status);
        editor.commit();
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();
        Intent intentMainActivity = new Intent(_context, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(intentMainActivity);
    }

    public void deleteSessionData() {
        editor.clear();
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }
}
