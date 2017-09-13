package com.wr.musicorganizer.utils

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


// @kotlin.internal.InlineOnly public inline fun <T> T.apply(block: T.() -> kotlin.Unit): T { /* compiled code */ }
public fun <T> Observable<T>.doInBackground(): Observable<T> = this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

public fun <T> Single<T>.doInBackground(): Single<T> = this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

public fun Completable.doInBackground(): Completable = this.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())