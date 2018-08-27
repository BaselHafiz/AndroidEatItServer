package com.bmacode17.androideatitserver.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bmacode17.androideatitserver.R;
import com.bmacode17.androideatitserver.commons.Common;
import com.bmacode17.androideatitserver.interfaces.ItemClickListener;

/**
 * Created by User on 19-Jul-18.
 */

public class MenuViewHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener ,
        View.OnCreateContextMenuListener{

    public TextView textView_menuName;
    public ImageView imageView_menuImage;
    private ItemClickListener itemClickListener;

    public MenuViewHolder(View itemView) {
        super(itemView);

        textView_menuName = (TextView) itemView.findViewById(R.id.textView_menuName);
        imageView_menuImage = (ImageView) itemView.findViewById(R.id.imageView_menuImage);
        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v,getAdapterPosition(),false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

        menu.setHeaderTitle("Select the action");

        menu.add(0,0,getAdapterPosition(), Common.UPDATE);
        menu.add(0,1,getAdapterPosition(), Common.DELETE);
    }
}
