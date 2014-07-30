package com.huanghua.mysecret.load;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.os.Handler;

import com.huanghua.mysecret.bean.CommentSupport;
import com.huanghua.mysecret.bean.Secret;
import com.huanghua.mysecret.bean.SecretSupport;
import com.huanghua.mysecret.util.CommonUtils;

public class DateLoad {

    private static final String TAG = "date_load";

    public interface OnDateLoadCompleteListener {
        public void OnLoadSecretSupportComplete(int position,
                List<SecretSupport> list);
        public void OnLoadSecretCommentComplete(int position, int count);
    }

    public DateLoad() {
    }

    public static void loadDate(Context context,
            OnDateLoadCompleteListener listener, Handler handler,
            Secret secret, int priority) {

        DateLoadTask task = new DateLoadTask(context, listener, handler,
                secret, priority);
        // new Thread(task).start();
        DateLoadThreadManager.submitTask(secret.getObjectId(), task);
        CommonUtils.showLog(TAG, "execute a ImageLoadTask !");
    }

    private static final int INITIAL_CAPACITY = 50;

    private static final LinkedHashMap<String, List<SecretSupport>> sSecretSupportMap = new LinkedHashMap<String, List<SecretSupport>>(
            INITIAL_CAPACITY / 2, 0.75f, true) {

        private static final long serialVersionUID = 1L;

        protected boolean removeEldestEntry(
                java.util.Map.Entry<String, List<SecretSupport>> eldest) {
            if (size() > INITIAL_CAPACITY) {
                sSoftSecretSupportMap.put(
                        eldest.getKey(),
                        new SoftReference<List<SecretSupport>>(eldest
                                .getValue()));
                return true;
            }
            return false;
        };
    };
    private static final ConcurrentHashMap<String, SoftReference<List<SecretSupport>>> sSoftSecretSupportMap = new ConcurrentHashMap<String, SoftReference<List<SecretSupport>>>();

    public static void put(String objectId, List<SecretSupport> list) {
        if (isEmptyOrWhitespace(objectId) || list == null) {
            return;
        }
        synchronized (sSecretSupportMap) {
            if (sSecretSupportMap.get(objectId) == null) {
                sSecretSupportMap.put(objectId, list);
            }
        }
    }

    public static List<SecretSupport> get(String objectId) {
        synchronized (sSecretSupportMap) {
            List<SecretSupport> secretSupport = (List<SecretSupport>) sSecretSupportMap
                    .get(objectId);
            if (secretSupport != null) {
                return secretSupport;
            }
            SoftReference<List<SecretSupport>> sSecretSupport = sSoftSecretSupportMap
                    .get(objectId);
            if (sSecretSupport != null) {
                secretSupport = sSecretSupport.get();
                if (secretSupport == null) {
                    sSoftSecretSupportMap.remove(objectId);
                } else {
                    return secretSupport;
                }
            }
            return null;
        }
    }

    public static void update(String objectId, List<SecretSupport> list) {
        if (isEmptyOrWhitespace(objectId) || list == null) {
            return;
        }
        synchronized (sSecretSupportMap) {
            if (sSecretSupportMap.get(objectId) == null) {
                sSecretSupportMap.put(objectId, list);
            } else {
                sSecretSupportMap.remove(objectId);
                sSecretSupportMap.put(objectId, list);
            }
        }
    }

    private static final LinkedHashMap<String, Integer> sCommentMap = new LinkedHashMap<String, Integer>(
            INITIAL_CAPACITY / 2, 0.75f, true) {

        private static final long serialVersionUID = 1L;

        protected boolean removeEldestEntry(
                java.util.Map.Entry<String, Integer> eldest) {
            if (size() > INITIAL_CAPACITY) {
                sSoftCommentMap.put(eldest.getKey(),
                        new SoftReference<Integer>(eldest.getValue()));
                return true;
            }
            return false;
        };
    };
    private static final ConcurrentHashMap<String, SoftReference<Integer>> sSoftCommentMap = new ConcurrentHashMap<String, SoftReference<Integer>>();

