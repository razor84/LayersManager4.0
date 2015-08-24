package com.lovejoy777.rroandlayersmanager.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.TypedValue;
import android.view.*;
import android.widget.TextView;
import com.bitsyko.liblayers.Layer;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.adapters.CardViewAdapter;
import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.helper.RecyclerItemClickListener;
import com.lovejoy777.rroandlayersmanager.menu;

import java.util.*;

public class PluginFragment extends android.support.v4.app.Fragment implements AppBarLayout.OnOffsetChangedListener {

    RecyclerView recList = null;
    CardViewAdapter ca = null;
    public int sortMode;
    ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0,  ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
            //Remove swiped item from list and notify the RecyclerView
            String packageName = ca.getLayerFromPosition(viewHolder.getAdapterPosition()).getPackageName();
            Uri packageURI = Uri.parse("package:" + packageName);
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
            startActivityForResult(uninstallIntent, 1);
        }
    };
    private Boolean TestBoolean = false;
    private CoordinatorLayout cordLayout = null;
    private SwipeRefreshLayout mSwipeRefresh;



    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        cordLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_plugins, container, false);

        ((DrawerLayout) getActivity().findViewById(R.id.drawer_layout)).setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        ((NavigationView) getActivity().findViewById(R.id.nav_view)).getMenu().getItem(0).setChecked(true);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_menu);
        toolbar.setTitle(getString(R.string.InstallOverlays));

        ViewPager viewPager = (ViewPager) getActivity().findViewById(R.id.tabanim_viewpager);
        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.tabs);
        viewPager.setVisibility(View.VISIBLE);
        tabLayout.setVisibility(View.VISIBLE);


        TextView toolbarTitle = (TextView) getActivity().findViewById(R.id.title2);
        toolbarTitle.setText("");

        int elevation = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics());
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56, getResources().getDisplayMetrics());


        AppBarLayout.LayoutParams layoutParams = new AppBarLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, height
        );

        toolbar.setElevation(elevation);
        toolbar.setLayoutParams(layoutParams);

        LoadRecyclerViewFabToolbar();

        sortMode = Commands.getSortMode(getActivity());

        new fillPluginList().execute();

        setHasOptionsMenu(true);

        return cordLayout;
    }



    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        if (i == 0) {
            mSwipeRefresh.setEnabled(true);
        } else {
            mSwipeRefresh.setEnabled(false);
        }
    }



    private void LoadRecyclerViewFabToolbar() {
        //create RecyclerView
        RecyclerView recyclerCardViewList = (RecyclerView) cordLayout.findViewById(R.id.cardList);
        recyclerCardViewList.setHasFixedSize(true);
        recyclerCardViewList.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        onListItemClick(position);
                    }
                })
        );

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerCardViewList.setLayoutManager(llm);


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerCardViewList);

        //create FAB
        FloatingActionButton fab = (FloatingActionButton) cordLayout.findViewById(R.id.fab3);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((menu) getActivity()).changeFragment(4, 0);
            }
        });

        mSwipeRefresh = (SwipeRefreshLayout) cordLayout.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefresh.setColorSchemeResources(R.color.accent);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new fillPluginList().execute();
            }
        });
    }

    //create list if no plugins are installed
    private List<Layer> createList2() {

        List<Layer> result = new ArrayList<>();
        result.add(new Layer(getString(R.string.tooBad), getString(R.string.noPlugins), getResources().getDrawable(R.drawable.ic_noplugin, null)));
        result.add(new Layer(getString(R.string.Showcase), getString(R.string.ShowCaseMore), getResources().getDrawable(R.mipmap.ic_launcher, null)));
        result.add(new Layer(getString(R.string.PlayStore), getString(R.string.PlayStoreMore), getResources().getDrawable(R.drawable.playstore, null)));
        return result;
    }


    //open Plugin page after clicked on a cardview
    protected void onListItemClick(int position) {
        if (!TestBoolean) {
            ((menu) getActivity()).changeFragment2(ca.getLayerFromPosition(position));
        } else {
            if (position == 2) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.PlaystoreSearch))));
            }
            if (position == 1) {
                NotAvailableSnackbar();
            }

        }
    }

    private void NotAvailableSnackbar() {
        final View coordinatorLayoutView = cordLayout.findViewById(R.id.main_content2);
        Snackbar.make(coordinatorLayoutView, "Sorry, not available yet.", Snackbar.LENGTH_SHORT)
                .show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            new fillPluginList().execute();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_pluginlist, menu);
        switch (sortMode) {
            default:
                menu.findItem(R.id.menu_sortName).setChecked(true);
                break;
            case 2:
                menu.findItem(R.id.menu_sortDeveloper).setChecked(true);
                break;
            case 3:
                menu.findItem(R.id.menu_sortRandom).setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reboot:
                Commands.reboot(getActivity());
                break;
            case R.id.menu_sortName:
                item.setChecked(true);
                Commands.setSortMode(getActivity(), 1);
                new fillPluginList().execute();
                break;
            case R.id.menu_sortDeveloper:
                item.setChecked(true);
                Commands.setSortMode(getActivity(), 2);
                new fillPluginList().execute();
                break;
            case R.id.menu_sortRandom:
                item.setChecked(true);
                Commands.setSortMode(getActivity(), 3);
                new fillPluginList().execute();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private class fillPluginList extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
            mSwipeRefresh.setRefreshing(true);
        }

        @Override
        protected Void doInBackground(Void... params) {

            List<Layer> layerList = Layer.getLayersInSystem(PluginFragment.this.getActivity());

            sortMode = Commands.getSortMode(getActivity());
            if (sortMode == 1 || sortMode == 0) {
                //Alphabetically NAME
                Collections.sort(layerList, new Comparator<Layer>() {
                    public int compare(Layer layer1, Layer layer2) {
                        return layer1.getName().compareToIgnoreCase(layer2.getName());
                    }
                });
            }
            if (sortMode == 2) {
                //Alphabetically DEVELOPER
                Collections.sort(layerList, new Comparator<Layer>() {
                    public int compare(Layer layer1, Layer layer2) {
                        return layer1.getDeveloper().compareToIgnoreCase(layer2.getDeveloper());
                    }
                });
            }
            if (sortMode == 3) {
                //RANDOM
                long seed = System.nanoTime();
                Collections.shuffle(layerList, new Random(seed));
                Collections.shuffle(layerList, new Random(seed));

            }


            if (layerList.size() > 0) {
                ca = new CardViewAdapter(layerList);
            } else {
                ca = new CardViewAdapter(createList2());
                TestBoolean = true;
            }

            return null;

        }

        protected void onPostExecute(Void result) {
            recList = (RecyclerView) cordLayout.findViewById(R.id.cardList);
            recList.setHasFixedSize(true);
            recList.setAdapter(ca);
            mSwipeRefresh.setRefreshing(false);
        }
    }


}
