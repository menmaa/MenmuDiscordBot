package com.menmasystems.menmudiscordbot;/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Print a list of videos matching a search term.
 *
 * @author Jeremy Walker
 * @author Menma
 */
public class YoutubeSearch {

    /**
     * Define a global variable that identifies the name of a file that
     * contains the developer's API key.
     */
    private static final Logger logger = LoggerFactory.getLogger(YoutubeSearch.class);
    private static final long NUMBER_OF_VIDEOS_RETURNED = 1;

    /**
     * Define a global instance of a Youtube object, which will be used
     * to make YouTube Data API requests.
     */
    private static YouTube youtube;
    private final String youtubeApiKey;

    YoutubeSearch(String youtubeApiKey) {
        this.youtubeApiKey = youtubeApiKey;

        try {
            // This object is used to make YouTube Data API requests. The last
            // argument is required, but since we don't need anything
            // initialized when the HttpRequest is initialized, we override
            // the interface and provide a no-op function.
            youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), request -> {
            }).setApplicationName("menmu-youtube-video-search").build();

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public SearchResult getYtVideoDataBySearchQuery(String queryTerm) {
        try {
            // Define the API request for retrieving search results.
            YouTube.Search.List search = youtube.search().list("id,snippet");

            // Set your developer key from the {{ Google Cloud Console }} for
            // non-authenticated requests. See:
            // {{ https://cloud.google.com/console }}
            search.setKey(youtubeApiKey);
            search.setQ(queryTerm);

            // Restrict the search results to only include videos. See:
            // https://developers.google.com/youtube/v3/docs/search/list#type
            search.setType("video");

            // To increase efficiency, only retrieve the fields that the
            // application uses.
            search.setFields("items(id/kind,id/videoId,snippet/title,snippet/channelTitle,snippet/thumbnails/default/url)");
            search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);

            // Call the API and print results.
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();

            if (!searchResultList.isEmpty()) {
                SearchResult sr = searchResultList.get(0);
                ResourceId rId = sr.getId();

                if(rId.getKind().equals("youtube#video")) {
                    return sr;
                }
            }
        } catch (IOException e) {
            logger.error("Unable to retrieve data from YouTube API.", e);
        }
        return null;
    }

    public VideoSnippet getYtVideoDataById(String videoId) {
        try {
            YouTube.Videos.List videos = youtube.videos().list("snippet");
            videos.setKey(youtubeApiKey);
            videos.setId(videoId);
            videos.setFields("items(kind,snippet/title,snippet/channelTitle,snippet/thumbnails/default/url)");
            VideoListResponse videoListResponse = videos.execute();
            List<Video> videoList = videoListResponse.getItems();

            if(!videoList.isEmpty()) {
                Video video = videoList.get(0);

                if(video.getKind().equals("youtube#video")) {
                    return video.getSnippet();
                }
            }
        } catch (IOException e) {
            logger.error("Unable to retrieve data from YouTube API.", e);
        }
        return null;
    }

    public Map<String, PlaylistItemSnippet> getYtVideoPlaylistById(String playlistId) {
        try {
            YouTube.PlaylistItems.List playlistItems = youtube.playlistItems().list("snippet");
            playlistItems.setKey(youtubeApiKey);
            playlistItems.setPlaylistId(playlistId);
            playlistItems.setFields("items(snippet/thumbnails/default/url,snippet/channelTitle,snippet/resourceId)");
            PlaylistItemListResponse playlistItemListResponse = playlistItems.execute();
            List<PlaylistItem> playlistItemList = playlistItemListResponse.getItems();

            if(!playlistItemList.isEmpty()) {
                Map<String, PlaylistItemSnippet> playlistItemMap = new HashMap<>();
                for(PlaylistItem playlistItem : playlistItemList) {
                    if(playlistItem.getSnippet().getResourceId().getKind().equals("youtube#video")) {
                        String videoId = playlistItem.getSnippet().getResourceId().getVideoId();
                        playlistItemMap.put(videoId, playlistItem.getSnippet());
                    }
                }
                return playlistItemMap;
            }
        } catch (IOException e) {
            logger.error("Unable to retrieve data from YouTube API.", e);
        }
        return null;
    }
}