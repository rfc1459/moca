// @formatter:off
/*
 * Copyright 2011, 2012 Michael Burton.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
// @formatter:on

package org.level28.android.moca.util;

import static com.google.common.base.Preconditions.checkState;

import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import com.google.common.collect.Lists;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * A class similar but unrelated to android's {@link android.os.AsyncTask}.
 * <p>
 * Unlike AsyncTask, this class properly propagates exceptions.
 * <p>
 * If you're familiar with AsyncTask and are looking for
 * {@link android.os.AsyncTask#doInBackground(Object[])}, we've named it
 * {@link #call()} here to conform with java 1.5's
 * {@link java.util.concurrent.Callable} interface.
 * <p>
 * Current limitations: does not yet handle progress, although it shouldn't be
 * hard to add.
 * <p>
 * If using your own executor, you must call future() to get a runnable you can
 * execute.
 * 
 * @param <ResultT>
 */
public abstract class SafeAsyncTask<ResultT> implements Callable<ResultT> {
    private static final String LOG_TAG = "SafeAsyncTask";

    /** Default size for the executor thread pool. */
    public static final int DEFAULT_POOL_SIZE = 25;

    /** Single-threaded executor. */
    public static final Executor SINGLE_THREAD_EXECUTOR = Executors
            .newFixedThreadPool(1);

    /** Multi-threaded executor with default thread pool size. */
    public static final Executor DEFAULT_EXECUTOR = Executors
            .newFixedThreadPool(DEFAULT_POOL_SIZE);

    /** Handler used to post events back to the UI thread. */
    protected Handler mHandler;

    /** Executor used to run jobs in background */
    protected Executor mExecutor;

    /** @hide */
    protected StackTraceElement[] mLaunchLocation;

    /** @hide */
    protected FutureTask<Void> mFuture;

    /**
     * Create a new SafeAsyncTask using the default multi-threaded executor.
     */
    public SafeAsyncTask() {
        this(null, DEFAULT_EXECUTOR);
    }

    /**
     * Create a new SafeAsyncTask with the specified handler and using the
     * default multi-threaded executor.
     * 
     * @param handler
     *            the handler to which UI events will be posted
     */
    public SafeAsyncTask(Handler handler) {
        this(handler, DEFAULT_EXECUTOR);
    }

    /**
     * Create a new SafeAsyncTask using a custom executor.
     * 
     * @param executor
     *            the executor which this SafeAsyncTask will use to run
     *            background jobs
     */
    public SafeAsyncTask(Executor executor) {
        this(null, executor);
    }

    /**
     * Create a fully-customized SafeAsyncTask.
     * 
     * @param handler
     *            the handler to which UI events will be posted
     * @param executor
     *            the executor which this SafeAsyncTask will use to run
     *            background jobs
     */
    public SafeAsyncTask(Handler handler, Executor executor) {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        mHandler = handler;
        mExecutor = executor;
    }

    /**
     * Obtain a new {@link FutureTask} for this SafeAsyncTask.
     * 
     * @return a {@code FutureTask} wrapping a new {@link Task} instance
     */
    public FutureTask<Void> future() {
        mFuture = new FutureTask<Void>(newTask());
        return mFuture;
    }

    /**
     * Bind a new {@link Executor} to this {@code SafeAsyncTask}.
     * 
     * @param executor
     *            the new executor
     * @return this {@code SafeAsyncTask} instance (useful for call chaining)
     */
    public SafeAsyncTask<ResultT> executor(Executor executor) {
        mExecutor = executor;
        return this;
    }

    /** Get the current {@link Executor}. */
    public Executor executor() {
        return mExecutor;
    }

    /**
     * Bind a new {@link Handler} to this {@code SafeAsyncTask}.
     * 
     * @param handler
     *            the new handler
     * @return this {@code SafeAsyncTask} instance (useful for call chaining)
     */
    public SafeAsyncTask<ResultT> handler(Handler handler) {
        mHandler = handler;
        return this;
    }

    public Handler handler() {
        return mHandler;
    }

    /**
     * Schedule a new {@link Task} for background execution.
     * <p>
     * Depending on the current {@link Executor}, the task might be queued or
     * executed concurrently with other background tasks.
     */
    public void execute() {
        execute(Thread.currentThread().getStackTrace());
    }

    /** @hide */
    protected void execute(StackTraceElement[] launchLocation) {
        mLaunchLocation = launchLocation;
        mExecutor.execute(future());
    }

    /**
     * Cancel the last submitted {@link Task}.
     * <p>
     * <b>Nota Bene</b>: once you submit a new task, older ones cannot be
     * cancelled.
     * 
     * @param mayInterruptIfRunning
     *            {@code true} if the thread executing this task should be
     *            interrupted; otherwise, in-progress tasks are allowed to
     *            complete
     * @return {@code false} if the task could not be cancelled, typically
     *         because it has already completed normally; {@code true} otherwise
     * @see FutureTask#cancel(boolean)
     */
    public boolean cancel(boolean mayInterruptIfRunning) {
        checkState(mFuture != null,
                "You cannot cancel this task before calling future()");
        return mFuture.cancel(mayInterruptIfRunning);
    }

