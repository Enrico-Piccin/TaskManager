package adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.taskmanager.R;

import java.util.ArrayList;

import model.ColorChoiceItem;

public class AlertListAdapter extends BaseAdapter {

    private ArrayList<ColorChoiceItem> mData;
    private Context mContext;
    private LayoutInflater inflater;

    public AlertListAdapter(ArrayList<ColorChoiceItem> data, Context context) {
        mData = data;
        mContext = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.color_choice_item, parent, false);
        }

        ImageView colorValue = convertView.findViewById(R.id.imgProjectColor);
        TextView colorName = convertView.findViewById(R.id.textViewColorName);
        colorValue.setColorFilter(mData.get(position).getColorValue(), PorterDuff.Mode.SRC_ATOP);
        colorName.setText(mData.get(position).getColorName());

        return convertView;
    }
}