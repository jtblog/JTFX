/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /storage/sdcard0/AppProjects/JTFX/src/com/JTNNG/aidl/JTFXaidl.aidl
 */
package com.JTNNG.aidl;
public interface JTFXaidl extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements com.JTNNG.aidl.JTFXaidl
{
private static final java.lang.String DESCRIPTOR = "com.JTNNG.aidl.JTFXaidl";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an com.JTNNG.aidl.JTFXaidl interface,
 * generating a proxy if needed.
 */
public static com.JTNNG.aidl.JTFXaidl asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof com.JTNNG.aidl.JTFXaidl))) {
return ((com.JTNNG.aidl.JTFXaidl)iin);
}
return new com.JTNNG.aidl.JTFXaidl.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_setLoginParameters:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
this.setLoginParameters(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements com.JTNNG.aidl.JTFXaidl
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public void setLoginParameters(java.lang.String mUsername, java.lang.String mPassword, java.lang.String mHost, java.lang.String mConnection) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(mUsername);
_data.writeString(mPassword);
_data.writeString(mHost);
_data.writeString(mConnection);
mRemote.transact(Stub.TRANSACTION_setLoginParameters, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_setLoginParameters = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void setLoginParameters(java.lang.String mUsername, java.lang.String mPassword, java.lang.String mHost, java.lang.String mConnection) throws android.os.RemoteException;
}
