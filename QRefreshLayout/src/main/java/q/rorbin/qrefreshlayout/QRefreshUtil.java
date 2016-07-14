package q.rorbin.qrefreshlayout;

import android.content.Context;

/**
 * @author chqiu
 */
public class QRefreshUtil {

    public static int dp2px(Context context,float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
