package q.rorbin.qrefreshlayout.widget.material;

import android.content.Context;
import android.view.Gravity;

import q.rorbin.qrefreshlayout.QRefreshLayout;

/**
 * Created by chqiu on 2016/12/8.
 */

public class MaterialFooterView extends MaterialHeaderView {
    public MaterialFooterView(Context context) {
        super(context);
    }

    @Override
    protected int getProgressGravity() {
        return Gravity.TOP;
    }

    @Override
    public void addToRefreshLayout(QRefreshLayout layout) {
        QRefreshLayout.LayoutParams params =
                new QRefreshLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM;
        layout.addView(this, params);
        params.height = 0;
        setLayoutParams(params);
    }
}
