package de.codesourcery.edit2d;

import java.util.ArrayList;
import java.util.List;

public abstract class RegularGraphNode extends AbstractGraphNode {

	protected final List<IGraphNode> children = new ArrayList<>();
	protected final NodeData metaData = new NodeData();

	@Override
	public final NodeData getMetaData() {
		return metaData;
	}

	@Override
	public final List<IGraphNode> getChildren() {
		return children;
	}
}