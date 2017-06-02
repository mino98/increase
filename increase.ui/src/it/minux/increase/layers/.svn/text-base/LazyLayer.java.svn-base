package it.minux.increase.layers;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;

public abstract class LazyLayer
	extends RenderableLayer
{
	private boolean loaded = false;
	private final BlockingQueue<Renderable> renderableQueue 
		= new ArrayBlockingQueue<Renderable>(10);
	
	
	protected void doPreRender(DrawContext dc)
    {
		if (!loaded) {
			loaded = loadData();
		}

		Renderable renderable = null;
		while ((renderable = renderableQueue.poll()) != null) {
			addRenderable(renderable);
		}
		
		super.doPreRender(dc);
    }

	private boolean loadData() {
		if (WorldWind.getTaskService().isFull()) {
			return false;
		}
		
		WorldWind.getTaskService().addTask(new LoadDataTask());
		return true;
	}
	
	private class LoadDataTask implements Runnable {

		@Override
		public void run() {
			List<Renderable> renderables = doLoadData();
			if (renderables != null) {
				renderableQueue.addAll(renderables);
			}
		}		
	}
	
	protected abstract List<Renderable> doLoadData();
}
