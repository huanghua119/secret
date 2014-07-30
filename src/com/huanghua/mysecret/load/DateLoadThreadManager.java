package com.huanghua.mysecret.load;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.huanghua.mysecret.util.CommonUtils;

public class DateLoadThreadManager {
    private static final String TAG = "date_load";
    private static final int CORE_POOL_SIZE = 2;
    private static final int MAXI_POOL_SIZE = 128;
    private static final int KEEP_ALIVE = 10;

    private static final ThreadPoolExecutor mExecutor;
    private static final ThreadFactory mThreadFactory;
    private static final PriorityBlockingQueue<Runnable> mWorkQueue;
    private static ConcurrentHashMap<String, DateLoadTask> mHashMap;
    private static final Comparator<Runnable> mComparator;

    static {
        mComparator = new TaskComparator();
        mHashMap = new ConcurrentHashMap<String, DateLoadTask>();
        mWorkQueue = new PriorityBlockingQueue<Runnable>(15, mComparator);
        mThreadFactory = new DefaultThreadFactory();
        mExecutor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXI_POOL_SIZE,
                KEEP_ALIVE, TimeUnit.SECONDS, mWorkQueue, mThreadFactory);
    }

    public DateLoadThreadManager() {
    }

    public static void removeTask(String objectId) {
        mHashMap.remove(objectId);
    }

    public static void removeAllTask() {
        if (mHashMap.size() > 0) {
            mHashMap.clear();
            mWorkQueue.clear();
            DateLoad.clearAll();
        }
    }

    public static void updateWorkQueue(ArrayList<String> newTaskUrl) {
        for (DateLoadTask task : mHashMap.values()) {
            if (!newTaskUrl.contains(task.mSecret.getObjectId())) {
                mWorkQueue.remove(task);
                mHashMap.remove(task.mSecret.getObjectId());
            }
        }
    }

    public static void submitTask(String objectId, DateLoadTask task) {
        if (mHashMap.get(objectId) == null) {
            mHashMap.put(objectId, task);
            mExecutor.execute(task);
        } else {
            CommonUtils.showLog(TAG, "there is already a task running !");
        }
    }

    static class DefaultThreadFactory implements ThreadFactory {

        private final AtomicInteger mCount;

        DefaultThreadFactory() {
            mCount = new AtomicInteger(1);
        }

        public Thread newThread(Runnable runnable) {

            CommonUtils.showLog(TAG, "New a Thread for  ImageLoadTask:"
                    + mCount.toString());
            return new Thread(runnable, "ImageLoadTask #"
                    + mCount.getAndIncrement());
        }
    }

    static class TaskComparator implements Comparator<Runnable> {

        @Override
        public int compare(Runnable runnable1, Runnable runnable2) {
            if (runnable1 instanceof DateLoadTask
                    && runnable2 instanceof DateLoadTask) {
                long x = ((DateLoadTask) runnable1).mPriority;
                long y = ((DateLoadTask) runnable2).mPriority;
                if (x < y) {
                    return -1;
                } else if (x > y) {
                    return 1;
                } else {
                    return 0;
                }
            }
            return 0;
        }

    }
}
