package diecast.fozil.com.diecast;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.apache.poi.ss.formula.functions.T;

import java.util.ArrayList;
import java.util.List;

import databases.Brand;
import databases.Diecast;

/**
 * Created by eduardo.benitez on 29/11/2017.
 */

public class DiecastArrayAdapter extends android.widget.ArrayAdapter<Diecast> {

    private final LayoutInflater mInflater;
    private final Context mContext;
    private final List<Diecast> items;
    private final int mResource;

    public <T extends  Diecast> DiecastArrayAdapter(@NonNull Context context, int resource, @NonNull List<T> diecastList) {
        super(context, resource, (List<Diecast>) diecastList);

        mContext = context;
        mInflater = LayoutInflater.from(context);
        mResource = resource;

        items = (List<Diecast>) diecastList;

    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createItemView(position, convertView, parent);
    }

    private View createItemView(int position, View convertView, ViewGroup parent){
        final View view = mInflater.inflate(mResource, parent, false);

        TextView name = (TextView) view.findViewById(R.id.spinner_text);

        Diecast diecast = items.get(position);

        name.setText(diecast.getName());

        return view;
    }
}
