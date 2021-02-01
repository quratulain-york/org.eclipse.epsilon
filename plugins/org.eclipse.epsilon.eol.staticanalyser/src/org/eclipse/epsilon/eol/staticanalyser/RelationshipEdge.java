package org.eclipse.epsilon.eol.staticanalyser;

import org.jgrapht.graph.DefaultEdge;

class RelationshipEdge extends DefaultEdge {
private String label;

/**
 * Constructs a relationship edge
 *
 * @param label the label of the new edge.
 * 
 */
public RelationshipEdge(String label)
{
    this.label = label;
}

/**
 * Gets the label associated with this edge.
 *
 * @return edge label
 */
public String getLabel()
{
    return label;
}

@Override
public String toString()
{
	return label;
   // return "(" + getSource() + " : " + getTarget() + " : " + label + ")";
}
}