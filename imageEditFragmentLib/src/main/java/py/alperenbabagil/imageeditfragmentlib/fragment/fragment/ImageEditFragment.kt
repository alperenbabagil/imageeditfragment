package py.alperenbabagil.imageeditfragmentlib.fragment.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.Fragment
import coil.Coil
import coil.request.LoadRequest
import com.alperenbabagil.simpleanimationpopuplibrary.SapFragment
import com.alperenbabagil.simpleanimationpopuplibrary.removeCurrentDialog
import com.alperenbabagil.simpleanimationpopuplibrary.showLoadingDialog
import com.alperenbabagil.simpleanimationpopuplibrary.showWarningDialog
import com.divyanshu.colorseekbar.ColorSeekBar.OnColorChangeListener
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import kotlinx.android.synthetic.main.photo_edit_fragment_layout.*
import py.alperenbabagil.imageeditfragmentlib.R
import py.alperenbabagil.imageeditfragmentlib.fragment.helper.GeneralViewHelper.hideKeyboard
import py.alperenbabagil.imageeditfragmentlib.fragment.photoeditor.OnPhotoEditorListener
import py.alperenbabagil.imageeditfragmentlib.fragment.photoeditor.PhotoEditor
import py.alperenbabagil.imageeditfragmentlib.fragment.photoeditor.PhotoEditor.OnSaveListener
import py.alperenbabagil.imageeditfragmentlib.fragment.photoeditor.ViewType
import java.io.File

class ImageEditFragment : Fragment(), SapFragment {
    private var currentMode = 0
    private var canUndo = false
    private var isKeyboardOpen = false

    //view variables
    private lateinit var photoEditor: PhotoEditor
    private var filePath: String? = null
    private var editTextToEdit: View? = null

    // default values
    private var currentSecondaryColor = -0x222223
    private var currentMainColor = -0x1000000
    private var warningString = "Warning"
    private var okString = "OK"
    private var loadingString = "Loading"
    private var imageWillBeLostString = "Image will be lost"

    private var saveImagePath: String? = null
    private var saveBtnDisabled = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.photo_edit_fragment_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var sourceType: SourceType? = null
        arguments?.let {
            if (it.containsKey(SOURCE_TYPE_KEY)) {
                sourceType = it[SOURCE_TYPE_KEY] as SourceType
            } else {
                throw Exception("You must put sourceType to fragment bundle")
            }

            if (it.containsKey(SAVE_IMAGE_PATH_KEY)) {
                saveImagePath = it.getString(SAVE_IMAGE_PATH_KEY)
            }
            if (it.containsKey(HIDE_SAVE_BTN_KEY)) {
                if (it.getBoolean(HIDE_SAVE_BTN_KEY)) {
                    saveBtnDisabled=true
                    saveBtn.visibility = View.GONE
                }
            }

            when (sourceType) {
                SourceType.URI -> {
                    val uri = it.getParcelable<Uri>(SOURCE_DATA_KEY)
                            ?: try {
                                throw Exception("corrupted uri")
                            } catch (e: Exception) {
                                e.printStackTrace()
                                return
                            }
                    photoEditorView!!.source.setImageURI(uri)
                    filePath = uri.path
                }
                SourceType.FILE_PATH -> {
                    filePath = it.getString(SOURCE_DATA_KEY)
                    if (filePath == null) try {
                        throw Exception("corrupted filePath")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return
                    }
                    photoEditorView.source.setImageURI(Uri.fromFile(File(filePath)))
                }
                SourceType.URL -> {

                    if(!saveBtnDisabled){
                        saveImagePath ?: kotlin.run {
                            throw Exception("You must enter save filePath")
                        }
                    }

                    it.getString(SOURCE_DATA_KEY)?.let {
                        val request = LoadRequest.Builder(requireContext())
                                .data(it)
                                .target { drawable ->
                                    photoEditorView.source.setImageDrawable(drawable)
                                }
                                .build()
                        val imageLoader = Coil.imageLoader(requireContext())
                        imageLoader.execute(request)
                    }
                }
            }

            if (it.containsKey(WARNING_STRING_KEY)) warningString = it.getString(WARNING_STRING_KEY)!!
            if (it.containsKey(OK_STRING_KEY)) okString = it.getString(OK_STRING_KEY)!!
            if (it.containsKey(LOADING_STRING_KEY)) loadingString = it.getString(LOADING_STRING_KEY)!!
            if (it.containsKey(IMAGE_WILL_BE_LOST_STRING_KEY)) imageWillBeLostString = it.getString(IMAGE_WILL_BE_LOST_STRING_KEY)!!
        } ?: run {
            throw Exception("You must provide a bundle")
        }