    /**
     * Runs on the UI thread before {@link #call()}.
     * 
     * @throws Exception
     *             captured or passed to {@link #onException(Exception)} if
     *             present.
     * @see {@link android.os.AsyncTask#onPreExecute() AsyncTask.onPreExecute()}
     */
    protected void onPreExecute() throws Exception {
    }

    /**
     * Runs on the UI thread after {@link #call()} has successfully completed
     * its execution.
     * 
     * @param r
     *            result of {@link #call()} invocation
     * @throws Exception
     *             captured or passed to {@link #onException(Exception)} if
     *             present.
     */
    protected void onSuccess(ResultT r) throws Exception {
    }

    /**
     * Runs on the UI thread if {@link #call()} has been interrupted by an
     * {@link InterruptedException} or an {@link InterruptedIOException}.
     * 
     * @param e
     *            The exception which caused {@link #call()} to be interrupted
     */
    protected void onInterrupted(Exception e) {
        onException(e);
    }

    /**
     * Runs on the UI thread if the background thread has encountered any
     * unhandled {@link Exception}.
     * 
     * @param e
     *            the unhandled execption
     * @throws RuntimeException
     *             if anything goes wrong (hope it does not)
     * @see #onThrowable(Throwable)
     */
    protected void onException(Exception e) throws RuntimeException {
        onThrowable(e);
    }

    /**
     * Runs on the UI thread if the background thread has encountered any
     * unhandled {@link Throwable}.
     * 
     * @param t
     *            the unhandled throwable
     * @throws RuntimeException
     *             if anything goes wrong (hope it does not)
     * @see #onException(Exception)
     */
    protected void onThrowable(Throwable t) throws RuntimeException {
        Log.e(LOG_TAG, "Throwable caught during background processing", t);
    }

    /**
     * Runs on the UI thread when the background thread has finished processing
     * regardless of its exception status.
     * 
     * @throws RuntimeException
     *             if anything goes wrong
     */
    protected void onFinally() throws RuntimeException {
    }

    /** @hide */
    protected Task<ResultT> newTask() {
        return new Task<ResultT>(this);
    }

    /** @hide */
    public static class Task<ResultT> implements Callable<Void> {
        protected final SafeAsyncTask<ResultT> mParent;
        protected final Handler mHandler;

        public Task(SafeAsyncTask<ResultT> parent) {
            mParent = parent;
            mHandler = parent.mHandler;
        }

        public Void call() throws Exception {
            try {
                doPreExecute();
                doSuccess(doCall());
            } catch (final Exception e) {
                try {
                    doException(e);
                } catch (Exception f) {
                    // Logged but ignored
                    Log.e(LOG_TAG, f.toString());
                }
            } catch (final Throwable t) {
                try {
                    doThrowable(t);
                } catch (Exception f) {
                    // Logged but ignored
                    Log.e(LOG_TAG, f.toString());
                }
            } finally {
                doFinally();
            }

            return null;
        }

        protected void doPreExecute() throws Exception {
            postToUiThreadAndWait(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    mParent.onPreExecute();
                    return null;
                }
            });
        }

        protected ResultT doCall() throws Exception {
            return mParent.call();
        }

        protected void doSuccess(final ResultT r) throws Exception {
            postToUiThreadAndWait(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    mParent.onSuccess(r);
                    return null;
                }
            });
        }

        protected void doException(final Exception e) throws Exception {
            fixupStackTrace(e);
            postToUiThreadAndWait(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    if (e instanceof InterruptedException
                            || e instanceof InterruptedIOException) {
                        mParent.onInterrupted(e);
                    } else {
                        mParent.onException(e);
                    }
                    return null;
                }
            });
        }

        protected void doThrowable(final Throwable e) throws Exception {
            fixupStackTrace(e);
            postToUiThreadAndWait(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    mParent.onThrowable(e);
                    return null;
                }
            });
        }

        protected void doFinally() throws Exception {
            postToUiThreadAndWait(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    mParent.onFinally();
                    return null;
                }
            });
        }

        protected void postToUiThreadAndWait(final Callable<Void> c)
                throws Exception {
            final CountDownLatch latch = new CountDownLatch(1);
            final Exception[] exceptions = new Exception[1];

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        c.call();
                    } catch (Exception e) {
                        exceptions[0] = e;
                    } finally {
                        latch.countDown();
                    }
                }
            });

            // Wait for the UI thread to return control to us
            latch.await();

            if (exceptions[0] != null) {
                throw exceptions[0];
            }
        }

        // @formatter:off
        private void fixupStackTrace(final Throwable e) {
            if (mParent.mLaunchLocation != null) {
                final ArrayList<StackTraceElement> stack = Lists.newArrayList(Arrays.asList(e.getStackTrace()));
                stack.addAll(Arrays.asList(mParent.mLaunchLocation));
                e.setStackTrace(stack.toArray(new StackTraceElement[stack.size()]));
            }
        }
        // @formatter:on
    }
}
