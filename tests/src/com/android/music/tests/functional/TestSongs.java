/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.music.tests.functional;

import android.app.Instrumentation;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.KeyEvent;
import com.android.music.MusicUtils;
import com.android.music.TrackBrowserActivity;
import com.android.music.tests.MusicPlayerNames;
import com.android.music.tests.Utils;
import com.android.music.tests.Utils.Log;
import com.jayway.android.robotium.solo.Solo;

import java.io.*;

/**
 * Junit / Instrumentation test case for the TrackBrowserActivity
 */
public class TestSongs extends ActivityInstrumentationTestCase2<TrackBrowserActivity> {
    private Solo solo;

    public TestSongs() {
        super("com.android.music", TrackBrowserActivity.class);
    }
    
    @Override 
    protected void setUp() throws Exception {
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
    }

    private void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
        Log.v("Copy file");
    }

    //Rescan the sdcard after copy the file
    private void rescanSdcard() throws Exception {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
                + Environment.getExternalStorageDirectory()));
        Log.v("start the intent");
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addDataScheme("file");
        getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"
                + Environment.getExternalStorageDirectory())));
        Utils.waitVeryLongTime();
    }

    /**
     * Test case 2: Set a song as ringtone
     * Test case precondition: The testing device should wipe data before
     * run the test case.
     * Verification: The count of audio.media.is_ringtone equal to 1.
     */
    @LargeTest
    public void testSetRingtone() throws Exception {
        Cursor mCursor;
        Instrumentation inst = getInstrumentation();
        inst.invokeContextMenuAction(getActivity(), MusicUtils.Defs.USE_AS_RINGTONE, 0);
        //This only check if there only 1 ringtone set in music player
        ContentResolver resolver = getActivity().getContentResolver();
        if (resolver == null) {
            System.out.println("resolver = null");
        } else {
            String whereclause = MediaStore.Audio.Media.IS_RINGTONE + " = 1";
            mCursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, whereclause, null, null);
            //Check the new playlist
            mCursor.moveToFirst();
            int isRingtoneSet = mCursor.getCount();
            assertEquals(Utils.TAG, MusicPlayerNames.EXPECTED_NO_RINGTONE, isRingtoneSet);
        }
    }

    /**
     * Test case 3: Delete a song
     * Test case precondition: Copy a song and rescan the sdcard
     * Verification: The song is deleted from the sdcard and mediastore
     */
    @LargeTest
    public void testDeleteSong() throws Exception {
        Instrumentation inst = getInstrumentation();
        Cursor mCursor;

        //Copy a song from the golden directory
        Log.v("Copy a temp file to the sdcard");
        File goldenfile = new File(MusicPlayerNames.GOLDENSONG);
        File toBeDeleteSong = new File(MusicPlayerNames.DELETESONG);
        copy(goldenfile, toBeDeleteSong);
        rescanSdcard();

        //Delete the file from music player
        Utils.waitLongTime();
        inst.sendStringSync(MusicPlayerNames.TOBEDELETESONGNAME);
        Utils.waitLongTime();
        inst.invokeContextMenuAction(getActivity(), MusicUtils.Defs.DELETE_ITEM, 0);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_CENTER);
        Utils.waitLongTime();

        //Clear the search string
        for (int j = 0; j < MusicPlayerNames.TOBEDELETESONGNAME.length(); j++)
            inst.sendKeyDownUpSync(KeyEvent.KEYCODE_DEL);

        //Verfiy the item is removed from sdcard
        File checkDeletedFile = new File(MusicPlayerNames.DELETESONG);
        assertFalse(checkDeletedFile.exists());

        ContentResolver resolver = getActivity().getContentResolver();
        if (resolver == null) {
            System.out.println("resolver = null");
        } else {
            String whereclause = MediaStore.Audio.Media.DISPLAY_NAME + " = '" +
                    MusicPlayerNames.TOBEDELETESONGNAME + "'";
            mCursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null, whereclause, null, null);
            boolean isEmptyCursor = mCursor.moveToFirst();
            assertFalse(isEmptyCursor);
        }
    }
}
 
