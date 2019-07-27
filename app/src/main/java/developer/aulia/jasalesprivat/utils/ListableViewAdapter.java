package developer.aulia.jasalesprivat.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;


public abstract class ListableViewAdapter<T> extends BaseAdapter {

    protected Context context;
    protected List<T> data;
    //private ImageLoader imageLoader;
    protected ListViewItem viewItem;


    public ListableViewAdapter(Context a, List<T> d){
        context=a;
        data=d;
//        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
//                .build();
//        imageLoader = ImageLoader.getInstance();
//        imageLoader.init(config);
    }

    @Override
    public int getCount() {
        if(data.size()<=0)
            return 0;
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        //MUST BE IMPLEMENTED
        return null;
    }
}
