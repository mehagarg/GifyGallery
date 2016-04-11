package mehagarg.android.gifygallery;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v7.widget.SearchView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mehagarg.android.gifygallery.model.GiffyDataObject;
import mehagarg.android.gifygallery.search.QueryPreferences;

public class GiffyGalleryFragment extends Fragment {
    public static final String TAG = GiffyGalleryFragment.class.getSimpleName();
    protected String currentSearchTerm = null;
    private int lastThreadId = 0; // to update result only for the latest starting thread

    private RecyclerView mGiffyRecyclerView;
    //    private GiffyDataObject giffyDataObject;
    private List<GiffyDataObject> mItems = new ArrayList<>();

    private RecyclerView.LayoutManager mLayoutManager;

    public GiffyGalleryFragment() {
        // Required empty public constructor
    }

    public static GiffyGalleryFragment newInstance() {
        return new GiffyGalleryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        getTrendingGiffy(TRENDING_METHOD, null);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_fragment_giffy_gallery, menu);



        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                QueryPreferences.setStoredQuery(getActivity(), query);
                getTrendingGiffy(SEARCH_METHOD, QueryPreferences.getStoredQuery(getActivity()));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear:
                QueryPreferences.setStoredQuery(getActivity(), null);
                getTrendingGiffy(SEARCH_METHOD, QueryPreferences.getStoredQuery(getActivity()));

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_giffy_gallery, container, false);
        mGiffyRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_giffy_gallery_recycler_view);
        mLayoutManager = new GridLayoutManager(getActivity(), 2);
        mGiffyRecyclerView.setLayoutManager(mLayoutManager);
        setupAdapter();
        return view;
    }


    private void setupAdapter() {
        if (isAdded()) {
            mGiffyRecyclerView.setAdapter(new GiffyAdapter(mItems));
        }
    }

    private class GiffyHolder extends RecyclerView.ViewHolder {
        private pl.droidsonroids.gif.GifImageView mItemImageView;

        public GiffyHolder(View itemView) {
            super(itemView);
            mItemImageView = (pl.droidsonroids.gif.GifImageView) itemView.findViewById(R.id.fragment_photo_gallery_image_view);
        }
    }

    private class GiffyAdapter extends RecyclerView.Adapter<GiffyHolder> {
        private List<GiffyDataObject> mGalleryItems;

        public GiffyAdapter(List<GiffyDataObject> mGalleryItems) {
            this.mGalleryItems = mGalleryItems;
        }

        @Override
        public GiffyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.gallery_item, parent, false);
            return new GiffyHolder(view);
        }

        @Override
        public void onBindViewHolder(final GiffyHolder holder, final int position) {
            Log.d(TAG, mGalleryItems.get(position).getSlug());
            Log.d(TAG, mGalleryItems.get(position).getUrl());
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Glide.with(getActivity())
                            .load(mGalleryItems.get(position).getUrl())
                            .thumbnail(0.1f)
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .centerCrop()
                            .into(new GlideDrawableImageViewTarget(holder.mItemImageView) {
                                @Override
                                public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                                    super.onResourceReady(resource, animation);
                                    //check isRefreshing
                                }
                            });

                }
            });
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }
    }

    protected void getGif(String jsonData) throws JSONException {
        JSONObject jsonBody = new JSONObject(jsonData);
        JSONArray dataArray = jsonBody.getJSONArray("data");

        for (int i = 0; i < dataArray.length(); i++) {
            JSONObject giffyObject = dataArray.getJSONObject(i);

            JSONObject imageObject = giffyObject.getJSONObject("images");
            JSONObject downSizeImageObject = imageObject.getJSONObject("downsized_still");
            GiffyDataObject item = new GiffyDataObject();

            item.setUrl(downSizeImageObject.getString("url"));
            item.setSlug(giffyObject.getString("slug"));
            mItems.add(item);
        }
    }

    private static final String API_KEY = "dc6zaTOxFJmzC";
    private static final String TRENDING_METHOD = "trending";
    private static final String SEARCH_METHOD = "search";
    private static final String FMT = "html";
    private static final Uri ENDPOINT = Uri.parse("http://api.giphy.com/v1/gifs/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .build();


    private String buildUrl(String method, String query) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon().appendPath(method);
        if (method.equals(SEARCH_METHOD)) {
            uriBuilder.appendQueryParameter("q", query);
        }
        return uriBuilder.build().toString();
    }

    protected void getTrendingGiffy(String method, String query) {

        if (isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(buildUrl(method, query))
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        }
                    });
                    try {
                        String jsonData = response.body().string();
                        Log.d(TAG, jsonData);
                        if (response.isSuccessful()) {
                            getGif(jsonData);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setupAdapter();
                                }
                            });
                        } else {
                            Log.i(TAG, "Response Unsuccessful");
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Exception Caught: ", e);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Toast.makeText(getActivity(), "Network is not available!", Toast.LENGTH_LONG).show();
        }

    }

    protected boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }


}
