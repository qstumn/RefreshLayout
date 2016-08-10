package q.rorbin.qrefreshlayout;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ScrollView;

/**
 * @author chqiu
 *         Email:qstumn@163.com
 */
public class QRefreshLayout extends FrameLayout {
    private RefreshHandler mHandler;

    private int mFinalHeight = QRefreshUtil.dp2px(getContext(), 50);

    private int mRefreshHeight = (int) (mFinalHeight * 1.3d);

    private QLoadView mHeaderView;
    private QLoadView mFooterView;
    private View mTarget;

    private double mDragIndex = 1f;

    private final double mRatio = 400d;

    private int mAnimeDuration = 300;

    private boolean mRefreshing = false;

    private boolean mLoading = false;

    private boolean mAction = false;

    private boolean mLoadMoreEnable = false;

    private float mTouchY;

    private int mMode = -1;

    private final int MODE_REFRESH = 0;

    private final int MODE_LOADMORE = 1;

    public static final int STYLE_CLASSIC = 2;
    public static final int STYLE_GOOGLE = 3;

    private int mStyle = STYLE_CLASSIC;

    public QRefreshLayout(Context context) {
        this(context, null);
    }

    public QRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.QRefreshLayout);
        mStyle = typedArray.getInteger(R.styleable.QRefreshLayout_refreshStyle, STYLE_CLASSIC);
        typedArray.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 1) {
            throw new IllegalStateException("QRefreshLayout can only have one child");
        }
        mTarget = getChildAt(0);
        if (mHeaderView == null) {
            createDefaultHeaderView();
        }
        if (mFooterView == null) {
            createDefaultFooterView();
        }
    }


    public void setHeaderView(QLoadView view) {
        if (view == mHeaderView) return;
        if (mHeaderView != null) removeView(mHeaderView);
        mHeaderView = view;
        LayoutParams headerParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        addView(mHeaderView, headerParams);
    }


    public void setFooterView(QLoadView view) {
        if (view == mFooterView) return;
        if (mFooterView != null) removeView(mFooterView);
        mFooterView = view;
        LayoutParams footerParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        footerParams.gravity = Gravity.BOTTOM;
        addView(mFooterView, footerParams);
    }


    public void setRefreshingHeight(int height) {
        mFinalHeight = height;
        mRefreshHeight = (int) (mFinalHeight * 1.3d);
    }

    private void createDefaultHeaderView() {
        setHeaderView(new HeaderView(getContext()));
    }

    private void createDefaultFooterView() {
        setFooterView(new FooterView(getContext()));
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN: {
                mTouchY = event.getY();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float currY = event.getY();
                float dy = currY - mTouchY;
                mTouchY = currY;
                if (dy > 0 && !canTargetScrollUp() && !mAction) {
                    mMode = MODE_REFRESH;
                } else if (dy < 0 && mLoadMoreEnable && !canTargetScrollDown() && !mAction) {
                    mMode = MODE_LOADMORE;
                }
                handleScroll(dy);
                if (mStyle == STYLE_GOOGLE && (mHeaderView.getHeight() != 0 || mFooterView.getHeight() != 0))
                    return true;
                break;
            }
            case MotionEvent.ACTION_UP: {
                onPointerUp();
                break;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void onPointerUp() {
        if (mMode == MODE_REFRESH) {
            onRefreshPointerUp();
        } else if (mMode == MODE_LOADMORE) {
            onLoadPointUp();
        }
    }

    private void onRefreshPointerUp() {
        if (!mRefreshing) onPointerUp(mHeaderView);
    }

    private void onLoadPointUp() {
        if (!mLoading) onPointerUp(mFooterView);
    }

    private void onPointerUp(QLoadView view) {
        int state = -1;
        int height = view.getHeight();
        if (height > mRefreshHeight) {
            height = mFinalHeight;
            state = QLoadView.STATE_REFRESH;
        } else if (height < mRefreshHeight) {
            height = 0;
            state = QLoadView.STATE_NORMAL;
        }
        startPullAnime(view, height, null);
        syncLoadViewState(view, state);
    }


    private void startPullAnime(final View view, int newHeight, Animator.AnimatorListener listener) {
        ValueAnimator anime = ValueAnimator.ofInt(view.getHeight(), newHeight);
        anime.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int h = Integer.parseInt(animation.getAnimatedValue().toString());
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                params.height = h;
                view.setLayoutParams(params);
                if (view.equals(mHeaderView)) handleTargetHeight(h);
                else if (view.equals(mFooterView)) handleTargetHeight(-h);

            }
        });
        if (listener != null)
            anime.addListener(listener);
        anime.setDuration(mAnimeDuration);
        anime.start();
    }

    private void handleScroll(float dy) {
        if (mMode == MODE_REFRESH) {
            mAction = true;
            handleHeaderScroll(dy);
        } else if (mMode == MODE_LOADMORE) {
            mAction = true;
            handleFooterScroll(dy);
        }
    }


    private void handleHeaderScroll(float dy) {
        LayoutParams params = (LayoutParams) mHeaderView.getLayoutParams();
        if (!canTargetScrollUp() && dy > 0) {
            if (mRefreshing) {
                params.height += dy;
                if (params.height > mFinalHeight) params.height = mFinalHeight;
            } else {
                mDragIndex = Math.exp(-(params.height / mRatio));
                if (mDragIndex < 0) mDragIndex = 0;
                params.height += dy * mDragIndex;
                if (params.height > mRefreshHeight) {
                    syncLoadViewState(mHeaderView, QLoadView.STATE_PULLING);
                }
            }
        }

        if (dy < 0) {
            params.height += dy;
            if (params.height < 0) {
                params.height = 0;
                syncLoadViewState(mHeaderView, QLoadView.STATE_NORMAL);
                mAction = false;
            } else if (params.height < mRefreshHeight) {
                syncLoadViewState(mHeaderView, QLoadView.STATE_NORMAL);
            }
        }
        mHeaderView.setLayoutParams(params);
        handleTargetHeight(params.height);
    }

    private void handleFooterScroll(float dy) {
        LayoutParams params = (LayoutParams) mFooterView.getLayoutParams();
        if (dy < 0 && !canTargetScrollDown()) {
            if (mLoading) {
                params.height -= dy;
                if (params.height > mFinalHeight) params.height = mFinalHeight;
            } else {
                mDragIndex = Math.exp(-(params.height / mRatio));
                if (mDragIndex < 0) mDragIndex = 0;
                params.height -= dy * mDragIndex;
                if (params.height > mRefreshHeight) {
                    syncLoadViewState(mFooterView, QLoadView.STATE_PULLING);
                }
            }
        }

        if (dy > 0) {
            params.height -= dy;
            if (params.height < 0) {
                params.height = 0;
                syncLoadViewState(mFooterView, QLoadView.STATE_NORMAL);
                mAction = false;
            } else if (params.height < mRefreshHeight) {
                syncLoadViewState(mFooterView, QLoadView.STATE_NORMAL);
            }
        }
        mFooterView.setLayoutParams(params);
        handleTargetHeight(-params.height);
    }

    private void syncLoadViewState(QLoadView view, int state) {
        if ((mRefreshing && view.equals(mHeaderView))
                || (mLoading && view.equals(mFooterView))) {
            view.setRefreshing();
        } else if (state == QLoadView.STATE_NORMAL) {
            view.setNormal();
        } else if (state == QLoadView.STATE_PULLING) {
            view.setPulling();
        } else if (state == QLoadView.STATE_REFRESH) {
            view.setRefreshing();
            if (view.equals(mHeaderView)) {
                mRefreshing = true;
                if (mHandler != null)
                    mHandler.onRefresh(this);
            } else if (view.equals(mFooterView)) {
                mLoading = true;
                if (mHandler != null)
                    mHandler.onLoadMore(this);
            }
        }
    }

    private void handleTargetHeight(int height) {
        if (mStyle == STYLE_CLASSIC)
            mTarget.setTranslationY(height);
    }


    private boolean canTargetScrollUp() {
        if (mTarget == null)
            return false;
        if (Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    private boolean canTargetScrollDown() {
        if (mTarget == null)
            return false;
        if (Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getLastVisiblePosition() == absListView.getChildCount() - 1
                        || absListView.getChildAt(absListView.getChildCount() - 1)
                        .getBottom() <= absListView.getPaddingBottom());
            } else {
                return ViewCompat.canScrollVertically(mTarget, 1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, 1);
        }
    }

    public void setRefreshHandler(RefreshHandler handler) {
        mHandler = handler;
    }

    public void refreshBegin() {
        startPullAnime(mHeaderView, mFinalHeight, null);
        syncLoadViewState(mHeaderView, QLoadView.STATE_REFRESH);
    }

    public void refreshComplete() {
        mRefreshing = false;
        mHeaderView.setComplete();
        startPullAnime(mHeaderView, 0, new AnimeListener(mHeaderView));
    }

    public void LoadMoreComplete() {
        mLoading = false;
        mFooterView.setComplete();
        startPullAnime(mFooterView, 0, new AnimeListener(mFooterView));
        handleTargetBottom();
    }

    public void setLoadMoreEnable(boolean enable) {
        mLoadMoreEnable = enable;
    }

    /**
     * @param style Use one of {@link #STYLE_CLASSIC},{@link #STYLE_GOOGLE}
     */
    public void setRefreshStyle(int style) {
        if (style != STYLE_CLASSIC && style != STYLE_GOOGLE)
            throw new IllegalStateException("not support style");
        mStyle = style;
    }

    private class AnimeListener extends AnimatorListenerAdapter {
        private QLoadView mView;

        public AnimeListener(QLoadView view) {
            mView = view;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (mView != null) mView.setNormal();
        }
    }

    private void handleTargetBottom() {
        mTarget.post(new Runnable() {
            @Override
            public void run() {
                if (mTarget instanceof AbsListView) {
                    ((AbsListView) mTarget).setSelection(((AbsListView) mTarget).getAdapter().getCount() - 1);
                } else if (mTarget instanceof RecyclerView) {
                    ((RecyclerView) mTarget).scrollToPosition(((RecyclerView) mTarget).getAdapter().getItemCount() - 1);
                } else if (mTarget instanceof ScrollView) {
                    ((ScrollView) mTarget).fullScroll(View.FOCUS_DOWN);
                }
            }
        });
    }
}
