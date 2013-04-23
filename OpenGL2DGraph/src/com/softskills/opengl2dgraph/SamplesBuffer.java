package com.softskills.opengl2dgraph;

import java.util.Arrays;
import java.util.concurrent.Semaphore;

import android.util.Log;

/**
 * Data structure for storing samples data. 
 * Samples buffer is circular, the start time of the buffer is allways set to the read pointer time
 *
 */
public class SamplesBuffer {

	private static final String TAG = "SamplesBuffer";

	short [] mSamples;	

	int mBufferStartIndex=0;	
	int mBufferSize;

	long mStartTimeStamp = Long.MAX_VALUE;

	boolean mCyclic = true;

	long mLatestSample = 0;
	
	Semaphore mSemaphore;
	
	
	//For testing purposes - checks buffer behavior in several scenarios 
	static void simpleTest()
	{
		SamplesBuffer sb = new SamplesBuffer(10,true);
		
		sb.setTimestamp(0);
		
		for(int i=0;i<10;i++)
		{
			sb.addSample((short)i, (long)i);
		}
		
		sb.addSample((short)10, (long)10);
					
		sb.addSample((short)11, (long)11);
		
		sb.printArray();
		
		sb.addSample((short)18, (long)18);				
		
		sb.printArray();
		
		sb.addSample((short)28, (long)28);
		sb.addSample((short)29, (long)29);
		
		for(int i=30;i<60;i++)
		{
			sb.addSample((short)i, (long)i);
		}
		
		sb.printArray();
		
		short atIndex4 = sb.getSampleAtTime(52);
		
		System.out.println("Value at time 52:"+atIndex4);
		
		sb.addSample((short)49, 49);
		sb.addSample((short)48, 48);
		sb.addSample((short)500, 50);
		sb.printArray();
		
		sb.addSample((short)28, 28);
		
		sb.printArray();
		
		for(int i=30;i<40;i++)
		{
			sb.addSample((short)i, (long)i);
		}
		
		sb.printArray();
		
		sb.addSample((short)27, 27);
		
		sb.printArray();
	}

	//Debug function - used in testing
	void printArray()
	{
		System.out.println("Buffer Index Start:"+mBufferStartIndex+" Start TS:"+mStartTimeStamp);
		
		for(int i=0;i<mBufferSize;i++)
		{
			System.out.println("Sample("+i+"):"+mSamples[i]);
		}
	}
	
	public SamplesBuffer(int bufferSize, boolean cyclic) {

		Log.i("SamplesBuffer","Constructor call");
		mSamples = new short[bufferSize];	
		
		int count =0;	
		
		mBufferSize = bufferSize;
		mCyclic = cyclic;
		
		mSemaphore = new Semaphore(1, true);
		
		invalidateWholeBuffer();
	}

	void setTimestamp(long timeStamp)
	{
		mStartTimeStamp = timeStamp;
	}

	int getIndexOffsetFromTimestamp(long timeStamp)
	{
		return (int)((timeStamp-mStartTimeStamp+1)/2);
	}

