package com.smutkiewicz.pagenotifier.utilities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

// kod oparto na przykładowej implementacji przygotowanej
// przez firmę Google: bit.ly/DividerItemDecoration.
public class ItemDivider extends RecyclerView.ItemDecoration {
   private final Drawable divider;

   public ItemDivider(Context context) {
      int[] attrs = {android.R.attr.listDivider};
      divider = context.obtainStyledAttributes(attrs).getDrawable(0);
   }

   // rysuje linie rozdzielające elementy widoku RecyclerView
   @Override
   public void onDrawOver(Canvas c, RecyclerView parent,
                          RecyclerView.State state) {
      super.onDrawOver(c, parent, state);

      int left = parent.getPaddingLeft();
      int right = parent.getWidth() - parent.getPaddingRight();

      for (int i = 0; i < parent.getChildCount() - 1; ++i) {
         View item = parent.getChildAt(i);

         int top = item.getBottom() + ((RecyclerView.LayoutParams)
            item.getLayoutParams()).bottomMargin;
         int bottom = top + divider.getIntrinsicHeight();

         divider.setBounds(left, top, right, bottom);
         divider.draw(c);
      }
   }
}
