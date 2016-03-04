package com.pratilipi.android.reader;

import android.content.Context;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Rahul Ranjan on 3/3/2016.
 */
public class ReaderTextView extends TextView {

    private int mLineY;
    private int mViewWidth;
    private int mLeftPadding;

    public ReaderTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint paint = getPaint();
        paint.setColor(getCurrentTextColor());
        paint.drawableState = getDrawableState();
        mLeftPadding = getPaddingLeft();
        mViewWidth = getMeasuredWidth() - mLeftPadding - getPaddingRight();
        String text = "" + getText();
        mLineY = 0;
        mLineY += getTextSize();
        Layout layout = getLayout();
        for (int i = 0; i < layout.getLineCount(); i++) {
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
            String line = text.substring(lineStart, lineEnd);

            float width = StaticLayout.getDesiredWidth(text, lineStart, lineEnd, getPaint());
            if (needScale(line)) {
                drawScaledText(canvas, lineStart, line, width);
            } else {
                canvas.drawText(line, mLeftPadding, mLineY, paint);
            }

            mLineY += getLineHeight();
        }
    }

    private void drawScaledText(Canvas canvas, int lineStart, String line, float lineWidth) {
        float x = mLeftPadding;
        if (isFirstLineOfParagraph(lineStart, line)) {
            String blanks = "  ";
            canvas.drawText(blanks, x, mLineY, getPaint());
            float bw = StaticLayout.getDesiredWidth(blanks, getPaint());
            x += bw;

            line = line.substring(3);
        }

        String[] words = line.split(" ");
        float d = (mViewWidth - lineWidth) / (words.length - 1);
        for(int i=0; i<words.length; ++i) {
            float cw = StaticLayout.getDesiredWidth(words[i], getPaint());
            canvas.drawText(words[i], x, mLineY, getPaint());
            x += cw;
            if (i != words.length - 1){
                canvas.drawText(" ", x, mLineY, getPaint());
                x += d + StaticLayout.getDesiredWidth(" ", getPaint());
            }
        }
    }

    private boolean isFirstLineOfParagraph(int lineStart, String line) {
        return line.length() > 3 && line.charAt(0) == ' ' && line.charAt(1) == ' ';
    }

    private boolean needScale(String line) {
        if (line.length() == 0) {
            return false;
        } else {
            return line.charAt(line.length() - 1) != '\n';
        }
    }
}
