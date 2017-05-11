package com.example.android.newsreader;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.example.android.newsreader.BuildConfig.API_KEY;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Article>>{

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.logo)
    ImageView mLogoView;

    @BindView(R.id.error_main)
    TextView emptyTextView;

    @BindView(R.id.loading_indicator)
    ProgressBar mProgressBar;

    public static final String LOG_TAG = MainActivity.class.getName();
    private Adapter adapter;
    private String NYT_BASE_API_REQUEST_URL = "http://api.nytimes.com/svc/topstories/v2";
    private String BASE_API_REQUEST_URL = "https://newsapi.org/v1/articles";
    private static final int ARTICLE_LOADER_ID = 1;
    private List<Article> mListArticle;
    public static final String ACTION_DATA_UPDATED = "com.example.android.newsreader.ACTION_DATA_UPDATED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (BuildConfig.DEBUG) {
            Timber.uprootAll();
            Timber.plant(new Timber.DebugTree());
        }

        if (networkUp()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(ARTICLE_LOADER_ID, null, this);
            emptyTextView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.GONE);
            emptyTextView.setText(R.string.no_connection_message);
            emptyTextView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String articleSource = sharedPrefs.getString(getString(R.string.settings_source_key), getString(R.string.settings_source_default));
        if(articleSource.equals(getResources().getString(R.string.settings_source_nyt_value))){
            mLogoView.setImageResource(R.drawable.nytimes);
        } else if(articleSource.equals(getResources().getString(R.string.settings_source_bloomberg_value))){
            mLogoView.setImageResource(R.drawable.bloomberg);
        } else if(articleSource.equals(getResources().getString(R.string.settings_source_businessinsider_value))){
            mLogoView.setImageResource(R.drawable.business_insider);
        } else if(articleSource.equals(getResources().getString(R.string.settings_source_huffpo_value))){
            mLogoView.setImageResource(R.drawable.huffpost);
        } else if(articleSource.equals(getResources().getString(R.string.settings_source_wapo_value))){
            mLogoView.setImageResource(R.drawable.washingtonpost);
        } else if(articleSource.equals(getResources().getString(R.string.settings_source_wsj_value))){
            mLogoView.setImageResource(R.drawable.wallstreetjournal);
        } else {
            Intent savedArticlesIntent = new Intent(this, SavedArticlesActivity.class);
            startActivity(savedArticlesIntent);
        }

        ((MyApplication) getApplication()).startTracking();

        Tracker tracker = (((MyApplication) getApplication()).getTracker());
        tracker.setScreenName(getResources().getString(R.string.screen_name));
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private boolean networkUp() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public Loader<List<Article>> onCreateLoader(int i, Bundle bundle) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String articleSource = sharedPrefs.getString(
                getString(R.string.settings_source_key),
                getString(R.string.settings_source_default));
        String articleSortBy = getResources().getString(R.string.sort_by_top);

        Tracker tracker = (((MyApplication) getApplication()).getTracker());

        if(articleSource.equals(getResources().getString(R.string.settings_source_nyt_value))){
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(getResources().getString(R.string.analytics_article_sources))
                    .setAction(getResources().getString(R.string.analytics_read_nytimes))
                    .setLabel(getResources().getString(R.string.analytics_nytimes))
                    .build());
        } else if(articleSource.equals(getResources().getString(R.string.settings_source_bloomberg_value))){
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(getResources().getString(R.string.analytics_article_sources))
                    .setAction(getResources().getString(R.string.analytics_read_bloomberg))
                    .setLabel(getResources().getString(R.string.analytics_bloomberg))
                    .build());
        } else if(articleSource.equals(getResources().getString(R.string.settings_source_businessinsider_value))){
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(getResources().getString(R.string.analytics_article_sources))
                    .setAction(getResources().getString(R.string.analytics_read_businessinsider))
                    .setLabel(getResources().getString(R.string.analytics_businessinsider))
                    .build());
        } else if(articleSource.equals(getResources().getString(R.string.settings_source_huffpo_value))){
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(getResources().getString(R.string.analytics_article_sources))
                    .setAction(getResources().getString(R.string.analytics_read_huffpost))
                    .setLabel(getResources().getString(R.string.analytics_huffpost))
                    .build());
        } else if(articleSource.equals(getResources().getString(R.string.settings_source_wapo_value))){
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(getResources().getString(R.string.analytics_article_sources))
                    .setAction(getResources().getString(R.string.analytics_read_wapo))
                    .setLabel(getResources().getString(R.string.analytics_wapo))
                    .build());
        } else if(articleSource.equals(getResources().getString(R.string.settings_source_wsj_value))){
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(getResources().getString(R.string.analytics_article_sources))
                    .setAction(getResources().getString(R.string.analytics_read_wsj))
                    .setLabel(getResources().getString(R.string.analytics_wsj))
                    .build());
        } else {
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(getResources().getString(R.string.analytics_article_sources))
                    .setAction(getResources().getString(R.string.analytics_read_saved_articles))
                    .setLabel(getResources().getString(R.string.analytics_saved_articles))
                    .build());
        }

        Uri baseUri = Uri.parse(BASE_API_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("source", articleSource);
        uriBuilder.appendQueryParameter("sortBy", articleSortBy);
        uriBuilder.appendQueryParameter("apiKey", API_KEY);
        return new ArticleLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> data) {
        if (data != null && !data.isEmpty()) {
            adapter = new Adapter(data);
            adapter.setHasStableIds(true);
            mProgressBar.setVisibility(View.GONE);
            mRecyclerView.setAdapter(adapter);
            int columnCount = 1;
            GridLayoutManager sglm =
                    new GridLayoutManager(this, columnCount);
            mRecyclerView.setLayoutManager(sglm);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {
        adapter.notifyDataSetChanged();
    }

    private class Adapter extends RecyclerView.Adapter<ViewHolder>{

        public Adapter(List<Article> listArticle) {
            mListArticle = listArticle;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.list_item_article, parent, false);
            final ViewHolder viewHolder = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Article currentArticle = mListArticle.get(viewHolder.getAdapterPosition());
                    Uri articleUri = Uri.parse(currentArticle.getWebUrl());
                    Intent websiteIntent = new Intent(Intent.ACTION_VIEW, articleUri);
                    startActivity(websiteIntent);
                }
            });
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Article currentArticle = mListArticle.get(position);
            holder.articleTitleTextView.setText(currentArticle.getWebTitle());
            holder.publicationDateTextView.setText(currentArticle.getWebPublicationDate());
            holder.articleDescriptionTextView.setText(currentArticle.getDescription());
            String thumbnailUrl = currentArticle.getThumbnailUrl();
            Picasso.with(holder.thumbnailView.getContext()).load(thumbnailUrl).into(holder.thumbnailView);
        }

        @Override
        public int getItemCount() {
            return mListArticle.size();
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView thumbnailView;
        public TextView articleTitleTextView;
        public TextView publicationDateTextView;
        public TextView articleDescriptionTextView;

        public ViewHolder(View view) {
            super(view);
            thumbnailView = (ImageView) view.findViewById(R.id.thumbnail);
            articleTitleTextView = (TextView) view.findViewById(R.id.article_title);
            publicationDateTextView = (TextView) view.findViewById(R.id.publication_date);
            articleDescriptionTextView = (TextView) view.findViewById(R.id.article_description);
        }
    }

    //reference for code below: http://stackoverflow.com/questions/27293960/swipe-to-dismiss-for-recyclerview

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();

            if (direction == ItemTouchHelper.LEFT) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage(R.string.save_article_message);

                builder.setNegativeButton(R.string.save_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveItem(position);

                        if (position == 0) {
                            mListArticle.remove(position);
                            adapter.notifyItemRangeChanged(0, adapter.getItemCount());
                        } else {
                            mListArticle.remove(position);
                            adapter.notifyItemRemoved(position);
                        }
                        return;
                    }
                }).setPositiveButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.notifyItemRemoved(position + 1);
                        adapter.notifyItemRangeChanged(position, adapter.getItemCount());
                        return;
                    }
                }).show();
            }
        }
    };

    private void saveItem(int position){
        ContentValues contentValues = new ContentValues();
        Article currentArticle = mListArticle.get(position);
        contentValues.put(SavedArticlesContract.SavedArticlesEntry.COLUMN_ARTICLE_TITLE, currentArticle.getWebTitle());
        contentValues.put(SavedArticlesContract.SavedArticlesEntry.COLUMN_ARTICLE_DESCRIPTION, currentArticle.getDescription());
        contentValues.put(SavedArticlesContract.SavedArticlesEntry.COLUMN_ARTICLE_DATE, currentArticle.getWebPublicationDate());
        contentValues.put(SavedArticlesContract.SavedArticlesEntry.COLUMN_ARTICLE_THUMBNAIL, currentArticle.getThumbnailUrl());
        contentValues.put(SavedArticlesContract.SavedArticlesEntry.COLUMN_ARTICLE_URL, currentArticle.getWebUrl());
        Uri newUri = getContentResolver().insert(SavedArticlesContract.SavedArticlesEntry.CONTENT_URI, contentValues);

        if (newUri != null) {
            Toast.makeText(this, R.string.toast_success, Toast.LENGTH_SHORT).show();
            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            this.getApplicationContext().sendBroadcast(dataUpdatedIntent);
        } else {
            Toast.makeText(this, R.string.toast_fail, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
