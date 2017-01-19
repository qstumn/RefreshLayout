package q.rorbin.qrefreshlayoutdemo;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Collections;
import java.util.LinkedList;

import q.rorbin.qrefreshlayout.QRefreshLayout;
import q.rorbin.qrefreshlayout.listener.RefreshHandler;
import q.rorbin.qrefreshlayout.widget.classics.FooterView;
import q.rorbin.qrefreshlayout.widget.classics.HeaderView;
import q.rorbin.qrefreshlayout.widget.material.MaterialBlackFooterView;
import q.rorbin.qrefreshlayout.widget.material.MaterialBlackHeaderView;
import q.rorbin.qrefreshlayout.widget.material.MaterialFooterView;
import q.rorbin.qrefreshlayout.widget.material.MaterialHeaderView;

public class MainActivity extends AppCompatActivity {
    private ListView mListView;
    private QRefreshLayout mRefreshLayout;
    private ArrayAdapter mAdapter;
    private LinkedList<String> mDatas;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getData();
        initView();
        initListener();
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.listview);
        mRefreshLayout = (QRefreshLayout) findViewById(R.id.refreshlayout);
        //设置上拉加载更多可用
        mRefreshLayout.setLoadMoreEnable(true);
        mRefreshLayout.setHeaderView(new HeaderView(this));
        mRefreshLayout.setFooterView(new FooterView(this));
        mAdapter = new ArrayAdapter<>(this, R.layout.item_textview,
                R.id.textview, mDatas);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(view.getContext(),
                        mDatas.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initListener() {
        mRefreshLayout.setRefreshHandler(new RefreshHandler() {
            @Override
            public void onRefresh(QRefreshLayout refresh) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDatas.addFirst("下拉刷新增加的数据");
                        mAdapter.notifyDataSetChanged();
                        mRefreshLayout.refreshComplete();
                    }
                }, 5000);
            }

            @Override
            public void onLoadMore(QRefreshLayout refresh) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mDatas.addLast("上拉增加的数据");
                        mAdapter.notifyDataSetChanged();
                        mRefreshLayout.loadMoreComplete();
                    }
                }, 5000);
            }
        });
    }

    public void getData() {
        mDatas = new LinkedList<>();
        Collections.addAll(mDatas, "第1条数据", "第2条数据", "第3条数据",
                "第4条数据","第5条数据","第6条数据","第7条数据","第8条数据","第9条数据","第10条数据");
    }
}