	/**
	 * @param timeStamp
	 * @return
	 */
	short getSampleAtTime(long timeStamp)
	{		
		try {
			mSemaphore.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int index = getIndexOffsetFromTimestamp(timeStamp);
		
		short returnValue = Short.MAX_VALUE;

		//Just update sample - no need to move the buffer!
		if((timeStamp>=mStartTimeStamp) && (timeStamp<=(mStartTimeStamp+mBufferSize*2)))
		{
			//Position is relative, must add to it buffer start index
			int position = wrap(mBufferStartIndex+index);

			returnValue =  mSamples[position];
		}

		mSemaphore.release();
		
		return returnValue;
	}

	protected int wrap(int i) {
		if(i>=mBufferSize)
		{
			return i-mBufferSize;
		}
		else
		{
			if(i<0)
			{
				i = mBufferSize+i;
			}
		}
		
		return i;
	}
	
	public long getLatestTimeStamp()
	{
		return mLatestSample;
	}
	
	public int addSample(short sampleValue, long timeStamp, boolean isHeartBeat)
	{
		return addSample(sampleValue, timeStamp);
	}

	int lastIndex = 0;
	
	public int addSample(short sampleValue, long timeStamp)
	{			
		try {
			mSemaphore.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if(mStartTimeStamp==Long.MAX_VALUE)
		{
			Log.i("ReadEcgFromFile","Setting ts:"+timeStamp);
			mStartTimeStamp = timeStamp;
		}
		
		if(timeStamp>mLatestSample)
		{
			//Log.i(TAG,"Setting latest timestamp:"+timeStamp);
			mLatestSample = timeStamp;
		}
		
		int index = getIndexOffsetFromTimestamp(timeStamp);
		
		if(lastIndex!=index-1)
		{
		//	Log.i("C0", "JumpedIndex! index:"+index+ " Last index:"+lastIndex);
		}
		
		lastIndex = index;
		
		int position =0;

		//If index falls inside current buffer -  Simply write it in the appropriate position
		//Just update sample - no need to move the buffer!
		if((index<mBufferSize) && (index>0))
		{
			//Position is relative, must add to it buffer start index
			position = wrap(mBufferStartIndex+index);

			mSamples[position] = sampleValue;
		}		
		//If index is in the future
		else if(index>=mBufferSize)
		{
			int delta = index+1-mBufferSize;

			//Now invalidate everything from write position to start position(If needed)

			//If have to move less than buffer size forward...			
			if(delta<mBufferSize)
			{	
				//Save the old last position of the buffer
				int oldStart = mBufferStartIndex;

				//Move the start pointer so that it points to write position-buffer size
				mBufferStartIndex=wrap(mBufferStartIndex+delta);
				
				mStartTimeStamp += delta*2;

				//Find the new position after buffer is moved. Sample is now last in buffer
				position = wrap(mBufferStartIndex+(mBufferSize-1));								

				//Fill area between old position and new position with uninitialized values.
				while(oldStart!=mBufferStartIndex)
				{
					mSamples[oldStart]=Short.MAX_VALUE;

					oldStart=wrap(oldStart+1);
				}

			}
			//Moved too much - have to erase the whole buffer
			else
			{
				if(delta>=mBufferSize)
				{
					invalidateWholeBuffer();
					mBufferStartIndex = 0;
					position  = 0;
				//	Log.i("ReadEcgFromFile","Setting ts:"+timeStamp);
					mStartTimeStamp = timeStamp;
				}
			}

		}
		else if(index<0) //If index is in the past
		{

			//Buffer samples still relevant
			if(index>=(-mBufferSize))
			{

				int oldStartPosition = mBufferStartIndex;

				//Move the start position to the write position.
				mBufferStartIndex = wrap(mBufferStartIndex+index);
				

				//Now invalidate everything from new start position to old start position
				while(mBufferStartIndex!=oldStartPosition)
				{
					mSamples[wrap(oldStartPosition+mBufferSize-1)]=Short.MAX_VALUE;
					oldStartPosition = wrap(oldStartPosition-1);
				}

				position = mBufferStartIndex;
				
				mStartTimeStamp += index*2;
				
				//Log.i("ReadEcgFromFile","Setting ts:"+mStartTimeStamp);
			}
			//Moved too much - erase all buffer
			else
			{
				invalidateWholeBuffer();
				mBufferStartIndex = 0;
				position  = 0;
				
				
				mStartTimeStamp = timeStamp;
				//Log.i("ReadEcgFromFile","Setting ts:"+mStartTimeStamp);
			}
		}
		mSamples[position] = sampleValue;
		
		mSemaphore.release();
		
		
		return position;
	}


	private void invalidateWholeBuffer() {
		//Log.i("OFFL","Invalidate buffer");
		Arrays.fill(mSamples, Short.MAX_VALUE);		
	}

	public long getStartTs() {
		return mStartTimeStamp;
	}


	public long lastSampleTime() {
		
		if(mLatestSample>(mStartTimeStamp+mBufferSize*2))
		{
			mLatestSample = mStartTimeStamp+mBufferSize*2;
		}
		
		return mLatestSample;
	}

	public void reset() {
	//	Log.i("OFFL","Reset");
		try {
			mSemaphore.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		mBufferStartIndex = 0;
		mStartTimeStamp = Long.MAX_VALUE;
		invalidateWholeBuffer();
		
		mSemaphore.release();
	}

	public int getBufferSize() {
		return mBufferSize;
	}
	
	long getTimeAtIndex(int index)
	{
		int delta = index - mBufferStartIndex;
		
		if(delta<0)
		{
			delta = mBufferSize+delta;
		}
		
		return mStartTimeStamp + (long)delta*2; 
		
	}

	public long getStartTsWithOffset() {		
		return 0;
	}
	
}
