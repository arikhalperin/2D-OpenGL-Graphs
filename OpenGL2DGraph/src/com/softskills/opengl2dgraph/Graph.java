package com.softskills.opengl2dgraph;

import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;

public class Graph {

	private static final int SAMPLES_IN_SCREEN = 1000;
	private static final int MAX_GRAPHS = 5;
	SamplesBuffer mSamplesBuffer[];
	Grid mGrid;
	Context mContext;
	GraphDataSource mDataSource[];
	ScreenBuffer mScreenBuffer[];

	class GraphColor
	{
		int R;
		int G;
		int B;
		
		GraphColor(int inR,int inG,int inB)
		{
			R = inR;
			G = inG;
			B = inB;
		}
	}

	GraphColor graphColor[]=new GraphColor[MAX_GRAPHS];
	
	void setColors()
	{
		graphColor[0]=new GraphColor(255, 0, 0);
		graphColor[1]=new GraphColor(0, 255, 0);
		graphColor[2]=new GraphColor(0, 0, 255);
		graphColor[3]=new GraphColor(255, 255, 0);
		graphColor[4]=new GraphColor(0, 255, 255);
	}
	

	public Graph(Context context, SamplesBuffer[] samplesBuffer) {
	
		//Set the colors array
		setColors();
		
		//Save the sample buffers
		mSamplesBuffer = samplesBuffer;

		mContext = context;

		//Create the grid
		mGrid = new Grid();

		//Create the data sources
		mDataSource = new GraphDataSource[samplesBuffer.length];
		
		for(int i=0;i<mDataSource.length;i++)
		{
			mDataSource[i] = new GraphDataSource(samplesBuffer[i]);
		}
	}

	public void update(long ts) {
		for(int i=0;i<5;i++)
		{
			mDataSource[i].updateScreenBuffer(ts, mScreenBuffer[i]);
		}
	}

	public void draw(GL10 gl) {
		mGrid.draw(gl);	

		gl.glEnableClientState (GL10.GL_VERTEX_ARRAY);
		gl.glLineWidth(2f);

		for(int i=0;i<mScreenBuffer.length;i++)
		{
			mScreenBuffer[i].setColor(gl);
			mScreenBuffer[i].drawScreenBuffer(gl);
		}

		gl.glDisableClientState (GL10.GL_VERTEX_ARRAY);
	}


	float mGraphWidth;
	float mGraphHeight;

	public void recalculate(float ratio) {
		mGraphWidth = 2.f*ratio;
		mGraphHeight = 2.0f;		

		mGrid.setBounds(-ratio, 1f,mGraphWidth, mGraphHeight,20,20);

		mScreenBuffer = new ScreenBuffer[mDataSource.length];
		
		for(int i=0;i<mScreenBuffer.length;i++)
		{
			mScreenBuffer[i] =  new PointScreenBuffer(SAMPLES_IN_SCREEN,-ratio, 1f, mGraphWidth, mGraphHeight);

			mScreenBuffer[i].setRGB(graphColor[i].R,graphColor[i].G,graphColor[i].B);
		}

	}						


}


