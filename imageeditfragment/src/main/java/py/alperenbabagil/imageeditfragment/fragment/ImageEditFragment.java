package py.alperenbabagil.imageeditfragment.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeProgressDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeWarningDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.divyanshu.colorseekbar.ColorSeekBar;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import py.alperenbabagil.imageeditfragment.helper.GeneralViewHelper;
import py.alperenbabagil.imageeditfragment.helper.WHEqualView;
import py.alperenbabagil.imageeditfragment.photoeditor.OnPhotoEditorListener;
import py.alperenbabagil.imageeditfragment.photoeditor.PhotoEditor;
import py.alperenbabagil.imageeditfragment.photoeditor.PhotoEditorView;
import py.alperenbabagil.imageeditfragment.photoeditor.ViewType;


public class ImageEditFragment extends Fragment{

    //static keys
    public static String SOURCE_TYPE_KEY = "SOURCE_TYPE_KEY";
    public static String SOURCE_DATA_KEY = "SOURCE_DATA_KEY";
    public static String WARNING_STRING_KEY = "WARNING_STRING_KEY";
    public static String OK_STRING_KEY = "OK_STRING_KEY";
    public static String LOADING_STRING_KEY = "LOADING_STRING_KEY";
    public static String IMAGE_WILL_BE_LOST_STRING_KEY = "IMAGE_WILL_BE_LOST_STRING_KEY";

    //state variables
    private static int MODE_DRAW = 1;
    private static int MODE_TEXT = 2;
    private static int MODE_INITIAL = 0;
    private int currentMode = 0;
    private boolean canUndo = false;
    private boolean isKeyboardOpen = false;

    //view variables
    private PhotoEditorView photoEditorView;
    private PhotoEditor photoEditor;
    private String filePath;
    private RelativeLayout drawingTop;
    private RelativeLayout mainColorBar;
    private LinearLayout textTop;
    private RelativeLayout secondaryColorBar;
    private RelativeLayout initialTop;
    private LinearLayout initialBottom;
    private ImageView undoBtn;
    private EditText annotationText;
    private View editTextToEdit = null;


    private DrawOnFragmentStatus drawOnFragmentStatus;