        photoEditorView.apply {
            //Back pressed Logic for fragment
            isFocusableInTouchMode = true
            requestFocus()
            //Back key listener
            setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        cancelFragment()
                        return@OnKeyListener true
                    }
                }
                false
            })
        }

        //building editor
        photoEditor = PhotoEditor.Builder(activity, photoEditorView)
                .setPinchTextScalable(true)
                .build().apply {
                    //default brush size
                    brushSize = 20f
                    brushColor = currentMainColor
                    setOnPhotoEditorListener(object : OnPhotoEditorListener {
                        override fun onEditTextChangeListener(rootView: View, text: String, colorCode: Int) {
                            if (currentMode != MODE_DRAW) {
                                rootView.visibility = View.GONE
                                currentMode = MODE_TEXT
                                annotationText!!.setText(text)
                                arrangeViewsByMode()
                                editTextToEdit = rootView
                            }
                        }

                        override fun onAddViewListener(viewType: ViewType, numberOfAddedViews: Int) {
                            canUndo = true
                            setUndoButtonsVisibility()
                        }

                        override fun onRemoveViewListener(numberOfAddedViews: Int) {}
                        override fun onStartViewChangeListener(viewType: ViewType) {}
                        override fun onStopViewChangeListener(viewType: ViewType) {}
                    })
                }

        initLayouts(view)
        // setting button click listeners
        initBtnClicks(view)
        mainColorSeekBar.setOnColorChangeListener(object : OnColorChangeListener {
            override fun onColorChangeListener(i: Int) {
                currentMainColor = i
                if (currentMode == MODE_DRAW) {
                    photoEditor.brushColor = i
                }
                if (currentMode == MODE_TEXT) {
                    annotationText!!.setTextColor(i)
                }
            }
        })
        secondaryColorSeekBar.setOnColorChangeListener(object : OnColorChangeListener {
            override fun onColorChangeListener(i: Int) {
                currentSecondaryColor = i
                annotationText!!.setBackgroundColor(i)
            }
        })
        arrangeViewsByMode()

        //listening for keyboard status
        view.findViewById<View>(R.id.drawingRoot).viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            view.findViewById<View>(R.id.drawingRoot).getWindowVisibleDisplayFrame(r)
            val screenHeight = view.findViewById<View>(R.id.drawingRoot).rootView.height

            // r.bottom is the position above soft keypad or device button.
            // if keypad is shown, the r.bottom is smaller than that before.
            val keypadHeight = screenHeight - r.bottom
            if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                // keyboard is opened
