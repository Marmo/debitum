package org.ebur.debitum.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.util.AttributeSet;

public class AvatarView extends androidx.appcompat.widget.AppCompatImageView {

    private Path clipPath = new Path();

    public AvatarView(Context context) {
        super(context);
    }

    public AvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AvatarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void initClipPath() {
        clipPath = new Path();
        clipPath.addCircle(
                this.getWidth()/2f,
                this.getHeight()/2f,
                Math.min(this.getWidth(), this.getHeight()),
                Path.Direction.CCW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        clipPath.reset();
        clipPath.addCircle(
                this.getWidth()/2f,
                this.getHeight()/2f,
                Math.min(this.getWidth(), this.getHeight()),
                Path.Direction.CCW);
        canvas.clipPath(clipPath);
        super.onDraw(canvas);
    }
}

