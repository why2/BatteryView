package com.shida.battery;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;


/**
 * 电池电量
 */
public class BatteryView extends View {
    private float currentPercent, remindLimitPercent, warningLimitPercent;// 当前电量百分比,提示电量百分比，警示电量百分比
    private int commonColor, remindColor, warningColor;//正常电量颜色，提示电量颜色，警示电量颜色
    private int commonFilterColor, remindFilterColor, warningFilterColor;//正常电量填充颜色，提示电量填充颜色，警示电量填充颜色
    private float boundWidth;//外边框圆环宽度
    private int widthAndHeight;//电池宽高
    private Paint paint, bezierPaint;
    private RectF oval;
    private float arcBoundSize;
    private Context context;
    private Bitmap bmBattery;//电池图片
    private float halfWidthAndHeight;
    private Path path;
    private Canvas bitmapCanvas;//bitmap canvas
    private Bitmap bitmap;//bitmap
    private int outBoundColor, outBoundColor2, bezierFilterColor;//外边框电量颜色，外边框底部颜色，贝塞尔电量填充颜色

    public BatteryView(Context context) {
        this(context, null);
    }

    public BatteryView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BatteryView);
        bmBattery = BitmapFactory.decodeResource(getResources(), R.mipmap.battery);
        remindLimitPercent = typedArray.getInteger(R.styleable.BatteryView_remindLimitPercent, 40);
        warningLimitPercent = typedArray.getInteger(R.styleable.BatteryView_warningLimitPercent, 20);
        currentPercent = typedArray.getInteger(R.styleable.BatteryView_currentPercent, 100);
        boundWidth = typedArray.getDimensionPixelSize(R.styleable.BatteryView_boundWidth, dp2px(context, 4));
        widthAndHeight = typedArray.getDimensionPixelSize(R.styleable.BatteryView_widthAndHeight, dp2px(context, 94));
        halfWidthAndHeight = widthAndHeight / 2f;
        commonColor = typedArray.getColor(R.styleable.BatteryView_commonColor, Color.parseColor("#83DD91"));
        commonFilterColor = typedArray.getColor(R.styleable.BatteryView_commonFilterColor, Color.parseColor("#1283DD91"));
        warningColor = typedArray.getColor(R.styleable.BatteryView_warningColor, Color.parseColor("#E56565"));
        warningFilterColor = typedArray.getColor(R.styleable.BatteryView_warningFilterColor, Color.parseColor("#12E56565"));
        remindColor = typedArray.getColor(R.styleable.BatteryView_remindColor, Color.parseColor("#FFC400"));
        remindFilterColor = typedArray.getColor(R.styleable.BatteryView_remindFilterColor, Color.parseColor("#12FFC400"));
        outBoundColor2 = typedArray.getColor(R.styleable.BatteryView_outBoundColor2, Color.parseColor("#4CEEF0F5"));
        typedArray.recycle();
        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        arcBoundSize = boundWidth / 2;
        oval = new RectF(arcBoundSize, arcBoundSize, widthAndHeight - arcBoundSize, widthAndHeight - arcBoundSize);
        bezierPaint = new Paint();
        bezierPaint.setAntiAlias(true);
        bezierPaint.setStyle(Paint.Style.FILL);//设置为填充，默认为填充，这里我们还是定义下
        bezierPaint.setColor(commonColor);
        bezierPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        bitmap = Bitmap.createBitmap(widthAndHeight, widthAndHeight, Bitmap.Config.ARGB_8888);
        bitmapCanvas = new Canvas(bitmap);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthAndHeight, widthAndHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBg();
        drawBezier(canvas);
        drawTextAndPic(canvas);
        drawArcEtc(canvas);
    }

    /**
     * draw bezier
     *
     * @param canvas
     */
    private void drawBezier(Canvas canvas) {
        bezierPaint.setColor(bezierFilterColor);
        path.reset();
        path.moveTo(widthAndHeight, (widthAndHeight / 2 + 150) - (currentPercent * 300f / 100));//通过此处根据进度设置高度
        path.lineTo(widthAndHeight, widthAndHeight / 2 + 200);
        path.lineTo(0, widthAndHeight / 2 +  200);
        path.lineTo(0, (widthAndHeight / 2 + 150) - (currentPercent * 300f / 100));//通过此处根据进
        for (int i = 0; i < 10; i++) {
            path.rQuadTo(60, 20, 120, 0);
            path.rQuadTo(60, -20, 120, 0);
        }
        bitmapCanvas.drawPath(path, bezierPaint);
        canvas.drawBitmap(bitmap, 0, 0, null);
        invalidate();
    }


    /**
     * draw arc etc
     *
     * @param canvas
     */
    private void drawArcEtc(Canvas canvas) {
        float sweepAngle = currentPercent * 360 / 100f;
        paint.setStrokeWidth(boundWidth);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(outBoundColor2);
        canvas.drawCircle(halfWidthAndHeight, halfWidthAndHeight, halfWidthAndHeight - arcBoundSize, paint);
        outBoundColor = commonColor;
        if (currentPercent > remindLimitPercent) {
            outBoundColor = commonColor;
            bezierFilterColor = commonFilterColor;
        } else if (currentPercent > warningLimitPercent && currentPercent <= remindLimitPercent) {
            outBoundColor = remindColor;
            bezierFilterColor = remindFilterColor;
        } else {
            outBoundColor = warningColor;
            bezierFilterColor = warningFilterColor;
        }
        paint.setColor(outBoundColor);
        canvas.drawArc(oval, -90, sweepAngle, false, paint);
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL);
        RectF oval = new RectF(halfWidthAndHeight - arcBoundSize, 0, halfWidthAndHeight + arcBoundSize, boundWidth);
        canvas.drawArc(oval, 90, 180, false, paint);
        canvas.save();//save before draw
        canvas.rotate(sweepAngle, halfWidthAndHeight, halfWidthAndHeight);
        canvas.drawArc(oval, -90, 180, false, paint);
    }


    /**
     * draw text value
     *
     * @param canvas
     */
    private void drawTextAndPic(Canvas canvas) {
        paint.setColor(Color.BLACK);
        paint.setTextSize(sp2px(context, 28));
        Paint.FontMetricsInt fontMetrics = paint.getFontMetricsInt();
        int baseline = (getHeight() - fontMetrics.bottom - fontMetrics.top) / 2;
        String text = ((int) currentPercent) + "";
        String percent = "%";
        float valueWidth = paint.measureText(text);
        paint.setTextSize(sp2px(context, 16));
        float percentWidth = paint.measureText(percent);
        paint.setTextSize(sp2px(context, 28));
        canvas.drawText(text, halfWidthAndHeight - valueWidth / 2 - percentWidth / 2, baseline - dp2px(context, 4), paint);
        paint.setTextSize(sp2px(context, 16));
        canvas.drawText(percent, halfWidthAndHeight + valueWidth / 2 - percentWidth / 2, baseline - dp2px(context, 4), paint);
        canvas.drawBitmap(bmBattery, null, new RectF(halfWidthAndHeight - dp2px(context, 10), halfWidthAndHeight + dp2px(context, 19), halfWidthAndHeight + dp2px(context, 10), halfWidthAndHeight + dp2px(context, 29)), paint);
    }


    /**
     * draw background and battery picture
     */
    private void drawBg() {
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        bitmapCanvas.drawCircle(halfWidthAndHeight, halfWidthAndHeight, halfWidthAndHeight, paint);
    }

    /**
     * dp to px
     *
     * @param context
     * @param value
     * @return
     */
    private int dp2px(Context context, int value) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics()) + 0.5f);
    }

    /**
     * dp to px
     *
     * @param context
     * @param value
     * @return
     */
    private int sp2px(Context context, int value) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, context.getResources().getDisplayMetrics()) + 0.5f);
    }

    public float getRemindLimitPercent() {
        return remindLimitPercent;
    }

    /**
     * 设置提示电量百分比
     *
     * @param remindLimitPercent
     */
    public void setRemindLimitPercent(float remindLimitPercent) {
        checkPercentValid(remindLimitPercent);
        this.remindLimitPercent = remindLimitPercent;
    }

    public float getWarningLimitPercent() {
        return warningLimitPercent;
    }

    /**
     * 设置警示电量百分比
     *
     * @param warningLimitPercent
     */
    public void setWarningLimitPercent(float warningLimitPercent) {
        checkPercentValid(warningLimitPercent);
        this.warningLimitPercent = warningLimitPercent;
    }

    public int getWarningColor() {
        return warningColor;
    }

    /**
     * 设置警示电量颜色
     *
     * @param warningColor
     */
    public void setWarningColor(@ColorInt int warningColor) {
        this.warningColor = warningColor;
    }

    public int getCommonColor() {
        return commonColor;
    }

    /**
     * 设置充足电量颜色
     *
     * @param commonColor
     */
    public void setCommonColor(@ColorInt int commonColor) {
        this.commonColor = commonColor;
    }

    public int getRemindColor() {
        return remindColor;
    }

    /**
     * 设置提醒电量颜色
     *
     * @param remindColor
     */
    public void setRemindColor(@ColorInt int remindColor) {
        this.remindColor = remindColor;
    }

    public int getWarningFilterColor() {
        return warningFilterColor;
    }


    /**
     * 设置警示电量填充颜色
     *
     * @param warningFilterColor
     */
    public void setWarningFilterColor(@ColorInt int warningFilterColor) {
        this.warningFilterColor = warningFilterColor;
    }

    public int getCommonFilterColor() {
        return commonFilterColor;
    }

    /**
     * 设置充足电量填充颜色
     *
     * @param commonFilterColor
     */
    public void setCommonFilterColor(@ColorInt int commonFilterColor) {
        this.commonFilterColor = commonFilterColor;
    }

    public int getRemindFilterColor() {
        return remindFilterColor;
    }

    /**
     * 设置提醒电量填充颜色
     *
     * @param remindFilterColor
     */
    public void setRemindFilterColor(@ColorInt int remindFilterColor) {
        this.remindFilterColor = remindFilterColor;
    }

    public float getBoundWidth() {
        return boundWidth;
    }

    /**
     * 外部边框宽度
     *
     * @param boundWidth
     */
    public void setBoundWidth(float boundWidth) {
        this.boundWidth = boundWidth;
    }

    public int getWidthAndHeight() {
        return widthAndHeight;
    }


    /**
     * 设置整个电池的宽高
     *
     * @param widthAndHeight
     */
    public void setWidthAndHeight(int widthAndHeight) {
        this.widthAndHeight = widthAndHeight;
    }

    public float getCurrentPercent() {
        return currentPercent;
    }


    /**
     * 设置当前电量百分比
     *
     * @param currentPercent 0-100
     */
    public void setCurrentPercent(float currentPercent) {
        checkPercentValid(currentPercent);
        this.currentPercent = currentPercent;
    }

    /**
     * 检测设置电量是否有效
     *
     * @param currentPercent
     */
    private void checkPercentValid(float currentPercent) {
        if (currentPercent < 0 || currentPercent > 100)
            throw new RuntimeException("battery percent invalid");
    }

    public int getOutBoundColor() {
        return outBoundColor;
    }

    /**
     * 电量边框颜色
     *
     * @param outBoundColor
     */
    public void setOutBoundColor(@ColorInt int outBoundColor) {
        this.outBoundColor = outBoundColor;
    }

    public int getOutBoundColor2() {
        return outBoundColor2;
    }

    /**
     * 设置电量外部边框颜色
     *
     * @param outBoundColor2
     */
    public void setOutBoundColor2(@ColorInt int outBoundColor2) {
        this.outBoundColor2 = outBoundColor2;
    }

    public int getBezierFilterColor() {
        return bezierFilterColor;
    }

    /**
     * 贝塞尔电量填充色
     *
     * @param bezierFilterColor
     */
    public void setBezierFilterColor(@ColorInt int bezierFilterColor) {
        this.bezierFilterColor = bezierFilterColor;
    }
}