    // default values
    private int currentSecondaryColor = 0xFFFFFFFF;
    private int currentMainColor = 0xFFAAAAAA;
    private String warningString = "Warning";
    private String okString = "OK";
    private String loadingString = "Loading";
    private String imageWillBeLostString = "Image will be lost";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,@Nullable ViewGroup container,@Nullable Bundle savedInstanceState){
        return inflater.inflate(py.alperenbabagil.imageeditfragment.R.layout.photo_edit_fragment_layout,container,false);
    }

    @Override
    public void onViewCreated(@NonNull final View view,@Nullable Bundle savedInstanceState){
        super.onViewCreated(view,savedInstanceState);

        drawOnFragmentStatus = (DrawOnFragmentStatus) getActivity();

        SourceType sourceType = null;

        //region Getting arguments
        try{
            sourceType = (SourceType) getArguments().get(SOURCE_TYPE_KEY);
        }catch(Exception e){
            e.printStackTrace();
        }
        if(sourceType == null) try{
            throw new Exception("You must put sourceType to fragment bundle bundle");
        }catch(Exception e){
            e.printStackTrace();
            return;
        }

        if(getArguments() != null){
            if(getArguments().containsKey(WARNING_STRING_KEY))
                warningString = getArguments().getString(WARNING_STRING_KEY);

            if(getArguments().containsKey(OK_STRING_KEY))
                okString = getArguments().getString(OK_STRING_KEY);

            if(getArguments().containsKey(LOADING_STRING_KEY))
                loadingString = getArguments().getString(LOADING_STRING_KEY);

            if(getArguments().containsKey(IMAGE_WILL_BE_LOST_STRING_KEY))
                imageWillBeLostString = getArguments().getString(IMAGE_WILL_BE_LOST_STRING_KEY);
        }


        //endregion

        photoEditorView = view.findViewById(py.alperenbabagil.imageeditfragment.R.id.photoEditorView);

        //Back pressed Logic for fragment
        photoEditorView.setFocusableInTouchMode(true);
        photoEditorView.requestFocus();

        //Back key listener
        photoEditorView.setOnKeyListener(new View.OnKeyListener(){
            @Override
            public boolean onKey(View v,int keyCode,KeyEvent event){
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    if(keyCode == KeyEvent.KEYCODE_BACK){
                        cancelFragment();
                        return true;
                    }
                }
                return false;
            }
        });


        switch(sourceType){
            case URI:
                Uri uri = getArguments().getParcelable(SOURCE_DATA_KEY);
                if(uri == null)
                    try{
                        throw new Exception("corrupted uri");
                    }catch(Exception e){
                        e.printStackTrace();
                        return;
                    }
                photoEditorView.getSource().setImageURI(uri);
                filePath = uri.getPath();
                break;
            case FILE_PATH:
                filePath = getArguments().getString(SOURCE_DATA_KEY);
                if(filePath == null)
                    try{
                        throw new Exception("corrupted filePath");
                    }catch(Exception e){
                        e.printStackTrace();
                        return;
                    }
                photoEditorView.getSource().setImageURI(Uri.fromFile(new File(filePath)));
                break;
        }

        //building editor
        photoEditor = new PhotoEditor.Builder(getActivity(),photoEditorView)
                .setPinchTextScalable(true)
                .build();

        //default brush size
        photoEditor.setBrushSize(20f);

        initLayouts(view);

        photoEditor.setOnPhotoEditorListener(new OnPhotoEditorListener(){
            @Override
            public void onEditTextChangeListener(View rootView,String text,int colorCode){
                if(currentMode != MODE_DRAW){
                    rootView.setVisibility(View.GONE);
                    currentMode = MODE_TEXT;
                    annotationText.setText(text);
                    arrangeViewsByMode();
                    editTextToEdit = rootView;
                }
            }

            @Override
            public void onAddViewListener(ViewType viewType,int numberOfAddedViews){
                canUndo = true;
                setUndoButtonsVisibility();
            }

            @Override
            public void onRemoveViewListener(int numberOfAddedViews){

            }

            @Override
            public void onStartViewChangeListener(ViewType viewType){

            }

            @Override
            public void onStopViewChangeListener(ViewType viewType){

            }
        });

        photoEditor.setBrushColor(currentMainColor);

        undoBtn = view.findViewById(py.alperenbabagil.imageeditfragment.R.id.undoBtn);

        // setting button click listeners
        initBtnClicks(view);

        ColorSeekBar mainCSB=view.findViewById(py.alperenbabagil.imageeditfragment.R.id.mainColorSeekBar);

        mainCSB.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener(){
            @Override
            public void onColorChangeListener(int i){
                currentMainColor = i;
                if(currentMode == MODE_DRAW){
                    photoEditor.setBrushColor(i);
                }
                if(currentMode == MODE_TEXT){
                    annotationText.setTextColor(i);
                }
            }
        });

        ColorSeekBar secondaryCSB=view.findViewById(py.alperenbabagil.imageeditfragment.R.id.secondaryColorSeekBar);

        secondaryCSB.setOnColorChangeListener(new ColorSeekBar.OnColorChangeListener(){
            @Override
            public void onColorChangeListener(int i){
                currentSecondaryColor = i;
                annotationText.setBackgroundColor(i);
            }
        });

        arrangeViewsByMode();

        //listening for keyboard status
        view.findViewById(py.alperenbabagil.imageeditfragment.R.id.drawingRoot).getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
            @Override
            public void onGlobalLayout(){

                Rect r = new Rect();
                view.findViewById(py.alperenbabagil.imageeditfragment.R.id.drawingRoot).getWindowVisibleDisplayFrame(r);
                int screenHeight = view.findViewById(py.alperenbabagil.imageeditfragment.R.id.drawingRoot).getRootView().getHeight();

                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;


                if(keypadHeight > screenHeight * 0.15){ // 0.15 ratio is perhaps enough to determine keypad height.
                    // keyboard is opened
//                    LogHelper.d("keybb","open");
                    isKeyboardOpen = true;
                    photoEditorView.setFocusableInTouchMode(false);
                    photoEditorView.clearFocus();
                }
                else{
                    // keyboard is closed
//                    LogHelper.d("keybb","closed");
                    isKeyboardOpen = false;
                    photoEditorView.setFocusableInTouchMode(true);
                    photoEditorView.requestFocus();
                }
            }
        });

    }


    private void initLayouts(View view){

        // onTouch events return true because if touch passes to lower view it may cause to unintended drawings
        View.OnTouchListener onTouchListener=new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v,MotionEvent event){
                return true;
            }
        };

        drawingTop = view.findViewById(py.alperenbabagil.imageeditfragment.R.id.drawingTop);
        drawingTop.setOnTouchListener(onTouchListener);

        mainColorBar = view.findViewById(py.alperenbabagil.imageeditfragment.R.id.mainColorBar);
        mainColorBar.setOnTouchListener(onTouchListener);

        textTop = view.findViewById(py.alperenbabagil.imageeditfragment.R.id.textTop);
        textTop.setOnTouchListener(onTouchListener);

        secondaryColorBar = view.findViewById(py.alperenbabagil.imageeditfragment.R.id.secondaryColorBar);
        secondaryColorBar.setOnTouchListener(onTouchListener);

        initialTop = view.findViewById(py.alperenbabagil.imageeditfragment.R.id.initialTop);
        initialTop.setOnTouchListener(onTouchListener);


        initialBottom = view.findViewById(py.alperenbabagil.imageeditfragment.R.id.initialBottom);
        initialBottom.setOnTouchListener(onTouchListener);

        annotationText = view.findViewById(py.alperenbabagil.imageeditfragment.R.id.annotationText);
        annotationText.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            public boolean onEditorAction(TextView v,int actionId,
                                          KeyEvent event){
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    if(currentMode == MODE_TEXT) textDone();
                    return true;
                }
                return false;
            }
        });

        annotationText.setBackgroundColor(currentSecondaryColor);
        annotationText.setTextColor(currentMainColor);
    }

    private void initBtnClicks(final View view){
        view.findViewById(py.alperenbabagil.imageeditfragment.R.id.penBtn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                currentMode = MODE_DRAW;
                photoEditor.setBrushDrawingMode(true);
                arrangeViewsByMode();
            }
        });

        view.findViewById(py.alperenbabagil.imageeditfragment.R.id.textBtn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                currentMode = MODE_TEXT;
                photoEditor.setBrushDrawingMode(false);
                arrangeViewsByMode();
            }
        });

        view.findViewById(py.alperenbabagil.imageeditfragment.R.id.cancelBtn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                cancelFragment();
            }
        });

        view.findViewById(py.alperenbabagil.imageeditfragment.R.id.drawingDoneBtn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                currentMode = MODE_INITIAL;
                photoEditor.setBrushDrawingMode(false);
                arrangeViewsByMode();
            }
        });

        view.findViewById(py.alperenbabagil.imageeditfragment.R.id.textDoneBtn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                textDone();
            }
        });


        view.findViewById(py.alperenbabagil.imageeditfragment.R.id.saveBtn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                final AwesomeProgressDialog awesomeProgressDialog = new AwesomeProgressDialog(getActivity())
                        .setTitle(loadingString)
                        .setMessage("")
                        .setCancelable(true);

                awesomeProgressDialog.show();

                //checking for write permissions
                if(ActivityCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    try{
                        throw new Exception("you must get write external storage permission");
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    return;
                }


                photoEditor.saveAsFile(filePath,new PhotoEditor.OnSaveListener(){
                    @Override
                    public void onSuccess(@NonNull String imagePath){
                        awesomeProgressDialog.hide();
                        drawOnFragmentStatus.drawingCompleted(true,filePath);
                    }

                    @Override
                    public void onFailure(@NonNull Exception exception){
                        awesomeProgressDialog.hide();
                        drawOnFragmentStatus.drawingCompleted(false,filePath);
                    }
                });
            }
        });

        SeekBar seekBar = view.findViewById(py.alperenbabagil.imageeditfragment.R.id.drawingSeekbar);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            seekBar.getThumb().setTint(currentMainColor);
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            seekBar.getProgressDrawable().setTint(currentMainColor);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar,int progress,boolean fromUser){
                float size = ((float) progress) / 8;
                //to prevent 0 brush size
                photoEditor.setBrushSize(size + 5);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar){

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar){

            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener(){
            @Override
            public void onClick(View v){
                canUndo = photoEditor.undo();
                setUndoButtonsVisibility();
            }
        };

        undoBtn.setOnClickListener(onClickListener);

    }

    private void setUndoButtonsVisibility(){
        if(canUndo){
            undoBtn.setVisibility(View.VISIBLE);
        }
        else{
            undoBtn.setVisibility(View.GONE);
        }

    }

    private void textDone(){
        currentMode = MODE_INITIAL;
        photoEditor.setBrushDrawingMode(false);
        if(editTextToEdit != null){
            photoEditor.editText(editTextToEdit,annotationText.getText().toString(),currentMainColor,currentSecondaryColor);
            editTextToEdit.setVisibility(View.VISIBLE);
            editTextToEdit = null;
        }
        else
            photoEditor.addText(annotationText.getText().toString(),currentMainColor,currentSecondaryColor);
        canUndo = true;
        setUndoButtonsVisibility();
        annotationText.setText("");
        if(isKeyboardOpen) GeneralViewHelper.hideKeyboard(getActivity());
        arrangeViewsByMode();
    }

    public void cancelFragment(){

        new AwesomeWarningDialog(getActivity())
                .setTitle(warningString)
                .setMessage(imageWillBeLostString)
                .setCancelable(true)
                .setButtonText(okString)
                .setWarningButtonClick(new Closure(){
                    @Override
                    public void exec(){
                        drawOnFragmentStatus.drawingCancelled(filePath);
                    }
                })
                .show();
    }

    private void arrangeViewsByMode(){
        if(currentMode == MODE_INITIAL){

            annotationText.setVisibility(View.GONE);

            drawingTop.setVisibility(View.GONE);
            mainColorBar.setVisibility(View.GONE);

            textTop.setVisibility(View.GONE);
            secondaryColorBar.setVisibility(View.GONE);

            initialTop.setVisibility(View.VISIBLE);
            initialBottom.setVisibility(View.VISIBLE);
        }

        if(currentMode == MODE_DRAW){

            annotationText.setVisibility(View.GONE);

            drawingTop.setVisibility(View.VISIBLE);
            mainColorBar.setVisibility(View.VISIBLE);

            secondaryColorBar.setVisibility(View.GONE);
            textTop.setVisibility(View.GONE);

            initialTop.setVisibility(View.GONE);
            initialBottom.setVisibility(View.GONE);
        }

        if(currentMode == MODE_TEXT){

            annotationText.setVisibility(View.VISIBLE);

            drawingTop.setVisibility(View.GONE);
            mainColorBar.setVisibility(View.VISIBLE);

            textTop.setVisibility(View.VISIBLE);
            secondaryColorBar.setVisibility(View.VISIBLE);

            initialTop.setVisibility(View.GONE);
            initialBottom.setVisibility(View.GONE);
        }
    }

    public enum SourceType{
        FILE_PATH,
        URI
    }

    public interface DrawOnFragmentStatus{
        void drawingCompleted(boolean success,String path);

        void drawingCancelled(String path);
    }
}
