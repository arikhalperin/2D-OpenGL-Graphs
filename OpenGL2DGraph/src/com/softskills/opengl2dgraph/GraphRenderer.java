package com.softskills.opengl2dgraph;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;

public class GraphRenderer extends AbstractRenderer {

	@Override
	public void onSurfaceChanged(GL10 gl, int w, int h) {
		super.onSurfaceChanged(gl, w, h);
		
		recalculateScreen();
	}

	private void recalculateScreen() {
		mGraph.recalculate(mRatio);
	}

	Graph mGraph;
	Context mContext;
	
	public GraphRenderer(Context context, SamplesBuffer[] samplesBuffer) {
		mContext = context;
		mGraph = new Graph(context,samplesBuffer);
	}

	@Override
	protected void draw(GL10 gl) {
		mGraph.draw(gl);		
	}

	void updateData(long ts)
	{
		mGraph.update(ts);				
	}
}