//                    LogHelper.d("keybb","open");
                isKeyboardOpen = true
                photoEditorView?.isFocusableInTouchMode = false
                photoEditorView?.clearFocus()
            } else {
                // keyboard is closed
//                    LogHelper.d("keybb","closed");
                isKeyboardOpen = false
                photoEditorView?.isFocusableInTouchMode = true
                photoEditorView?.requestFocus()
            }
        }
        photoEditor.setBrushDrawingMode(false)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initLayouts(view: View) {

        // onTouch events return true because if touch passes to lower view it may cause to unintended drawings
        val onTouchListener = OnTouchListener { _, _ -> true }
        drawingTop.setOnTouchListener(onTouchListener)
        mainColorBar.setOnTouchListener(onTouchListener)
        textTop.setOnTouchListener(onTouchListener)
        secondaryColorBar.setOnTouchListener(onTouchListener)
        initialTop.setOnTouchListener(onTouchListener)
        initialBottom.setOnTouchListener(onTouchListener)
        annotationText.apply {
            setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (currentMode == MODE_TEXT) textDone()
                    return@OnEditorActionListener true
                }
                false
            })
            setBackgroundColor(currentSecondaryColor)
            setTextColor(currentMainColor)
        }
    }

    private fun initBtnClicks(view: View) {
        penBtn.setOnClickListener {
            currentMode = MODE_DRAW
            photoEditor.setBrushDrawingMode(true)
            arrangeViewsByMode()
        }
        textBtn.setOnClickListener {
            currentMode = MODE_TEXT
            photoEditor.setBrushDrawingMode(false)
            arrangeViewsByMode()
        }
        cancelBtn.setOnClickListener { cancelFragment() }
        drawingDoneBtn.setOnClickListener {
            currentMode = MODE_INITIAL
            photoEditor.setBrushDrawingMode(false)
            arrangeViewsByMode()
        }
        textDoneBtn.setOnClickListener { textDone() }
        saveBtn.setOnClickListener {
            showLoadingDialog()
            runWithPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, options = QuickPermissionsOptions(permanentDeniedMethod = {
                (activity as? DrawOnFragmentStatus)?.drawingCompleted(false,
                        saveImagePath ?: filePath)
            }, permissionsDeniedMethod = {
                (activity as? DrawOnFragmentStatus)?.drawingCompleted(false,
                        saveImagePath ?: filePath)
            })
            ) {
                photoEditor.saveAsFile(filePath!!, object : OnSaveListener {
                    override fun onSuccess(imagePath: String) {
                        removeCurrentDialog()
                        (activity as? DrawOnFragmentStatus)?.drawingCompleted(true,
                                saveImagePath ?: filePath)
                    }

                    override fun onFailure(exception: Exception) {
                        removeCurrentDialog()
                        (activity as? DrawOnFragmentStatus)?.drawingCompleted(false,
                                saveImagePath ?: filePath)
                    }
                })
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawingSeekbar.thumb.setTint(-0x1)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawingSeekbar.progressDrawable.setTint(-0x1)
        }
        drawingSeekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val size = progress.toFloat() / 8
                //to prevent 0 brush size
                photoEditor.brushSize = size + 5
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        val onClickListener = View.OnClickListener {
            canUndo = photoEditor.undo()
            setUndoButtonsVisibility()
        }
        undoBtn.setOnClickListener(onClickListener)
    }

    private fun setUndoButtonsVisibility() {
        if (canUndo) {
            undoBtn.visibility = View.VISIBLE
        } else {
            undoBtn.visibility = View.GONE
        }
    }

    private fun textDone() {
        currentMode = MODE_INITIAL
        photoEditor.setBrushDrawingMode(false)
        if (editTextToEdit != null) {
            photoEditor.editText(editTextToEdit, annotationText!!.text.toString(), currentMainColor, currentSecondaryColor)
            editTextToEdit!!.visibility = View.VISIBLE
            editTextToEdit = null
        } else photoEditor.addText(annotationText!!.text.toString(), currentMainColor, currentSecondaryColor)
        canUndo = true
        setUndoButtonsVisibility()
        annotationText.setText("")
        if (isKeyboardOpen) hideKeyboard(requireActivity())
        arrangeViewsByMode()
    }

    private fun cancelFragment() {
        showWarningDialog(titleStr = warningString,
                warningStr = imageWillBeLostString,
                positiveButtonStr = okString,
                positiveButtonClick = {
                    (activity as? DrawOnFragmentStatus)?.drawingCancelled(filePath)
                }
        )
    }

    private fun arrangeViewsByMode() {
        if (currentMode == MODE_INITIAL) {
            annotationText!!.visibility = View.GONE
            drawingTop!!.visibility = View.GONE
            mainColorBar!!.visibility = View.GONE
            textTop!!.visibility = View.GONE
            secondaryColorBar!!.visibility = View.GONE
            initialTop!!.visibility = View.VISIBLE
            initialBottom!!.visibility = View.VISIBLE
        }
        if (currentMode == MODE_DRAW) {
            annotationText!!.visibility = View.GONE
            drawingTop!!.visibility = View.VISIBLE
            mainColorBar!!.visibility = View.VISIBLE
            secondaryColorBar!!.visibility = View.GONE
            textTop!!.visibility = View.GONE
            initialTop!!.visibility = View.GONE
            initialBottom!!.visibility = View.GONE
        }
        if (currentMode == MODE_TEXT) {
            annotationText!!.visibility = View.VISIBLE
            drawingTop!!.visibility = View.GONE
            mainColorBar!!.visibility = View.VISIBLE
            textTop!!.visibility = View.VISIBLE
            secondaryColorBar!!.visibility = View.VISIBLE
            initialTop!!.visibility = View.GONE
            initialBottom!!.visibility = View.GONE
        }
    }

    enum class SourceType {
        FILE_PATH, URI, URL
    }

    interface DrawOnFragmentStatus {
        fun drawingCompleted(success: Boolean, path: String?)
        fun drawingCancelled(path: String?)
    }

    companion object {
        //static keys
        var SOURCE_TYPE_KEY = "SOURCE_TYPE_KEY"
        var SOURCE_DATA_KEY = "SOURCE_DATA_KEY"
        var SAVE_IMAGE_PATH_KEY = "SAVE_IMAGE_PATH_KEY"
        var HIDE_SAVE_BTN_KEY = "HIDE_SAVE_BTN_KEY"
        var WARNING_STRING_KEY = "WARNING_STRING_KEY"
        var OK_STRING_KEY = "OK_STRING_KEY"
        var LOADING_STRING_KEY = "LOADING_STRING_KEY"
        var IMAGE_WILL_BE_LOST_STRING_KEY = "IMAGE_WILL_BE_LOST_STRING_KEY"

        //state variables
        private const val MODE_DRAW = 1
        private const val MODE_TEXT = 2
        private const val MODE_INITIAL = 0
    }

    override var currentDialogView: View? = null
}