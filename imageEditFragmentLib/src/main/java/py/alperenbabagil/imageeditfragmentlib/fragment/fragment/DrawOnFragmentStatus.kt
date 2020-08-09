package py.alperenbabagil.imageeditfragmentlib.fragment.fragment

interface DrawOnFragmentStatus {
    fun drawingCompleted(success: Boolean, path: String?)
    fun drawingCancelled(path: String?)
}