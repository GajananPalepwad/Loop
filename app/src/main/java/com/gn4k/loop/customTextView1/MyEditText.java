package com.gn4k.loop.customTextView1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Author S Mahbub Uz Zaman on 5/9/15.
 * License Under MIT
 */
public class MyEditText extends EditText {

    private Rect rect;
    private Paint paint;
    private int padding;

    public MyEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        rect = new Rect();
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize(45);
        padding = (int) (paint.measureText("000") + 10); // Add more padding based on max line number width
        setPadding(padding, getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int baseline = getBaseline();
        for (int i = 0; i < getLineCount(); i++) {
            canvas.drawText("" + (i + 1), rect.left + 10, baseline, paint); // Adjust the left position with +10 for more padding
            baseline += getLineHeight();
        }
        super.onDraw(canvas);
    }
}
