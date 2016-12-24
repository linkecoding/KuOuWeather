package com.codekong.kuouweather.view;

/**
 * Created by szh on 2016/12/24.
 */

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import com.codekong.kuouweather.R;

/**
 * 刷新控制view
 *
 * @author Nono
 *
 */
public class RefreshableView extends LinearLayout {

    private static final String TAG = "LILITH";
    private Scroller scroller;
    private View header;
    private ImageView refreshIndicatorView;
    private int refreshTargetTop = -50;
    private ProgressBar bar;
    private TextView downTextView;
    private TextView timeTextView;
    private LinearLayout reFreshTimeLayout;//显示上次刷新时间的layout
    private RefreshListener refreshListener;

    //   private Long refreshTime = null;
    private int lastX;
    private int lastY;
    // 拉动标记
    private boolean isDragging = false;
    // 是否可刷新标记
    private boolean isRefreshEnabled = true;
    // 在刷新中标记
    private boolean isRefreshing = false;

    private Context mContext;

    public RefreshableView(Context context) {
        this(context, null);
    }
    public RefreshableView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView(context);
    }

    /**
     * 初始化界面添加顶部布局文件到ScrollView
     * @param context
     */
    private void initView(Context context){
        //滑动对象
        scroller = new Scroller(mContext);
        LayoutInflater inflater = LayoutInflater.from(context);
        header = inflater.inflate(R.layout.header_layout, null);

        measureView(header);
        int mHeaderHight = header.getMeasuredHeight();
        topPadding(-mHeaderHight);
        this.addView(header);
    }

    /**
     * 通知父布局占用的宽高
     */
    private void measureView(View view) {
        ViewGroup.LayoutParams p = view.getLayoutParams();
        if (p == null){
            p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int width = getChildMeasureSpec(0, 0, p.width);
        int height;
        int tempHeight = p.height;
        if (tempHeight > 0){
            height = MeasureSpec.makeMeasureSpec(tempHeight, MeasureSpec.EXACTLY);
        }else{
            height = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        view.measure(width, height);
    }

    /**
     * 设置header布局的上边距
     * @param height
     */
    private void topPadding(int height){
        header.setPadding(header.getPaddingLeft(), height,
                header.getPaddingRight(), header.getPaddingBottom());
        header.invalidate();
    }


/*

    private void init() {
        //滑动对象
        scroller = new Scroller(mContext);

        //刷新视图顶端的的view
        refreshView = LayoutInflater.from(mContext).inflate(R.layout.refresh_top_item, null);
        //指示器view
        refreshIndicatorView = (ImageView) refreshView.findViewById(R.id.indicator);
        //刷新bar
        bar = (ProgressBar) refreshView.findViewById(R.id.progress);
        //下拉显示text
        downTextView = (TextView) refreshView.findViewById(R.id.refresh_hint);
        //下来显示时间
        timeTextView = (TextView) refreshView.findViewById(R.id.refresh_time);
        reFreshTimeLayout=(LinearLayout)refreshView.findViewById(R.id.refresh_time_layout);

        LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, -refreshTargetTop);

        final float scale = getResources().getDisplayMetrics().density;
        lp.topMargin = (int) (refreshTargetTop * scale + 0.5f);

        lp.gravity = Gravity.CENTER;
        addView(refreshView, lp);
    }

*/

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int y= (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //记录下y坐标
                lastY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                //y移动坐标
                int m = y - lastY;
                if(((m < 6) && (m > -1)) || (!isDragging )){
                    doMovement(m);
                }
                //记录下此刻y坐标
                this.lastY = y;
                break;

            case MotionEvent.ACTION_UP:
                fling();
                break;
            default:
                break;
        }
        return true;
    }


    /**
     * up事件处理
     */
    private void fling() {
        // TODO Auto-generated method stub
        LinearLayout.LayoutParams lp = (LayoutParams) header.getLayoutParams();
        if(lp.topMargin > 0){//拉到了触发可刷新事件
            refresh();
        }else{
            returnInitState();
        }
    }



    private void returnInitState() {
        // TODO Auto-generated method stub
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)this.header.getLayoutParams();
        int i = lp.topMargin;
        scroller.startScroll(0, i, 0, refreshTargetTop);
        invalidate();
    }
    private void refresh() {
        // TODO Auto-generated method stub
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)this.header.getLayoutParams();
        int i = lp.topMargin;
        reFreshTimeLayout.setVisibility(View.GONE);
        refreshIndicatorView.setVisibility(View.GONE);
        bar.setVisibility(View.VISIBLE);
        timeTextView.setVisibility(View.GONE);
        downTextView.setVisibility(View.GONE);
        scroller.startScroll(0, i, 0, 0-i);
        invalidate();
        if(refreshListener !=null){
            refreshListener.onRefresh(this);
            isRefreshing = true;
        }
    }

    /**
     *
     */
    @Override
    public void computeScroll() {
        // TODO Auto-generated method stub
        if(scroller.computeScrollOffset()){
            int i = this.scroller.getCurrY();
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams)this.header.getLayoutParams();
            int k = Math.max(i, refreshTargetTop);
            lp.topMargin = k;
            this.header.setLayoutParams(lp);
            this.header.invalidate();
            invalidate();
    }
    }
    /**
     * 下拉move事件处理
     * @param moveY
     */
    private void doMovement(int moveY) {
        // TODO Auto-generated method stub
        LinearLayout.LayoutParams lp = (LayoutParams) header.getLayoutParams();
        if(moveY>0){
            //获取view的上边距
            float f1 =lp.topMargin;
            float f2 = moveY * 0.3F;
            int i = (int)(f1+f2);
            //修改上边距
            lp.topMargin = i;
            //修改后刷新
            header.setLayoutParams(lp);
            header.invalidate();
            invalidate();
        }else{
            float f1 =lp.topMargin;
            int i=(int)(f1+moveY*0.9F);
            Log.i("aa", String.valueOf(i));
            if(i>=refreshTargetTop)
            {
                lp.topMargin = i;
                //修改后刷新
                header.setLayoutParams(lp);
                header.invalidate();
                invalidate();
            }
        }
        downTextView.setVisibility(View.VISIBLE);

        refreshIndicatorView.setVisibility(View.VISIBLE);
        bar.setVisibility(View.GONE);
        if(lp.topMargin >  0){
            downTextView.setText(R.string.str_pull_down_to_refresh);
            refreshIndicatorView.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
        }else{
            downTextView.setText(R.string.str_pull_down_to_refresh);
            refreshIndicatorView.setImageResource(R.drawable.ic_arrow_downward_black_24dp);
        }

    }

    public void setRefreshEnabled(boolean b) {
        this.isRefreshEnabled = b;
    }

    public void setRefreshListener(RefreshListener listener) {
        this.refreshListener = listener;
    }


    /**
     * 结束刷新事件
     */
    public void finishRefresh(){
        LinearLayout.LayoutParams lp= (LinearLayout.LayoutParams)this.header.getLayoutParams();
        int i = lp.topMargin;
        refreshIndicatorView.setVisibility(View.VISIBLE);
        timeTextView.setVisibility(View.VISIBLE);
        scroller.startScroll(0, i, 0, refreshTargetTop);
        invalidate();
        isRefreshing = false;
    }


    /*该方法一般和ontouchEvent 一起用
     * (non-Javadoc)
     * @see android.view.ViewGroup#onInterceptTouchEvent(android.view.MotionEvent)
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        // TODO Auto-generated method stub
        int action = e.getAction();
        int y= (int) e.getRawY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                lastY = y;
                break;

            case MotionEvent.ACTION_MOVE:
                //y移动坐标
                int m = y - lastY;

                //记录下此刻y坐标
                this.lastY = y;
                if(m > 6 &&  canScroll()){
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:

                break;

            case MotionEvent.ACTION_CANCEL:

                break;
        }
        return false;
    }
    private boolean canScroll() {
        // TODO Auto-generated method stub
        View childView;
        if(getChildCount()>1){
            childView = this.getChildAt(1);
            if(childView instanceof ListView){
                int top =((ListView)childView).getChildAt(0).getTop();
                int pad =((ListView)childView).getListPaddingTop();
                if((Math.abs(top-pad)) < 3&&
                        ((ListView) childView).getFirstVisiblePosition() == 0){
                    return true;
                }else{
                    return false;
                }
            }else if(childView instanceof ScrollView){
                if(((ScrollView)childView).getScrollY() == 0){
                    return true;
                }else{
                    return false;
                }
            }

        }
        return false;
    }
    /**
     * 刷新监听接口
     * @author Nono
     *
     */
    public interface RefreshListener{
        public void onRefresh(RefreshableView view);
    }
}
