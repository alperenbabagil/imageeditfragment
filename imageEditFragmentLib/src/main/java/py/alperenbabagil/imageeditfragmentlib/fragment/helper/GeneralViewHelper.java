package py.alperenbabagil.imageeditfragmentlib.fragment.helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import java.util.ArrayList;

import androidx.annotation.NonNull;

/**
 * Created by alperen on 12.06.2017.
 */

public class GeneralViewHelper{

    public static int dpToPx(int dp,Context context){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int pxToDp(int px,Context context){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * returns real pixels of screen
     *
     * @param context
     * @return int[0] as w and int[1] as h
     */
    public static int[] getScreenSizes(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int[] sizes = new int[2];
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getRealSize(size);
        sizes[1] = size.y;
        sizes[0] = size.x;
        return sizes;
    }

    public static ArrayList<int[]> getResolutionSameRatioWithScreen(Context context,ArrayList<int[]> ints){
        int[] sizes = getScreenSizes(context);
        ArrayList<int[]> returnList = new ArrayList<>();
        float ratio = sizes[0] / ((float) sizes[1]);
        for(int[] size : ints){
            if(size[1] / ((float) size[0]) == ratio) returnList.add(size);
        }
        return returnList;
    }

    public static void hideKeyboard(View view,Activity activity){
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(),0);
    }

    public static void hideKeyboard(Activity activity){
        if(activity == null) return;
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        if(imm != null) imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY,0);
    }


    public static void measure(@NonNull final View view){
        final ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        final int horizontalMode;
        final int horizontalSize;
        switch(layoutParams.width){
            case ViewGroup.LayoutParams.MATCH_PARENT:
                horizontalMode = View.MeasureSpec.EXACTLY;
                if(view.getParent() instanceof LinearLayout
                        && ((LinearLayout) view.getParent()).getOrientation() == LinearLayout.VERTICAL){
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                    horizontalSize = ((View) view.getParent()).getMeasuredWidth() - lp.leftMargin - lp.rightMargin;
                }
                else{
                    horizontalSize = ((View) view.getParent()).getMeasuredWidth();
                }
                break;
            case ViewGroup.LayoutParams.WRAP_CONTENT:
                horizontalMode = View.MeasureSpec.UNSPECIFIED;
                horizontalSize = 0;
                break;
            default:
                horizontalMode = View.MeasureSpec.EXACTLY;
                horizontalSize = layoutParams.width;
                break;
        }
        final int horizontalMeasureSpec = View.MeasureSpec
                .makeMeasureSpec(horizontalSize,horizontalMode);

        final int verticalMode;
        final int verticalSize;
        switch(layoutParams.height){
            case ViewGroup.LayoutParams.MATCH_PARENT:
                verticalMode = View.MeasureSpec.EXACTLY;
                if(view.getParent() instanceof LinearLayout
                        && ((LinearLayout) view.getParent()).getOrientation() == LinearLayout.HORIZONTAL){
                    ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                    verticalSize = ((View) view.getParent()).getMeasuredHeight() - lp.topMargin - lp.bottomMargin;
                }
                else{
                    verticalSize = ((View) view.getParent()).getMeasuredHeight();
                }
                break;
            case ViewGroup.LayoutParams.WRAP_CONTENT:
                verticalMode = View.MeasureSpec.UNSPECIFIED;
                verticalSize = 0;
                break;
            default:
                verticalMode = View.MeasureSpec.EXACTLY;
                verticalSize = layoutParams.height;
                break;
        }
        final int verticalMeasureSpec = View.MeasureSpec.makeMeasureSpec(verticalSize,verticalMode);

        view.measure(horizontalMeasureSpec,verticalMeasureSpec);
    }


}
