package q.rorbin.qrefreshlayout;

/**
 * @author chqiu
 */
public interface RefreshHandler {
    void onRefresh(QRefreshLayout refresh);

    void onLoadMore(QRefreshLayout refresh);
}
