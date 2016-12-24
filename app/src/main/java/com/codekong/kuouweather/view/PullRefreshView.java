package com.codekong.kuouweather.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.codekong.kuouweather.R;

/**
 * Created by szh on 2016/12/24.
 * 可以下拉刷新的ScrollView
 */

public class PullRefreshView extends LinearLayout{

    private View header;
    //顶部布局文件的高度
    private int mHeaderHight;

    //当前是在页面最顶端按下的
    private boolean isDownOnTop = true;
    //按下时的Y值
    private int startY;
    //当前状态
    private int status;
    //各自状态
    //正常状态
    private final int NONE = 0;
    //下拉状态
    private final int PULL = 1;
    //释放状态
    private final int RELEASE = 2;
    //正在刷新的状态
    private final int REFRESHING = 3;

    public PullRefreshView(Context context) {
        this(context, null);
    }

    public PullRefreshView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullRefreshView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    /**
     * 初始化界面添加顶部布局文件到ScrollView
     * @param context
     */
    private void initView(Context context){
        LayoutInflater inflater = LayoutInflater.from(context);
        header = inflater.inflate(R.layout.header_layout, null);

        measureView(header);
        mHeaderHight = header.getMeasuredHeight();
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //记录下y坐标
                startY = (int) event.getRawY();
                break;

            case MotionEvent.ACTION_MOVE:
                onMove(event);
                startY = (int) event.getRawY();
                break;

            case MotionEvent.ACTION_UP:
                //fling();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 移动过程中的操作
     * @param ev
     */
    private void onMove(MotionEvent ev){
        //当前移动到的Y值
        int tempY = (int) ev.getRawY();
        int space = tempY - startY;
        int topPadding = space - mHeaderHight;

        if (space > -1 && space < 6){

        }
        switch (status){
            case NONE:
                if (space > 0){
                    status = PULL;
                }
                break;
            case PULL:
                topPadding(topPadding);
                if (space > mHeaderHight + 300){
                    status = RELEASE;
                }
                break;

            case RELEASE:
                topPadding(topPadding);
                if (space < mHeaderHight + 300){
                    status = PULL;
                }else if (space <= 0){
                    status = NONE;
                    isDownOnTop = true;
                }
                break;
            case REFRESHING:
                break;
            default:
                break;
        }

    }
}
