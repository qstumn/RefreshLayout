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
public abstract class QLoadView extends FrameLayout implements ILoadView {

    public QLoadView(Context context) {
        super(context);
    }

    public View getView() {
        return this;
    }
}
