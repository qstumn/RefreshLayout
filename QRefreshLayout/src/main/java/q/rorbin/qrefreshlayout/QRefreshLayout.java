package q.rorbin.qrefreshlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import q.rorbin.qrefreshlayout.listener.RefreshHandler;
import q.rorbin.qrefreshlayout.widget.ILoadView;
import q.rorbin.qrefreshlayout.widget.QLoadView;
import q.rorbin.qrefreshlayout.widget.classics.FooterView;
import q.rorbin.qrefreshlayout.widget.classics.HeaderView;

/**
 * @author chqiu
 *         Email:qstumn@163.com
 */
public class QRefreshLayout extends FrameLayout implements NestedScrollingChild, NestedScrollingParent {
    private RefreshHandler mHandler;
    protected ILoadView mHeaderView;
    protected ILoadView mFooterView;
    protected View mTarget;
    private float mResistance;
    protected boolean mLoadMoreEnable;
    protected float mTouchY;
    protected int mMovedDisY;
    private float mRefreshDis;
    protected int mMode;
    public final static int MODE_REFRESH = 1;
    public final static int MODE_LOADMORE = 2;

    private NestedScrollingChildHelper mNestedChildHelper;
    private NestedScrollingParentHelper mNestedParentHelper;
    private int[] mParentOffsetInWindow;

