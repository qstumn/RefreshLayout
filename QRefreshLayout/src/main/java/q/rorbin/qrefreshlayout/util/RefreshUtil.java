package q.rorbin.qrefreshlayout.util;

import android.content.Context;

/**
 * @author chqiu
 */
public class RefreshUtil {

    public static int dp2px(Context context,float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
