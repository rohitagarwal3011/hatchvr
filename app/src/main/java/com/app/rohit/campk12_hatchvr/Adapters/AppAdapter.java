package com.app.rohit.campk12_hatchvr.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.rohit.campk12_hatchvr.Activities.HomeActivity;
import com.app.rohit.campk12_hatchvr.Activities.MainActivity;
import com.app.rohit.campk12_hatchvr.Activities.ScanActivity;
import com.app.rohit.campk12_hatchvr.R;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.app.rohit.campk12_hatchvr.Models.Apps;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by rohit on 16/1/18.
 */

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.MyViewHolder> {

    private List<Apps> data;
    private Context context;

    OkHttpClient client = new OkHttpClient();
    List<Bitmap> decodedByte = new ArrayList<>();

    public class MyViewHolder extends RecyclerView.ViewHolder {


        private ImageView mIconApp;
        private TextView mNameApp;
        private TextView mDeveloperApp;
        private TextView likes_count;

        public MyViewHolder(View view) {
            super(view);
            mIconApp = (ImageView) view.findViewById(R.id.app_icon);
            mNameApp = (TextView) view.findViewById(R.id.app_name);
            mDeveloperApp = (TextView) view.findViewById(R.id.app_developer);
            likes_count=(TextView)view.findViewById(R.id.likes_count);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra("code", "https://hatchvr.io/"+data.get(getAdapterPosition()).getUsername()+"/"+data.get(getAdapterPosition()).getUniqId()+"?fullscreen");
                    context.startActivity(intent);
                }
            });

            likes_count.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((HomeActivity)context).like_app(data.get(getAdapterPosition()),getAdapterPosition());
                }
            });


        }

    }

    public AppAdapter(Context context, List<Apps> data) {
        this.context = context;
        this.data = data;

    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.app_item_view, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        holder.mDeveloperApp.setText("by "+data.get(position).getUsername());
        holder.mNameApp.setText(data.get(position).getUniqId());
        holder.likes_count.setText(String.valueOf(data.get(position).getLikeValue()));
        if(data.get(position).getLiked())
        {
            Log.d("App : "+data.get(position).getUniqId(),"is liked");
            holder.likes_count.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.heart_green,0);
        }

        else
            holder.likes_count.setCompoundDrawablesWithIntrinsicBounds( 0, 0, R.drawable.ic_heart,0);
        if(!data.get(position).getDownloaded()) {
            okhttp3.Request request = new okhttp3.Request.Builder()
                    .url("https://s3.amazonaws.com/my-trybucket/" + data.get(position).getThumbLink())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, okhttp3.Response response) throws IOException {
                    //

                    BufferedReader br = new BufferedReader(new InputStreamReader(response.body().byteStream()));
                    String s = br.readLine();
                    // response.body().byteStream(); // Read the data from the stream
                    if (s.charAt(0) == 'd') {
                        Log.d("Image uploaded for : ", data.get(position).getUniqId());
                        s = s.substring(s.indexOf(",") + 1);
                        Log.d("Base64 string ", s);
                        byte[] decodedString = Base64.decode(s, Base64.DEFAULT);
                        //  Log.d("Base 64 image : ",response.body().string().replace("data:image/png;base64,",""));
                        data.get(position).setImage(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
                        data.get(position).setDownloaded(true);
//                    notifyDataSetChanged();
                        //apps.add(app_data);

                    } else {
                        data.get(position).setImage(null);
                        data.get(position).setDownloaded(true);
                        //Picasso.with(context).load("http://ec2-18-221-211-180.us-east-2.compute.amazonaws.com:8080/sampleProject.jpg").into(holder.mIconApp);

                        // apps.add(app_data);
                    }
                }

            });
        }
        if(data.get(position).getImage()==null)
        Picasso.with(context).load("https://hatchvr.io/sampleProject.jpg").into(holder.mIconApp);
        else
            holder.mIconApp.setImageBitmap(data.get(position).getImage());
//        if(data.get(position).getImage()!=null)
//            holder.mIconApp.setImageBitmap(data.get(position).getImage());
//        else
//            Picasso.with(context).load("http://ec2-18-221-211-180.us-east-2.compute.amazonaws.com:8080/sampleProject.jpg").into(holder.mIconApp);


    }



    @Override
    public int getItemCount() {
        return data.size();
    }
}
