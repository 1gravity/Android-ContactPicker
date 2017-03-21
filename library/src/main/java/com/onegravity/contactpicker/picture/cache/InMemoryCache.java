/*
 * Copyright (C) 2015-2017 Emanuel Moecklin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.onegravity.contactpicker.picture.cache;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;

import java.lang.ref.SoftReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is an in-memory implementation of the Cache interface
 */
public abstract class InMemoryCache<K, V> implements Cache<K, V> {

    // Both hard and soft caches are purged after n seconds idling.
    private final int mDelayBeforePurge;

    private Handler mPurgeHandler;
    private Runnable mPurger;

    private final HardLruCache mHardCacheMap;

    // Soft object cache for objects removed from the hard cache
    // this gets cleared by the Garbage Collector every time we get low on memory
    private ConcurrentHashMap<K, SoftReference<V>> mSoftCache;

    // this cache keeps track of misses
    // the caller can use this to decide whether to attempt to retrieve the value
    // (which might be very expensive) or not depending on whether a previous miss has occurred
    private Set<K> mMissCache;

    protected boolean mDebug;

    protected class HardLruCache extends LruCache<K, V> {
        public HardLruCache(int initialCapacity) {
            super(initialCapacity);
        }

        @Override
        protected void entryRemoved (boolean evicted, K key, V oldValue, V newValue) {
            // move the removed item to the soft cache
            mSoftCache.put(key, new SoftReference<V>(newValue));
        }
    }

    // ****************************************** Public Methods *******************************************

    protected InMemoryCache(int delayBeforePurge, int cacheCapacity) {
        mDelayBeforePurge = delayBeforePurge;

        try {
            mPurgeHandler = new Handler();
        }
        catch (RuntimeException e) {
            // can't create handler inside thread that has not called Looper.prepare()
            // --> we need to create our own Looper thread
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    mPurgeHandler = new Handler();
                    Looper.loop();
                }
            }).start();
        }

        mPurger = new Purger();

        mMissCache = new HashSet<K>();
        mSoftCache = new ConcurrentHashMap<>();
        mHardCacheMap = createHardLruCache(cacheCapacity);
    }

    protected HardLruCache createHardLruCache(int cacheCapacity) {
        return new HardLruCache(cacheCapacity);
    }

    /**
     * Purges & clears the caches.
     */
    @Override
    public synchronized void evictAll() {
        stopPurgeTimer();
        clearCaches();
    }

    @Override
    public synchronized void put(K key, V value) {
        if (key != null) {
            if (mDebug) Log.e("1gravity", getClass().getSimpleName() + ".put(" + key + "): " + value);
            mHardCacheMap.put(key, value);
            mMissCache.remove(key);
        }
    }

    /**
     * As the name suggests, this method attempts to obtain a Object stored in one of the caches.
     * First it checks the hard cache for the key.
     * If a key is found, it moves the cached Object to the head of the cache so it gets moved to the soft cache last.
     *
     * If the hard cache doesn't contain the Object, it checks the soft cache for the cached Object.
     * If neither of the caches contain the Object, this returns null.
     */
    @Override
    public synchronized V get(K key) {
        // we reset the caches after every 30 or so seconds of inactivity for memory efficiency
        resetPurgeTimer();

        if (key != null) {
            V value = mHardCacheMap.get(key);
            if(value != null) {
                if (mDebug) Log.e("1gravity", getClass().getSimpleName() + ".get(" + key + "): hit");
                return value;
            }

            SoftReference<V> objectRef = mSoftCache.get(key);
            if(objectRef != null){
                value = objectRef.get();
                if(value != null){
                    if (mDebug) Log.e("1gravity", getClass().getSimpleName() + ".get(" + key + "): hit");
                    return value;
                }
                else {
                    // must have been collected by the Garbage Collector so we remove the bucket from the cache.
                    mSoftCache.remove(key);
                }
            }
        }

        mMissCache.add(key);
        return null;
    }

    /**
     * This get method returns:
     *  1) the cached value if one exists in the cache
     *  2) the missedValue if a previous cache miss has occurred before
     *  3) null in every other case
     */
    public synchronized V get(K key, V missedValue) {
        if (key != null) {
            boolean hadMiss = mMissCache.contains(key);
            V result = get(key);
            if (result != null) {
                return result;
            }
            if (hadMiss) {
                if (mDebug) Log.e("1gravity", getClass().getSimpleName() + ".get(" + key + "): miss repeatedly");
                return missedValue;
            }
            if (mDebug) Log.e("1gravity", getClass().getSimpleName() + ".get(" + key + "): miss");
        }
        return null;
    }

    // ****************************************** Private Classes + Methods *******************************************

    private class Purger implements Runnable {
        @Override
        public void run() {
            clearCaches();
        }
    }

    private void clearCaches() {
        mMissCache.clear();
        mSoftCache.clear();
        mHardCacheMap.evictAll();
    }

    /**
     * Stops the cache purger from running until it is reset again.
     */
    private void stopPurgeTimer() {
        if (mPurgeHandler!=null) mPurgeHandler.removeCallbacks(mPurger);
    }

    /**
     * Purges the cache every (DELAY_BEFORE_PURGE) milliseconds.
     */
    private void resetPurgeTimer() {
        if (mPurgeHandler!=null) {
            mPurgeHandler.removeCallbacks(mPurger);
            mPurgeHandler.postDelayed(mPurger, mDelayBeforePurge);
        }
    }

}