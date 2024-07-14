package site.gitzhai.mydemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyView2 extends View {

    private final Model mModel = new Model();
    private Paint mPaint = null;
    private Paint mPaint2 = null;


    private static class Model {
        private float a = 0;
        private float b = 0;
        private float c = 0;
        private float d = 0;

        private float maxX = 0;

        private final Ball ball1 = new Ball();
        private final Ball ball2 = new Ball();

        public void update(float a, float b, float c, float d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;

            maxX = 2*a;
        }

        public void updateBall1(float x, float y, float r, float q) {
            ball1.set(x, y, r, q);
        }

        public void updateBall2(float x, float y, float r, float q) {
            ball2.set(x, y, r, q);
        }
    }

    private static class Ball {
        private float x = 0;
        private float y = 0;
        private float r = 0;
        private float q = 0;

        public void set(float x, float y, float r, float q) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.q = q;
        }
    }


    public MyView2(Context context) {
        this(context, null);
    }

    public MyView2(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        drawShutterButton(canvas);
        drawTouchPoint(canvas);
    }


    private void drawTouchPoint(Canvas canvas) {
        if (mbTouchDown) {
            float x1 = mDownX;
            float y1 = mDownY;
            float x2 = mShutterX;
            float y2 = mShutterY;
            float r1 = mTouchPointRadius;
            float r2 = mShutterMaxRadius - (mShutterMaxRadius - mShutterMinRadius) * mTouchProgress;;
            canvas.drawCircle(x1, y1, r1, mPaint);



            float d = Math.abs(y1 - y2);

            float q1 = r1 * r1;
            float q2 = r2 * r2 * 1.414f;

            float a = r1;
            float b = q1;
            float c = q2;

            mModel.update(a, b, c, d);
            mModel.updateBall1(x1, y1, r1, q1);
            mModel.updateBall2(x2, y2, r2, q2);

            Path path = new Path();
            path.moveTo(x1 - r1, y1);
            path.quadTo(x2, y2, x2 - r2, y2);
            path.lineTo(x2, y2);
            path.lineTo(x1, y1);
            path.close();

            canvas.drawPath(path, mPaint);
        }
    }


    private float mShutterRadius = 28;
    private float mShutterMaxRadius = 66;
    private float mShutterMinRadius = mShutterRadius;
    private float mShutterCornerRadius = 6;
    private float mShutterX = 6;
    private float mShutterY = 6;
    private float mTouchMaxDistance = 0;

    private boolean mbTouchDown = false;
    private float mDownX = 0;
    private float mDownY = 0;
    private float mTouchPointRadius = 66;
    private float mTouchProgress = 0;

    private void drawShutterButton(Canvas canvas) {
        float radius = mShutterMaxRadius - (mShutterMaxRadius - mShutterMinRadius) * mTouchProgress;
        float cornerRadius = radius - (radius - mShutterCornerRadius) * mTouchProgress;
        canvas.drawRoundRect(mShutterX - radius, mShutterY - radius, mShutterX + radius, mShutterY + radius, cornerRadius, cornerRadius, mPaint);
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.RED);

        mPaint2 = new Paint();
        mPaint2.setAntiAlias(true);
        mPaint2.setColor(Color.BLUE);

        mTouchMaxDistance = mTouchPointRadius * 4;
    }

    private void updateParams() {
        float width = getWidth();
        mShutterX = width * 0.5f;
        mShutterY = getHeight() - mShutterMaxRadius - 300;

        mDownX = width * 0.5f;
        mDownY = Math.max(Math.min(mDownY, mShutterY), mShutterY - mTouchMaxDistance);
        mTouchPointRadius = 66;

        float distance = Math.abs(mDownY - mShutterY);
        distance = Math.min(distance, mTouchMaxDistance);
        mTouchProgress = distance / mTouchMaxDistance;

        Log.e("test0714", "mTouchProgress: " + mTouchProgress);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        updateParams();
    }

    private PointF getChargeQuantity(float r1, float r2, float distance, float k) {
        double a = k / r1, b = k / (r1+distance),
                c = k / r2, d = k / (r2+distance), e = r1, f = r2;

        double q1 = (e*c - b*f) / (a*c + b -d);
        double q2 = (f - d*q1) / c;

        return new PointF((float) q1, (float) q2);
    }

    private void drawDisconnect(Canvas canvas, Model model, PointF disconnectPoint) {
        Path path1 = new Path();
        Path path2 = new Path();
        Path path3 = new Path();
        Path path4 = new Path();

        path1.moveTo(model.ball1.x - model.ball1.r, model.ball1.y);
        path2.moveTo(model.ball1.x + model.ball1.r, model.ball1.y);
        path3.moveTo(model.ball1.x, disconnectPoint.y);
        path4.moveTo(model.ball1.x, disconnectPoint.y);

        for (float dy = 0; dy <= model.d; dy = dy + 6) {
            if (dy > disconnectPoint.x && dy < disconnectPoint.y) {
                continue;
            }

            for (float dx = model.ball1.r; dx >= 0; dx = dx - 1) {
                if (checkPoint(model.a, model.b, model.c, model.d, dx, dy)) {
                    if (dy < disconnectPoint.x) {
                        path1.lineTo(model.ball1.x - dx, model.ball1.y + dy);
                        path2.lineTo(model.ball1.x + dx, model.ball1.y + dy);
                    } else {
                        path3.lineTo(model.ball1.x - dx, model.ball1.y + dy);
                        path4.lineTo(model.ball1.x + dx, model.ball1.y + dy);
                    }

                    break;
                }
            }
        }

        canvas.drawPath(path1, mPaint);
        canvas.drawPath(path2, mPaint);
        canvas.drawPath(path3, mPaint);
        canvas.drawPath(path4, mPaint);
    }

    long count = 0;
    long point = 0;
    private void drawConnect(Canvas canvas, Model model) {
        Path path1 = new Path();
        Path path2 = new Path();

        path1.moveTo(model.ball1.x - model.ball1.r, model.ball1.y);
        path2.moveTo(model.ball1.x + model.ball1.r, model.ball1.y);
        count = 0;
        point = 0;
        for (int dy = 0; dy <= model.d + (model.ball1.r); dy = dy + 3) {
            for (int dx = (int) model.maxX; dx >= 0; dx = dx - 1) {
                count++;
                if (checkPoint(model.a, model.b, model.c, model.d, dx, dy)) {
                    path1.lineTo(model.ball1.x - dx + 1, model.ball1.y + dy);
                    path2.lineTo(model.ball1.x + dx - 1, model.ball1.y + dy);
                    point++;
                    break;
                }
            }
        }

        Log.e("test0714count", "count: " + count + ", point: "+point);

        path1.lineTo(model.ball2.x, model.ball2.y);
        path1.lineTo(model.ball1.x, model.ball1.y);
        path1.close();
        path2.lineTo(model.ball2.x, model.ball2.y);
        path2.lineTo(model.ball1.x, model.ball1.y);
        path2.close();

        canvas.drawPath(path1, mPaint);
        canvas.drawPath(path2, mPaint);
    }

    private PointF getDisconnectY(float a, float b, float c, float d) {
        float a1 = a;
        float b1 = (c -b - a*d);
        float c1 = b*d;

        if (Float.compare(a1, 0) == 0) {
            return new PointF(-1, -1);
        }

        double b24ac = b1 * b1 - 4 * a1 * c1;

//        if (Double.compare(b24ac, 0) < 0) {
//            return new PointF(-1, -1);
//        }

        b24ac = Math.sqrt(b24ac);
        double y1 = ((-b1 + b24ac) / (2*a1));
        double y2 = ((-b1 - b24ac) / (2*a1));

        float little = (float) Math.min(y1, y2);
        float big = (float) Math.max(y1, y2);
        return new PointF(little, big);
    }

    private boolean checkPoint(float a, float b, float c, float d, float dx, float dy) {
        float x2 = dx * dx;
        float y2 = dy * dy;
        float dy2 = (d - dy) * (d - dy);
        float result = (b / (float) Math.sqrt(x2 + y2)) + (c / (float) Math.sqrt(x2 + dy2)) - a;
        return Float.compare(Math.abs(result), 3) < 0;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mDownY = event.getY();
                mbTouchDown = true;

                break;
            }

            case MotionEvent.ACTION_MOVE: {
                mDownY = event.getY();

                break;
            }

            case MotionEvent.ACTION_UP: {
//                mbTouchDown = false;
                break;
            }

            default:
                break;
        }

        updateParams();
        invalidate();

        return true;
    }
}

