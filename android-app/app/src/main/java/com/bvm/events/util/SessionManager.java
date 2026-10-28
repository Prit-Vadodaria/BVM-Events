package com.bvm.events.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String KEY_TOKEN = "key_token";
    private static final String KEY_USER_ID = "key_user_id";
    private static final String KEY_NAME = "key_name";
    private static final String KEY_EMAIL = "key_email";
    private static final String KEY_ROLE = "key_role";
    private static final String KEY_ENROLLMENT_NO = "key_enrollment_no";
    private static final String KEY_LAST_UPDATE_CHECK = "key_last_update_check";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveUserSession(String token, int userId, String name, String email, String role, String enrollmentNo) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_NAME, name)
                .putString(KEY_EMAIL, email)
                .putString(KEY_ROLE, role)
                .putString(KEY_ENROLLMENT_NO, enrollmentNo)
                .apply();
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "");
    }

    public String getEnrollmentNo() {
        return prefs.getString(KEY_ENROLLMENT_NO, "");
    }

    public boolean isStudent() {
        return Constants.ROLE_STUDENT.equalsIgnoreCase(getRole());
    }

    public boolean isOrganizer() {
        return Constants.ROLE_ORGANIZER.equalsIgnoreCase(getRole());
    }

    public boolean isAdmin() {
        return Constants.ROLE_ADMIN.equalsIgnoreCase(getRole());
    }

    public String getLastUpdateCheck() {
        return prefs.getString(KEY_LAST_UPDATE_CHECK, null);
    }

    public void setLastUpdateCheck(String serverTime) {
        prefs.edit().putString(KEY_LAST_UPDATE_CHECK, serverTime).apply();
    }

    public void clearSession() {
        prefs.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_USER_ID)
                .remove(KEY_NAME)
                .remove(KEY_EMAIL)
                .remove(KEY_ROLE)
                .remove(KEY_ENROLLMENT_NO)
                .remove(KEY_LAST_UPDATE_CHECK)
                .apply();
    }
}

