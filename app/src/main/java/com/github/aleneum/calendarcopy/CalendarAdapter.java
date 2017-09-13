package com.github.aleneum.calendarcopy;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.github.aleneum.calendarcopy.models.CalendarInfo;

import java.util.ArrayList;
import java.util.List;

class CalendarAdapter extends ArrayAdapter<String>{

    private final List<CalendarInfo> mCalendars;

    public CalendarAdapter(Context context, List<CalendarInfo> calendars) {
        super(context, R.layout.calendar_spinner_entry);
        mCalendars = calendars;
        List<String> names = new ArrayList<>();
        for (CalendarInfo calendar: calendars) {
            names.add(calendar.getName());
        }
        addAll(names);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getView(position, convertView, parent);
        return decorateView(view, position);
    }

    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        TextView view = (TextView) super.getDropDownView(position, convertView, parent);
        return decorateView(view, position);
    }

    private View decorateView(TextView view, int position) {
        Drawable circle = ContextCompat.getDrawable(getContext(), R.drawable.circle);
        circle.setColorFilter(mCalendars.get(position).getColor(), PorterDuff.Mode.SRC_IN);
        view.setCompoundDrawablesWithIntrinsicBounds(circle, null, null, null);
        return view;
    }

}
