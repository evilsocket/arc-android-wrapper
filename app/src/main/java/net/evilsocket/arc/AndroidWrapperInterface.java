package net.evilsocket.arc;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.support.v4.app.ActivityCompat;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URLConnection;

import static android.content.Context.NOTIFICATION_SERVICE;

public class AndroidWrapperInterface {
    private Context context;
    private MainActivity activity;


    public AndroidWrapperInterface(MainActivity activity, Context context) {
        this.context = context;
        this.activity = activity;
    }

    private  boolean isWriteStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission( this.context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                ActivityCompat.requestPermissions(this.activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else {
            return true;
        }
    }

    private  boolean isReadStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission( this.context, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {

                ActivityCompat.requestPermissions(this.activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else {
            return true;
        }
    }

    private String getMimeType(String url) {
        String parts[]=url.split("\\.");
        String extension=parts[parts.length-1];
        String type = null;
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            type = mime.getMimeTypeFromExtension(extension);
        }
        return type;
    }

    @JavascriptInterface
    public void onFileDownload(String filename, byte[] bytes) {
        if( isWriteStoragePermissionGranted() == false || isReadStoragePermissionGranted() == false ) {
            return;
        }

        File base = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(base, filename);

        Log.i( "ARC", "Saving " + bytes.length + " bytes to " + file );

        try {
            FileOutputStream os = new FileOutputStream(file);
            os.write(bytes);
            os.close();

            Intent intent = new Intent(Intent.ACTION_VIEW);

            intent.setDataAndType(Uri.fromFile(file), getMimeType(file.getAbsolutePath()));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            this.activity.startActivity(intent);
        }
        catch( Exception e ) {
            e.printStackTrace();
            Log.e("ARC", e.getMessage() );

            AlertDialog alert = new AlertDialog.Builder(this.activity).create();

            alert.setTitle("Error");
            alert.setMessage("Error saving " + file + ": " + e.getMessage());

            alert.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alert.show();
        }
    }
}