package com.vbm.logger.floating;

import android.app.Activity;
import android.app.Application;
import android.content.res.ColorStateList;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;

import com.vbm.logger.NetworkLogger;
import com.vbm.logger.R;
import com.vbm.logger.ui.NetworkLogActivity;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

/**
 * Draggable in-app debug bubble attached to each activity's own content view via
 * {@link android.app.Application.ActivityLifecycleCallbacks}. Deliberately not a system-alert-window
 * overlay: it only ever renders inside this app's activities, so no SYSTEM_ALERT_WINDOW permission
 * or foreground service is needed, and it can never appear over other apps.
 */
public final class FloatingBubbleManager {

    private static final int BUBBLE_SIZE_DP = 52;
    private static final int CLICK_DRAG_TOLERANCE_DP = 8;

    private static final WeakHashMap<Activity, View> activeBubbles = new WeakHashMap<>();
    private static Application registeredApplication;
    private static WeakReference<Activity> currentActivityRef;
    private static volatile boolean enabled = false;

    // Remembered position (fraction of screen), shared across activities for continuity.
    private static float lastXFraction = 0.85f;
    private static float lastYFraction = 0.7f;

    private FloatingBubbleManager() {
    }

    public static synchronized void start(Application application) {
        registerTracking(application);
        enabled = true;
        Activity current = currentActivityRef != null ? currentActivityRef.get() : null;
        if (current != null) {
            attach(current);
        }
    }

    public static synchronized void stop(Application application) {
        enabled = false;
        for (Activity activity : activeBubbles.keySet().toArray(new Activity[0])) {
            detach(activity);
        }
    }

    /**
     * Tracks the current foreground activity for the lifetime of the app so {@link #start} can
     * attach immediately even if the bubble is turned on mid-session, on an activity that already
     * started. Must be called as early as possible (from {@code NetworkLogger.init()}) — a
     * lifecycle callback registered later never fires for an activity that already started.
     * Registered once and left in place; {@code enabled} gates whether a view is actually added.
     */
    public static synchronized void registerTracking(Application application) {
        if (registeredApplication != null) {
            return;
        }
        registeredApplication = application;
        application.registerActivityLifecycleCallbacks(new SimpleActivityLifecycleCallbacks() {
            @Override
            public void onActivityStarted(Activity activity) {
                currentActivityRef = new WeakReference<>(activity);
                if (enabled) {
                    attach(activity);
                }
            }

            @Override
            public void onActivityStopped(Activity activity) {
                detach(activity);
            }
        });
    }

    public static boolean isEnabled() {
        return enabled;
    }

    private static void attach(Activity activity) {
        if (activity == null || activeBubbles.containsKey(activity)
                || activity instanceof NetworkLogActivity) {
            return;
        }

        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content == null) {
            return;
        }

        ImageView bubble = new ImageView(activity);
        int sizePx = dp(activity, BUBBLE_SIZE_DP);
        int paddingPx = dp(activity, 14);
        bubble.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
        bubble.setImageResource(R.drawable.ic_bug_report_24);
        bubble.setBackgroundResource(R.drawable.bg_bubble_circle);
        bubble.setElevation(dp(activity, 6));
        bubble.setContentDescription("NetworkLogger debug bubble");
        updateBubbleTint(bubble);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(sizePx, sizePx);
        bubble.setOnTouchListener(new DragHandler(activity, bubble, params, content));

        content.addView(bubble, params);
        activeBubbles.put(activity, bubble);

