package com.github.aleneum.calendarcopy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alneuman on 11.09.17.
 */

public class EventAdapter extends ArrayAdapter<String>{

    private CalendarService service;

    public EventAdapter(Context context, CalendarService aService) {
        super(context, android.R.layout.simple_list_item_multiple_choice);
        service = aService;
        List<String> names = new ArrayList<>();
        for (EventSummary event: aService.events) {
            names.add(event.toString());
        }
        addAll(names);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CheckedTextView view = (CheckedTextView) super.getView(position, convertView, parent);
        EventSummary summary = service.events.get(position);
        List<Long> childrenIds = new ArrayList<>(summary.childrenCalendarIds);
        if (childrenIds.contains(service.targetCalendarId)) {
            ((ListView)parent).setItemChecked(position, true);
            //childrenIds.remove(service.targetCalendarId);
        }

        List<Drawable> children = new ArrayList<>();
        for (long child: childrenIds) {
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
