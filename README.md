# Image Edit Fragment
A fragment to put drawings and text to images like social media apps

  - Draw-free on image with arrangable color and thickness 
  - Put text on image. Edit text background color, text content and text color
  - Move text anywhere you want via dragging.
  - Prevent edited content loss with interrupting back button
  - Ready to use with couple of lines code.

It is more like wrapper to [@burhanrashid52]'s [PhotoEditor] library. It ads changing text background color capability and UI within a fragment to PhotoEditor.

It is developed for [Telepath] project of [Virasoft] and made public to contribute open-source. UI mostly designed by [@safakurt]

### Screenshots
![Initial mode](https://github.com/alperenbabagil/imageeditfragment/blob/master/screenshots/tutor1.PNG)

![Drawing mode](https://github.com/alperenbabagil/imageeditfragment/blob/master/screenshots/tutor2.PNG)

### Installation

### Usage

  To use ImageEditFragment you must implement ImageEditFragment.DrawOnFragmentStatus interface and give neccesary parameters before put the fragment to a view. Example code is given in an Activity:
  
Implementing interface

```java
public class MainActivity extends AppCompatActivity implements ImageEditFragment.DrawOnFragmentStatus{

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

}
```

Creating fragment and attaching to a view
  
```java
Bundle bundle = new Bundle();
//setting data source type
bundle.putSerializable(ImageEditFragment.SOURCE_TYPE_KEY,ImageEdit
//setting image path
bundle.putString(ImageEditFragment.SOURCE_DATA_KEY,PATH);
//creating fragment
ImageEditFragment imageEditFragment = new ImageEditFragment();
//setting arguments
imageEditFragment.setArguments(bundle);
//putting fragment
getSupportFragmentManager().beginTransaction()
        .add(R.id.fragmentContainer,imageEditFragment).commit();
```

You can put texts visible to users to bundle like above code with these keys:

```java

SOURCE_TYPE_KEY // look at the ImageEditFragment.SourceType enum
SOURCE_DATA_KEY // to put data source
WARNING_STRING_KEY // title of warning popup 
OK_STRING_KEY = // text at the "OK" button of info popup
LOADING_STRING_KEY // title of loading popup
IMAGE_WILL_BE_LOST_STRING_KEY // warning text of warning popup which is appeared when pressed to back button

```






[@burhanrashid52]: <https://github.com/burhanrashid52>
[PhotoEditor]: <https://github.com/burhanrashid52/PhotoEditor>
[Telepath]: <https://www.virasoft.com.tr/en/cozumlerimiz/telepath/>
[Virasoft]: <https://www.virasoft.com.tr/en/>
[@safakurt]: <https://github.com/safakurt>
