package shaunhossain.com.rcode;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Collections;
import java.util.List;

import shaunhossain.com.rcode.DB.DBAdapter;
import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;

/**
 * Created by Aris on 07-Apr-15.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private LayoutInflater inflater;
    List<ListViewItem> items = Collections.emptyList();
    int ScreenHeight;
    AssetManager am;
    Context con;
    private int previousPosition = -1;
    GoogleMap mMap;

    public RecyclerViewAdapter(Context context, List<ListViewItem> items, AssetManager am, GoogleMap mMap) {
        inflater = LayoutInflater.from(context);
        this.items = items;
        //this.ScreenHeight = ScreenHeight;
        this.am = am;
        this.con = context;
        this.mMap = mMap;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.card, parent, false);
        MyViewHolder holder = new MyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final ListViewItem current = items.get(position);
        //set Text
        holder.code.setText(current.code);
        //set Subtitle
        holder.address.setText(current.address);


        //if we click the business card
        holder.cardview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(con, "card clicked",
//                        Toast.LENGTH_SHORT).show();

                WGS84Point decrypted = GeoHash.fromGeohashString(current.code.toLowerCase().replaceAll("-","")).getPoint();

                LatLng myLocationAfterHash = new LatLng (decrypted.getLatitude(), decrypted.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocationAfterHash, 18), 3000, null);
            }
        });

        holder.discard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeleteFromDB(current.code, position);
            }
        });

        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sharingIntent = new Intent (Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Generated RCode");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, current.code );
                con.startActivity(Intent.createChooser(sharingIntent, "Share via"));
            }
        });

    }

    private void DeleteFromDB(final String code, final int position) {
        AlertDialog.Builder alert = new AlertDialog.Builder(con);
        alert.setTitle("Delete entry");
        alert.setIcon(R.mipmap.ic_delete_black_48dp);
        alert.setMessage("Are you sure you want to delete this Code?");
        alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                DBAdapter db = new DBAdapter(con);
                db.open();
                boolean test = db.deleteCode(code.toLowerCase());
                db.close();
                items.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, items.size());
                updateMarkers();
            }
        });
        alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alert.show();
    }
    private void updateMarkers(){
        mMap.clear();
        for(int i=0; i< items.size(); i++){
            WGS84Point decrypted = GeoHash.fromGeohashString(items.get(i).code.toLowerCase().replaceAll("-","")).getPoint();

            LatLng myLocationAfterHash = new LatLng (decrypted.getLatitude(), decrypted.getLongitude());
            mMap.addMarker(new MarkerOptions ().position(myLocationAfterHash).title("RCode: " + items.get(i).code).snippet(items.get(i).code));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView code;
        TextView address;
        Button discard;
        Button share;
        CardView cardview;

        public MyViewHolder(View itemView) {
            super(itemView);

            code = (TextView) itemView.findViewById(R.id.card_code);
            address = (TextView) itemView.findViewById(R.id.card_address);
            discard = (Button) itemView.findViewById(R.id.deleteButton);
            share = (Button) itemView.findViewById(R.id.shareButton);
            cardview = (CardView) itemView.findViewById(R.id.card_view);

            Typeface tfa = Typeface.createFromAsset(am, "fonts/Roboto-Black.ttf");
            code.setTypeface(tfa);
            address.setTypeface(tfa);

        }
    }

    public void removeAt(int position) {
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size());
    }
}

