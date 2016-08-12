# QRefreshLayout
一个上拉刷新下拉加载更多的Android自定义布局

##Change Log
    v1.1.3
    1. 新增了google抽屉式刷新加载风格

    v1.1.1
    1. 修复了内部为RecyclerView时，上拉加载会滚动到底部的BUG

    v1.1.0
    1. 修复了某些情况下无法触发下拉刷新的BUG
    


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
 
 
###4. 属性说明
 
 xml | code | 说明
 --- | --- | ---
 app:refreshStyle | setRefreshStyle | 设置刷新风格

