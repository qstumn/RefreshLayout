package q.rorbin.qrefreshlayout.widget.material;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import q.rorbin.qrefreshlayout.QRefreshLayout;
import q.rorbin.qrefreshlayout.util.RefreshUtil;
import q.rorbin.qrefreshlayout.util.RefreshAnimeUtil;
import q.rorbin.qrefreshlayout.widget.QLoadView;

import static android.content.ContentValues.TAG;
import static q.rorbin.qrefreshlayout.widget.QLoadView.STATE.REFRESH;
import static q.rorbin.qrefreshlayout.widget.QLoadView.STATE.START;

/**
 * Created by chqiu on 2016/9/28.
 */

public class MaterialHeaderView extends QLoadView {
    private final int CIRCLE_BG_LIGHT = 0xFFFAFAFA;
    private int mCircleSize;
    protected int mMeasuredHeight;
    private CircleImageView mImageView;
    private MaterialProgressDrawable mProgressDrawable;
    private STATE mState;
    private int mChildMargin;

    public MaterialHeaderView(Context context) {
        super(context);
        init(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mMeasuredHeight = mImageView.getMeasuredHeight() * 2;
    }

    private void init(Context context) {
        mCircleSize = RefreshUtil.dp2px(context, 45);
        mChildMargin = RefreshUtil.dp2px(context, 5);
        mImageView = new CircleImageView(context, CIRCLE_BG_LIGHT, mCircleSize);
        mProgressDrawable = new MaterialProgressDrawable(context, this);
        mProgressDrawable.setBackgroundColor(CIRCLE_BG_LIGHT);
        mProgressDrawable.setColorSchemeColors(0xFF03A9F4, 0xFF42BD41);
        mProgressDrawable.setAlpha(80);
        mProgressDrawable.updateSizes(MaterialProgressDrawable.LARGE);
        mImageView.setImageDrawable(mProgressDrawable);
        LayoutParams params = new LayoutParams(mCircleSize, mCircleSize);
        params.gravity = Gravity.CENTER_HORIZONTAL | getProgressGravity();
        params.setMargins(mChildMargin, mChildMargin, mChildMargin, mChildMargin);
        addView(mImageView, params);
    }

    @Override
    public STATE getState() {
        return mState;
    }

    @Override
    public boolean canTargetScroll() {
        return mState == STATE.REFRESH || getLayoutParams().height <= 0;
    }

    protected int getProgressGravity() {
        return Gravity.BOTTOM;
    }

    @Override
    public void onRefreshBegin(View targetView) {
        RefreshAnimeUtil.startHeightAnime(this, null, null, mMeasuredHeight, null);
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
        if (mState == STATE.COMPLETE) {
            RefreshAnimeUtil.startScaleAnime(this, 0, new MaterialAnimeListener());
        } else {
            RefreshAnimeUtil.startHeightAnime(this, null, null, 0, null);
        }
    }

    @Override
    public void onPulling(float dis, View targetView) {
        LayoutParams params = (LayoutParams) getLayoutParams();
        if (dis > 0) {
            if (mState != REFRESH) {
                params.height += dis;
                pullingProgress(params);
            }
        } else if (dis < 0) {
            if (mState != REFRESH) {
                params.height += dis;
                if (params.height < 0) {
                    params.height = 0;
                }
                pullingProgress(params);
            }
        }
        setLayoutParams(params);
    }

    private void pullingProgress(LayoutParams params) {
        mProgressDrawable.showArrow(true);
        float percent = (float) params.height / mMeasuredHeight;
        mProgressDrawable.setProgressRotation(percent);
        mProgressDrawable.setArrowScale(percent > 1f ? 1f : percent);
        if (percent > 0.8f) percent = 0.8f;
        mProgressDrawable.setStartEndTrim(0f, percent);
    }

    @Override
    public void addToRefreshLayout(QRefreshLayout layout) {
        QRefreshLayout.LayoutParams params =
                new QRefreshLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL;
        layout.addView(this, params);
        params.height = 0;
        setLayoutParams(params);
    }

    private class MaterialAnimeListener extends AnimatorListenerAdapter {

        @Override
        public void onAnimationEnd(Animator animation) {
            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = 0;
            setLayoutParams(params);
            setScaleX(1f);
            setScaleY(1f);
            mProgressDrawable.stop();
            setState(START);
        }
    }
}
