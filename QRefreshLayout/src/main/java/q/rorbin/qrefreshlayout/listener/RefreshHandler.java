package q.rorbin.qrefreshlayout.listener;

import q.rorbin.qrefreshlayout.QRefreshLayout;

/**
 * @author chqiu
 */
public interface RefreshHandler {
    void onRefresh(QRefreshLayout refresh);

    void onLoadMore(QRefreshLayout refresh);
}
