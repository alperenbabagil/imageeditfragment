package py.alperenbabagil.imageeditfragment.example;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;


public class GeneralHelper{



    public static boolean checkWRPermissionsGiven(Context context){
        boolean wrPermNotGiven = ContextCompat.checkSelfPermission(context,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;

        boolean rdPermNotGiven = ContextCompat.checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED;

        return !wrPermNotGiven && !rdPermNotGiven;
    }

    public static void requestWRPermissions(Activity activity,int PERM_REQ_CODE){
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                activity.requestPermissions(perms,PERM_REQ_CODE);
            }
    }

}
