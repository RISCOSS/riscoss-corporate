package eu.riscoss.client;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.Tree.Resources;

public interface DefTreeImages extends Resources {
	
	@Source("treeitem-closed.png")
	public ImageResource treeClosed();
	
	@Source("treeitem-leaf.png")
	public ImageResource treeLeaf();
	
	@Source("treeitem-open.png")
	public ImageResource treeOpen();

}
