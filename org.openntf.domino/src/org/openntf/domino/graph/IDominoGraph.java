package org.openntf.domino.graph;

import java.util.Map;
import java.util.Set;

import org.openntf.domino.Document;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.MetaGraph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;

@SuppressWarnings("rawtypes")
public interface IDominoGraph extends Graph, MetaGraph, TransactionalGraph {
	public static enum VertexStrategy {
		SINGLE, SHARDED
	}

	public static enum EdgeStrategy {
		CROSSMATCHED, INDEXED, MEMORY
	}

	public VertexStrategy getVertexStrategy();

	public void setVertexStrategy(VertexStrategy strategy);

	public EdgeStrategy getEdgeStrategy();

	public void setEdgeStrategy(EdgeStrategy strategy);

	public Map<Class<?>, String> getShardingMap();

	public void setShardingMap(Map<Class<?>, String> shardingMap);

	public void addSharding(Class<?> cls, String replicaId);

	public IDominoShard getShard(Class<?> cls);

	public IDominoShard getShard(String replicaId);

	public IDominoShard getEdgeShard();

	@Override
	public IDominoEdge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label);

	@Override
	public IDominoVertex addVertex(Object id);

	public IDominoVertex addVertex(Class<?> cls, Object id);

	@Override
	public IDominoEdge getEdge(Object id);

	public IDominoEdge getEdge(final Vertex outVertex, final Vertex inVertex, final String label);

	public Set<Edge> getEdgesFromIds(final Set<String> set);

	@Override
	public org.openntf.domino.Database getRawGraph();

	public org.openntf.domino.Database getRawDatabase(Class<?> cls);

	@Override
	public IDominoVertex getVertex(Object id);

	public IDominoVertex getVertex(Class<?> cls, Object id);

	public IEdgeHelper getHelper(final String key);

	public IEdgeHelper getHelper(final IDominoEdgeType edgeType);

	public Set<IEdgeHelper> findHelpers(final Vertex in, final Vertex out);

	public IEdgeHelper findHelper(final Vertex in, final Vertex out);

	public void addHelper(final String key, final IEdgeHelper helper);

	public void removeHelper(final String key);

	public void startTransaction(final Element elem);

	Document getDocument(final Object id, final boolean createOnFail);

	public boolean isValidId(String id);

	public boolean isCompressed();

}