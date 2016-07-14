package q.rorbin.qrefreshlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * @author chqiu
 */
public abstract class QLoadView extends ViewGroup{
    public static final int STATE_REFRESH = 0;
    public static final int STATE_NORMAL = 1;
    public static final int STATE_PULLING = 2;
    public static final int STATE_COMPLETE = 3;

    public abstract void setRefreshing();
    public abstract void setNormal();
    public abstract void setPulling();
    public abstract void setComplete();

    public QLoadView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
