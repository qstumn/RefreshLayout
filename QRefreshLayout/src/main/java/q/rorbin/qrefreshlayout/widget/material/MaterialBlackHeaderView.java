package q.rorbin.qrefreshlayout.widget.material;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.Gravity;
import android.view.View;

import q.rorbin.qrefreshlayout.QRefreshLayout;
import q.rorbin.qrefreshlayout.util.RefreshUtil;
import q.rorbin.qrefreshlayout.util.RefreshAnimeUtil;
import q.rorbin.qrefreshlayout.widget.QLoadView;

import static q.rorbin.qrefreshlayout.widget.ILoadView.STATE.REFRESH;
import static q.rorbin.qrefreshlayout.widget.ILoadView.STATE.START;

/**
 * Created by chqiu on 2016/9/28.
 */

public class MaterialBlackHeaderView extends QLoadView {
    private final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;
    private int mCircleSize;
    private CircleImageView mImageView;
    private MaterialProgressDrawable mProgressDrawable;
    private View mLayer;
    private STATE mState;
    private int scDis = 0;

    public MaterialBlackHeaderView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mLayer = new View(context);
        mLayer.setBackgroundColor(0xFF000000);
        addView(mLayer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mCircleSize = RefreshUtil.dp2px(context, 50);
        mImageView = new CircleImageView(context, CIRCLE_BG_LIGHT, mCircleSize);
        mProgressDrawable = new MaterialProgressDrawable(context, this);
        mProgressDrawable.setBackgroundColor(CIRCLE_BG_LIGHT);
        mProgressDrawable.setColorSchemeColors(0xFF03A9F4, 0xFF42BD41);
        mProgressDrawable.setAlpha(80);
        mProgressDrawable.updateSizes(MaterialProgressDrawable.LARGE);
        mImageView.setImageDrawable(mProgressDrawable);
        LayoutParams params = new LayoutParams(mCircleSize, mCircleSize);
        params.gravity = Gravity.CENTER_HORIZONTAL | getProgressGravity();
        params.setMargins(0, mCircleSize, 0, mCircleSize);
        addView(mImageView, params);
    }

    @Override
    public STATE getState() {
        return mState;
    }

    @Override
    public boolean canTargetScroll() {
        return mState == STATE.REFRESH || scDis <= 0;
    }

    protected int getProgressGravity() {
        return Gravity.TOP;
    }

    @Override
    public void onRefreshBegin(View targetView) {
        RefreshAnimeUtil.startScaleAnime(mImageView, 1f, null);
        ObjectAnimator.ofFloat(mLayer, "alpha", mLayer.getAlpha(), 0.8f).setDuration(300).start();
        mProgressDrawable.showArrow(false);
        mProgressDrawable.start();
    }

    @Override
    public void setState(STATE state) {
        mState = state;
        if (mState == START) {
            ObjectAnimator.ofInt(mProgressDrawable, "alpha", 255, 80).setDuration(300).start();
        } else {
            ObjectAnimator.ofInt(mProgressDrawable, "alpha", 80, 255).setDuration(300).start();
        }
    }

    @Override
    public void onPrepare(View targetView) {
        RefreshAnimeUtil.startScaleAnime(mImageView, 0, new MaterialAnimeListener());
        ObjectAnimator.ofFloat(mLayer, "alpha", mLayer.getAlpha(), 0f).setDuration(300).start();
        scDis = 0;
    }

    @Override
    public void onPulling(float dis, View targetView) {
        if (dis > 0) {
            if (mState != REFRESH) {
                scDis += dis;
                pullingProgress(scDis);
            }
        } else if (dis < 0) {
            if (mState != REFRESH) {
                scDis += dis;
                if (scDis < 0) {
                    scDis = 0;
                }
                pullingProgress(scDis);
            }
        }
    }

    private void pullingProgress(int scDis) {
        mProgressDrawable.showArrow(true);
        float percent = (float) scDis / (mCircleSize * 3);
        mProgressDrawable.setProgressRotation(percent);
        if (percent > 1f) percent = 1f;
        mProgressDrawable.setArrowScale(percent);
        mImageView.setScaleX(percent);
        mImageView.setScaleY(percent);
        if (percent > 0.8f) percent = 0.8f;
        mProgressDrawable.setStartEndTrim(0f, percent);
        mLayer.setAlpha(percent);
    }

    @Override
    public void addToRefreshLayout(QRefreshLayout layout) {
        layout.addView(this, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mImageView.setScaleX(0);
        mImageView.setScaleY(0);
        mLayer.setAlpha(0);
    }

    private class MaterialAnimeListener extends AnimatorListenerAdapter {

        @Override
        public void onAnimationEnd(Animator animation) {
            mProgressDrawable.stop();
            setState(START);
        }
    }
}
