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

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;
import com.android.music.CreatePlaylist;
import com.android.music.PlaylistBrowserActivity;
import com.android.music.R;
import com.android.music.tests.Utils;
import com.jayway.android.robotium.solo.Solo;

/**
 * Junit / Instrumentation test case for the PlaylistBrowserActivity
 * This test case need to run in the landscape mode and opened keyboard
 
 */
public class TestPlaylist extends ActivityInstrumentationTestCase2<PlaylistBrowserActivity> {
    private static final String TAG = "musicplayertests";
    public static final String DELETE_PLAYLIST_NAME = "TestDeletPlaylist";
    public static final String ORIGINAL_PLAYLIST_NAME = "Original_playlist_name";
    public static final String RENAMED_PLAYLIST_NAME = "Rename_playlist_name";
    
    
    private Solo solo;
  
    public TestPlaylist() {
        super("com.android.music",PlaylistBrowserActivity.class);
    }

    @Override 
    protected void setUp() throws Exception {
        super.setUp();
        solo = new Solo(getInstrumentation(), getActivity());
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        solo.finishOpenedActivities();
    }

    /**
     * Test case 1: Add a playlist and delete the playlist just added.
     * Verification: The media store playlist should be empty
     */
    @LargeTest
    public void testDeletePlaylist() {
        addNewPlaylist(DELETE_PLAYLIST_NAME);
        assertTrue("testAddDeletePlaylist", isEmptyPlaylist(DELETE_PLAYLIST_NAME));
        deletePlaylist(DELETE_PLAYLIST_NAME);
        assertFalse("testAddDeletePlaylist", isEmptyPlaylist(DELETE_PLAYLIST_NAME));
    }

    /**
     * Test case 2: Add playlist and rename the playlist just added.
     * Verification: The media store playlist should contain the updated name.
     */
    @LargeTest
    public void testRenamePlaylist() {
        addNewPlaylist(ORIGINAL_PLAYLIST_NAME);
        assertTrue("testAddDeletePlaylist", isEmptyPlaylist(ORIGINAL_PLAYLIST_NAME));

        renamePlaylist(ORIGINAL_PLAYLIST_NAME, RENAMED_PLAYLIST_NAME);
        assertFalse("testAddDeletePlaylist", isEmptyPlaylist(ORIGINAL_PLAYLIST_NAME));
        assertTrue("testAddDeletePlaylist", isEmptyPlaylist(RENAMED_PLAYLIST_NAME));

        deletePlaylist(RENAMED_PLAYLIST_NAME);
        assertFalse("testDeletePlaylist", isEmptyPlaylist(RENAMED_PLAYLIST_NAME));
    }

    /**
     * Remove playlist
     * @param playlistName playlist name
     */
    public void deletePlaylist(String playlistName) {
        solo.clickLongOnText(playlistName);
        solo.clickOnText(solo.getString(R.string.delete_playlist_menu));
        Utils.waitShortTime();
    }

    /**
     *  Start the trackBrowserActivity and add the new playlist
     * @param playlistName playlist name
     */
    public void addNewPlaylist(String playlistName) {
        solo.sendKey(Solo.MENU);
        solo.clickOnText(solo.getString(R.string.new_playlist));
        solo.assertCurrentActivity("No Playlist Activity", CreatePlaylist.class);
        solo.clearEditText(0);
        solo.enterText(0, playlistName);
        solo.clickOnButton(0);
        Utils.waitShortTime();
    }

    /**
     * Rename playlist
     * @param oldPlaylistName old playlist name
     * @param newPlaylistName new playlist name
     */
    public void renamePlaylist(String oldPlaylistName, String newPlaylistName) {
        solo.clickLongOnText(oldPlaylistName);
        solo.clickOnText(solo.getString(R.string.rename_playlist_menu));
        solo.clearEditText(0);
        solo.enterText(0, newPlaylistName);
        solo.clickOnButton(0);        
        Utils.waitShortTime();
    }

    public boolean isEmptyPlaylist(String playlistName) {
        ContentResolver resolver = getActivity().getContentResolver();
        assertNotNull(TAG, resolver);
        
        String[] cols = new String[] {
            MediaStore.Audio.Playlists.NAME
        };
        String whereClause = MediaStore.Audio.Playlists.NAME + " = '" + playlistName +"'";
        Cursor cursor = resolver.query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                cols, 
                whereClause, 
                null,
                null
        );
        //while (cursor.moveToNext()) {
        //    Log.v(TAG, "cursor: " + cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME)));
        //}
        boolean result = cursor.moveToFirst();
        cursor.close();
        return result;
    }

}    
