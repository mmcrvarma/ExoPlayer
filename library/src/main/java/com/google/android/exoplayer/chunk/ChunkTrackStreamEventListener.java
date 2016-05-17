/*
 * Copyright (C) 2014 The Android Open Source Project
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
package com.google.android.exoplayer.chunk;

import com.google.android.exoplayer.C;
import com.google.android.exoplayer.Format;
import com.google.android.exoplayer.TrackStream;
import com.google.android.exoplayer.util.Assertions;
import com.google.android.exoplayer.util.Util;

import android.os.Handler;

import java.io.IOException;

/**
 * Interface for callbacks to be notified of chunk based {@link ChunkTrackStream} events.
 */
public interface ChunkTrackStreamEventListener {

  /**
   * Invoked when an upstream load is started.
   *
   * @param sourceId The id of the reporting {@link ChunkTrackStream}.
   * @param length The length of the data being loaded in bytes, or {@link C#LENGTH_UNBOUNDED} if
   *     the length of the data is not known in advance.
   * @param type The type of the data being loaded.
   * @param trigger The reason for the data being loaded.
   * @param format The particular format to which this data corresponds, or null if the data being
   *     loaded does not correspond to a format.
   * @param mediaStartTimeMs The media time of the start of the data being loaded, or -1 if this
   *     load is for initialization data.
   * @param mediaEndTimeMs The media time of the end of the data being loaded, or -1 if this
   *     load is for initialization data.
   */
  void onLoadStarted(int sourceId, long length, int type, int trigger, Format format,
      long mediaStartTimeMs, long mediaEndTimeMs);

  /**
   * Invoked when the current load operation completes.
   *
   * @param sourceId The id of the reporting {@link ChunkTrackStream}.
   * @param bytesLoaded The number of bytes that were loaded.
   * @param type The type of the loaded data.
   * @param trigger The reason for the data being loaded.
   * @param format The particular format to which this data corresponds, or null if the loaded data
   *     does not correspond to a format.
   * @param mediaStartTimeMs The media time of the start of the loaded data, or -1 if this load was
   *     for initialization data.
   * @param mediaEndTimeMs The media time of the end of the loaded data, or -1 if this load was for
   *     initialization data.
   * @param elapsedRealtimeMs {@code elapsedRealtime} timestamp of when the load finished.
   * @param loadDurationMs Amount of time taken to load the data.
   */
   void onLoadCompleted(int sourceId, long bytesLoaded, int type, int trigger, Format format,
       long mediaStartTimeMs, long mediaEndTimeMs, long elapsedRealtimeMs, long loadDurationMs);

  /**
   * Invoked when the current upstream load operation is canceled.
   *
   * @param sourceId The id of the reporting {@link ChunkTrackStream}.
   * @param bytesLoaded The number of bytes that were loaded prior to the cancellation.
   */
  void onLoadCanceled(int sourceId, long bytesLoaded);

  /**
   * Invoked when an error occurs loading media data.
   *
   * @param sourceId The id of the reporting {@link ChunkTrackStream}.
   * @param e The cause of the failure.
   */
  void onLoadError(int sourceId, IOException e);

  /**
   * Invoked when data is removed from the back of the buffer, typically so that it can be
   * re-buffered using a different representation.
   *
   * @param sourceId The id of the reporting {@link ChunkTrackStream}.
   * @param mediaStartTimeMs The media time of the start of the discarded data.
   * @param mediaEndTimeMs The media time of the end of the discarded data.
   */
  void onUpstreamDiscarded(int sourceId, long mediaStartTimeMs, long mediaEndTimeMs);

  /**
   * Invoked when the downstream format changes (i.e. when the format being supplied to the
   * caller of {@link TrackStream#readData} changes).
   *
   * @param sourceId The id of the reporting {@link ChunkTrackStream}.
   * @param format The format.
   * @param trigger The trigger specified in the corresponding upstream load, as specified by the
   *     {@link ChunkSource}.
   * @param mediaTimeMs The media time at which the change occurred.
   */
  void onDownstreamFormatChanged(int sourceId, Format format, int trigger, long mediaTimeMs);

  /**
   * Dispatches events to a {@link ChunkTrackStreamEventListener}.
   */
  final class EventDispatcher {

    private final Handler handler;
    private final ChunkTrackStreamEventListener listener;
    private final int sourceId;

    public EventDispatcher(Handler handler, ChunkTrackStreamEventListener listener, int sourceId) {
      this.handler = listener != null ? Assertions.checkNotNull(handler) : null;
      this.listener = listener;
      this.sourceId = sourceId;
    }

    public void loadStarted(final long length, final int type, final int trigger,
        final Format format, final long mediaStartTimeUs, final long mediaEndTimeUs) {
      if (listener != null) {
        handler.post(new Runnable()  {
          @Override
          public void run() {
            listener.onLoadStarted(sourceId, length, type, trigger, format,
                Util.usToMs(mediaStartTimeUs), Util.usToMs(mediaEndTimeUs));
          }
        });
      }
    }

    public void loadCompleted(final long bytesLoaded, final int type, final int trigger,
        final Format format, final long mediaStartTimeUs, final long mediaEndTimeUs,
        final long elapsedRealtimeMs, final long loadDurationMs) {
      if (listener != null) {
        handler.post(new Runnable()  {
          @Override
          public void run() {
            listener.onLoadCompleted(sourceId, bytesLoaded, type, trigger, format,
                Util.usToMs(mediaStartTimeUs), Util.usToMs(mediaEndTimeUs), elapsedRealtimeMs,
                loadDurationMs);
          }
        });
      }
    }

    public void loadCanceled(final long bytesLoaded) {
      if (listener != null) {
        handler.post(new Runnable()  {
          @Override
          public void run() {
            listener.onLoadCanceled(sourceId, bytesLoaded);
          }
        });
      }
    }

    public void loadError(final IOException e) {
      if (listener != null) {
        handler.post(new Runnable()  {
          @Override
          public void run() {
            listener.onLoadError(sourceId, e);
          }
        });
      }
    }

    public void upstreamDiscarded(final long mediaStartTimeUs, final long mediaEndTimeUs) {
      if (listener != null) {
        handler.post(new Runnable()  {
          @Override
          public void run() {
            listener.onUpstreamDiscarded(sourceId, Util.usToMs(mediaStartTimeUs),
                Util.usToMs(mediaEndTimeUs));
          }
        });
      }
    }

    public void downstreamFormatChanged(final Format format, final int trigger,
        final long positionUs) {
      if (listener != null) {
        handler.post(new Runnable()  {
          @Override
          public void run() {
            listener.onDownstreamFormatChanged(sourceId, format, trigger,
                Util.usToMs(positionUs));
          }
        });
      }
    }

  }

}
