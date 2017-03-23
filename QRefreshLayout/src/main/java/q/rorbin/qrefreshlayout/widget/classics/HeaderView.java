package q.rorbin.qrefreshlayout.widget.classics;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import q.rorbin.qrefreshlayout.QRefreshLayout;
import q.rorbin.qrefreshlayout.listener.TargetHandler;
import q.rorbin.qrefreshlayout.util.RefreshUtil;
import q.rorbin.qrefreshlayout.R;
import q.rorbin.qrefreshlayout.util.RefreshAnimeUtil;
import q.rorbin.qrefreshlayout.widget.QLoadView;

import static android.content.ContentValues.TAG;
import static q.rorbin.qrefreshlayout.widget.QLoadView.STATE.COMPLETE;
import static q.rorbin.qrefreshlayout.widget.QLoadView.STATE.PULL;
import static q.rorbin.qrefreshlayout.widget.QLoadView.STATE.REFRESH;
import static q.rorbin.qrefreshlayout.widget.QLoadView.STATE.START;


/**
 * LoadView默认实现
 *
 * @author chqiu
 */
public class HeaderView extends QLoadView {
    protected STATE mState;
    protected int mMeasuredHeight;
    protected ImageView mIvIcon;
    protected TextView mTvTips;
    protected ProgressBar mProgressBar;
    protected final int mMargin = RefreshUtil.dp2px(getContext(), 15);
    protected ViewGroup mContent;
    int i = 0;

    public HeaderView(Context context) {
        super(context);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        mMeasuredHeight = mContent.getMeasuredHeight() + mMargin * 2;
        mContent.layout(0, height - mContent.getMeasuredHeight() - mMargin, width, height - mMargin);
    }

    @Override
    public boolean canTargetScroll() {
        return getLayoutParams().height <= 0;
    }

    private void setRefreshState() {
        if (mState != REFRESH) {
            mIvIcon.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
            mTvTips.setText(getStateTips(REFRESH));
            mState = REFRESH;
        }
    }

    private void setStartState() {
        if (mState != START) {
            mIvIcon.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            if (mState == PULL) {
                ObjectAnimator.ofFloat(mIvIcon, "rotation", 180, 0).start();
            }
            mTvTips.setText(getStateTips(START));
            mState = START;
        }
    }

    private void setPullState() {
        if (mState != PULL) {
            mIvIcon.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            if (mState == START) {
                ObjectAnimator.ofFloat(mIvIcon, "rotation", 0, 180).start();
            }
            mTvTips.setText(getStateTips(PULL));
            mState = PULL;
        }
    }

    private void setCompleteState() {
        if (mState != COMPLETE) {
            mIvIcon.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            mTvTips.setText(getStateTips(COMPLETE));
            mState = COMPLETE;
            mIvIcon.setRotation(0);
        }
    }

    protected String getStateTips(STATE state) {
        if (state == REFRESH) {
            return getContext().getString(R.string.loading_tips);
        } else if (state == START) {
            return getContext().getString(R.string.normal_tips);
        } else if (state == PULL) {
            return getContext().getString(R.string.pulling_tips);
        } else if (state == COMPLETE) {
            return getContext().getString(R.string.complete_tips);
        }
        return "";
    }

    @Override
    public STATE getState() {
        return mState;
    }

    @Override
    public void setState(STATE state) {
        if (state == REFRESH) {
            setRefreshState();
        } else if (state == START) {
            setStartState();
        } else if (state == PULL) {
            setPullState();
        } else if (state == COMPLETE) {
            setCompleteState();
        }
    }

    @Override
    public void onPrepare(View targetView) {
        RefreshAnimeUtil.startHeightAnime(this, targetView, getTargetHandler(), 0, new AnimeListener());
    }

    @Override
    public void onRefreshBegin(View targetView) {
        RefreshAnimeUtil.startHeightAnime(this, targetView, getTargetHandler(), mMeasuredHeight, null);
    }

    @Override
    public void onPulling(float dis, View targetView) {
        LayoutParams params = (LayoutParams) getLayoutParams();
        if (dis > 0) {
            if (mState == REFRESH) {
                params.height += 30;
                if (params.height > mMeasuredHeight) params.height = mMeasuredHeight;
            } else {
                i += dis;
                params.height += dis;
            }
        } else if (dis < 0) {
            params.height += dis;
            if (params.height < 0) {
                params.height = 0;
            }
        }
        setLayoutParams(params);
        getTargetHandler().handleTarget(targetView, params.height);
    }

    @Override
    public void addToRefreshLayout(QRefreshLayout layout) {
        QRefreshLayout.LayoutParams params =
                new QRefreshLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layout.addView(this, params);
        params.height = 0;
        setLayoutParams(params);
    }

    protected TargetHandler getTargetHandler() {
        return new TargetHandler() {
            @Override
            public void handleTarget(View targetView, float dis) {
                targetView.setTranslationY(dis);
            }
        };
    }

    private void init() {
        LinearLayout content = new LinearLayout(getContext());
        content.setGravity(Gravity.CENTER);
        content.setOrientation(LinearLayout.HORIZONTAL);
        mContent = content;
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(mContent, lp);
        initContentView();
    }

    protected int getIconRes() {
        return R.drawable.icon_pull;
    }

    private void initContentView() {
        mContent.removeAllViews();
        ImageView imageView = new ImageView(getContext());
        LinearLayout.LayoutParams imageViewParmas =
                new LinearLayout.LayoutParams(RefreshUtil.dp2px(getContext(), 20), RefreshUtil.dp2px(getContext(), 20));
        imageViewParmas.setMargins(0, 0, RefreshUtil.dp2px(getContext(), 10), 0);
        imageView.setLayoutParams(imageViewParmas);
        imageView.setImageResource(getIconRes());
        mContent.addView(imageView);

        ProgressBar progress = new ProgressBar(getContext());
        LinearLayout.LayoutParams progressParmas =
                new LinearLayout.LayoutParams(RefreshUtil.dp2px(getContext(), 20), RefreshUtil.dp2px(getContext(), 20));
        progressParmas.setMargins(0, 0, RefreshUtil.dp2px(getContext(), 10), 0);
        progress.setLayoutParams(progressParmas);
        progress.setVisibility(View.GONE);
        mContent.addView(progress);

        TextView tvTips = new TextView(getContext());
        tvTips.setText(getStateTips(START));
        mContent.addView(tvTips);

        mIvIcon = imageView;
        mTvTips = tvTips;
        mProgressBar = progress;

        mState = START;
    }

    private class AnimeListener extends AnimatorListenerAdapter {

        @Override
        public void onAnimationEnd(Animator animation) {
            setState(START);
        }
    }
}
