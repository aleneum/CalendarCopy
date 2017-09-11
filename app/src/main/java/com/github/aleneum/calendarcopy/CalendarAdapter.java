package com.github.aleneum.calendarcopy;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alneuman on 11.09.17.
 */

public class CalendarAdapter extends ArrayAdapter<String>{

    private List<CalendarInfo> mCalendars;

    public CalendarAdapter(Context context, List<CalendarInfo> calendars) {
        super(context, R.layout.support_simple_spinner_dropdown_item);
        mCalendars = calendars;
        List<String> names = new ArrayList<>();
        for (CalendarInfo calendar: calendars) {
            names.add(calendar.getName());
        }
        addAll(names);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        return decorateView(view, position);
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        return decorateView(view, position);
    }

    private View decorateView(TextView view, int position) {
        view.setText(" " + view.getText());
        Drawable circle = ContextCompat.getDrawable(getContext(), R.drawable.circle);
        circle.setColorFilter(mCalendars.get(position).color, PorterDuff.Mode.SRC_IN);
        view.setCompoundDrawablesWithIntrinsicBounds(circle, null, null, null);
        return view;
    }

}
