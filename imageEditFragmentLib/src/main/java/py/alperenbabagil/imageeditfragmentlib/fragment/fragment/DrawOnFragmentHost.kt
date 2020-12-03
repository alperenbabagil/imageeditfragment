package py.alperenbabagil.imageeditfragmentlib.fragment.fragment

interface DrawOnFragmentHost {
    fun drawingCompleted(success: Boolean, path: String?)
    fun drawingCancelled(path: String?)
    // to warn host to focus this tab
    fun unsavedChangesClose(fragmentTag:String)
}