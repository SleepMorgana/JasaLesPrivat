package developer.aulia.jasalesprivat.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;


public abstract class ListViewItem<T>{
    protected Context context;
    protected T element;
    //protected ImageLoader imageLoader;
    protected int resourceLayout;

//    public ListViewItem(Context context, T element, ImageLoader imageLoader, int layout){
//        this.context=context;
//        this.element=element;
//        this.imageLoader=imageLoader;
//        resourceLayout=layout;
//    }

    public ListViewItem(Context context, T element, int layout){
        this.context=context;
        this.element=element;
        resourceLayout=layout;
    }

    public View getView(){
        LayoutInflater inflater = (LayoutInflater) context.getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(resourceLayout,null);
    }
}
