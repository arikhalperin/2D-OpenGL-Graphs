package com.softskills.opengl2dgraph;

import android.util.Log;

/**
 * Object to connect between a samples buffer and a screen buffer
 *
 */
public class GraphDataSource {

	private static final long SCREEN_BUFFER_TIME_RESOLUTION = 2;//500 samples per second

	private static final float NORMALIZATION_FACTOR = 90.0f;

	float mScreenPart = 1.0f;
	
	float mMaxSample = Float.MIN_VALUE;
	float mMinSample = Float.MAX_VALUE;
	
	/**
	 * Load samples to screen buffer from a samples buffer in offline mode.
	 * @param fromTimeStamp
	 * @param upToTimeStamp
	 * @param screenBuffer
	 */
	void loadSamplesToScreenBuffer(long fromTimeStamp, long upToTimeStamp, ScreenBuffer screenBuffer)
	{		
		screenBuffer.reset();

		updateScreenBuffer(upToTimeStamp, screenBuffer);		
	}

	/**
	 * Update a screen buffer with data
	 * @param upToTimeStamp
	 * @param screenBuffer
	 * @param online
	 * @return
	 */
	boolean updateScreenBuffer(long upToTimeStamp, ScreenBuffer screenBuffer)
	{		
		int count = 0;		
		
		//Safety measure
		if(screenBuffer == null)
		{
			return false;
		}
		
		
		//Get start time of buffer
		long startTime = screenBuffer.getLastTimeStamp();

		if(startTime==Long.MAX_VALUE)
		{
			startTime=0;
		}
	
		
		float lastNormalizedSample=0;
		float normalizedSample;
		
		//Add sample by sample based on time
		while(startTime<=upToTimeStamp)
		{								
			short sample = mSamplesBuffer.getSampleAtTime(startTime);			
					
			if(sample!=Short.MAX_VALUE)
			{											
				normalizedSample = ((float)sample)/NORMALIZATION_FACTOR;
				
				//Remember last sample - buffer may not contain an entry in the next read position
				lastNormalizedSample = normalizedSample;
			}
			else
			{				
				normalizedSample = lastNormalizedSample;
			}

			//Add the sample to screen buffer
			screenBuffer.addSample(normalizedSample);
			
			//Move to next sample
			startTime += SCREEN_BUFFER_TIME_RESOLUTION;
			
			screenBuffer.setLastTimeStamp(startTime);
		}		
		
								
		return true;
	}


	SamplesBuffer mSamplesBuffer;

	GraphDataSource(SamplesBuffer samplesBuffer)
	{
		mSamplesBuffer = samplesBuffer;
	}
	
	public long getEndTs()
	{		
		return mSamplesBuffer.lastSampleTime();
	}

	public long getStartTs() {
		return mSamplesBuffer.getStartTs();				
	}

}