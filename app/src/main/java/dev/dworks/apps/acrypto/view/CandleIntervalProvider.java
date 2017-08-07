package dev.dworks.apps.acrypto.view;

import android.content.Context;
import android.support.v4.view.ActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

import static dev.dworks.apps.acrypto.home.HomeFragment.TIMESERIES_DAY;
import static dev.dworks.apps.acrypto.home.HomeFragment.TIMESERIES_WEEK;
import static dev.dworks.apps.acrypto.home.HomeFragment.TIMESERIES_YEAR;

public class CandleIntervalProvider extends ActionProvider implements MenuItem.OnMenuItemClickListener{

    private Context mContext;
    private MenuItem.OnMenuItemClickListener mOnMenuItemClickListener;
    private MenuItem mParentMenuItem;
    private int mTimeSeries;

    public CandleIntervalProvider(Context context) {
        super(context);

        mContext = context;
    }

    @Override
    public View onCreateActionView() {
        return null;
    }

    @Override
    public void onPrepareSubMenu(SubMenu subMenu) {
        super.onPrepareSubMenu(subMenu);

        subMenu.clear();
        if(mTimeSeries >= TIMESERIES_YEAR){
            subMenu.add(Menu.NONE, Menu.FIRST + 7, 1, "1D").setOnMenuItemClickListener(this);
        } else if(mTimeSeries >= TIMESERIES_WEEK){
            subMenu.add(Menu.NONE, Menu.FIRST + 4, 1, "1H").setOnMenuItemClickListener(this);
            subMenu.add(Menu.NONE, Menu.FIRST + 5, 1, "2H").setOnMenuItemClickListener(this);
            subMenu.add(Menu.NONE, Menu.FIRST + 6, 1, "4H").setOnMenuItemClickListener(this);
            subMenu.add(Menu.NONE, Menu.FIRST + 7, 1, "1D").setOnMenuItemClickListener(this);
        } else {
            subMenu.add(Menu.NONE, Menu.FIRST + 1, 1, "5M").setOnMenuItemClickListener(this);
            subMenu.add(Menu.NONE, Menu.FIRST + 2, 1, "15M").setOnMenuItemClickListener(this);
            subMenu.add(Menu.NONE, Menu.FIRST + 3, 1, "30M").setOnMenuItemClickListener(this);
            subMenu.add(Menu.NONE, Menu.FIRST + 4, 1, "1H").setOnMenuItemClickListener(this);
            subMenu.add(Menu.NONE, Menu.FIRST + 5, 1, "2H").setOnMenuItemClickListener(this);
            subMenu.add(Menu.NONE, Menu.FIRST + 6, 1, "4H").setOnMenuItemClickListener(this);
            subMenu.add(Menu.NONE, Menu.FIRST + 7, 1, "1D").setOnMenuItemClickListener(this);
        }

    }

    @Override
    public boolean hasSubMenu() {
        return true;
    }

    @Override
    public boolean onPerformDefaultAction() {
        return super.onPerformDefaultAction();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item){
        if(null != mParentMenuItem){
            mParentMenuItem.setTitle(item.getTitle());
        }
        if(null != mOnMenuItemClickListener){
            mOnMenuItemClickListener.onMenuItemClick(item);
        }
        return true;
    }

    public void setParentMenuItem(MenuItem parentMenuItem) {
        this.mParentMenuItem = parentMenuItem;
    }

    public void setOnMenuItemClickListener(MenuItem.OnMenuItemClickListener onMenuItemClickListener){
        mOnMenuItemClickListener = onMenuItemClickListener;
    }

    public void setTimeSeries(int timeseries) {
        mTimeSeries = timeseries;
    }
}