    public static void putComment(String objectId, Integer list) {
        if (isEmptyOrWhitespace(objectId) || list == null) {
            return;
        }
        synchronized (sCommentMap) {
            if (sCommentMap.get(objectId) == null) {
                sCommentMap.put(objectId, list);
            }
        }
    }

    public static Integer getComment(String objectId) {
        synchronized (sCommentMap) {
            Integer secretComment = (Integer) sCommentMap
                    .get(objectId);
            if (secretComment != null) {
                return secretComment;
            }
            SoftReference<Integer> sSecretComment = sSoftCommentMap
                    .get(objectId);
            if (sSecretComment != null) {
                secretComment = sSecretComment.get();
                if (secretComment == null) {
                    sSoftCommentMap.remove(objectId);
                } else {
                    return secretComment;
                }
            }
            return null;
        }
    }

    public static void updateComment(String objectId, Integer list) {
        if (isEmptyOrWhitespace(objectId) || list == null) {
            return;
        }
        synchronized (sCommentMap) {
            if (sCommentMap.get(objectId) == null) {
                sCommentMap.put(objectId, list);
            } else {
                sCommentMap.remove(objectId);
                sCommentMap.put(objectId, list);
            }
        }
    }

    public static void clearAll() {
        sSecretSupportMap.clear();
        sCommentMap.clear();
    }

    public static boolean isEmptyOrWhitespace(String s) {
        s = makeSafe(s);
        for (int i = 0, n = s.length(); i < n; i++) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String makeSafe(String s) {
        return (s == null) ? "" : s;
    }

    private static final LinkedHashMap<String, List<CommentSupport>> sCommentSupportMap = new LinkedHashMap<String, List<CommentSupport>>(
            INITIAL_CAPACITY / 2, 0.75f, true) {

        private static final long serialVersionUID = 1L;

        protected boolean removeEldestEntry(
                java.util.Map.Entry<String, List<CommentSupport>> eldest) {
            if (size() > INITIAL_CAPACITY) {
                sSoftCommentSupportMap.put(
                        eldest.getKey(),
                        new SoftReference<List<CommentSupport>>(eldest
                                .getValue()));
                return true;
            }
            return false;
        };
    };
    private static final ConcurrentHashMap<String, SoftReference<List<CommentSupport>>> sSoftCommentSupportMap = new ConcurrentHashMap<String, SoftReference<List<CommentSupport>>>();

    public static void putCommentSupport(String objectId, List<CommentSupport> list) {
        if (isEmptyOrWhitespace(objectId) || list == null) {
            return;
        }
        synchronized (sCommentSupportMap) {
            if (sCommentSupportMap.get(objectId) == null) {
                sCommentSupportMap.put(objectId, list);
            }
        }
    }

    public static List<CommentSupport> getCommentSupport(String objectId) {
        synchronized (sCommentSupportMap) {
            List<CommentSupport> secretSupport = (List<CommentSupport>) sCommentSupportMap
                    .get(objectId);
            if (secretSupport != null) {
                return secretSupport;
            }
            SoftReference<List<CommentSupport>> sSecretSupport = sSoftCommentSupportMap
                    .get(objectId);
            if (sSecretSupport != null) {
                secretSupport = sSecretSupport.get();
                if (secretSupport == null) {
                    sSoftCommentSupportMap.remove(objectId);
                } else {
                    return secretSupport;
                }
            }
            return null;
        }
    }

    public static void updateCommentSupport(String objectId, List<CommentSupport> list) {
        if (isEmptyOrWhitespace(objectId) || list == null) {
            return;
        }
        synchronized (sCommentSupportMap) {
            if (sCommentSupportMap.get(objectId) == null) {
                sCommentSupportMap.put(objectId, list);
            } else {
                sCommentSupportMap.remove(objectId);
                sCommentSupportMap.put(objectId, list);
            }
        }
    }

    public static void clearCommentSupport() {
        sCommentSupportMap.clear();
    }
}
