package q.rorbin.qrefreshlayout.widget.material;

import android.content.Context;
import android.view.Gravity;

/**
 * Created by chqiu on 2016/12/8.
 */

public class MaterialBlackFooterView extends MaterialBlackHeaderView {
    public MaterialBlackFooterView(Context context) {
        super(context);
    }

    @Override
    protected int getProgressGravity() {
        return Gravity.BOTTOM;
    }
}
