package com.softskills.opengl2dgraph;

import javax.microedition.khronos.opengles.GL10;

public class PointScreenBuffer extends ScreenBuffer {

	PointScreenBuffer(int samplesInScreen, float startX, float startY,
			float width, float height) {
		super(samplesInScreen, startX, startY, width, height);
		// TODO Auto-generated constructor stub
	}

	@Override
	void putSample(float sample)
	{
		mFVertexBuffer.put(mBufferWritePointer*2+1,sample);
	}
	
	@Override
	int getAllocation(int samplesInScreen)
	{
		return mSamplesInScreen*2 * 2 * 4;
	}
	
	@Override
	void fillVertexArrayX()
	{
		//Fill the data buffer with 0 value samples.
		for(int i=0;i<mSamplesInScreen;i++)
		{
			float value = (float)(mWidth*(float)i)/(float)mSamplesInScreen;
			mFVertexBuffer.put(value);
			
			mFVertexBuffer.put(0);			
		}
	}
	
	
	@Override
	void drawScreenBuffer(GL10 gl) {
		{		
			int readPointer = mBufferReadPointer;

			float xOffset = mStartX-mFVertexBuffer.get(readPointer*2);						


			gl.glPointSize(3f);

			//Draw Right Side
			gl.glTranslatef(xOffset, mStartY-mHeight/2.f, 0);

			mFVertexBuffer.position(0);				
			
			mFVertexBuffer.mark();

			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, mFVertexBuffer);				

			gl.glDrawArrays(GL10.GL_POINTS, readPointer, getNumberOfSamplesLeft(readPointer));

			gl.glTranslatef(-xOffset, -mStartY+mHeight/2.0f, 0);
			
			mFVertexBuffer.reset();

			//If there is a left side - draw left side
			if(getNumberOfSamplesRight(readPointer)!=0)
			{
				xOffset = mStartX+mWidth-mFVertexBuffer.get(readPointer*2);

				gl.glTranslatef(xOffset, mStartY-mHeight/2.f, 0);

				gl.glDrawArrays(GL10.GL_POINTS, 0, getNumberOfSamplesRight(readPointer));

				gl.glTranslatef(-xOffset, -mStartY+mHeight/2.f, 0);

			}

		}
	}

}
