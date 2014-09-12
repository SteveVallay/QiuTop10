
package com.goodluck.hackerexample;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import com.markupartist.android.widget.PullToRefreshListView;
import com.markupartist.android.widget.PullToRefreshListView.OnRefreshListener;
public class MainActivity extends ListActivity {

    /**
     * Currently running background network thread.
     */
    private RSSWorker mWorker;
    public static final String QIUBAI_RSS_ADDR = "http://feed.qiushibaike.com/rss";
    /**
     * Handler used to post runnables to the UI thread.
     */
    private Handler mHandler;
    /**
     * Custom list adapter that fits our rss data into the list.
     */
    private RSSListAdapter mAdapter;
    /**
     * Status text field.
     */
    private TextView mStatusText;

    private static final String[] PROJECTION = new String[] {
        RSSApp.RssItems._ID,
        RSSApp.RssItems.COLUMN_NAME_TITLE,
        RSSApp.RssItems.COLUMN_NAME_DESCRIPTION,
        RSSApp.RssItems.COLUMN_NAME_PUBDATE
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mHandler = new Handler();
        mStatusText = (TextView)findViewById(R.id.statustext);
        List<RssItem> items = new ArrayList<RssItem>();
        mAdapter = new RSSListAdapter(this, items);
        addDataFromDB();
        getListView().setAdapter(mAdapter);
        ((PullToRefreshListView) getListView()).setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Do work to refresh the list here.
                new GetDataTask().execute();
            }
        });
    }

    private class GetDataTask extends AsyncTask<Void, Void, String[]> {
        protected void onPostExecute(String[] result) {
            // Call onRefreshComplete when the list has been refreshed.
            ((PullToRefreshListView) getListView()).onRefreshComplete();
            super.onPostExecute(result);
        }

        @Override
        protected String[] doInBackground(Void... arg0) {
            if (isNetworkAvailable()){
                doRss(QIUBAI_RSS_ADDR);
            } else {
                // post to make toast
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        Toast.makeText(getApplicationContext(), "network not available...",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return null;
        }
    }

    private void addDataFromDB() {
        //mAdapter.add(new RssItem());
        Cursor cursor = getContentResolver().query(RSSApp.RssItems.CONTENT_URI, PROJECTION,null,null,null);
        if (cursor !=null && cursor.getCount() != 0){
            while(cursor.moveToNext()){
                RssItem item = new RssItem(cursor.getString(1), null, cursor.getString(2),
                        cursor.getString(3));
                mAdapter.add(item);
            }
        }
    }

    /**
     * ArrayAdapter encapsulates a java.util.List of T, for presentation in a
     * ListView. This subclass specializes it to hold RssItems and display
     * their title/description data in a TwoLineListItem.
     */
    private class RSSListAdapter extends ArrayAdapter<RssItem> {
        private LayoutInflater mInflater;

        public RSSListAdapter(Context context, List<RssItem> objects) {
            super(context, 0, objects);

            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        /**
         * This is called to render a particular item for the on screen list.
         * Uses an off-the-shelf TwoLineListItem view, which contains text1 and
         * text2 TextViews. We pull data from the RssItem and set it into the
         * view. The convertView is the view from a previous getView(), so
         * we can re-use it.
         *
         * @see ArrayAdapter#getView
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TwoLineListItem view;

            // Here view may be passed in for re-use, or we make a new one.
            if (convertView == null) {
                view = (TwoLineListItem) mInflater.inflate(android.R.layout.simple_list_item_2,
                        null);
            } else {
                view = (TwoLineListItem) convertView;
            }

            RssItem item = this.getItem(position);

            view.getText1().setText(item.getTitle());
            return view;
        }

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    /**
     * Given an rss url string, starts the rss-download-thread going.
     *
     * @param rssUrl
     */
    private void doRss(String uri){
        RSSWorker worker = new RSSWorker(uri);
        setCurrentWorker(worker);
        //mStatusText.setText("Downloading\u2026");
        worker.start();
    }

    /**
     * Is the given worker the currently active one.
     *
     * @param worker
     * @return
     */
    public synchronized boolean isCurrentWorker(RSSWorker worker) {
        return (mWorker == worker);
    }

    /**
     * Sets the currently active running worker. Interrupts any earlier worker,
     * so we only have one at a time.
     * 
     * @param worker the new worker
     */
    public synchronized void setCurrentWorker(RSSWorker worker) {
        if (mWorker != null) mWorker.interrupt();
        mWorker = worker; 
    }
    /**
     * Runnable that the worker thread uses to post RssItems to the
     * UI via mHandler.post
     */
    private class ItemAdder implements Runnable {
        RssItem mItem;

        ItemAdder(RssItem item) {
            mItem = item;
        }

        public void run() {
            //should add to the first position.
            mAdapter.insert(mItem, 0);
        }

        // NOTE: Performance idea -- would be more efficient to have he option
        // to add multiple items at once, so you get less "update storm" in the UI
        // compared to adding things one at a time.
    }
    /**
     * Worker thread takes in an rss url string, downloads its data, parses
     * out the rss items, and communicates them back to the UI as they are read.
     */
    private class RSSWorker extends Thread {
        private CharSequence mUrl;

        public RSSWorker(CharSequence url) {
            mUrl = url;
        }
        @Override
        public void run() {
            String status = "";
            try {
                // Standard code to make an HTTP connection.
                URL url = new URL(mUrl.toString());
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(10000);

                connection.connect();
                InputStream in = connection.getInputStream();

                parseRSS(in);
                status = "done";
            } catch (Exception e) {
                status = "failed:" + e.getMessage();
            }

            // Send status to UI (unless a newer worker has started)
            // To communicate back to the UI from a worker thread,
            // pass a Runnable to handler.post().
            final String temp = status;
            if (isCurrentWorker(this)) {
                mHandler.post(new Runnable() {
                    public void run() {
                            log("load content..." + temp);
                            //mStatusText.setText(temp);
                    }
                });
            }
        }
    }

    /**
     * Called when user clicks an item in the list. Starts an activity to
     * open the url for that item.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //Wrokround since PullToRefreshView already has one item in this list.
        RssItem item = mAdapter.getItem(position - 1);
        // Creates and starts an intent to open the item.link url.
        Intent intent = new Intent();
        intent.setClass(this, DetailActivity.class);
        intent.putExtra("data", item.getDescription());
        startActivity(intent);
    }

    class RssItem {

        private String mTitle;
        private String mLink;
        private String mDescription;
        private String mPubDate;

        public RssItem() {
            mTitle = "";
            mLink = "";
            mDescription = "";
        }
        public RssItem(String title, String link, String desc, String pubDate) {
            mTitle = title;
            mLink = link;
            mDescription = desc;
            mPubDate = pubDate;
        }

        public String getPubDate() {
            return mPubDate;
        }

        public String getTitle() {
            return mTitle;
        }

        public String getLink() {
            return mLink;
        }

        public String getDescription() {
            return mDescription;
        }
    }

    /**
     * Does rudimentary RSS parsing on the given stream and posts rss items to
     * the UI as they are found. Uses Android's XmlPullParser facility. This is
     * not a production quality RSS parser -- it just does a basic job of it.
     *
     * @param in stream to read
     * @param adapter adapter for ui events
     */
    RssItem parseRSS(InputStream in) throws IOException,
            XmlPullParserException {
        // TODO: switch to sax

        XmlPullParser xpp = Xml.newPullParser();
        xpp.setInput(in, null);  // null = default to UTF-8

        int eventType;
        String title = "";
        String link = "";
        String description = "";
        String pubDate = "";
        eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String tag = xpp.getName();
                if (tag.equals("item")) {
                    title = link = description = "";
                } else if (tag.equals("title")) {
                    xpp.next(); // Skip to next element -- assume text is directly inside the tag
                    title = xpp.getText();
                } else if (tag.equals("link")) {
                    xpp.next();
                    link = xpp.getText();
                } else if (tag.equals("description")) {
                    xpp.next();
                    description = xpp.getText();
                } else if (tag.equals("pubDate")) {
                    xpp.next();
                    pubDate = xpp.getText();
                }
            } else if (eventType == XmlPullParser.END_TAG) {
                // We have a comlete item -- post it back to the UI
                // using the mHandler (necessary because we are not
                // running on the UI thread).
                String tag = xpp.getName();
                if (tag.equals("item")) {
                    RssItem item = new RssItem(title, link, description, pubDate);
                    log("paraseRss: desc=" + description);
                    //need load from DB
                    if (insertToDB(item)) {
                        mHandler.post(new ItemAdder(item));
                    }
                    return item;
                }
            }
            eventType = xpp.next();
        }
        return null;
    }

    public void log(String msg) {
        Log.d("HACKER", msg);
    }

    public boolean insertToDB(RssItem item) {
        // query if it already exit
        Cursor cursor = getContentResolver().query(RSSApp.RssItems.CONTENT_URI, PROJECTION,
                RSSApp.RssItems.COLUMN_NAME_PUBDATE + "=" + "'" + item.getPubDate() + "'", null,
                null);
        int count = 0;
        if (cursor != null) {
            count = cursor.getCount();
        }
        if (count == 0) {
            ContentValues values = new ContentValues();
            values.put(RSSApp.RssItems.COLUMN_NAME_TITLE, item.getTitle());
            values.put(RSSApp.RssItems.COLUMN_NAME_DESCRIPTION, item.getDescription());
            values.put(RSSApp.RssItems.COLUMN_NAME_PUBDATE, item.getPubDate());
            Uri newUri = getContentResolver().insert(RSSApp.RssItems.CONTENT_URI, values);
            if (newUri != null) {
                log("insertToDB: " + newUri);
                return true;
            } else {
                log("insertToDb: fail!");
            }
        } else {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "no more update !",
                            Toast.LENGTH_SHORT).show();
                }
            });
            log("DB already has this record :  title = " + item.getTitle());
        }
        return false;
    }
}
