package pt.ua.ieeta.healthintegration;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.HashMap;
import com.google.gson.Gson;

public class CustomAdapter extends BaseAdapter{
    String [] result;
    Context context;
    int [] imageId;
    String [] time;
    String [] date;
    Globals g = Globals.getInstance();


    Gson gson = new Gson();
    private static LayoutInflater inflater=null;
    public CustomAdapter(MainActivity mainActivity, String[] prgmNameList, int[] prgmImages, String [] timeList, String [] dateList) {
        // TODO Auto-generated constructor stub
        result=prgmNameList;
        time=timeList;
        date=dateList;
        context=mainActivity;
        imageId=prgmImages;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }



    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return result.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView tv;
        ImageView img;
        TextView tv2;
        TextView tv3;

    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        final View rowView;
        rowView = inflater.inflate(R.layout.list_entry, null);
        holder.tv=(TextView) rowView.findViewById(R.id.textView1);
        holder.tv2=(TextView) rowView.findViewById(R.id.textView2);
        holder.tv3=(TextView) rowView.findViewById(R.id.textView3);

        holder.img=(ImageView) rowView.findViewById(R.id.imageView1);
        holder.tv.setText("Sessão " + result[position]);
        holder.tv2.setText(time[position]);
        holder.tv3.setText(date[position]);

        holder.img.setImageResource(imageId[position]);
        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(context, "You Clicked "+result[position], Toast.LENGTH_LONG).show();
                g.setCurrentSessionSelected(Integer.parseInt(result[position]));
                Intent intent = new Intent(context, Info_Session.class);
                context.startActivity(intent);
            }
        });
        return rowView;
    }

}