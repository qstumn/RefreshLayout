package q.rorbin.qrefreshlayout.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import q.rorbin.qrefreshlayout.QRefreshLayout;

/**
 * @author chqiu
 */
public abstract class QLoadView extends FrameLayout {

    public QLoadView(Context context) {
        super(context);
    }

    public enum STATE {
        REFRESH, START, PULL, COMPLETE
    }

    public abstract STATE getState();

    public abstract boolean canTargetScroll();

    public abstract void onRefreshBegin(View targetView);

    public abstract void setState(STATE state);

    public abstract void onPrepare(View targetView);

    public abstract void onPulling(float dis, View targetView);

    public abstract void addToRefreshLayout(QRefreshLayout layout);


}
