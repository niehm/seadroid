package com.seafile.seadroid2.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.seafile.seadroid2.R;
import com.seafile.seadroid2.ui.NotificationUtils;
import com.seafile.seadroid2.ui.adapter.TransferTaskAdapter;
import com.seafile.seadroid2.ui.fragment.DownloadTaskFragment;
import com.seafile.seadroid2.ui.fragment.UploadTaskFragment;
import com.viewpagerindicator.TabPageIndicator;

public class TransferActivity extends SherlockFragmentActivity {
    private static final String DEBUG_TAG = "TransferActivity";

    /**  0 mark as Download Fragment, 1 mark as Upload Fragment, the same convention with {@link TransferTaskAdapter #mTransferTaskType} */
    private int currentPosition = 0;
    private TransferTabsAdapter tabsAdapter;
    private ViewPager pager;
    private TabPageIndicator indicator;

    private Menu overFlowMenu = null;

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.containsKey(NotificationUtils.NOTIFICATION_MESSAGE_KEY)) {
                // extract the extra-data in the Notification
                String msg = extras.getString(NotificationUtils.NOTIFICATION_MESSAGE_KEY);
                if (msg.equals(NotificationUtils.NOTIFICATION_OPEN_DOWNLOAD_TAB)) {
                    currentPosition = 0;
                    indicator.setCurrentItem(0);
                } else if (msg.equals(NotificationUtils.NOTIFICATION_OPEN_UPLOAD_TAB)) {
                    currentPosition = 1;
                    indicator.setCurrentItem(1);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transfer_list_layout);

        tabsAdapter = new TransferTabsAdapter(getSupportFragmentManager());

        pager = (ViewPager) findViewById(R.id.transfer_list_pager);
        pager.setAdapter(tabsAdapter);

        indicator = (TabPageIndicator) findViewById(R.id.transfer_list_indicator);
        indicator.setViewPager(pager);
        indicator.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(final int position) {
                Log.d(DEBUG_TAG, "current tab index " + position);
                currentPosition = position;
                supportInvalidateOptionsMenu();
                pager.setCurrentItem(position);
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
                // TODO Auto-generated method stub
            }
        });

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        /** this is hacky to explicitly call onNewIntent()
         * because it was never called when start the TransferActivity
         * by notification bar */
        onNewIntent(getIntent());
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_MENU:
            if (overFlowMenu != null) {
                overFlowMenu.performIdentifierAction(R.id.transfer_overflow_menu, 0);
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.transfer_list_menu, menu);
        overFlowMenu = menu;
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // MenuItem cancel = menu.findItem(R.id.cancel_transfer_tasks);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        case R.id.cancel_transfer_tasks:
            if (currentPosition == 0) {
                getDownloadTaskFragment().cancelAllDownloadTasks();
                
            } else getUploadTaskFragment().cancelUploadTasks();
            
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public DownloadTaskFragment getDownloadTaskFragment() {
        return (DownloadTaskFragment)getFragment(0);
    }

    public UploadTaskFragment getUploadTaskFragment() {
        return (UploadTaskFragment)getFragment(1);
    }

    public Fragment getFragment(int index) {
        return getSupportFragmentManager().findFragmentByTag(makeFragmentName(index));
    }

    private String makeFragmentName(int index) {
        return "android:switcher:" + R.id.transfer_list_pager + ":" + index;
    }

    /**
     * Adapter for {@link ViewPager} to bind DownloadTaskFragment and UploadTaskFragment
     */
    public class TransferTabsAdapter extends FragmentPagerAdapter {

        private String downloadTabTitle;
        private String uploadTabTitle;

        public TransferTabsAdapter(FragmentManager fm) {
            super(fm);
            downloadTabTitle = getString(R.string.transfer_tabs_downloads);
            uploadTabTitle = getString(R.string.transfer_tabs_uploads);
        }

        private DownloadTaskFragment downloadsFragment = null;
        private UploadTaskFragment uploadsFragment = null;

        @Override
        public Fragment getItem(int position) {
            switch (position) {
            case 0:
                if (downloadsFragment == null) {
                    downloadsFragment = new DownloadTaskFragment();
                }
                return downloadsFragment;
            case 1:
                if (uploadsFragment == null) {
                    uploadsFragment = new UploadTaskFragment();
                }
                return uploadsFragment;
            default:
                return new Fragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
            case 0:
                return downloadTabTitle;
            case 1:
                return uploadTabTitle;

            default:
                return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

    }
}