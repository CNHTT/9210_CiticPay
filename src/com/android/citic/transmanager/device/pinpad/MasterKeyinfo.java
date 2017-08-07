package com.android.citic.transmanager.device.pinpad;

import com.pos.device.ped.KeySystem;
import com.pos.device.ped.KeyType;

/**
 * Created by zhouqiang on 2017/3/15.
 */

public class MasterKeyinfo {

    private int masterIndex ;
    private KeyType keyType ;
    private byte[] plainKeyData ;
    private KeySystem keySystem ;

    public int getMasterIndex() {
        return masterIndex;
    }

    public void setMasterIndex(int masterIndex) {
        this.masterIndex = masterIndex;
    }

    public KeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(KeyType keyType) {
        this.keyType = keyType;
    }

    public byte[] getPlainKeyData() {
        return plainKeyData;
    }

    public void setPlainKeyData(byte[] plainKeyData) {
        this.plainKeyData = plainKeyData;
    }

    public KeySystem getKeySystem() {
        return keySystem;
    }

    public void setKeySystem(KeySystem keySystem) {
        this.keySystem = keySystem;
    }
}
