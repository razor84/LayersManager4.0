package com.lovejoy777.rroandlayersmanager.fragments;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.*;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.lovejoy777.rroandlayersmanager.R;
import com.lovejoy777.rroandlayersmanager.commands.Commands;
import com.lovejoy777.rroandlayersmanager.commands.RootCommands;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class BackupRestoreFragment extends Fragment {

    private static final String TAG = null;
    FloatingActionButton fab2;
    private ArrayList<String> Files = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private CardViewAdapter3 mAdapter;
    private CoordinatorLayout cordLayout = null;

    private static void zipFolder(String inputFolderPath, String outZipPath) {
        try {
            FileOutputStream fos = new FileOutputStream(outZipPath);
            ZipOutputStream zos = new ZipOutputStream(fos);
            File srcFile = new File(inputFolderPath);
            File[] files = srcFile.listFiles();
            Log.d("", "Zip directory: " + srcFile.getName());
            for (File file : files) {
                Log.d("", "Adding file: " + file.getName());
                byte[] buffer = new byte[1024];
                FileInputStream fis = new FileInputStream(file);
                zos.putNextEntry(new ZipEntry(file.getName()));
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
                fis.close();
            }
            zos.close();
        } catch (IOException ioe) {
            Log.e("", ioe.getMessage());
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        cordLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_backuprestore, container, false);

        loadToolbarRecyclerViewFab();

        new LoadAndSet().execute();

        setHasOptionsMenu(true);

        return cordLayout;
    }

    private void loadToolbarRecyclerViewFab() {


        mRecyclerView = (RecyclerView) cordLayout.findViewById(R.id.cardList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        fab2 = (android.support.design.widget.FloatingActionButton) cordLayout.findViewById(R.id.fab6);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                final EditText input = new EditText(getActivity());
                alert.setTitle(R.string.backupInstalledOverlays);
                alert.setView(input);
                input.setHint(R.string.enterBackupName);
                alert.setInverseBackgroundForced(true);

                alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {

                                // get editText String
                                String backupname = input.getText().toString().replace(" ", "");

                                if (backupname.length() <= 1) {

                                    Toast.makeText(getActivity(), R.string.noInputName, Toast.LENGTH_LONG).show();

                                    //finish();

                                } else {
                                    File directory = new File("/vendor/overlay");
                                    File[] contents = directory.listFiles();

                                    // Folder is empty
                                    if (contents.length == 0) {

                                        Toast.makeText(getActivity(), R.string.nothingToBackup, Toast.LENGTH_LONG).show();

                                        //finish();
                                    } else {
                                        // CREATES /SDCARD/OVERLAYS/BACKUP/BACKUPNAME
                                        String sdOverlays = Environment.getExternalStorageDirectory() + "/Overlays";
                                        File dir2 = new File(sdOverlays + "/Backup/" + backupname);
                                        if (!dir2.exists() && !dir2.isDirectory()) {
                                            CommandCapture command1 = new CommandCapture(0, "mkdir " + sdOverlays + "/Backup/" + backupname);
                                            try {
                                                RootTools.getShell(true).add(command1);
                                                while (!command1.isFinished()) {
                                                    Thread.sleep(1);
                                                }
                                            } catch (IOException | TimeoutException | RootDeniedException | InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        //Async Task to backup Overlays
                                        new BackupOverlays().execute(backupname);
                                    }
                                }
                            }
                        }

                );

                alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        }

                );

                alert.show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_reboot:
                Commands.reboot(getActivity());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_main, menu);
    }

    //Adapter
    private class CardViewAdapter3 extends RecyclerView.Adapter<CardViewAdapter3.ViewHolder> {

        private ArrayList<String> themes;
        private int rowLayout;
        private Context mContext;

        public CardViewAdapter3(ArrayList<String> themes, int rowLayout, Context context) {
            this.themes = themes;
            this.rowLayout = rowLayout;
            this.mContext = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder viewHolder, final int i) {

            final String layerBackupName = themes.get(i);

            viewHolder.themeName.setText(layerBackupName);
            viewHolder.themeName.setId(i);
            viewHolder.themeName.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    AlertDialog.Builder installdialog = new AlertDialog.Builder(getActivity());
                    installdialog.setTitle(themes.get(i));
                    installdialog.setMessage(Html.fromHtml(getResources().getString(R.string.DoYouWantToRestore)));
                    installdialog.setPositiveButton(R.string.restore, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            new RestoreOverlays().execute(layerBackupName);
                        }
                    });
                    installdialog.setNegativeButton(R.string.delete, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            new DeleteBackup().execute(layerBackupName);
                        }
                    });
                    installdialog.setNeutralButton(R.string.show, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AlertDialog.Builder contentDialog = new AlertDialog.Builder(getActivity());
                            contentDialog.setTitle(layerBackupName + " " + getString(R.string.contains));
                            try {

                                String overlays = "";

                                ArrayList<String> files = Commands.fileNamesFromZip(new File(Environment.getExternalStorageDirectory() + "/Overlays/Backup/" + layerBackupName + "/overlay.zip"));

                                for (int i = 0; i < files.size(); i++) {
                                    if (i == 0) {
                                        overlays = files.get(i);
                                    } else {
                                        overlays = overlays + "\n" + files.get(i);
                                    }
                                }

                                contentDialog.setMessage(overlays.replace(".apk", "").replaceAll("_", " "));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            contentDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            contentDialog.show();
                        }
                    });
                    installdialog.show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return themes.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView themeName;


            public ViewHolder(View itemView) {
                super(itemView);
                themeName = (TextView) itemView.findViewById(R.id.txt);
            }
        }
    }

    //Delete Overlays
    private class DeleteBackup extends AsyncTask<String, String, Void> {
        ProgressDialog progressBackup;

        protected void onPreExecute() {

            progressBackup = ProgressDialog.show(getActivity(), getString(R.string.DeleteBackup),
                    getString(R.string.deleting) + "...", true);
        }

        @Override
        protected Void doInBackground(String... params) {
            String backupName = params[0];
            backupName = Environment.getExternalStorageDirectory() + "/Overlays/Backup/" + backupName;
            try {

                // DELETE /VENDOR/OVERLAY
                RootCommands.DeleteFileRoot(backupName);

                // CLOSE ALL SHELLS
                RootTools.closeAllShells();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

        protected void onPostExecute(Void result) {

            progressBackup.dismiss();
            CoordinatorLayout coordinatorLayoutView = (CoordinatorLayout) cordLayout.findViewById(R.id.main_content4);
            Snackbar.make(coordinatorLayoutView, R.string.deletedBackup, Snackbar.LENGTH_LONG)
                    .show();
            new LoadAndSet().execute();
        }
    }

    private class BackupOverlays extends AsyncTask<String, String, Void> {
        ProgressDialog progressBackup;

        protected void onPreExecute() {

            progressBackup = ProgressDialog.show(getActivity(), getString(R.string.BackupOverlays),
                    getString(R.string.backingUp) + "...", true);
        }

        @Override
        protected Void doInBackground(String... params) {

            String backupname = params[0];
            try {

                String sdOverlays = Environment.getExternalStorageDirectory() + "/Overlays";

                ArrayList<String> backedupOverlays = Commands.loadFiles("/system/vendor/overlay");

                // CREATES /SDCARD/OVERLAYS/BACKUP/TEMP
                File dir1 = new File(sdOverlays + "/Backup/temp");
                if (!dir1.exists() && !dir1.isDirectory()) {
                    CommandCapture command = new CommandCapture(0, "mkdir " + sdOverlays + "/Backup/temp");
                    try {
                        RootTools.getShell(true).add(command);
                        while (!command.isFinished()) {
                            Thread.sleep(1);
                        }
                    } catch (IOException | TimeoutException | InterruptedException | RootDeniedException e) {
                        e.printStackTrace();
                    }
                }


                RootTools.remount("/system", "RW");

                // CHANGE PERMISSIONS OF /VENDOR/OVERLAY && /SDCARD/OVERLAYS/BACKUP
                CommandCapture command2 = new CommandCapture(0,
                        "chmod -R 755 /vendor/overlay",
                        "chmod -R 755 " + Environment.getExternalStorageDirectory() + "/Overlays/Backup/",
                        "cp -fr /vendor/overlay " + Environment.getExternalStorageDirectory() + "/Overlays/Backup/temp/");
                RootTools.getShell(true).add(command2);
                while (!command2.isFinished()) {
                    Thread.sleep(1);
                }

                // ZIP OVERLAY FOLDER
                zipFolder(Environment.getExternalStorageDirectory() + "/Overlays/Backup/temp/overlay", Environment.getExternalStorageDirectory() + "/Overlays/Backup/" + backupname + "/overlay.zip");

                FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Overlays/Backup/" + backupname + "/content.txt");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(backedupOverlays);
                oos.close();

                // CHANGE PERMISSIONS OF /VENDOR/OVERLAY/ 666  && /VENDOR/OVERLAY 777 && /SDCARD/OVERLAYS/BACKUP/ 666
                CommandCapture command18 = new CommandCapture(0, "chmod 777 " + Environment.getExternalStorageDirectory() + "/Overlays/Backup/temp");
                RootTools.getShell(true).add(command18);
                while (!command18.isFinished()) {
                    Thread.sleep(1);
                }
                // DELETE /SDCARD/OVERLAYS/BACKUP/TEMP FOLDER
                RootCommands.DeleteFileRoot(Environment.getExternalStorageDirectory() + "/Overlays/Backup/temp");
                // CHANGE PERMISSIONS OF /VENDOR/OVERLAY/ 666  && /VENDOR/OVERLAY 777 && /SDCARD/OVERLAYS/BACKUP/ 666
                CommandCapture command17 = new CommandCapture(0, "chmod -R 666 /vendor/overlay", "chmod 755 /vendor/overlay", "chmod -R 666" + Environment.getExternalStorageDirectory() + "/Overlays/Backup/");
                RootTools.getShell(true).add(command17);
                while (!command17.isFinished()) {
                    Thread.sleep(1);
                }

                RootTools.remount("/system", "RW");

                // CLOSE ALL SHELLS
                RootTools.closeAllShells();

            } catch (IOException | RootDeniedException | TimeoutException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;

        }

        protected void onPostExecute(Void result) {

            progressBackup.dismiss();
            CoordinatorLayout coordinatorLayoutView = (CoordinatorLayout) cordLayout.findViewById(R.id.main_content4);
            Snackbar.make(coordinatorLayoutView, R.string.backupComplete, Snackbar.LENGTH_LONG)
                    .show();
            new LoadAndSet().execute();
        }
    }

    private class RestoreOverlays extends AsyncTask<String, String, Void> {
        ProgressDialog progressBackup;

        protected void onPreExecute() {

            progressBackup = ProgressDialog.show(getActivity(), getString(R.string.Restore),
                    getString(R.string.restoring) + "...", true);
        }

        @Override
        protected Void doInBackground(String... params) {
            String SZP = params[0];
            SZP = Environment.getExternalStorageDirectory() + "/Overlays/Backup/" + SZP + "/overlay.zip";
            System.out.println(SZP);
            try {

                RootTools.remount("/system", "RW");

                // MK DIR /SDCARD/OVERLAYS/BACKUP/TEMP
                CommandCapture command4 = new CommandCapture(0, "mkdir" + Environment.getExternalStorageDirectory() + "/Overlays/Backup/Temp");

                RootTools.getShell(true).add(command4);
                while (!command4.isFinished()) {
                    Thread.sleep(1);
                }

                // MK DIR /SDCARD/OVERLAYS/BACKUP/TEMP/OVERLAY
                CommandCapture command5 = new CommandCapture(0, "mkdir" + Environment.getExternalStorageDirectory() + "/Overlays/Backup/Temp/overlay");

                RootTools.getShell(true).add(command5);
                while (!command5.isFinished()) {
                    Thread.sleep(1);
                }

                // UNZIP SZP TO /SDCARD/OVERLAYS/BACKUP/TEMP/OVERLAY FOLDER
                Commands.unzipNormalOverlays(SZP, Environment.getExternalStorageDirectory() + "/Overlays/Backup/Temp/overlay");

                // MOVE STUFF FROM /SDCARD/OVERLAYS/BACKUP/TEMP/OVERLAY TO /SYSTEM/VENDOR/OVERLAY
                RootCommands.moveRoot(Environment.getExternalStorageDirectory() + "/Overlays/Backup/Temp/overlay/*", "/system/vendor/overlay");

                // DELETE /SDCARD/OVERLAYS/BACKUP/TEMP FOLDER
                RootCommands.DeleteFileRoot(Environment.getExternalStorageDirectory() + "/Overlays/Backup/Temp");

                // CHANGE PERMISSIONS OF /VENDOR/OVERLAY/ 666  && /VENDOR/OVERLAY 777 && /SDCARD/OVERLAYS/BACKUP/ 666
                CommandCapture command7 = new CommandCapture(0, "chmod -R 666 /system/vendor/overlay", "chmod 755 /system/vendor/overlay", "chmod -R 666" + Environment.getExternalStorageDirectory() + "/Overlays/Backup");
                RootTools.getShell(true).add(command7);
                while (!command7.isFinished()) {
                    Thread.sleep(1);
                }

                RootTools.remount("/system", "RO");

                // CLOSE ALL SHELLS
                RootTools.closeAllShells();

            } catch (IOException | RootDeniedException | TimeoutException | InterruptedException e) {
                e.printStackTrace();
            }
            return null;

        }

        protected void onPostExecute(Void result) {

            progressBackup.dismiss();
            CoordinatorLayout coordinatorLayoutView = (CoordinatorLayout) cordLayout.findViewById(R.id.main_content4);
            Snackbar.make(coordinatorLayoutView, getResources().getString(R.string.restored), Snackbar.LENGTH_LONG)
                    .setAction(R.string.Reboot, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Commands.reboot(getActivity());
                        }
                    })
                    .show();
            new LoadAndSet().execute();
        }
    }

    private class LoadAndSet extends AsyncTask<String, String, Void> {


        protected void onPreExecute() {
        }

        @Override
        protected Void doInBackground(String... params) {

            Files.clear();
            Files = Commands.loadFolders(Environment.getExternalStorageDirectory() + "/Overlays/Backup");

            return null;

        }

        protected void onPostExecute(Void result) {

            mAdapter = new CardViewAdapter3(Files, R.layout.adapter_backups, getActivity());
            mRecyclerView.setAdapter(mAdapter);
            if (Files == null) {
                ImageView noOverlays = (ImageView) cordLayout.findViewById(R.id.imageView);
                TextView noOverlaysText = (TextView) cordLayout.findViewById(R.id.textView7);
                noOverlays.setVisibility(View.VISIBLE);
                noOverlaysText.setVisibility(View.VISIBLE);
            }
        }
    }
}
