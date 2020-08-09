package py.alperenbabagil.imageeditfragment.example

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import py.alperenbabagil.imageeditfragmentlib.fragment.fragment.DrawOnFragmentStatus
import py.alperenbabagil.imageeditfragmentlib.fragment.fragment.ImageEditFragment
import java.util.*

class DrawerHolderFragment : Fragment(),DrawOnFragmentStatus {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.drawer_holder_fragment,container,false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //creating fragment
        val imageEditFragment = ImageEditFragment()
        //setting arguments
        imageEditFragment.arguments = Bundle().apply {
            putSerializable(ImageEditFragment.SOURCE_TYPE_KEY, ImageEditFragment.SourceType.URL)
            putString(ImageEditFragment.SOURCE_DATA_KEY, "https://picsum.photos/600/1200")
            putString(ImageEditFragment.SAVE_IMAGE_PATH_KEY,
                    "${activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path}/${UUID.randomUUID()}.jpg"
                    )
        }
        childFragmentManager.beginTransaction().replace(R.id.fragmentContainer, imageEditFragment).commit()
    }

    override fun drawingCompleted(success: Boolean, path: String?) {
        Toast.makeText(requireContext(),"Drawing completed in fragment",Toast.LENGTH_SHORT).show()
        (activity as MainActivity).removeFragment()
    }

    override fun drawingCancelled(path: String?) {
        Toast.makeText(requireContext(),"Drawing cancelled in fragmentç",Toast.LENGTH_SHORT).show()
        (activity as MainActivity).removeFragment()
    }

    companion object{
        fun newInstance(): DrawerHolderFragment{
            return DrawerHolderFragment()
        }
    }
}