    protected int mTouchSlop;

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
        float resistance = typedArray.getFloat(R.styleable.QRefreshLayout_resistance, 0.65f);
        setResistance(resistance);
        typedArray.recycle();
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        mNestedChildHelper = new NestedScrollingChildHelper(this);
        mNestedParentHelper = new NestedScrollingParentHelper(this);
        mParentOffsetInWindow = new int[2];
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 1) {
            throw new IllegalStateException("QRefreshLayout can only have one child");
        }
        mTarget = getChildAt(0);
        mTarget.setClickable(true);
        boolean targetNestedScrollingEnabled = false;
        if (mTarget instanceof NestedScrollingChild) {
            targetNestedScrollingEnabled = ((NestedScrollingChild) mTarget).isNestedScrollingEnabled();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            targetNestedScrollingEnabled = mTarget.isNestedScrollingEnabled();
        }
        setNestedScrollingEnabled(targetNestedScrollingEnabled);
        ViewGroup.LayoutParams params = mTarget.getLayoutParams();
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.MATCH_PARENT;
        mTarget.setLayoutParams(params);
        if (mHeaderView == null) {
            setHeaderView(new HeaderView(getContext()));
        }
        if (mFooterView == null) {
            setFooterView(new FooterView(getContext()));
        }
    }

    public void setHeaderView(ILoadView view) {
        addLoadView(view, mHeaderView);
        mHeaderView = view;
    }

    public void setFooterView(ILoadView view) {
        addLoadView(view, mFooterView);
        mFooterView = view;
    }

    private void addLoadView(ILoadView newView, ILoadView oldView) {
        if (newView == oldView) return;
        if (oldView != null) removeView(oldView.getView());
        newView.addToRefreshLayout(this);
    }

    protected boolean isNestedScrollInProgress() {
        return isNestedScrollingEnabled();
    }

    //NestedScrollingParent
    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isNestedScrollingEnabled() && isEnabled() &&
                (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        mNestedParentHelper.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
        mMovedDisY = 0;
    }

    @Override
    public void onStopNestedScroll(View child) {
        mNestedParentHelper.onStopNestedScroll(child);
        onPointerUp();
        mMovedDisY = 0;
        stopNestedScroll();
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        if (dy > 0 && (mMovedDisY > 0 || !mHeaderView.canTargetScroll()) && mMode == MODE_REFRESH) {
            if (dy > mMovedDisY) {
                consumed[1] = dy - mMovedDisY;
            } else {
                consumed[1] = dy;
            }
            handleHeaderScroll(-dy);
        } else if (mLoadMoreEnable && dy < 0 && (mMovedDisY > 0 || !mFooterView.canTargetScroll())
                && mMode == MODE_LOADMORE) {
            if (Math.abs(dy) > mMovedDisY) {
                consumed[1] = dy + mMovedDisY;
            } else {
                consumed[1] = dy;
            }
            handleFooterScroll(-dy);
        }
        final int[] parentConsumed = new int[2];
        if (dispatchNestedPreScroll(dx, dy - consumed[1], parentConsumed, null)) {
            consumed[1] += parentConsumed[1];
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, mParentOffsetInWindow);
        final int dy = dyUnconsumed + mParentOffsetInWindow[1];
        if (dy < 0 && !canTargetScrollUp()) {
            mMode = MODE_REFRESH;
            handleHeaderScroll(-dy);
        } else if (mLoadMoreEnable && dy > 0 && !canTargetScrollDown()) {
            mMode = MODE_LOADMORE;
            handleFooterScroll(-dy);
        }
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return flingAndDispatch(velocityX, velocityY);
    }

    private boolean flingAndDispatch(float velocityX, float velocityY) {
        boolean consumed = dispatchNestedPreFling(velocityX, velocityY);
        if (!consumed) {
            return consumeFling(velocityY);
        }
        return true;
    }

    private boolean consumeFling(float velocityY) {
        boolean consumeFling = false;
        if (mMode == MODE_REFRESH) {
            consumeFling = velocityY > 0 && !canTargetScrollUp();
        } else if (mMode == MODE_LOADMORE) {
            consumeFling = velocityY < 0 && !canTargetScrollDown();
        }
        return consumeFling;
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedParentHelper.getNestedScrollAxes();
    }

    //    NestedScrollingChild
    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNestedChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, @Nullable @Size(value = 2) int[] offsetInWindow) {
        return mNestedChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, @Nullable @Size(value = 2) int[] consumed, @Nullable @Size(value = 2) int[] offsetInWindow) {
        return mNestedChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (isNestedScrollInProgress()) {
            return super.dispatchTouchEvent(event);
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mTouchY = event.getY();
                mMovedDisY = 0;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float currY = event.getY();
                float dy = currY - mTouchY;
                mTouchY = currY;
                if (dy > 0 && !canTargetScrollUp() && mFooterView.canTargetScroll()) {
                    mMode = MODE_REFRESH;
                } else if (dy < 0 && mLoadMoreEnable && !canTargetScrollDown()
                        && mHeaderView.canTargetScroll()) {
                    mMode = MODE_LOADMORE;
                }
                handleScroll(dy);
                if ((mMode == MODE_REFRESH && !mHeaderView.canTargetScroll()) ||
                        (mMode == MODE_LOADMORE && !mFooterView.canTargetScroll())) {
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                float slop = mTouchSlop / 2f;
                //if mMovedDisY < mTouchSlop this event is targetView do onClick
                if (mMovedDisY > (slop > 0 ? slop : mTouchSlop)) {
                    onPointerUp();
                    cancelPressed(mTarget, event);
                    return true;
                }
                break;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    protected void cancelPressed(View view, MotionEvent event) {
        MotionEvent obtain = MotionEvent.obtain(event);
        obtain.setAction(MotionEvent.ACTION_CANCEL);
        view.dispatchTouchEvent(obtain);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRefreshDis = h / 6;
    }


    protected void onPointerUp() {
        if (mMode == MODE_REFRESH) {
            onRefreshPointerUp();
        } else if (mMode == MODE_LOADMORE) {
            onLoadPointUp();
        }
    }

    private void onRefreshPointerUp() {
        if (mHeaderView.getState() != QLoadView.STATE.REFRESH) {
            onPointerUp(mHeaderView);
        }
    }

    private void onLoadPointUp() {
        if (mFooterView.getState() != QLoadView.STATE.REFRESH) {
            onPointerUp(mFooterView);
        }
    }

    private void onPointerUp(ILoadView view) {
        if (mMovedDisY >= mRefreshDis && view.getState() != QLoadView.STATE.REFRESH) {
            view.setState(QLoadView.STATE.REFRESH);
            view.onRefreshBegin(mTarget);
            if (mHandler != null) {
                if (view.equals(mHeaderView)) {
                    mHandler.onRefresh(this);
                } else if (view.equals(mFooterView)) {
                    mHandler.onLoadMore(this);
                }
            }
        } else if (mMovedDisY < mRefreshDis) {
            view.onPrepare(mTarget);
        }
    }


    protected void handleScroll(float dy) {
        if (mMode == MODE_REFRESH) {
            handleHeaderScroll(dy);
        } else if (mMode == MODE_LOADMORE) {
            handleFooterScroll(dy);
        }
    }

    protected void handleHeaderScroll(float dy) {
        if (!canTargetScrollUp() && dy > 0) {
            double dragIndex = Math.exp(-(mMovedDisY / mResistance));
            if (dragIndex < 0) dragIndex = 0;
            dy = (float) (dy * dragIndex);
            mMovedDisY += dy;
            mHeaderView.onPulling(dy, mTarget);
            if (mMovedDisY >= mRefreshDis && mHeaderView.getState() != QLoadView.STATE.REFRESH
                    && mHeaderView.getState() != QLoadView.STATE.PULL) {
                mHeaderView.setState(QLoadView.STATE.PULL);
            }
        } else if (!canTargetScrollUp() && dy < 0) {
            mMovedDisY += dy;
            mHeaderView.onPulling(dy, mTarget);
            if (mMovedDisY < 0) mMovedDisY = 0;
            if (mMovedDisY < mRefreshDis && mHeaderView.getState() != QLoadView.STATE.REFRESH
                    && mHeaderView.getState() != QLoadView.STATE.START) {
                mHeaderView.setState(QLoadView.STATE.START);
            }
        }
    }

    protected void handleFooterScroll(float dy) {
        if (dy < 0 && !canTargetScrollDown()) {
            dy = Math.abs(dy);
            double dragIndex = Math.exp(-(mMovedDisY / mResistance));
            if (dragIndex < 0) dragIndex = 0;
            dy = (float) (dy * dragIndex);
            mMovedDisY += dy;
            mFooterView.onPulling(dy, mTarget);
            if (mMovedDisY >= mRefreshDis && mFooterView.getState() != QLoadView.STATE.REFRESH
                    && mFooterView.getState() != QLoadView.STATE.PULL) {
                mFooterView.setState(QLoadView.STATE.PULL);
            }
        } else if (dy > 0 && !canTargetScrollDown()) {
            mFooterView.onPulling(-dy, mTarget);
            mMovedDisY -= dy;
            if (mMovedDisY < 0) mMovedDisY = 0;
            if (mMovedDisY < mRefreshDis && mFooterView.getState() != QLoadView.STATE.REFRESH
                    && mFooterView.getState() != QLoadView.STATE.START) {
                mFooterView.setState(QLoadView.STATE.START);
            }
        }
    }


    protected boolean canTargetScrollUp() {
        if (mTarget == null)
            return false;
        return mTarget.canScrollVertically(-1);
    }

    protected boolean canTargetScrollDown() {
        if (mTarget == null)
            return false;
        return mTarget.canScrollVertically(1);
    }

    public void setRefreshHandler(RefreshHandler handler) {
        mHandler = handler;
    }

    public void refreshComplete() {
        mHeaderView.setState(QLoadView.STATE.COMPLETE);
        mHeaderView.onPrepare(mTarget);
    }

    public void loadMoreComplete() {
        mFooterView.setState(QLoadView.STATE.COMPLETE);
        mFooterView.onPrepare(mTarget);
    }

    /**
     * @param resistance range 0 ~ 1f
     */
    public void setResistance(float resistance) {
        if (resistance >= 0 && resistance <= 1f)
            mResistance = 1000f - 1000f * resistance;
    }

    public void setLoadMoreEnable(boolean enable) {
        mLoadMoreEnable = enable;
    }

}
