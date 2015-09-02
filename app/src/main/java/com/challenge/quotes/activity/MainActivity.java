package com.challenge.quotes.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.challenge.quotes.R;
import com.challenge.quotes.model.Item;
import com.challenge.quotes.util.Const;
import com.challenge.quotes.util.RestAgent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private ItemTouchHelper mItemTouchHelper;
    private List<Item> mItems;

    private SwipeRefreshLayout.OnRefreshListener mRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            refresh();
        }
    };

    private ItemTouchHelper.SimpleCallback mSimpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder1) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = mRecyclerView.getChildAdapterPosition(viewHolder.itemView);
            mItems.remove(position);
            mAdapter.notifyItemRemoved(position);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mItems = new ArrayList<>();
        bindUIElements();
        setUpRecyclerView();
        setUpListeners();

        refresh();
    }

    private void refresh() {
        mItems.clear();
        QuoteAsyncTask task = new QuoteAsyncTask();
        task.execute();
    }

    private void bindUIElements() {
        mSwipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.quote_swipe_refresh_layout);
        mRecyclerView = (RecyclerView)findViewById(R.id.quote_recycler_view);
    }

    private void setUpRecyclerView()
    {
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new QuoteAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mItemTouchHelper = new ItemTouchHelper(mSimpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    private void setUpListeners() {
        mSwipeRefreshLayout.setOnRefreshListener(mRefreshListener);
    }

    private class QuoteAdapter extends RecyclerView.Adapter<QuoteAdapter.ViewHolder> {
        public class ViewHolder extends RecyclerView.ViewHolder {
            public CardView cardView;
            public TextView indexTextView;
            public TextView authorTextView;
            public TextView quoteTextView;
            public ViewHolder(View v) {
                super(v);
                cardView = (CardView)v.findViewById(R.id.item_card_view);
                indexTextView = (TextView)v.findViewById(R.id.item_index_text_view);
                authorTextView = (TextView)v.findViewById(R.id.item_author_text_view);
                quoteTextView = (TextView)v.findViewById(R.id.item_quote_text_view);
            }
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_list_item, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Item item = mItems.get(position);
            holder.indexTextView.setText("" + item.getIndex());
            holder.authorTextView.setText(item.getAuthor());
            holder.quoteTextView.setText(item.getQuote());

            //notifyItemInserted(position);
        }
    };

    private class QuoteAsyncTask extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                RestAgent agent = new RestAgent(Const.LIST_QUOTE_URL, RestAgent.GET, null);
                String result = agent.send();
                return result;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (TextUtils.isEmpty(result)) {
                Toast.makeText(MainActivity.this, "Something went wrong. Please retry.", Toast.LENGTH_SHORT).show();
            } else {
                String[] lines = result.split("\n");
                for (int i = 0; i < lines.length; i++) {
                    Item item = Item.fromCSV(lines[i]);
                    if (item != null)
                        mItems.add(item);
                }

                mAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }
    }
}
