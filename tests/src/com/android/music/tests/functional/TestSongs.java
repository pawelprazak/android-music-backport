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
import android.app.Instrumentation.ActivityMonitor;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;
import com.android.music.ConfirmDeleteItems;
import com.android.music.TrackBrowserActivity;
import com.android.music.tests.Utils;
import com.android.music.tests.Utils.Log;
import com.jayway.android.robotium.solo.Solo;

import java.io.*;

/**
 * Junit / Instrumentation test case for the TrackBrowserActivity
 */
public class TestSongs extends ActivityInstrumentationTestCase2<TrackBrowserActivity> {
    public static final String EXTERNAL_DIR = Environment.getExternalStorageDirectory().toString();
    public static final String GOLDENSONG = EXTERNAL_DIR + "/media_api/music/GOLDEN.mp3";
    public static final String DELETESONG = EXTERNAL_DIR + "/aaaToBeDeleted.mp3";
    public static final String TOBEDELETESONGNAME = "aaaToBeDeleted";
    
    private Solo solo;
    
    public TestSongs() {
        super("com.android.music", TrackBrowserActivity.class);
    }
    
    @Override 
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        solo.finishOpenedActivities();
        super.tearDown();
    }

    /**
     * Test case 1: Delete a song
     * Test case precondition: Copy a song with
     *     adb push tests/GOLDEN.mp3 /sdcard/media_api/music/GOLDEN.mp3
     * Verification: The song is deleted from the sdcard and mediastore
     * @throws Exception exceptions
     */
    @LargeTest
    public void testDeleteSong() throws Exception {
        // Copy a song from the golden directory
        copy(GOLDENSONG, DELETESONG);
        assertTrue(fileExists(DELETESONG));
        rescanSdcard();

        // Find the song on the list
        waitForUpdateAndLongClick(TOBEDELETESONGNAME, 30000);  // wait for list view to refresh
        clickOnTextWithin("Delete", Utils.WAIT_SHORT_TIME);
        solo.assertCurrentActivity("Not a ConfirmDeleteItems dialog", ConfirmDeleteItems.class);
        solo.clickOnButton("OK");

        // Verify the item was removed from sdcard
        assertFalse(fileExists(DELETESONG));

        ContentResolver resolver = getActivity().getContentResolver();
        assertNotNull(resolver);
        
        String whereClause = MediaStore.Audio.Media.DISPLAY_NAME + " = '" + TOBEDELETESONGNAME + "'";
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, whereClause, null, null);
        boolean isEmptyCursor = cursor.moveToFirst();
        cursor.close();
        assertFalse(isEmptyCursor);
    }

    /*
     * waits for ListView update and scrolls up, asserts that item to click will be first on the list
     */
    private void waitForUpdateAndLongClick(String toLongClick, int timeout) {
        boolean wasThere = false;
        int step = 200;
        for (int elapsed = 0; elapsed < timeout; elapsed += step) {
            solo.scrollUp();
            wasThere = solo.waitForText(toLongClick, 1, step, false, true);
            if (wasThere)
                break;
        }
        if (!wasThere) 
            fail(toLongClick + " not found for long click" 
            + ", timeout " + timeout + " ms");
        solo.clickLongOnText(toLongClick);
    }

    private void clickOnTextWithin(String textToClick, long timeout) {
        waitForTextOrFail(textToClick, timeout, true, true);
        solo.clickOnText(textToClick);
        
        // workaround
        boolean isStillHere = solo.waitForText(textToClick, 1, Utils.WAIT_SHORT_TIME);
        if (isStillHere) {
            View view = solo.getText(textToClick);
            solo.clickOnView(view);
        }
    }

    private void waitForTextOrFail(String text, long timeout, boolean scroll, boolean onlyVisible) {
        boolean wasThere = solo.waitForText(text, 1, timeout, scroll, onlyVisible);
        if (!wasThere) 
            fail(text + " not found" 
                + ", timeout " + timeout + " ms" 
                + ", scroll: " + scroll
                + ", only visible: " + onlyVisible);
    }

    //Rescan the sdcard after copy the file
    private void rescanSdcard() throws Exception {
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + EXTERNAL_DIR));
        getActivity().sendBroadcast(scanIntent);
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addDataScheme("file");
        Instrumentation inst = getInstrumentation();
        ActivityMonitor am = inst.addMonitor(intentFilter, null, false);
        am.waitForActivityWithTimeout(Utils.WAIT_VERY_LONG_TIME);
    }

    private boolean fileExists(String fileName) {
        File file = new File(fileName);
        if (file.exists())
            Utils.waitLongTime();
        return file.exists();
    }

    private void copy(String srcName, String dstName) throws IOException {
        Log.v("Copy a temp file to the sdcard");
        File src = new File(srcName);
        File dst = new File(dstName);
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
}
 
