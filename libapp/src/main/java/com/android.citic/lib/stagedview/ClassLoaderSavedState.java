package com.android.citic.lib.stagedview;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class ClassLoaderSavedState implements Parcelable {
    public static final ClassLoaderSavedState EMPTY_STATE = new ClassLoaderSavedState() {};

    private Parcelable mSuperState = EMPTY_STATE;
    private ClassLoader mClassLoader;

    /**
     * Constructor used to make the EMPTY_STATE singleton
     */
    private ClassLoaderSavedState() {
        mSuperState = null;
        mClassLoader = null;
    }

    /**
     * Constructor called by derived classes when creating their ListSavedState objects
     *
     * @param superState The state of the superclass of this view
     */
    protected ClassLoaderSavedState(Parcelable superState, ClassLoader classLoader) {
        mClassLoader = classLoader;
        if (superState == null) {
            throw new IllegalArgumentException("superState must not be null");
        }
        else {
            mSuperState = superState != EMPTY_STATE ? superState : null;
        }
    }

    /**
     * Constructor used when reading from a parcel. Reads the state of the superclass.
     *
     * @param source
     */
    protected ClassLoaderSavedState(Parcel source) {
        // ETSY : we're using the passed super class loader unlike AbsSavedState
        Parcelable superState = source.readParcelable(mClassLoader);
        mSuperState = superState != null ? superState : EMPTY_STATE;
    }

    final public Parcelable getSuperState() {
        return mSuperState;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mSuperState, flags);
    }

    public static final Creator<ClassLoaderSavedState> CREATOR
            = new Creator<ClassLoaderSavedState>() {

        public ClassLoaderSavedState createFromParcel(Parcel in) {
            Parcelable superState = in.readParcelable(null);
            if (superState != null) {
                throw new IllegalStateException("superState must be null");
            }
            return EMPTY_STATE;
        }

        public ClassLoaderSavedState[] newArray(int size) {
            return new ClassLoaderSavedState[size];
        }
    };
}
