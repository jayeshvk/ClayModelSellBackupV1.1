package com.appdev.jayes.claymodelsell;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.appdev.jayes.claymodelsell.R;

import java.util.ArrayList;
import java.util.List;


public class LocationAdapter extends ArrayAdapter<Location> {

    private Context mContext;
    private List<Location> locationList = new ArrayList<>();

    public LocationAdapter(@NonNull Context context, ArrayList<Location> list) {
        super(context, 0, list);
        mContext = context;
        locationList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.location_list_item, parent, false);

        Location currentLocation = locationList.get(position);


        TextView name = (TextView) listItem.findViewById(R.id.guidText);
        name.setText(currentLocation.getGuid());

        TextView release = (TextView) listItem.findViewById(R.id.locationText);
        release.setText(currentLocation.getLocationName());

        return listItem;
    }
}