        bubble.post(() -> {
            int parentWidth = content.getWidth();
            int parentHeight = content.getHeight();
            if (parentWidth == 0 || parentHeight == 0) {
                return;
            }
            params.leftMargin = clamp((int) (lastXFraction * parentWidth), 0, parentWidth - sizePx);
            params.topMargin = clamp((int) (lastYFraction * parentHeight), 0, parentHeight - sizePx);
            bubble.setLayoutParams(params);
        });
    }

    private static void detach(Activity activity) {
        View bubble = activeBubbles.remove(activity);
        if (bubble != null && bubble.getParent() instanceof ViewGroup) {
            ((ViewGroup) bubble.getParent()).removeView(bubble);
        }
    }

    private static void updateBubbleTint(ImageView bubble) {
        boolean loggingEnabled = NetworkLogger.getInstance().isLoggingEnabled();
        int colorRes = loggingEnabled ? R.color.nl_accent : R.color.nl_text_secondary;
        int color = ContextCompat.getColor(bubble.getContext(), colorRes);
        bubble.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    private static void showMenu(Activity activity, ImageView bubble) {
        PopupMenu popupMenu = new PopupMenu(activity, bubble);
        popupMenu.getMenuInflater().inflate(R.menu.menu_floating_bubble, popupMenu.getMenu());

        boolean loggingEnabled = NetworkLogger.getInstance().isLoggingEnabled();
        popupMenu.getMenu().findItem(R.id.action_start_logging).setEnabled(!loggingEnabled);
        popupMenu.getMenu().findItem(R.id.action_stop_logging).setEnabled(loggingEnabled);

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_start_logging) {
                NetworkLogger.getInstance().setEnabled(true);
                updateBubbleTint(bubble);
                return true;
            }
            if (id == R.id.action_stop_logging) {
                NetworkLogger.getInstance().setEnabled(false);
                updateBubbleTint(bubble);
                return true;
            }
            if (id == R.id.action_view_logs) {
                activity.startActivity(NetworkLogActivity.newIntent(activity));
                return true;
            }
            if (id == R.id.action_clear_logs) {
                new AlertDialog.Builder(activity)
                        .setTitle("Delete all logs?")
                        .setMessage("This action cannot be undone.")
                        .setPositiveButton("Delete", (dialog, which) -> NetworkLogger.getInstance().clearLogs())
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private static int clamp(int value, int min, int max) {
        if (max < min) {
            return min;
        }
        return Math.max(min, Math.min(value, max));
    }

    private static int dp(Activity activity, int dp) {
        float density = activity.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    /** Handles drag-to-move vs. tap-to-open-menu on the bubble. */
    private static class DragHandler implements View.OnTouchListener {
        private final Activity activity;
        private final ImageView bubble;
        private final FrameLayout.LayoutParams params;
        private final ViewGroup parent;
        private final int touchSlopPx;

        private float downRawX;
        private float downRawY;
        private int downMarginLeft;
        private int downMarginTop;

        DragHandler(Activity activity, ImageView bubble, FrameLayout.LayoutParams params, ViewGroup parent) {
            this.activity = activity;
            this.bubble = bubble;
            this.params = params;
            this.parent = parent;
            this.touchSlopPx = Math.max(
                    dp(activity, CLICK_DRAG_TOLERANCE_DP),
                    ViewConfiguration.get(activity).getScaledTouchSlop());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downRawX = event.getRawX();
                    downRawY = event.getRawY();
                    downMarginLeft = params.leftMargin;
                    downMarginTop = params.topMargin;
                    return true;

                case MotionEvent.ACTION_MOVE: {
                    int dx = (int) (event.getRawX() - downRawX);
                    int dy = (int) (event.getRawY() - downRawY);
                    int maxLeft = Math.max(0, parent.getWidth() - bubble.getWidth());
                    int maxTop = Math.max(0, parent.getHeight() - bubble.getHeight());
                    params.leftMargin = clamp(downMarginLeft + dx, 0, maxLeft);
                    params.topMargin = clamp(downMarginTop + dy, 0, maxTop);
                    bubble.setLayoutParams(params);
                    return true;
                }

                case MotionEvent.ACTION_UP: {
                    float totalDx = Math.abs(event.getRawX() - downRawX);
                    float totalDy = Math.abs(event.getRawY() - downRawY);
                    if (totalDx < touchSlopPx && totalDy < touchSlopPx) {
                        showMenu(activity, bubble);
                    } else {
                        int parentWidth = parent.getWidth();
                        int parentHeight = parent.getHeight();
                        if (parentWidth > 0 && parentHeight > 0) {
                            lastXFraction = params.leftMargin / (float) parentWidth;
                            lastYFraction = params.topMargin / (float) parentHeight;
                        }
                    }
                    return true;
                }

                default:
                    return false;
            }
        }
    }
}
