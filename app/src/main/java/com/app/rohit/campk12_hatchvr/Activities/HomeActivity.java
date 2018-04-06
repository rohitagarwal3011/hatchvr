package com.app.rohit.campk12_hatchvr.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.rohit.campk12_hatchvr.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.app.rohit.campk12_hatchvr.Adapters.AppAdapter;
import com.app.rohit.campk12_hatchvr.Models.Apps;
import com.app.rohit.campk12_hatchvr.Utils.EndlessRecyclerViewScrollListener;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;

import static com.app.rohit.campk12_hatchvr.Utils.Constants.PAGE_SIZE;


public class HomeActivity extends AppCompatActivity  {

    RecyclerView recyclerView;
    AppAdapter appAdapter;
    List<Apps> apps = new ArrayList<>();
    private EndlessRecyclerViewScrollListener scrollListener;
    GridLayoutManager manager;

    private boolean isLastPage = false;
    private int currentPage = 1;
    private String current_search="all";
    private String current_category="project";
    private String current_sort ="popular";
    private TextView noapptext;

    private boolean isLoading = false;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        recyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        noapptext = (TextView)findViewById(R.id.noapptext);
        fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this,ScanActivity.class);
                startActivity(intent);
            }
        });
        get_apps(currentPage,current_search,current_category, current_sort);
        // setRecyclerView();
    }

    private void setRecyclerView()
    {
//        apps = new ArrayList<>();
        Log.d("Home Activity","Set Recycler View");
        appAdapter = new AppAdapter(HomeActivity.this,apps );
        manager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(appAdapter);
//        scrollListener = new EndlessRecyclerViewScrollListener(manager) {
//            @Override
//            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
//                // Triggered only when new data needs to be appended to the list
//                // Add whatever code is needed to append new items to the bottom of the list
//                Log.d("Next page : ", String.valueOf(page));
//                get_apps(page);
//            }
//        };
        // Adds the scroll listener to RecyclerView
        recyclerView.addOnScrollListener(recyclerViewOnScrollListener);

//       get_apps(1);

    }

    private void get_apps(final int page,final String search,final String category,final String sort)
    {
        currentPage=page;
        isLoading=true;
//        List<Map<String,String>> listMap =  new ArrayList<Map<String, String>>();
//        Map<String, String>  params = new HashMap<>();
//        // the POST parameters:
//        params.put("s", "all");
//        params.put("page", String.valueOf(page));
//        listMap.add(params);
//        Log.d("HomeActivity","Param : "+listMap.toString());
//        params.put("network", "tutsplus");
        String url = "https://hatchvr.io/pagePull";


            final SweetAlertDialog progressBar = new SweetAlertDialog(HomeActivity.this, SweetAlertDialog.PROGRESS_TYPE);
            progressBar.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            progressBar.setTitleText("Loading");
            progressBar.setCancelable(false);
        if(currentPage==1) {
            progressBar.show();


        }

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(currentPage==1)
                            progressBar.dismiss();
                        Log.d("HomeScreen",response.toString());
                        try {

                            set_app_list(new JSONObject(response));

                        } catch (JSONException e) {
                            recyclerView.setVisibility(View.GONE);
                            noapptext.setVisibility(View.VISIBLE);
                            e.printStackTrace();
                        }

                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(currentPage==1)
                            progressBar.dismiss();
                        error.printStackTrace();
                    }
                }

        ){

            @Override

            protected Map<String, String> getParams()

            {

                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("s", search);
                params.put("page", String.valueOf(page));
                params.put("category",category);
                params.put("sort",sort);

                Log.d("HomeActivity",params.toString());

                return params;

            }

        };

        Log.d("HomeActivity","Post Request : "+postRequest.toString());
        Volley.newRequestQueue(this).add(postRequest);
    }

    private void set_app_list(JSONObject response) throws JSONException {

        JSONArray newResponse = response.getJSONArray("result2");
        String ip= response.getString("ip");

        recyclerView.setVisibility(View.VISIBLE);
        noapptext.setVisibility(View.GONE);
        if(currentPage==1)
            apps = new ArrayList<>();

        if(newResponse.length()<PAGE_SIZE)
            isLastPage=true;
        else
            isLastPage=false;
        for(int i =0;(apps.size()-(PAGE_SIZE*(currentPage-1)))<newResponse.length();i++)
        {
            if(i<newResponse.length()) {

                JSONObject data = new JSONObject(newResponse.get(i).toString());
                Log.d("Value of i : ",String.valueOf(i)+"  App : "+data.getString("uniqId"));
                final Apps app_data = new Apps();
                try {
                    app_data.setLikeValue(data.getInt("likeValue"));
                  //  app_data.setLikes(data.getJSONArray("likes"));
                }
                catch (JSONException e)
                {
                    app_data.setLikeValue(0);
                }
                try {
                    app_data.setViews(data.getInt("views"));
                }
                catch (JSONException e)
                {
                    app_data.setViews(0);
                }
                app_data.setThumbLink(data.getString("thumbLink"));
                app_data.setUniqId(data.getString("uniqId"));
                app_data.setUsername(data.getString("username"));
                app_data.setImage(null);
                app_data.setDownloaded(false);

                app_data.setLiked(false);
                for(int count =0;count<data.getJSONArray("likes").length();count++)
                {
                    if(ip.equalsIgnoreCase(data.getJSONArray("likes").getString(count)))
                    {
                        app_data.setLiked(true);
                        break;

                    }
                }



                apps.add(app_data);


//                String url = "https://s3.amazonaws.com/my-trybucket/" + data.getString("thumbLink");
//
//// Request a string response
//                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
//                        new Response.Listener<String>() {
//                            @Override
//                            public void onResponse(String response) {
//
//                                // Result handling
//
//                                if (response!= null) {
//                            Log.d("Image uploaded for : ", app_data.getUniqId());
//                            byte[] decodedString = Base64.decode(response, Base64.DEFAULT);
//                            Log.d("Base 64 image : ",response);
//                            app_data.setImage(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
//
//                            apps.add(app_data);
//
//                        } else {
//                            app_data.setImage(null);
//                            apps.add(app_data);
//                        }
//                            }
//                        }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//
//                        // Error handling
//                        error.printStackTrace();
//
//                    }
//                });

// Add the request to the queue
//                Volley.newRequestQueue(this).add(stringRequest);

//                OkHttpClient client = new OkHttpClient();
//
//                okhttp3.Request request = new okhttp3.Request.Builder()
//                        .url("https://s3.amazonaws.com/my-trybucket/" + data.getString("thumbLink"))
//                        .build();
//
//                client.newCall(request).enqueue(new Callback() {
//                    @Override
//                    public void onFailure(Call call, IOException e) {
//
//                    }
//
//                    @Override
//                    public void onResponse(Call call, okhttp3.Response response) throws IOException {
//                        //
//
//                        BufferedReader br=new BufferedReader(new InputStreamReader(response.body().byteStream()));
//                        String s=br.readLine();
//                        // response.body().byteStream(); // Read the data from the stream
//                        if (s.charAt(0)=='d') {
//                            Log.d("Image uploaded for : ", app_data.getUniqId());
//                           s= s.substring(s.indexOf(",")  + 1);
//                            Log.d("Base64 string ",s);
//                            byte[] decodedString = Base64.decode(s, Base64.DEFAULT);
//                          //  Log.d("Base 64 image : ",response.body().string().replace("data:image/png;base64,",""));
//                            app_data.setImage(BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
//
//                            apps.add(app_data);
//
//                        } else {
//                            app_data.setImage(null);
//                            apps.add(app_data);
//                        }
//                    }
//
//                });


            }


        }

//        appAdapter.notifyItemRangeInserted(appAdapter.getItemCount(), apps.size() - 1);


            isLoading=false;

        if(currentPage==1)
        {
            setRecyclerView();
        }
        else {
            appAdapter.notifyDataSetChanged();
        }


    }


    private RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            int visibleItemCount = manager.getChildCount();
            int totalItemCount = manager.getItemCount();
            int firstVisibleItemPosition = manager.findFirstVisibleItemPosition();

            if (!isLoading && !isLastPage) {
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= PAGE_SIZE) {
                    loadMoreItems();
                }
            }
        }
    };

    private void loadMoreItems() {
        isLoading = true;

        currentPage += 1;
        Log.d("Current Page : ", String.valueOf(currentPage));

        get_apps(currentPage,current_search,current_category, current_sort);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.action_filter :
                show_filter_dialog();

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void show_filter_dialog()
    {
        final Dialog dialog  = new Dialog(this);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_filter);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        final EditText search = (EditText)dialog.findViewById(R.id.search);
        final Spinner filter = (Spinner)dialog.findViewById(R.id.filter);
        final Spinner sort = (Spinner) dialog.findViewById(R.id.sort);
        Button go = (Button) dialog.findViewById(R.id.go);


        final String[] sort_types = getResources().getStringArray(R.array.sort);
        ArrayAdapter<String> status_adapter = new ArrayAdapter<String>(HomeActivity.this,
                R.layout.support_simple_spinner_dropdown_item,
                sort_types);
        sort.setAdapter(status_adapter);

        final String[] filter_types = getResources().getStringArray(R.array.filter);
        ArrayAdapter<String> filter_adapter = new ArrayAdapter<String>(HomeActivity.this,
                R.layout.support_simple_spinner_dropdown_item,
                filter_types);
        filter.setAdapter(filter_adapter);

        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPage=1;
                current_search = search.getText().toString().trim();
                current_category= filter_types[filter.getSelectedItemPosition()].toLowerCase();
                current_sort =sort_types[sort.getSelectedItemPosition()].toLowerCase();

                get_apps(currentPage,current_search,current_category, current_sort);
                dialog.dismiss();

            }
        });

//        sort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
//
//        filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        dialog.show();
    }


    public void like_app(final Apps app , final int position)
    {
        String url = "https://hatchvr.io/likes";



        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        Log.d("HomeActivity  : ","Liked app "+app.getUniqId()+" Response : "+response.toString());
                        if(response.toString().equalsIgnoreCase("1"))
                        {
                            Toast.makeText(HomeActivity.this,"Liked",Toast.LENGTH_SHORT).show();
                            apps.get(position).setLiked(true);
                            appAdapter.notifyDataSetChanged();
                        }
                        else
                        {
                            Toast.makeText(HomeActivity.this,"Already Liked by you",Toast.LENGTH_SHORT).show();
                        }


                    }

                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                }

        ){

            @Override

            protected Map<String, String> getParams()

            {

                Map<String, String>  params = new HashMap<>();
                // the POST parameters:
                params.put("username", app.getUsername());
                params.put("projectId", app.getUniqId());

                Log.d("HomeActivity",params.toString());

                return params;

            }

        };

        Log.d("HomeActivity","Post Request : "+postRequest.toString());
        Volley.newRequestQueue(this).add(postRequest);

    }

}


