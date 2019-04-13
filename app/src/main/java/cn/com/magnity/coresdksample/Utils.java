package cn.com.magnity.coresdksample;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class Utils {
    public static boolean requestRuntimePermission(final Context context, final String permissionType,
                                                   final int requestCode, final int titleId) {
        if (ContextCompat.checkSelfPermission(context, permissionType)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }

        if (!isActivity(context)) {
            return false;
        }

        //if the permission has been rejected by user before, should explain sth. to user
        if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)context,
                permissionType)) {
//            Toast.makeText(context, R.string.permissionNotPermitted,Toast.LENGTH_SHORT).show();

            new AlertDialog.Builder(context)
                    .setTitle(titleId)
                    .setMessage(R.string.permissionAllowedOrNo)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity)context, new String[]{ permissionType },
                                    requestCode);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create().show();
        } else { //request permission
            ActivityCompat.requestPermissions((Activity)context, new String[]{ permissionType },
                    requestCode);
        }
        return false;
    }

    private static boolean isActivity(Context context) {
        if (context instanceof Activity) {
            return true;
        }
        return false;
    }
}
