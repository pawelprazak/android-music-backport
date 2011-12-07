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
import com.android.music.PlaylistBrowserActivity;
import com.android.music.R;
import com.android.music.tests.Utils;
import com.android.music.tests.Utils.Log;
import com.jayway.android.robotium.solo.Solo;

import java.util.ArrayList;
import java.util.List;

/**
 * Instrumentation test case for the PlaylistBrowserActivity
 * This test case need to run in the landscape mode and opened keyboard
 */
public class TestPlaylist extends ActivityInstrumentationTestCase2<PlaylistBrowserActivity> {
    public static final String DELETE_PLAYLIST_NAME = "TestDeletPlaylist";
    public static final String ORIGINAL_PLAYLIST_NAME = "Original_playlist_name";
    public static final String RENAMED_PLAYLIST_NAME = "Rename_playlist_name";

    //Expected result of the sorted playlist name
    public static final String expectedPlaylistTitles[] = {
            "**1E?:|}{[]~~.,;'",
            "//><..", "0123456789",
            "0random@112", "MyPlaylist", "UPPERLETTER",
            "combination011", "loooooooog",
            "normal", "~!@#$%^&*()_+"
    };

    //Unsorted input playlist name
    public static final String unsortedPlaylistTitles[] = {
            "MyPlaylist", "//><..", 
            "0random@112", "UPPERLETTER", "normal",
            "combination011", "0123456789",
            "~!@#$%^&*()_+", "**1E?:|}{[]~~.,;'",
            "loooooooog"
    };

    private Solo solo;

    public TestPlaylist() {
        super("com.android.music", PlaylistBrowserActivity.class);
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
     * Test case 1: Add a playlist and delete the playlist just added.
     * Verification: The media store playlist should be empty
     */
    @LargeTest
    public void testDeletePlaylist() {
        addNewPlaylist(DELETE_PLAYLIST_NAME);
        assertTrue("testAddDeletePlaylist", playlistExists(DELETE_PLAYLIST_NAME));
        
        deletePlaylist(DELETE_PLAYLIST_NAME);
        assertTrue("testAddDeletePlaylist", playlistGone(DELETE_PLAYLIST_NAME));
    }

    /**
     * Test case 2: Add playlist and rename the playlist just added.
     * Verification: The media store playlist should contain the updated name.
     */
    @LargeTest
    public void testRenamePlaylist() {
        addNewPlaylist(ORIGINAL_PLAYLIST_NAME);
        assertTrue("testAddDeletePlaylist", playlistExists(ORIGINAL_PLAYLIST_NAME));

        renamePlaylist(ORIGINAL_PLAYLIST_NAME, RENAMED_PLAYLIST_NAME);
        assertTrue("testAddDeletePlaylist", playlistGone(ORIGINAL_PLAYLIST_NAME));
        assertTrue("testAddDeletePlaylist", playlistExists(RENAMED_PLAYLIST_NAME));

        deletePlaylist(RENAMED_PLAYLIST_NAME);
        assertTrue("testDeletePlaylist", playlistGone(RENAMED_PLAYLIST_NAME));
    }

    /**
     * Test case 3: tests the new playlist added with sorted order.
     * Verification: The new playlist title should be sorted in alphabetical order
     */
    @LargeTest
    public void testAddPlaylist() {
        for (String title : unsortedPlaylistTitles) {
            addNewPlaylist(title);
        }

        //Verify the new playlist is created, check the playlist table
        String whereClause = MediaStore.Audio.Playlists.NAME + " != ''";
        List<String> names = getPlaylists(whereClause);
        for (int i = 0; i < names.size(); i++) {
            assertEquals("New sorted Playlist title:", expectedPlaylistTitles[i], names.get(i));
        }
        ContentResolver resolver = getActivity().getContentResolver();
        assertNotNull(Utils.TAG, resolver);
        resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, null, null);
    }

    /**
     * Start the trackBrowserActivity and add the new playlist
     *
     * @param playlistName playlist name
     */
    public void addNewPlaylist(String playlistName) {
        solo.sendKey(Solo.MENU);
        solo.clickOnText(solo.getString(R.string.new_playlist));
        solo.waitForActivity("CreatePlaylist", Utils.WAIT_SHORT_TIME);
        solo.clearEditText(0);
        solo.enterText(0, playlistName);
        solo.clickOnButton(0);
    }

    /**
     * Delete playlist
     *
     * @param playlistName playlist name
     */
    public void deletePlaylist(String playlistName) {
        solo.clickLongOnText(playlistName);
        solo.clickOnText(solo.getString(R.string.delete_playlist_menu));
    }

    /**
     * Rename playlist
     *
     * @param oldPlaylistName old playlist name
     * @param newPlaylistName new playlist name
     */
    public void renamePlaylist(String oldPlaylistName, String newPlaylistName) {
        solo.clickLongOnText(oldPlaylistName);
        solo.clickOnText(solo.getString(R.string.rename_playlist_menu));
        solo.waitForActivity("CreatePlaylist", Utils.WAIT_SHORT_TIME);
        solo.clearEditText(0);
        solo.enterText(0, newPlaylistName);
        solo.clickOnButton(0);
    }

    private boolean playlistGone(String playlistName) {
        boolean gone = playlistProbe(playlistName) == 0;        
        if (!gone) {
            Utils.waitLongTime();
            gone = playlistProbe(playlistName) == 0;
        }
        if (!gone) {  // deletes are sometimes slow, give them some time
            Utils.waitVeryLongTime();
            gone = playlistProbe(playlistName) == 0;
        }
        return gone;
    }

    public boolean playlistExists(String playlistName) {
        boolean exists = playlistProbe(playlistName) > 0;
        if (!exists) {
            Utils.waitLongTime();
            exists = playlistProbe(playlistName) > 0;
        }
        return exists;
    }
    
    private int playlistProbe(String playlistName) {
        String whereClause = MediaStore.Audio.Playlists.NAME + " = '" + playlistName + "'";
        List<String> names = getPlaylists(whereClause);
        Log.v("names: " + names);
        Log.e("names size > 1 = " + (names.size() > 0) + " , " + names.size());
        return names.size();    
    }

    private List<String> getPlaylists(String whereClause) {
        ContentResolver resolver = getActivity().getContentResolver();
        assertNotNull(Utils.TAG, resolver);

        String[] cols = new String[]{
                MediaStore.Audio.Playlists.NAME
        };
        Cursor cursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                cols, whereClause, null, MediaStore.Audio.Playlists.NAME);

        List<String> names = new ArrayList<String>();
        while (cursor.moveToNext()) {
            int id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME);
            names.add(cursor.getString(id));
        }
        cursor.close();
        return names;
    }

}    
