package q.rorbin.qrefreshlayoutdemo;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Collections;
import java.util.LinkedList;

import q.rorbin.qrefreshlayout.QRefreshLayout;
import q.rorbin.qrefreshlayout.listener.RefreshHandler;

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
        mAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                android.R.id.text1, mDatas);
        mListView.setAdapter(mAdapter);
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
                "第4条数据","第7条数据","第8条数据","第9条数据","第10条数据");
    }
}
