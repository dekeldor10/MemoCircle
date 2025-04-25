package com.dordekel.memocircle;

import android.content.Context;

public class GoogleSignInHelper {

    public interface SignInCallback {
        //void onSignInSuccess(OAuth2Credential credential);
        void onSignInFailure(Exception exception);
    }

    public static void signInWithGoogle(Context context, String clientId, final SignInCallback callback){

    }

}
