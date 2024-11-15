package com.example.plantpoints;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

//        arraylist.add(new AreasView("nazwa", "opis", "dystans")
public class AreasViewAdapter extends ArrayAdapter<AreasView> {
    public AreasViewAdapter(@NonNull Context context, ArrayList<AreasView> arrayList){

        super(context, 0, arrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        View currentItemView = convertView;

        if (currentItemView == null){
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.custom_list_view, parent, false);
        }

        AreasView currentNumberPosition = getItem(position);

        TextView name = currentItemView.findViewById(R.id.name);
        name.setText(currentNumberPosition.getName());

        TextView desc = currentItemView.findViewById(R.id.desc);
        desc.setText(currentNumberPosition.getDesc());

        TextView dist = currentItemView.findViewById(R.id.dist);
        dist.setText(currentNumberPosition.getDist());

        return currentItemView;
    }
}
