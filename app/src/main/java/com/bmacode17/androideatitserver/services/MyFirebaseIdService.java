package com.bmacode17.androideatitserver.services;

import com.bmacode17.androideatitserver.commons.Common;
import com.bmacode17.androideatitserver.models.Token;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by User on 16-Aug-18.
 */

public class MyFirebaseIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        String tokenRefreshed = FirebaseInstanceId.getInstance().getToken();
        if(Common.currentUser != null)
            updateTokenToServer(tokenRefreshed);
    }

    private void updateTokenToServer(String tokenRefreshed) {

        if(Common.currentUser != null){

            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference table_token = db.getReference("token");
            Token token = new Token(tokenRefreshed , true);  // True because this token is sent from the Server
            table_token.child(Common.currentUser.getPhone()).setValue(token);
        }
    }
}

