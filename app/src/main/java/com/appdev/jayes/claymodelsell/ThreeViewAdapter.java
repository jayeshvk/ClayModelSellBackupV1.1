package com.appdev.jayes.claymodelsell;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class ThreeViewAdapter extends ArrayAdapter<ClayModel> {

    private Context mContext;
    private List<ClayModel> modelsList = new ArrayList<>();

    public ThreeViewAdapter(@NonNull Context context, ArrayList<ClayModel> list) {
        super(context, 0, list);
        mContext = context;
        modelsList = list;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null)
            listItem = LayoutInflater.from(mContext).inflate(R.layout.claymodel_list_item, parent, false);

        ClayModel currentModel = modelsList.get(position);

        TextView guid = (TextView) listItem.findViewById(R.id.guidText);
        guid.setText(currentModel.getKey());

        TextView modelName = (TextView) listItem.findViewById(R.id.modelText);
        modelName.setText(currentModel.getModelName());

        TextView modelPrice = (TextView) listItem.findViewById(R.id.priceText);
        modelPrice.setText(currentModel.getModelPrice());

        return listItem;
    }
}