# QRefreshLayout
一个上拉刷新下拉加载更多的Android自定义布局

##Change Log
    V1.1.1(2016.8.10) 
    1. 修复了某些情况下无法触发下拉刷新的BUG
    2. 修复了内部为RecyclerView时，上拉加载后没有滚动到底部的BUG


![](https://github.com/qstumn/QRefreshLayout/blob/master/demo.gif?raw=true)
##how to use:
###1. gradle
    compile 'q.rorbin:QRefreshLayout:1.1.1'  

###2. xml

    <q.rorbin.qrefreshlayout.QRefreshLayout
        android:id="@+id/refreshlayout"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        
        <ListView 
            android:id="@+id/listview"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
            
    </q.rorbin.qrefreshlayout.QRefreshLayout>
    

###3. code
  
   如果需要上拉加载更多功能，请调用以下方法
  
  `mRefreshLayout.setLoadMoreEnable(true);`

   设置监听 
  
    mRefreshLayout.setRefreshHandler(new RefreshHandler() {
    
            @Override
            public void onRefresh(QRefreshLayout refresh) {
                do something...
                mRefreshLayout.refreshComplete();
            }
            
            @Override
            public void onLoadMore(QRefreshLayout refresh) {
                do something...
                mRefreshLayout.LoadMoreComplete();
            }
        });

  如果你想自定义自己的头部或者尾部的View，只需要将你的View继承自QLoadView即可
  
  `public class HeaderView extends QLoadView`
  
   然后在设置进布局
  
  `mRefreshLayout.setHeaderView(new HeaderView());`

