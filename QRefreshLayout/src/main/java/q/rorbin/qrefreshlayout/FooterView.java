package q.rorbin.qrefreshlayout;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * LoadView默认实现
 * @author chqiu
 */
public class FooterView extends QLoadView {
    private int mState;

    private ImageView mIvIcon;
    private TextView mTvTips;
    private ProgressBar mProgressBar;


    private final int mMargin = QRefreshUtil.dp2px(getContext(), 15);
    private ViewGroup mContent;

    public FooterView(Context context) {
        this(context, null);
    }

    public FooterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        measureChild(mContent, MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST)
                , MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
        mContent.layout(0, mMargin, width, mContent.getMeasuredHeight() + mMargin);
    }

    @Override
    public void setRefreshing() {
        if (mState != STATE_REFRESH) {
            mIvIcon.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
            mTvTips.setText(getContext().getString(R.string.loading_tips));
            mState = STATE_REFRESH;
        }
    }

    @Override
    public void setNormal() {
        if (mState != STATE_NORMAL) {
            mIvIcon.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            if (mState == STATE_PULLING) {
                ObjectAnimator.ofFloat(mIvIcon, "rotation", 180, 0).start();
            }
            mTvTips.setText(getContext().getString(R.string.normal_tips_2));
            mState = STATE_NORMAL;
        }
    }

    @Override
    public void setPulling() {
        if (mState != STATE_PULLING) {
            mIvIcon.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
            if (mState == STATE_NORMAL) {
                ObjectAnimator.ofFloat(mIvIcon, "rotation", 0, 180).start();
            }
            mTvTips.setText(getContext().getString(R.string.pulling_tips_2));
            mState = STATE_PULLING;
        }
    }

    @Override
    public void setComplete() {
        if (mState != STATE_COMPLETE) {
            mIvIcon.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.GONE);
            mTvTips.setText(getContext().getString(R.string.complete_tips));
            mState = STATE_COMPLETE;
        }
    }

    private void init() {
        LinearLayout content = new LinearLayout(getContext());
        content.setGravity(Gravity.CENTER);
        content.setOrientation(LinearLayout.HORIZONTAL);
        mContent = content;
        addView(mContent);
        initContentView();
    }

    private void initContentView() {
        mContent.removeAllViews();
        ImageView imageView = new ImageView(getContext());
        LinearLayout.LayoutParams imageViewParmas =
                new LinearLayout.LayoutParams(QRefreshUtil.dp2px(getContext(), 20), QRefreshUtil.dp2px(getContext(), 20));
        imageViewParmas.setMargins(0, 0, QRefreshUtil.dp2px(getContext(), 10), 0);
        imageView.setLayoutParams(imageViewParmas);
        imageView.setImageResource(R.drawable.icon_pull_2);
        mContent.addView(imageView);

        ProgressBar progress = new ProgressBar(getContext());
        LinearLayout.LayoutParams progressParmas =
                new LinearLayout.LayoutParams(QRefreshUtil.dp2px(getContext(), 20), QRefreshUtil.dp2px(getContext(), 20));
        progressParmas.setMargins(0, 0, QRefreshUtil.dp2px(getContext(), 10), 0);
        progress.setLayoutParams(progressParmas);
        progress.setVisibility(View.GONE);
        mContent.addView(progress);

        TextView tvTips = new TextView(getContext());
        tvTips.setText("上拉加载更多...");
        mContent.addView(tvTips);

        mIvIcon = imageView;
        mTvTips = tvTips;
        mProgressBar = progress;

        mState = STATE_NORMAL;
    }
}
