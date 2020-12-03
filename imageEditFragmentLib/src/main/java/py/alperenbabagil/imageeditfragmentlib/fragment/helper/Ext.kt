package py.alperenbabagil.imageeditfragmentlib.fragment.helper

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment

fun Activity.hideKeyboard() {
    (getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)?.
        toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
}

fun View.show(){
    visibility=View.VISIBLE
}

fun View.hide(){
    visibility=View.GONE
}

inline fun <reified T> Fragment.getParentAsInterface() : T?{
    return parentFragment?.let {
        if(it is T) (it)
        else null
    } ?: run{
        activity?.let {
            if(it is T) (it)
            else null
        }
    }
}