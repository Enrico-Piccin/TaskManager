package adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.example.taskmanager.R;

import java.util.ArrayList;

import helper.EnglishNumberWord;
import model.Project;

public class ManageProjectAdapter extends BaseAdapter {

    private ArrayList<Project> mData;
    private Context mContext;
    private LayoutInflater inflater;

    public ManageProjectAdapter(ArrayList<Project> data, Context context) {
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
            convertView = mInflater.inflate(R.layout.project_list_item, parent, false);
        }

        ImageView projectColor = convertView.findViewById(R.id.projectColor);
        TextView projectName = convertView.findViewById(R.id.projectName);
        ImageView favorite = convertView.findViewById(R.id.favorite);

        projectColor.setColorFilter(mData.get(position).getColore(), PorterDuff.Mode.SRC_ATOP);
        projectName.setText(mData.get(position).getNome());

        if(mData.get(position).isFavorite()) {
            favorite.setImageResource(mContext.getResources().getIdentifier("heart_full", "drawable", mContext.getPackageName()));
            favorite.setColorFilter(mContext.getResources().getColor(R.color.red), PorterDuff.Mode.SRC_ATOP);
        } else {
            favorite.setImageResource(mContext.getResources().getIdentifier("no_heart", "drawable", mContext.getPackageName()));
            favorite.setColorFilter(mContext.getResources().getColor(R.color.teal_700), PorterDuff.Mode.SRC_ATOP);
        }
        return convertView;
    }
}