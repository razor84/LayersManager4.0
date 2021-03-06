package com.lovejoy777.rroandlayersmanager;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.VoiceInteractor;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.bitsyko.liblayers.Layer;
import com.lovejoy777.rroandlayersmanager.activities.*;
import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.fragments.*;
import com.rubengees.introduction.IntroductionActivity;
import com.rubengees.introduction.IntroductionBuilder;
import com.rubengees.introduction.IntroductionConfiguration;
import com.rubengees.introduction.entity.Option;
import com.rubengees.introduction.entity.Slide;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class menu extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_container);

        loadToolbarNavDrawer();

        if (!RootTools.isAccessGiven()) {
            Toast.makeText(this, getString(R.string.noRoot), Toast.LENGTH_LONG).show();
        } else {
            createImportantDirectories();
        }

        Boolean tutorialShown = PreferenceManager.getDefaultSharedPreferences(menu.this).getBoolean("tutorialShown", false);
        if (!tutorialShown) {
            loadTutorial(this);
        }else{
            changeFragment(1, 0);
        }
    }

    public static void loadTutorial(final Activity context) {
        new IntroductionBuilder(context).withSlides(generateSlides()).introduceMyself();
    }

    public static List<Slide> generateSlides() {
        List<Slide> slides = new ArrayList<>();

        slides.add(new Slide().withTitle(R.string.Slide1_Heading).withDescription(R.string.Slide1_Text).
                withColorResource(R.color.tutorial_background_1).withImage(R.drawable.layersmanager));
        slides.add(new Slide().withTitle(R.string.Slide2_Heading).withDescription(R.string.Slide2_Text)
                .withColorResource(R.color.tutorial_background_2).withImage(R.drawable.intro_2));
        slides.add(new Slide().withTitle(R.string.Slide3_Heading).withDescription(R.string.Slide3_Text)
                .withColorResource(R.color.tutorial_background_3).withImage(R.drawable.intro_3));
        slides.add(new Slide().withTitle(R.string.Slide4_Heading).withDescription(R.string.Slide4_Text)
                .withColorResource(R.color.tutorial_background_4).withImage(R.drawable.intro_4));
        slides.add(new Slide().withTitle(R.string.Slide5_Heading).withDescription(R.string.Slide5_Text)
                .withColorResource(R.color.tutorial_background_5).withImage(R.drawable.intro_5));
        slides.add(new Slide().withTitle(R.string.Slide6_Heading).withOption(new Option(R.string.SettingLauncherIconDetail))
                .withColorResource(R.color.tutorial_background_6).withImage(R.drawable.layersmanager_crossed));
        slides.add(new Slide().withTitle(R.string.Slide7_Heading).withOption(new Option(R.string.SettingsHideOverlays))
                .withColorResource(R.color.tutorial_background_6).withImage(R.drawable.intro_7));
        return slides;
    }





    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IntroductionBuilder.INTRODUCTION_REQUEST_CODE &&
                resultCode == RESULT_OK) {

            for (Option option : data.<Option>getParcelableArrayListExtra(IntroductionActivity.
                    OPTION_RESULT)) {

                if (option.getPosition()==5 && option.isActivated()){
                    SharedPreferences myprefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    myprefs.edit().putBoolean("switch1",true).commit();
                    Commands.killLauncherIcon(this);
                }
                if (option.getPosition()==6 && option.isActivated()){
                    SharedPreferences myprefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    myprefs.edit().putBoolean("disableNotInstalledApps",true).commit();
                }
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("tutorialShown", true).commit();
                changeFragment(1, 0);

            }
        }else{
            if (resultCode == RESULT_CANCELED){
                loadTutorial(this);
            }
        }

    }

    private void loadToolbarNavDrawer() {
        //set Toolbar
        final android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //set NavigationDrawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }
    }


    //navigationDrawerIcon Onclick
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Fragment currentFragment = menu.this.getFragmentManager().findFragmentById(R.id.fragment_container);
                if (currentFragment instanceof InstallFragment) {
                    changeFragment(1, 1);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //set NavigationDrawerContent
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        mDrawerLayout.closeDrawers();
                        menuItem.setChecked(true);
                        Bundle bndlanimation =
                                ActivityOptions.makeCustomAnimation(getApplicationContext(), R.anim.anni1, R.anim.anni2).toBundle();
                        int id = menuItem.getItemId();
                        switch (id) {
                            case R.id.nav_home:
                                changeFragment(1, 0);
                                break;
                            case R.id.nav_about:
                                Intent about = new Intent(menu.this, AboutActivity.class);
                                startActivity(about, bndlanimation);
                                break;
                            case R.id.nav_delete:
                                changeFragment(2, 0);
                                break;
                            case R.id.nav_tutorial:
                                Intent tutorial = new Intent(menu.this, DetailedTutorialActivity.class);
                                startActivity(tutorial, bndlanimation);
                                break;
                            case R.id.nav_restore:
                                changeFragment(3, 0);
                                getSupportActionBar().setElevation(0);
                                getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_menu);
                                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                                break;
                            case R.id.nav_showcase:
                                boolean installed = appInstalledOrNot("com.lovejoy777.showcase");
                                if (installed) {
                                    //This intent will help you to launch if the package is already installed
                                    Intent intent = new Intent();
                                    intent.setComponent(new ComponentName("com.lovejoy777.showcase", "com.lovejoy777.showcase.MainActivity1"));
                                    startActivity(intent);
                                    break;
                                } else {
                                    Toast.makeText(menu.this, "Please install the layers showcase plugin", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=com.lovejoy777.showcase")), bndlanimation);
                                    //System.out.println("App is currently not installed on your phone");
                                    break;
                                }
                            case R.id.nav_settings:
                                Intent settings = new Intent(menu.this, SettingsActivity.class);
                                startActivity(settings, bndlanimation);
                                break;
                            case R.id.nav_playStore:
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/search?q=Layers+Theme&c=apps&docType=1&sp=CAFiDgoMTGF5ZXJzIFRoZW1legIYAIoBAggB:S:ANO1ljK_ZAY")), bndlanimation);
                                break;
                        }
                        return false;
                    }
                });
    }

    public void changeFragment(int position, int mode) {
        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        Fragment fragment = null;
        FragmentManager fragmentManager = getFragmentManager();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        switch (position) {
            case 1:
                fragment = new PluginFragment();
                break;
            case 2:
                fragment = new UninstallFragment();
                break;
            case 3:
                fragment = new BackupRestoreFragment();
                break;
            case 4:
                fragment = new InstallFragment();
                break;
        }


        fragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();


    }

    public void changeFragment2(Layer layer) {
        Bundle args = new Bundle();
        args.putString("PackageName", layer.getPackageName());

        Intent intent = new Intent(this, OverlayDetailActivity.class);

        intent.putExtra("PackageName", layer.getPackageName());

        startActivity(intent);

    }


    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        boolean app_installed;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }


    private void createImportantDirectories() {


        String sdOverlays1 = Environment.getExternalStorageDirectory() + "/Overlays/Backup";
        // CREATES /SDCARD/OVERLAYS/BACKUP
        File dir1 = new File(sdOverlays1);

        dir1.mkdirs();

        Commands.remountSystem("rw");
        String vendover = DeviceSingleton.getInstance().getOverlayFolder();
        // CREATES /VENDOR/OVERLAY
        File dir2 = new File(vendover);
        if (!dir2.exists()) {
            CommandCapture command5 = new CommandCapture(0, "mkdir " + vendover);
            try {
                RootTools.getShell(true).add(command5);
                while (!command5.isFinished()) {
                    Thread.sleep(1);
                }

            } catch (IOException | TimeoutException | InterruptedException | RootDeniedException e) {
                e.printStackTrace();
            }
        }

        Commands.remountSystem("ro");

    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = menu.this.getFragmentManager().findFragmentById(R.id.fragment_container);

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
            return;
        }

        if (currentFragment instanceof BackButtonListener && !((BackButtonListener) currentFragment).onBackButton()) {
            return;
        }

        FragmentManager fm = getFragmentManager();

        //First commit is omitted
        if (fm.getBackStackEntryCount() > 1) {
            fm.popBackStack();
            return;
        }


        super.onBackPressed();
    }

}
