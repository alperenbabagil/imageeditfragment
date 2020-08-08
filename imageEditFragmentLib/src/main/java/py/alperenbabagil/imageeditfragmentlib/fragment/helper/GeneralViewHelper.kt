package py.alperenbabagil.imageeditfragmentlib.fragment.helper

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import java.util.*
import kotlin.math.roundToInt

/**
 * Created by alperen on 12.06.2017.
 */
object GeneralViewHelper {
    fun dpToPx(dp: Int, context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return (dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    fun pxToDp(px: Int, context: Context): Int {
        val displayMetrics = context.resources.displayMetrics
        return (px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)).roundToInt()
    }

    /**
     * returns real pixels of screen
     *
     * @param context
     * @return int[0] as w and int[1] as h
     */
    private fun getScreenSizes(context: Context): IntArray {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val sizes = IntArray(2)
        val display = wm.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        sizes[1] = size.y
        sizes[0] = size.x
        return sizes
    }

    fun getResolutionSameRatioWithScreen(context: Context, ints: ArrayList<IntArray>): ArrayList<IntArray> {
        val sizes = getScreenSizes(context)
        val returnList = ArrayList<IntArray>()
        val ratio = sizes[0] / sizes[1].toFloat()
        for (size in ints) {
            if (size[1] / size[0].toFloat() == ratio) returnList.add(size)
        }
        return returnList
    }

    fun hideKeyboard(view: View, activity: Activity) {
        val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun hideKeyboard(activity: Activity) {
        (activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).
            toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
    }

    fun measure(view: View) {
        val layoutParams = view.layoutParams
        val horizontalMode: Int
        val horizontalSize: Int
        when (layoutParams.width) {
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                horizontalMode = View.MeasureSpec.EXACTLY
                horizontalSize = if (view.parent is LinearLayout
                        && (view.parent as LinearLayout).orientation == LinearLayout.VERTICAL) {
                    val lp = view.layoutParams as MarginLayoutParams
                    (view.parent as View).measuredWidth - lp.leftMargin - lp.rightMargin
                } else {
                    (view.parent as View).measuredWidth
                }
            }
            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                horizontalMode = View.MeasureSpec.UNSPECIFIED
                horizontalSize = 0
            }
            else -> {
                horizontalMode = View.MeasureSpec.EXACTLY
                horizontalSize = layoutParams.width
            }
        }
        val horizontalMeasureSpec = View.MeasureSpec
                .makeMeasureSpec(horizontalSize, horizontalMode)
        val verticalMode: Int
        val verticalSize: Int
        when (layoutParams.height) {
            ViewGroup.LayoutParams.MATCH_PARENT -> {
                verticalMode = View.MeasureSpec.EXACTLY
                verticalSize = if (view.parent is LinearLayout
                        && (view.parent as LinearLayout).orientation == LinearLayout.HORIZONTAL) {
                    val lp = view.layoutParams as MarginLayoutParams
                    (view.parent as View).measuredHeight - lp.topMargin - lp.bottomMargin
                } else {
                    (view.parent as View).measuredHeight
                }
            }
            ViewGroup.LayoutParams.WRAP_CONTENT -> {
                verticalMode = View.MeasureSpec.UNSPECIFIED
                verticalSize = 0
            }
            else -> {
                verticalMode = View.MeasureSpec.EXACTLY
                verticalSize = layoutParams.height
            }
        }
        val verticalMeasureSpec = View.MeasureSpec.makeMeasureSpec(verticalSize, verticalMode)
        view.measure(horizontalMeasureSpec, verticalMeasureSpec)
    }
}