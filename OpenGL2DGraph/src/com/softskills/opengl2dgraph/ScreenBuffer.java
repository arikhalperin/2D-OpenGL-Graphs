package com.softskills.opengl2dgraph;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

/**
 * A screen buffer for drawing an Open GL graph. Inherited by the line & point screen buffer classes.
 * The screen buffer is a cyclic buffer, so adding a sample moves the write pointer while not invalidating the
 * rest of the samples. If adding a sample will cause the buffer to overflow the sample at the read position 
 * is thrown away to free space.
 */
abstract public class ScreenBuffer {

	private static final String TAG = "ScreenBuffer";
	int mBufferWritePointer;
	int mBufferReadPointer;
	int mSamplesInScreen;
	float mStartX;
	float mStartY;
	float mHeight;
	float mWidth;
	float mRComponent = 0;
	float mGComponent = 1;
	float mBComponent = 0;
	protected FloatBuffer mFVertexBuffer;
	long mLastTimeStamp;
				
	boolean mBufferFull = false;
	
	boolean mCyclic = true;
	
			
	void setCyclic(boolean cyclic)
	{
		mCyclic = cyclic;
	}
	

	abstract void fillVertexArrayX();
	abstract int getAllocation(int samplesInScreen);
	
	ScreenBuffer(int samplesInScreen, float startX, float startY, float width, float height) 
	{
		mSamplesInScreen = samplesInScreen;
		mStartX = startX;
		mStartY = startY;
		mHeight = height;
		mWidth  = width;
		
		
		ByteBuffer vbb = ByteBuffer.allocateDirect(getAllocation(mSamplesInScreen));
		vbb.order(ByteOrder.nativeOrder());
		mFVertexBuffer = vbb.asFloatBuffer();
		
		
		fillVertexArrayX();					
	}
	
		
	private void advanceReadPointer()
	{
		mBufferReadPointer++;
		if( mBufferReadPointer == getBufferSize())
		{
			mBufferReadPointer = 0;
		}
	}
	
	private int getBufferSize() {
		return (mSamplesInScreen);
	}

	private void advanceWritePointer()
	{
		mBufferWritePointer++;
		if( mBufferWritePointer == getBufferSize() )
		{
			mBufferWritePointer = 0;
		}
		
		//If no place left in buffer we just ran over a sample - in this case: Move the read pointer.
		if( mBufferWritePointer == mBufferReadPointer )
		{
			mBufferFull = true;
		}
	}
	
	synchronized boolean addSample(float sample)
	{		
		float putValue = sample;
		
		if(putValue>(mHeight/2))
		{
			putValue = mHeight/2;
		}
		
		if(putValue<(-mHeight/2))
		{
			putValue = -mHeight/2;
		}
		
		//Log.i(TAG,"height:"+mHeight+" sample:"+sample);
		
		if(mBufferFull)
		{
			if(mCyclic==true)
			{
				advanceReadPointer();
			}
			else 
			{
				return false;
			}
		}
		putSample(putValue);
								
		advanceWritePointer();
		
		return true;
	}
	
	abstract void putSample(float sample);
		
	int getNumberOfSamplesLeft(int readPointer)
	{
		return mSamplesInScreen - readPointer;
	}
	
	int getNumberOfSamplesRight(int readPointer)
	{
		return readPointer;
	}
	

	abstract void drawScreenBuffer(GL10 gl);

	
	int getScreenBufferSize()
	{
		return mSamplesInScreen;
	}

	public void copyScreenBuffer(ScreenBuffer screenBuffer) {
		for(int i=0;i<mSamplesInScreen;i++)
		{
			float sample = mFVertexBuffer.get(((mBufferReadPointer+i)%mSamplesInScreen)*2+1);
			screenBuffer.addSample(sample);
		}
		
	}

	public void reset() {
		mBufferReadPointer = 0;
		mBufferWritePointer = 0;
		mBufferFull = false;
		
		mLastTimeStamp = Long.MAX_VALUE;
		
	}

	public long getSamplesInScreen() {
		return mSamplesInScreen;
	}

	public int getNumberOfValidSamples() {
		// TODO Auto-generated method stub
		return 0;
	}

	void setRGB(float r, float g, float b)
	{
		mRComponent = r;
		mGComponent = g;
		mBComponent = b;
	}

	public void setColor(GL10 gl) {
		gl.glColor4f(mRComponent, mGComponent, mBComponent, 1);
	}
	
	public void setLastTimeStamp(long ts)
	{
		mLastTimeStamp = ts;
	}
	
	public long getLastTimeStamp() {
		// TODO Auto-generated method stub
		return mLastTimeStamp;
	}

}
