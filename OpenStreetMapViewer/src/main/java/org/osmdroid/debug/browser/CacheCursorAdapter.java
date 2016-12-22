package org.osmdroid.debug.browser;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.osmdroid.debug.util.HumanTime;

import org.osmdroid.R;
import org.osmdroid.debug.util.FileDateUtil;
import org.osmdroid.debug.model.MapTileExt;
import org.osmdroid.debug.model.SqlTileWriterExt;
import org.osmdroid.tileprovider.modules.DatabaseFileArchive;
import org.osmdroid.tileprovider.modules.SqlTileWriter;

import java.util.Date;

/**
 * created on 12/20/2016.
 *
 * @author Alex O'Ree
 */

public class CacheCursorAdapter extends ArrayAdapter {

    SqlTileWriterExt cursor;

    public CacheCursorAdapter(Context context, int pageSize, SqlTileWriterExt cursor) {
        super(context, R.layout.item_cache);
        this.cursor=cursor;
    }


    @Override
    public int getCount() {
        return (int) cursor.getRowCount(null);
    }

    @Override
    public Object getItem(int id) {
        Cursor select = cursor.select(1, id);
        if (select.moveToNext()) {
            MapTileExt tile = new MapTileExt(0, 0, 0);
            tile.key = select.getLong(select.getColumnIndex(DatabaseFileArchive.COLUMN_KEY));
            tile.source = select.getString(select.getColumnIndex(DatabaseFileArchive.COLUMN_PROVIDER));
            Long expires = select.getLong(select.getColumnIndex(SqlTileWriter.COLUMN_EXPIRES));
            if (expires != null) {
                tile.setExpires(new Date(expires));
            }
            return tile;
        }
        return null;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_cache, parent, false);
        }
        MapTileExt p = (MapTileExt) getItem(position);
        if (p!=null) {
            // Find fields to populate in inflated template
            TextView source = (TextView) convertView.findViewById(R.id.tvSource);
            TextView key = (TextView) convertView.findViewById(R.id.tvDbKey);
            TextView expires = (TextView) convertView.findViewById(R.id.tvExpires);

            source.setText(p.source);
            key.setText(p.key + "");
            try {
                long time = p.getExpires().getTime();

                //time should be in the future
                String durationUtilExpires = FileDateUtil.getModifiedDate(time);
                if (time > System.currentTimeMillis()) {
                    //has not expired yet
                    durationUtilExpires += "\nValid for " + HumanTime.approximately(time - System.currentTimeMillis());
                } else {
                    //expired already
                    durationUtilExpires += "\nExpired at " + HumanTime.approximately(System.currentTimeMillis() - time);
                }
                expires.setText(durationUtilExpires);

            } catch (Exception ex) {
                expires.setText("null!");
            }
        }



        return convertView;
    }


}