package py.alperenbabagil.imageeditfragment.example;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import py.alperenbabagil.imageeditfragment.fragment.ImageEditFragment;


public class MainActivity extends AppCompatActivity implements ImageEditFragment.DrawOnFragmentStatus{

    static final int PERM_REQ_CODE = 136;
    static String PATH;
    String drawedImagePath;
    LinearLayout buttonLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolean permissionGiven = false;

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!GeneralHelper.checkWRPermissionsGiven(this)){
                GeneralHelper.requestWRPermissions(this,PERM_REQ_CODE);
            }
            else{
                permissionGiven = true;
            }
        }
        else{
            permissionGiven = true;
        }

        if(!permissionGiven){
            Toast.makeText(this,"please give write permissions",Toast.LENGTH_LONG).show();
            return;
        }

        onPermissionsGiven();

    }

    private void openImageEditFragment(){

        //hiding status bar and action bar to enter full screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try{
            getSupportActionBar().hide();
        }catch(Exception e){
            e.printStackTrace();
        }


        // prevent buttons to be seen
        buttonLayout.setVisibility(View.GONE);

        Bundle bundle = new Bundle();

        //setting data source type
        bundle.putSerializable(ImageEditFragment.SOURCE_TYPE_KEY,ImageEditFragment.SourceType.FILE_PATH);
        //setting image path
        bundle.putString(ImageEditFragment.SOURCE_DATA_KEY,PATH);

        //creating fragment
        ImageEditFragment imageEditFragment = new ImageEditFragment();

        //setting arguments
        imageEditFragment.setArguments(bundle);

        //putting fragment
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentContainer,imageEditFragment).commit();
    }


    //putting an example image to external storage
    private boolean writeDrawableToDisk(){
        Bitmap bm = BitmapFactory.decodeResource(getResources(),R.drawable.dp);

        File imageFile = new File(PATH);

        FileOutputStream fos = null;
        try{
            fos = new FileOutputStream(imageFile);

            bm.compress(Bitmap.CompressFormat.PNG,100,fos);

            fos.close();

            return true;
        }catch(IOException e){
            Log.e("app",e.getMessage());
            if(fos != null){
                try{
                    fos.close();
                }catch(IOException e1){
                    e1.printStackTrace();
                }
            }
        }
        return false;
    }


    private void onPermissionsGiven(){
        //written image's path
        PATH = Environment.getExternalStorageDirectory().getPath() + "/" + "dp.jpg";

        // showing loading popup
        final ProgressDialog dialog = ProgressDialog.show(this, "",
                "loading image", true);

        dialog.show();

        buttonLayout=findViewById(R.id.buttonLayout);

        //setting open edited image button click listener
        findViewById(R.id.openImage).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(drawedImagePath == null){
                    Toast.makeText(MainActivity.this,"No edited image",Toast.LENGTH_SHORT).show();
                }
                //opening image in gallery
                else{
                    //for demo app. In production, you must implement a file provider
                    if(Build.VERSION.SDK_INT >= 24){
                        try{
                            Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                            m.invoke(null);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse("file://" + drawedImagePath),"image/*");
                    startActivity(intent);
                }
            }
        });

        findViewById(R.id.openFragment).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                openImageEditFragment();

            }
        });

        findViewById(R.id.resetImage).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //refreshing image
                dialog.show();
                writeDrawableToDisk();
                dialog.hide();
            }
        });

        writeDrawableToDisk();

        dialog.hide();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[],int[] grantResults){
        switch(requestCode){
            case PERM_REQ_CODE:
                // If request is cancelled, the result arrays are empty.
                if(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // permission was granted, yay! Do the
                    onPermissionsGiven();
                    break;
                }
                else{
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this,"please give write permissions",Toast.LENGTH_LONG).show();
                }
                break;
            default:
                finish();
        }
    }

    @Override
    public void drawingCompleted(boolean success,String path){
        drawedImagePath = path;
        Toast.makeText(this,"Edited image saved succesfully",Toast.LENGTH_SHORT).show();
        removeFragment();
    }

    @Override
    public void drawingCancelled(String path){
        removeFragment();
    }

    private void removeFragment(){
        // exiting full screen
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try{
            getSupportActionBar().show();
        }catch(Exception e){
            e.printStackTrace();
        }

        //showing buttons
        buttonLayout.setVisibility(View.VISIBLE);

        //Here we are clearing back stack fragment entries
        int backStackEntry = getSupportFragmentManager().getBackStackEntryCount();
        if(backStackEntry > 0){
            for(int i = 0; i < backStackEntry; i++){
                getSupportFragmentManager().popBackStackImmediate();
            }
        }

        //Here we are removing all the fragment that are shown here
        if(getSupportFragmentManager().getFragments() != null && getSupportFragmentManager().getFragments().size() > 0){
            for(int i = 0; i < getSupportFragmentManager().getFragments().size(); i++){
                Fragment mFragment = getSupportFragmentManager().getFragments().get(i);
                if(mFragment != null){
                    getSupportFragmentManager().beginTransaction().remove(mFragment).commit();
                }
            }
        }
    }
}
