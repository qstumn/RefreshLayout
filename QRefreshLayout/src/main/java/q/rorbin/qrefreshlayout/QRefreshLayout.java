package q.rorbin.qrefreshlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.FloatRange;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;

import q.rorbin.qrefreshlayout.listener.RefreshHandler;
import q.rorbin.qrefreshlayout.widget.QLoadView;
import q.rorbin.qrefreshlayout.widget.classics.FooterView;
import q.rorbin.qrefreshlayout.widget.classics.HeaderView;
import q.rorbin.qrefreshlayout.widget.material.MaterialBlackFooterView;
import q.rorbin.qrefreshlayout.widget.material.MaterialBlackHeaderView;
import q.rorbin.qrefreshlayout.widget.material.MaterialHeaderView;

import static android.R.attr.enabled;
import static android.net.wifi.WifiConfiguration.Status.DISABLED;
import static android.net.wifi.WifiConfiguration.Status.ENABLED;

/**
 * @author chqiu
 *         Email:qstumn@163.com
 */
public class QRefreshLayout extends FrameLayout {
    private RefreshHandler mHandler;
    private QLoadView mHeaderView;
    private QLoadView mFooterView;
    private View mTarget;
    private float mResistance;
    private boolean mHeaderPulling;
    private boolean mFooterPulling;
    private boolean mLoadMoreEnable;
    private float mTouchY;
    private int mMovedDisY;
    private float mRefreshDis;
    private int mMode;
    public final static int MODE_REFRESH = 1;
    public final static int MODE_LOADMORE = 2;

    private int mTouchSlop;

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
        float resistance = typedArray.getFloat(R.styleable.QRefreshLayout_resistance, 0.6f);
        setResistance(resistance);
        typedArray.recycle();
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getChildCount() > 1) {
            throw new IllegalStateException("QRefreshLayout can only have one child");
        }
        mTarget = getChildAt(0);
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


    public void setHeaderView(QLoadView view) {
        addLoadView(view, mHeaderView);
        mHeaderView = view;
    }


    public void setFooterView(QLoadView view) {
        addLoadView(view, mFooterView);
        mFooterView = view;
    }

    private void addLoadView(QLoadView newView, QLoadView oldView) {
        if (newView == oldView) return;
        if (oldView != null) removeView(oldView);
        newView.addToRefreshLayout(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (MotionEventCompat.getActionMasked(event)) {
            case MotionEvent.ACTION_DOWN: {
                mTouchY = event.getY();
                mMovedDisY = 0;
                mTarget.setClickable(true);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float currY = event.getY();
                float dy = currY - mTouchY;
                mTouchY = currY;
                if (dy > 0 && !canTargetScrollUp() && !mFooterPulling) {
                    mMode = MODE_REFRESH;
                } else if (dy < 0 && mLoadMoreEnable && !canTargetScrollDown() && !mHeaderPulling) {
                    mMode = MODE_LOADMORE;
                }
                handleScroll(dy);
                if ((mMode == MODE_REFRESH && mHeaderPulling) || (mMode == MODE_LOADMORE && mFooterPulling)) {
                    if (Math.abs(dy) > mTouchSlop) {
                        return true;
                    }
                    break;
                }
            }
            case MotionEvent.ACTION_UP: {
                float slop = mTouchSlop / 2f;
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

    private void cancelPressed(View view, MotionEvent event) {
        MotionEvent obtain = MotionEvent.obtain(event);
        obtain.setAction(MotionEvent.ACTION_CANCEL);
        view.onTouchEvent(obtain);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRefreshDis = h / 6;
    }


    private void onPointerUp() {
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

    private void onPointerUp(QLoadView view) {
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
            if (view.equals(mHeaderView)) {
                mHeaderPulling = false;
            } else if (view.equals(mFooterView)) {
                mFooterPulling = false;
            }
        }
    }


    private void handleScroll(float dy) {
        if (mMode == MODE_REFRESH) {
            handleHeaderScroll(dy);
        } else if (mMode == MODE_LOADMORE) {
            handleFooterScroll(dy);
        }
    }

    private void handleHeaderScroll(float dy) {
        if (!canTargetScrollUp() && dy > 0) {
            mHeaderPulling = true;
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
            mHeaderPulling = true;
            mMovedDisY += dy;
            mHeaderView.onPulling(dy, mTarget);
            if (mMovedDisY < 0) mMovedDisY = 0;
            if (mMovedDisY < mRefreshDis && mHeaderView.getState() != QLoadView.STATE.REFRESH
                    && mHeaderView.getState() != QLoadView.STATE.START) {
                mHeaderView.setState(QLoadView.STATE.START);
            }
        }
        if (mHeaderView.canTargetScroll()) {
            mHeaderPulling = false;
        }
    }

    private void handleFooterScroll(float dy) {
        if (dy < 0 && !canTargetScrollDown()) {
            dy = Math.abs(dy);
            mFooterPulling = true;
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
            mFooterPulling = true;
            mFooterView.onPulling(-dy, mTarget);
            mMovedDisY -= dy;
            if (mMovedDisY < 0) mMovedDisY = 0;
            if (mMovedDisY < mRefreshDis && mFooterView.getState() != QLoadView.STATE.REFRESH
                    && mFooterView.getState() != QLoadView.STATE.START) {
                mFooterView.setState(QLoadView.STATE.START);
            }
        }
        if (mFooterView.canTargetScroll()) {
            mFooterPulling = false;
        }
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
            return mTarget.canScrollVertically(-1);
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
            return mTarget.canScrollVertically(1);
        }
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
    public void setResistance(@FloatRange(from = 0f, to = 1f) float resistance) {
        if (resistance >= 0 && resistance <= 1f)
            mResistance = 1000f - 1000f * resistance;
    }

    public void setLoadMoreEnable(boolean enable) {
        mLoadMoreEnable = enable;
    }

}
