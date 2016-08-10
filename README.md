# QRefreshLayout
[中文版](https://github.com/qstumn/QRefreshLayout/blob/master/READMEC.md)


this is a custom layout with refresh and loadmore function for android

##Change Log
    V1.1.1(2016.8.10) 
    1. 修复了某些情况下无法触发下拉刷新的BUG
    2. 修复了内部为RecyclerView时，上拉加载后没有滚动到底部的BUG


![](https://github.com/qstumn/QRefreshLayout/blob/master/demo.gif?raw=true)
##how to use:
####1.gradle
    compile 'q.rorbin:QRefreshLayout:1.1.1'  

####2.xml

    <q.rorbin.qrefreshlayout.QRefreshLayout
        android:id="@+id/refreshlayout"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        
        <ListView 
            android:id="@+id/listview"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
            
    </q.rorbin.qrefreshlayout.QRefreshLayout>
    

####3.code
  
   if you want loadmore, call this funcation
  
  `mRefreshLayout.setLoadMoreEnable(true);`

   then 
  
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

  If you want to custom header or footer view, create your view and extends QLoadView like this
  
  `public class HeaderView extends QLoadView`
  
  then
  
  `mRefreshLayout.setHeaderView(new HeaderView());`
