package com.github.aleneum.calendarcopy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.github.aleneum.calendarcopy.models.EventSummary;

import java.util.ArrayList;
import java.util.List;


class EventAdapter extends ArrayAdapter<String>{

    private static final String DEBUG_TAG = "ccopy.EventAdapter";
    private static final int BASE_LAYOUT = android.R.layout.simple_list_item_multiple_choice;
    private final CalendarService service;

    EventAdapter(Context context, CalendarService aService) {
        super(context, BASE_LAYOUT);
        service = aService;
        List<String> names = new ArrayList<>();
        for (EventSummary event: aService.events) {
            names.add(event.toString());
        }
        addAll(names);
    }

    @Override
    public boolean isEnabled(int position) {
        EventSummary summary = service.events.get(position);
        List<Long> childrenIds = new ArrayList<>(summary.childrenCalendarIds);
        return ! childrenIds.contains(service.targetCalendarId);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        CheckedTextView view = (CheckedTextView) super.getView(position, convertView, parent);
        EventSummary summary = service.events.get(position);
        List<Long> childrenIds = new ArrayList<>(summary.childrenCalendarIds);
        if (childrenIds.contains(service.targetCalendarId)) {
            ((ListView)parent).setItemChecked(position, true);
            view.setEnabled(false);
            childrenIds.remove(service.targetCalendarId);
        } else {
            view.setEnabled(true);
        }

        List<Drawable> children = new ArrayList<>();

        if (summary.parentCalendarId > -1) {
            Log.d(DEBUG_TAG, "Add parent circle for " + summary.parentId);
            Drawable circle = ContextCompat.getDrawable(getContext(), R.drawable.outlined_circle);
            circle.setColorFilter(service.getCalendarById(summary.parentCalendarId).getColor(),
                    PorterDuff.Mode.MULTIPLY);
            children.add(circle);
        }

        for (long child: childrenIds) {
            Log.d(DEBUG_TAG, "Add child circle for " + child);
            Drawable circle = ContextCompat.getDrawable(getContext(), R.drawable.circle);
            circle.setColorFilter(service.getCalendarById(child).getColor(), PorterDuff.Mode.SRC_IN);
            children.add(circle);
        }

        if (children.size() > 0) {
            Bitmap big = Bitmap.createBitmap(children.get(0).getIntrinsicWidth() * children.size(),
                    children.get(0).getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            int idx = 0;
            Canvas canvas = new Canvas(big);
            for (Drawable child: children) {
                child.setBounds(idx * child.getIntrinsicWidth(), 0,
                        (idx + 1 ) *  child.getIntrinsicWidth(), child.getIntrinsicHeight());
                child.draw(canvas);
                idx++;
            }

            view.setCompoundDrawablesWithIntrinsicBounds(null, null, new BitmapDrawable(
                    service.activity.getResources(), big), null);
        } else {
            view.setCompoundDrawables(null, null, null, null);
        }

        return view;
    }
}
