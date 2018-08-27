package com.bmacode17.androideatitserver.commons;

/**
 * Created by User on 18-Jul-18.
 */

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.bmacode17.androideatitserver.models.Request;
import com.bmacode17.androideatitserver.models.User;
import com.bmacode17.androideatitserver.remote.APIService;
import com.bmacode17.androideatitserver.remote.GeoCoordinates;
import com.bmacode17.androideatitserver.remote.RetrofitClient1;
import com.bmacode17.androideatitserver.remote.RetrofitClient2;

public class Common {

    public static User currentUser;
    public static Request currentRequest;
    public static final String UPDATE = "Update";
    public static final String DELETE = "Delete";
    public static final String baseUrl = "https://maps.googleapis.com";
    public static final String fcmUrl = "https://fcm.googleapis.com";

    public static String convertStatusToCode(String status) {

        if(status.equals("0"))
            return "Placed";
        else if(status.equals("1"))
            return "On my way";
        else
            return "Shipped";
    }

    public static APIService getFCMService(){
        return RetrofitClient2.getClient(fcmUrl).create(APIService.class);
    }

    public static GeoCoordinates getGeoCodeService(){
        return RetrofitClient1.getClient(baseUrl).create(GeoCoordinates.class);
    }

    public static Bitmap scaleBitmap(Bitmap bitmap , int newWidth , int newHeight){

        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth,newHeight,Bitmap.Config.ARGB_8888);
        float scaleX = newWidth/(float) bitmap.getWidth();
        float scaleY = newHeight/(float) bitmap.getHeight();
        float pivotX = 0 , pivotY = 0;
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX,scaleY,pivotX,pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap,0,0,new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }
}
