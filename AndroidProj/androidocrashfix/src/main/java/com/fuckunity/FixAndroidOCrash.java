package com.fuckunity;
import android.app.Application;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Debug;
import android.util.ArrayMap;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

import java.lang.reflect.Field;

/**
 * Created by junke.zhu on 2018/7/18.
 */

public class FixAndroidOCrash {

    public  static boolean TryFix(boolean isShowLog)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return getRunningServiceInfo( isShowLog );
        return  true;
    }
    private static boolean getRunningServiceInfo( boolean isShowLog ) {

        try {
            Class<?> ContextImplClass = Class.forName("android.app.ContextImpl");

            Field mPackageInfoField = ContextImplClass.getDeclaredField("mPackageInfo");
            mPackageInfoField.setAccessible(true);

            Object packageInfo = mPackageInfoField.get(UnityPlayer.currentActivity.getBaseContext());
            Class c = mPackageInfoField.getType();
            if(isShowLog)
                Log.v("Test","Declared Field :" + c.getName());

            Field servicesField = c.getDeclaredField("mServices");
            servicesField.setAccessible(true);
            ArrayMap<Context, ArrayMap<ServiceConnection, Object>>  mServices = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                mServices = (ArrayMap<Context, ArrayMap<ServiceConnection, Object>>) servicesField.get(packageInfo);
            }
            if( mServices == null)
                return true;
            if(isShowLog ) {
                Log.d("Test", "servicesField Field :" + servicesField.getType().getName());

                Log.d("Test", "Size:" + mServices.size());
            }
            for (Context context : mServices.keySet()){

                for (ServiceConnection con : mServices.get(context).keySet()){
                    if(isShowLog )
                        Log.d("Test", "ContextName: " + context.toString() + "    " + "ConnectionName: " + con.toString());
                    if(con.toString().contains("<native proxy object>") )
                    {
                        UnityPlayer.currentActivity.unbindService(con);
                        return  true;
                    }

                    if(isShowLog )
                        Log.d("Test", "ContextName: " + context.toString() + "    " + "ConnectionName: " + con.toString());
                }

            }

        }
        catch (NoSuchFieldException e){
            e.printStackTrace();
            return  true;
        }
        catch (IllegalAccessException e){
            e.printStackTrace();
            return  true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return  true;
        }catch ( Exception e)
        {
            e.printStackTrace();
            return  true;
        }
        return  false;
    }
}
