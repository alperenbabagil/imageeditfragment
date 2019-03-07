package py.alperenbabagil.imageeditfragmentlib.fragment.helper;

import android.content.Context;
import android.view.View;

public class WHEqualView extends View{

    public WHEqualView(Context context){
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec,int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);

        int width = getMeasuredWidth();
        //setMeasuredDimension(width,width);
    }
}