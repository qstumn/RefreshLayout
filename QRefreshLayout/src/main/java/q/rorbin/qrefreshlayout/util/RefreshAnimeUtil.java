package q.rorbin.qrefreshlayout.util;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;

import q.rorbin.qrefreshlayout.QRefreshLayout;
import q.rorbin.qrefreshlayout.listener.TargetHandler;
import q.rorbin.qrefreshlayout.widget.QLoadView;

/**
 * Created by chqiu on 2016/9/28.
 */

public class RefreshAnimeUtil {
    private static final long mAnimeDuration = 300;

    public static void startHeightAnime(final View View, final View targetView, final TargetHandler handler,
                                        int newHeight, Animator.AnimatorListener listener) {
        ValueAnimator anime = ValueAnimator.ofInt(View.getLayoutParams().height, newHeight);
        anime.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int h = Integer.parseInt(animation.getAnimatedValue().toString());
                QRefreshLayout.LayoutParams params = (QRefreshLayout.LayoutParams) View.getLayoutParams();
                params.height = h;
                View.setLayoutParams(params);
                if (targetView != null && handler != null) {
                    handler.handleTarget(targetView, params.height);
                }
            }
        });
        if (listener != null) {
            anime.addListener(listener);
        }
        anime.setDuration(mAnimeDuration);
        anime.start();
    }

    public static void startScaleAnime(final View view,
                                       float newScale, Animator.AnimatorListener listener) {
        ValueAnimator anime = ValueAnimator.ofFloat(view.getScaleX(), newScale);
        anime.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float s = Float.parseFloat(animation.getAnimatedValue().toString());
                view.setScaleX(s);
                view.setScaleY(s);
            }
        });
        if (listener != null) {
            anime.addListener(listener);
        }
        anime.setDuration(mAnimeDuration);
        anime.start();
    }
}
