/*************************************************************************************************************************
 *************************************************************************************************************************
 *
 * These classes draw graphs from the given set of edges separted by comma(,)
 *
 * The source code is hired from "http://www.cs.rpi.edu/research/groups/pb/graphdraw/"
 *
 *************************************************************************************************************************
 *************************************************************************************************************************/


package packageGraphDrawing;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.net.*;

public class myGraph extends Applet {

protected Blackboard _bb;
protected GraphWindow _window;
protected GraphPanel _panel;

public GraphPanel panel() {
	return _panel;
}
	
public void init(String edges) {
	_bb = new Blackboard( this );

	//add(new Button("Show"));

	//String edges = getParameter("edges");
	
	for(StringTokenizer t = new StringTokenizer(edges,","); t.hasMoreTokens();) {
		String str = t.nextToken();
		int i = str.indexOf('-');
		if (i > 0) {
			int len = 50;
			int j = str.indexOf('/');
			if (j > 0) {
				len = Integer.valueOf(str.substring(j+1)).intValue();
				str = str.substring(0, j);
			}
			_bb.addEdge(str.substring(0,i), str.substring(i+1));
		}
	}
	_bb.PreprocessNodes();

	//String basedir = getParameter("files");
	//_bb.globals.setBasedir( basedir );
	//_bb.globals.depth3D( 0.0 );
	
	// NB: The order of the following matters.
	ControlWindow controls = new ControlWindow( _bb );
	_bb.Init();
	_panel = new GraphPanel( _bb );
	_window = new GraphWindow( _bb, _panel, controls );
	
	_window.pack();
	
	_window.show();
	_panel.start();
}
/*public void destroy() {
	_window.handleEvent(new Event(_window,Event.WINDOW_DESTROY,null));
	_panel.stop();
}*/

/*public boolean action(Event evt, Object arg) {
	if (evt.target instanceof Button && "Show".equals(arg)) {
		_window.show();
		_panel.start();
		((Button)evt.target).setLabel( "Hide" );
	}
	else if (evt.target instanceof Button && "Hide".equals(arg)) {
		_window.handleEvent(new Event(_window,Event.WINDOW_DESTROY,null));
		_panel.stop();
		((Button)evt.target).setLabel( "Show" );
	}
	else
		return false;
	return true;
}*/

	public static void main(String[] args)
	{
	
		myGraph applet = new myGraph();
		applet.init();
	}

} // class myGraph


//**********************************************************************************************************

class Blackboard {

protected Vector _edges;
protected Vector _nodes;
protected Stack _history;
protected myGraph _applet;
public Globals globals;
public Matrix3D projection;
public Blackboard( myGraph applet ) {
  _edges = new Vector();
  _nodes = new Vector();
  _history = new Stack();
  _applet = (myGraph) applet;
  globals = new Globals(this);
  projection = new Matrix3D();
  _embedders = new Hashtable();
}
public final myGraph applet() { 
  return _applet;
}

  // Node and Edge stuff
  //
public final Vector edges() {
  return _edges;
}
public final Vector nodes() {
  return _nodes;
}
public Node addNode(String lbl) {
  Node n = new Node( this, lbl );
  _nodes.addElement(n);
  return n;
}
public Node findNode(String lbl) {
  int nodecnt = _nodes.size();
  for (int i = 0; i < nodecnt; ++i ) {
    Node n = (Node) _nodes.elementAt(i);
    if (n.label().equals(lbl))
      return n;
  }
  return addNode(lbl);
}
public Edge addEdge(String f, String t) {
  Node from = findNode(f);
  Node to = findNode(t);
  Edge e = new Edge( this, from, to );
  _edges.addElement( e );
  from.add_out( e );
  to.add_in( e );
  return e;
}
public double Norm(Node u, Node v) {
  double dx = v.x() - u.x();
  double dy = v.y() - u.y();
  double dz = v.z() - u.z();
  return Math.sqrt( dx*dx + dy*dy + dz*dz );
}


  // Assign layers, break cycles.
  //
protected final void unmarkNodes() {
  int nodecnt = _nodes.size();
  for (int i = 0; i < nodecnt; ++i ) {
    Node n = (Node) _nodes.elementAt(i);
    n.unmark();
  }
}
protected final void break_cycles( Node curr ) {
  curr.mark();
  curr.pick( true );
  Vector outedges = curr.outedges();
  int outedgecnt = outedges.size();
  for (int i = 0; i < outedgecnt; ++i ) {
    Edge e = (Edge) outedges.elementAt(i);
    Node n = e.to();
    if( n.picked() ) 
      e.reverseDir();
    else if( !n.marked() )
      break_cycles( n );
  }
  curr.pick( false );
}
protected final void fixReversedEdges() {
  int edgecnt = _edges.size();
  for (int i = 0; i < edgecnt; ++i ) {
    Edge e = (Edge) _edges.elementAt(i);
    if( e.reversed() ) {
      Node n = e.to();
      n.remove_in( e );
      n.add_out( e );
      n = e.from();
      n.remove_out( e );
      n.add_in( e );
      e.swapDir();
    }
  }
}
public final void topoSort( Node curr, Vector topoSortedNodes ) {
  curr.mark();
  Vector inedges = curr.inedges();
  int inedgecnt = inedges.size();
  for (int i = 0; i < inedgecnt; ++i ) {
    Node n = ((Edge) inedges.elementAt(i)).from();
    if( n.marked() == false )
      topoSort( n, topoSortedNodes );
  }
  topoSortedNodes.addElement( curr );
}
  // Create and remove a meta-root and meta-edges to all nodes.
  //
protected Node _meta_root;
protected Vector _meta_edges;
protected Node makeMetaRoot() {
  _meta_root = new Node( this, "meta-root" );
  _meta_edges = new Vector();
  int nodecnt = _nodes.size();
  for (int i = 0; i < nodecnt; ++i ) {
    Node n = (Node) _nodes.elementAt(i);
    Edge e = new Edge( this, n, _meta_root );
    _meta_edges.addElement( e );
    _edges.addElement( e );
    n.add_out( e );
    _meta_root.add_in( e );
  }
  _nodes.addElement( _meta_root );
  return _meta_root;
}
protected void removeMetaRoot() {
  int junkcnt = _meta_edges.size();
  for (int i = 0; i < junkcnt; ++i ) {
    Edge e = (Edge) _meta_edges.elementAt(i);
    _edges.removeElement( e );
    e.from().remove_out( e );
    e.to().remove_in( e );
  }
  _nodes.removeElement( _meta_root );
}
public void PreprocessNodes() {
  // Break any cycles in the graph by reversing some edges
  unmarkNodes();
  int nodecnt = _nodes.size();
  for (int i = 0; i < nodecnt; ++i ) {
    Node n = (Node) _nodes.elementAt(i);
    if( !n.marked() ) break_cycles( n );      
  }
  fixReversedEdges();

  // Do a topological sort on the meta-graph, then assign levels to nodes.
  Vector topoSortedNodes = new Vector();
  Node meta_root = makeMetaRoot();
  unmarkNodes();
  topoSort( meta_root, topoSortedNodes );
  removeMetaRoot();

  int toposize = topoSortedNodes.size();
  int maxlevel = 0;
  for (int i = 0; i < toposize; ++i ) {
    Node v = (Node) topoSortedNodes.elementAt(i);
    int level = 0;
    Vector inedges = v.inedges();
    int inedgecnt = inedges.size();
    for (int j = 0; j < inedgecnt; ++j ) {
      Node u = ((Edge) inedges.elementAt(j)).from();
      if( u.level() > level )
        level = u.level();
    }
    v.level( level+1 );
    if( level+1 > maxlevel )
      maxlevel = level+1;
  }
  // Hoist the nodes up to the maximum possible usage level.
  for (int i = toposize-1; i >= 0; --i ) {
    Node v = (Node) topoSortedNodes.elementAt(i);
    int min_useage_level = maxlevel;
    Vector outedges = v.outedges();
    int outedgecnt = outedges.size();
    if( outedgecnt == 0 )
      min_useage_level = v.level();
    for (int j = 0; j < outedgecnt; ++j ) {
      Node u = ((Edge) outedges.elementAt(j)).to();
      int useage_level = u.useage_level()-1;
      if( useage_level < min_useage_level )
        min_useage_level = useage_level;
    }
    v.useage_level( min_useage_level );
  }
  for (int i = 0; i < toposize; ++i ) {
    Node v = (Node) topoSortedNodes.elementAt(i);
    v.level( v.useage_level() );
  }
}

// Add and remove dummy nodes between nodes between levels that are far apart
protected boolean _hasDummies = false;
public boolean hasDummies() { 
  return _hasDummies; 
}
public synchronized void addDummies() {
  if( _hasDummies ) return;
  _hasDummies = true;
  int nodecnt = _nodes.size();
  for (int i = 0; i < nodecnt; ++i ) {
    Node to = (Node) _nodes.elementAt(i);
    if( to.dummy() ) continue;
    Vector inedges = to.inedges();
    int inedgecnt = inedges.size();
    for (int j = 0; j < inedgecnt; ++j ) {
      Edge e = (Edge) inedges.elementAt(j);
      if( e.from().dummy() ) continue;
      boolean showit = (e.from().showing() && to.showing());
      while( to.level() > e.from().level()+1 ) {
        Node from = e.from();
        Node dummy = new Node( this );          // Make new node
        if( showit ) dummy.showing( true );
        dummy.level( from.level()+1 );          // Set the level of the new node
        _nodes.addElement( dummy );
        Edge inedge = new Edge( this, from, dummy );  // First new edge 
        if( e.reversed() ) inedge.reverseDir();
        from.replace_out( e, inedge );
        dummy.add_in( inedge );
        Edge outedge = new Edge( this, dummy, to );   // Second new edge
        if( e.reversed() ) outedge.reverseDir();
        to.replace_in( e, outedge );
        dummy.add_out( outedge );
        _edges.addElement( inedge );            // Substitute new edges for old
        _edges.addElement( outedge );
        _edges.removeElement( e );
        e = outedge;
      }
    }
  }
}
protected final void removeDummiesDFS( Node curr, Node meta_root ) {
  curr.mark();
  Vector inedges = curr.inedges();
  int inedgecnt = inedges.size();
  for (int i = 0; i < inedgecnt; ++i ) {
    Edge e = (Edge) inedges.elementAt(i);
    Node n = e.from();
    if( n.dummy() ) {
      boolean reverse = e.reversed();
      _edges.removeElement( e );
      if( e.to() == meta_root ) continue;
      while( n.dummy() ) {
        _nodes.removeElement( n );
        Edge other = (Edge) n.inedges().firstElement();
        Node next = other.from();
        _edges.removeElement( other );
        if( !next.dummy() ) 
          next.remove_out( other );
        n = next;
      }
      Edge shortcut = new Edge( this, n, curr );
      if( reverse ) shortcut.reverseDir();
      n.add_out( shortcut );
      curr.replace_in( e, shortcut );
      _edges.addElement( shortcut );
    }
    if( !n.marked() )
      removeDummiesDFS( n, meta_root );
  }
  Update();
}
public synchronized void removeDummies() {
  if( !_hasDummies ) return;
  _hasDummies = false;
  unmarkNodes();
  Node meta_root = makeMetaRoot();
  removeDummiesDFS( meta_root, meta_root );
  removeMetaRoot();
}

  // Localizaton and grouping stuff
  //
public Vector get_fixed() {
  Vector fixed = new Vector();
  int nodecnt = _nodes.size();
  for (int i = 0; i < nodecnt; ++i ) {
    Node n = (Node) _nodes.elementAt(i);
    if( n.fixed() ) fixed.addElement( n );
  }
  return fixed;
}

protected Node _center;
protected void push_history( Node center, Vector ins, Vector outs ) {
  // Save the old "stack"
  Vector showing = new Vector();
  Object v[] = new Object[6];
  v[0] = _nodes.clone();
  v[1] = _edges.clone();
  v[2] = showing;
  v[3] = _center;
  v[4] = ins;
  v[5] = outs;
  _history.push( v );
  int nodecnt = _nodes.size();
  for (int i = 0; i < nodecnt; ++i ) {
    Node u = (Node) _nodes.elementAt(i);
    if( u.showing() ) showing.addElement( u );
    u.center( false );
  }
  _center = center;
}
protected void mark_local( Node center ) {
  // Mark all nodes within the right depth as showing
  center.mark();
  int nodecnt = _nodes.size();
  int edgecnt = _edges.size();
  for( int depth = 0; depth < globals.localizationDepth(); ++depth ) {
    for( int i = 0; i < edgecnt; ++i ) {
      Edge e = (Edge) _edges.elementAt(i);
      Node from = e.from();
      Node to = e.to();
      if( from.marked() && !to.marked() )  // Use center as flag for next round
        to.center( true );
      if( !from.marked() && to.marked() )
        from.center( true);
    }
    for( int i = 0; i < nodecnt; ++i ) {
      Node n = (Node) _nodes.elementAt(i);
      if( n.center() ) {
        n.center( false );
        n.mark();
      }
    }    
  }
  // Finish marking all the dummy-studded edges which are partially marked 
  for (int i = 0; i < edgecnt; ++i ) {
    Edge e = (Edge) _edges.elementAt(i);
    Node from = e.from();
    Node to = e.to();
    if( from.dummy() && from.marked() && !to.marked() ) {
      while( to.dummy() ) {
        to.mark();
        to = ((Edge) to.outedges().firstElement()).to();
      } 
      to.mark();
    }
    else if( to.dummy() && to.marked() && !from.marked() ) {
      while( from.dummy() ) {
        from.mark();
        from = ((Edge) from.inedges().firstElement()).from();
      }
      from.mark();
    }
  }
}
public void only_show_marked() {
  int nodecnt = _nodes.size();
  for( int i = 0; i < nodecnt; ++i ) {
    Node u = (Node) _nodes.elementAt(i);
    if( u.marked() ) u.showing( true );
    else             u.showing( false );
  }
}
public synchronized void localize( Node center ) {
  push_history( center, null, null );
  unmarkNodes();
  mark_local( center );
  only_show_marked();
  center.center( true );
}
public synchronized void group( Node center ) {
  Node grouped = new Node( this, "*" + center.label() + "*" );
  grouped.showing( true );
  grouped.x( center.x() );
  grouped.y( center.y() );
  grouped.z( center.z() );
  Vector ins = new Vector();
  Vector outs = new Vector();

  push_history( grouped, ins, outs );
  unmarkNodes();
  Vector fixed = get_fixed();
  if( fixed.size() == 1 ) {
    mark_local( center );
  }
  else {
    int nodecnt = _nodes.size();
    for (int i = 0; i < nodecnt; ++i ) {
      Node n = (Node) _nodes.elementAt( i );
      if( n.fixed() ) {
        n.mark();
        n.fix(false);
      }
    }
  }
  grouped.mark();

  // Make _nodes contain only unmarked nodes, complement the set of marked.
  Vector group_complement = new Vector();
  int nodecnt = _nodes.size();
  for (int i = 0; i < nodecnt; ++i ) {
    Node n = (Node) _nodes.elementAt( i );
    if( n.marked() )
      n.unmark();
    else {
      group_complement.addElement( n );
      n.mark();
    }
  }
  _nodes = group_complement;
  _nodes.addElement( grouped );
  grouped.mark();

  // Add all the relevant edges to the set of edges, modify if neccessary.
  Vector new_edges = new Vector();
  Vector seen_in_nodes = new Vector();
  Vector seen_out_nodes = new Vector();
  int edgecnt = _edges.size();
  for (int i = 0; i < edgecnt; ++i ) {
    Edge e = (Edge) _edges.elementAt(i);
    Node from = e.from();
    Node to = e.to();
    if( from.marked() && to.marked() )
      new_edges.addElement( e );
    else if( from.marked() && !to.marked() ) {
      ins.addElement( e );
      if( seen_in_nodes.contains( from ) ) {
        from.remove_out( e );
      } else {
        seen_in_nodes.addElement( from );
        Edge newin = new Edge( this, from, grouped );
        grouped.add_in( newin );
        from.replace_out( e, newin );
        new_edges.addElement( newin );
      }
    }
    else if( to.marked() && !from.marked() ) {
      outs.addElement( e );
      if( seen_out_nodes.contains( to ) ) {
        to.remove_in( e );
      } else {
        seen_out_nodes.addElement( to );
        Edge newout = new Edge( this, grouped, to );
        grouped.add_out( newout );
        to.replace_in( e, newout );
        new_edges.addElement( newout );
      }
    }
  }
  _edges = new_edges;
  grouped.center( true );
}
public synchronized void backtrack() {
  if( _history.empty() )
    return;

  Object v[] = (Object[]) _history.pop();
  _nodes = (Vector) v[0];
  _edges = (Vector) v[1];
  Vector showing = (Vector) v[2];
  Node new_center = (Node) v[3];
  Vector ins = (Vector) v[4];
  Vector outs = (Vector) v[5];

  int nodecnt = _nodes.size();            // Initialize nodes
  for (int i = 0; i < nodecnt; ++i ) {
    Node u = (Node) _nodes.elementAt(i);
    u.showing( false );
    u.center( false );
  }
  int showcnt = showing.size();           // Show old showing nodes
  for (int i = 0; i < showcnt; ++i )
    ((Node) showing.elementAt(i)).showing( true );
  if( new_center != null )                // Mark the old center as center
    new_center.center( true );
  // Deal with removing the grouping, if any
  if( ins != null ) {             
    Vector ingrp = _center.inedges();      // Remove the edges to group center
    int ingrpcnt = ingrp.size();     
    for( int i = 0; i < ingrpcnt; ++i ) {
      Edge e = (Edge) ingrp.elementAt(i);
      e.from().remove_out( e );
    }
    Vector outgrp = _center.outedges();
    int outgrpcnt = outgrp.size();
    for( int i = 0; i < outgrpcnt; ++i ) {
      Edge e = (Edge) outgrp.elementAt(i);
      e.to().remove_in( e );
    }
    int inscnt = ins.size();               // Add the original edges back in
    for( int i = 0; i < inscnt; ++i ) {
      Edge e = (Edge) ins.elementAt(i);
      e.from().add_out( e );
    }
    int outscnt = outs.size();
    for( int i = 0; i < outscnt; ++i ) {
      Edge e = (Edge) outs.elementAt(i);
      e.to().add_in( e );
    }
  }
  _center = new_center;
}

  // Embedder stuff
  //
protected Embedder _embedder;
protected Hashtable _embedders;
public Embedder embedder() {
  return _embedder;
}
public synchronized void embedder( Embedder e ) {
  _embedder = e;
}
public void addEmbedder( String name, Embedder embedder ) {
  _embedders.put( name, embedder );
}
private boolean _embedderChanged = true;
public void setEmbedding( String name ) {
  _embedder = (Embedder) _embedders.get(name);
  _embedderChanged = true;
}
public boolean catchEmbedderChange() {
  if( _embedderChanged ) {
    _embedderChanged = false;
    return true;
  }
  return false;
}
public void Init() {
  _embedder.Init();
  double k = globals.k();
}
public synchronized void Update() {
  int edgecnt = _edges.size();
  for (int i = 0; i < edgecnt; ++i ) {
    Edge e = (Edge) _edges.elementAt(i);
    e.updateLength();
  }
}


  // Size stuff
  //
protected double _lx;
protected double _ly;
protected double _ux;
protected double _uy;
public final double lx() { return _lx; }
public final double ly() { return _ly; }
public final double ux() { return _ux; }
public final double uy() { return _uy; }
public void setArea(double lx, double ly, double ux, double uy ) {
  _lx = lx;
  _ly = ly;
  _ux = ux;
  _uy = uy;
}


  // Some debug stuff
  //
public synchronized void printDebug() {
  int realminx = 0;
  int realmaxx = 0;
  int realminy = 0;
  int realmaxy = 0;
  int nodecnt = _nodes.size();
  for (int i = 0; i < nodecnt; ++i ) {
    Node n = (Node) _nodes.elementAt(i);
    if( n.x() > realmaxx )      realmaxx = (int)(n.x());
    else if( n.x() < realminx ) realminx = (int)(n.x());
    if( n.y() > realmaxy )      realmaxy = (int)(n.y());
    else if( n.y() < realminy ) realminy = (int)(n.y());
    System.err.println( n.toString() );
  }
  int edgecnt = _edges.size();
  for (int i = 0; i < edgecnt; ++i ) {
    Edge e = (Edge) _edges.elementAt(i);
    System.err.println( e.toString() );
  }
  System.err.println("lx:\t" + _lx);
  System.err.println("ly:\t" + _ly);
  System.err.println("ux:\t" + _ux);
  System.err.println("uy:\t" + _uy);
  System.err.println("D:\t" + globals.D());
  System.err.println("L:\t" + globals.L());
  System.err.println("realminx:\t" + realminx);
  System.err.println("realmaxx:\t" + realmaxx);
  System.err.println("realminy:\t" + realminy);
  System.err.println("realmaxy:\t" + realmaxy);
  if( _applet.panel() != null ) {
    System.err.println("panelminx:\t" + _applet.panel().minx());
    System.err.println("panelmaxx:\t" + _applet.panel().maxx());
    System.err.println("panelminy:\t" + _applet.panel().miny());
    System.err.println("panelmaxy:\t" + _applet.panel().maxy());
    System.err.println("panelwidth:\t" + _applet.panel().size().width);
    System.err.println("panelheight:\t" + _applet.panel().size().height);
  }
  System.err.println("depth3D:\t" + globals.depth3D() );
}


} // class Blackboard

//*****************************************************************************************************

class GraphWindow extends ScrollingFrame {

protected ControlWindow _controls;
protected Blackboard _bb;

public GraphWindow(Blackboard black, GraphPanel panel, ControlWindow c) {
	super( "Graph Embedding", panel );
	_bb = black;
	panel.setScrollbars( _sbv, _sbh ); // Hook the scrollbars up with the panel
	_controls = c;
	_controls.pack();
}

public boolean action(Event evt, Object arg) {
	if (evt.target == _button ) {
		if( !_controls.showing() ) {
			_controls.start();
			_controls.show();
			_controls.showing( true );
		}
		else {
			_controls.showing( false );
			_controls.stop();
			_controls.handleEvent(new Event(_controls,Event.WINDOW_DESTROY,null));
		}
		return true;
	}
	return super.action( evt, arg );
}

public boolean handleEvent(Event evt) {
	if( evt.id == Event.WINDOW_ICONIFY || evt.id == Event.WINDOW_DESTROY ) {
    if( _controls.showing() ) {
			_controls.showing( false );
			_controls.stop();
			_controls.handleEvent(new Event(_controls,Event.WINDOW_DESTROY,null));
		}
	}
	return super.handleEvent(evt);
}

} // class GraphWindow

//******************************************************************************************************

class ScrollingFrame extends Frame {

protected Scrollbar _sbv; 
protected Scrollbar _sbh; 
protected Button _button; 

public ScrollingFrame(String title, Panel panel) {
	super(title);
	

	this.setLocation(400,250);
	
	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	setLayout(gridbag);
		
	c.fill = GridBagConstraints.BOTH;	// Add the canvas
	c.anchor = GridBagConstraints.CENTER;
	c.weightx = 1.0;
	c.weighty = 1.0;
	c.gridwidth = GridBagConstraints.RELATIVE; 
	c.gridheight = GridBagConstraints.RELATIVE;
	gridbag.setConstraints(panel, c);
	add(panel);

	c.fill = GridBagConstraints.VERTICAL;	// Add vertical scrollbar
	c.weightx = 0.0;
	c.weighty = 0.0;
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.gridheight = GridBagConstraints.RELATIVE;
	_sbv = new Scrollbar( Scrollbar.VERTICAL, 1, 1, 1, 1 );
	_sbv.setLineIncrement( 1 );
	_sbv.setPageIncrement( panel.size().height );
	gridbag.setConstraints(_sbv, c);
	add(_sbv);

	c.fill = GridBagConstraints.HORIZONTAL;	// Add horizontal scrollbar
	c.weightx = 0.0;
	c.weighty = 0.0;
	c.gridwidth = GridBagConstraints.RELATIVE;
	c.gridheight = GridBagConstraints.REMAINDER;
	_sbh = new Scrollbar( Scrollbar.HORIZONTAL, 1, 1, 1, 1 );
	_sbh.setLineIncrement( 1 );
	_sbh.setPageIncrement( panel.size().width );
	gridbag.setConstraints(_sbh, c);
	add(_sbh);

	c.fill = GridBagConstraints.NONE;	// Add spurious button
	c.weightx = 0.0;
	c.weighty = 0.0;
	c.gridwidth = GridBagConstraints.REMAINDER;
	c.gridheight = GridBagConstraints.REMAINDER;
	_button = new Button();
	_button.setBackground( Color.blue );
	gridbag.setConstraints(_button, c);
	add(_button);
	
	/*this.addWindowListener(new WindowAdapter(){
      public void windowClosing(WindowEvent we){
        System.exit(0);
      }
    });*/
}

public boolean handleEvent(Event evt) {
	if (evt.id == Event.WINDOW_ICONIFY) {
		hide();
		return true;
	}
	if (evt.id == Event.WINDOW_DESTROY) {
		dispose();
		return true;
	}
	return super.handleEvent(evt);
}

} // class GraphWindow

//******************************************************************************************************
class GraphPanel extends ScrollingPanel implements Runnable {

protected Blackboard _bb;
public GraphPanel(Blackboard black) {
	super( (int)black.lx(), (int)black.ly(), (int)black.ux(), (int)black.uy() );
	_bb = black;

}

	// Thread stuff
	//
protected boolean _painted = true;    
protected synchronized void setPainted() {
	_painted = true;
	notifyAll();
}
protected synchronized void waitPainted() {
	while (!_painted)
		try {
			wait();
		} catch (InterruptedException e) {
		}
	_painted = false;
}

protected Thread _updater;
public void run() {
	Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
	while (true) {
		waitPainted();
		if( _bb.catchEmbedderChange() )
			super.focusScrollbars();
		_bb.embedder().Embed();
		repaint();
		setPainted();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			break;
		}
	}
}
public void start() {
	_updater = new Thread(this);
	_updater.start();
}
public void stop() {
	if( _updater != null )
		_updater.stop();
	_updater = null;
}
	

	// the drawing routine
	//
protected Node _pick;                  // Picked node
protected boolean _pickfixed = false;  // Is the picked node fixed?
protected boolean _extending = false;  // Extending edges of the picked node?
protected Image _offscreen;
protected Dimension _offscreensize;
protected Graphics _offgraphics;

protected final void sortByZ( Vector nodes ) {
  // Do insertionsort on the Z depth of the nodes
	int len = nodes.size();
  for( int P = 1; P < len; ++P ) {
    Node tmp = (Node) nodes.elementAt( P );
		double Z = tmp.Z();
    int j;
    for( j = P; j > 0; --j ) {
      Node tmp2 = (Node) nodes.elementAt( j-1 );
      if( Z >= tmp2.Z() ) break;
      nodes.setElementAt( tmp2, j );
		}
		nodes.setElementAt( tmp, j );
	}
}

public void update(Graphics g) {
	Dimension d = size();
	adjustScrollbars( d.width, d.height );
	if ((_offscreen == null) || (d.width != _offscreensize.width) 
			|| (d.height != _offscreensize.height)) {
		_offscreen = createImage(d.width, d.height);
		_offscreensize = d;
		_offgraphics = _offscreen.getGraphics();
		_offgraphics.setFont(getFont());
	}

	_offgraphics.setColor(getBackground());
  _offgraphics.clipRect(0, 0, d.width, d.height);
	_offgraphics.fillRect(0, 0, d.width, d.height);
	_offgraphics.translate( -_sbh.getValue(), -_sbv.getValue() );
	Vector edges = _bb.edges();
	int edgecnt = edges.size();
	for( int i = 0; i < edgecnt; ++i ) {
		Edge e = (Edge) edges.elementAt(i);
    Node to = e.to();
    Node from = e.from();
		if( to != from && to.showing() && from.showing() )
      e.paint( _offgraphics );
	}
	Vector nodes = _bb.nodes();
	sortByZ( nodes );
	int nodecnt = nodes.size();
  for( int i = 0; i < nodecnt; ++i ) {
		Node n = (Node) nodes.elementAt(i);
    if( n.showing() )
       n.paint( _offgraphics );
	}
	g.drawImage( _offscreen, 0, 0, null );
	_offgraphics.translate( _sbh.getValue(), _sbv.getValue() );
}

	// The event handler
	//
public synchronized boolean mouseDown(Event evt, int x, int y) {
	x = x + _sbh.getValue();
	y = y + _sbv.getValue();

	int bestdist = Integer.MAX_VALUE;
	Vector nodes = _bb.nodes();
	int nodecnt = nodes.size();
  for( int i = 0; i < nodecnt; ++i ) {
		Node n = (Node) nodes.elementAt(i);
		int dx = n.X()-x;
		int dy = n.Y()-y;
		int dist = dx*dx + dy*dy;
		if (dist < bestdist) {
			_pick = n;
			bestdist = dist;
		}
	}
	_pick.pick( true );
	_pickfixed = _pick.fixed();
	_pick.XY( x, y );

	if( evt.clickCount > 1 ) {
    boolean shift_on = ((evt.modifiers & Event.SHIFT_MASK) != 0);
    if (shift_on) {
      _pickfixed = !_pickfixed;
      _bb.group( _pick );
    }
    else
      _bb.localize( _pick );
	}
  else {
    boolean shift_on = ((evt.modifiers & Event.SHIFT_MASK) != 0);
    if (shift_on) { // Toggle the fix of the picked node
      _pickfixed = !_pickfixed;
    }
    boolean ctrl_on = ((evt.modifiers & Event.CTRL_MASK) != 0); 
    if (ctrl_on) { // Now extending the picked node
      _extending = true;
    }
    boolean alt_on = ((evt.modifiers & Event.ALT_MASK) != 0);
    if( alt_on ) { // Now trying to open the URL of the picked node
      try {
        URL doc = _bb.applet().getDocumentBase();
        AppletContext page = _bb.applet().getAppletContext();
        page.showDocument( new URL(doc, _bb.globals.basedir() + _pick.label()) );
      } catch( MalformedURLException mal ) {
        _bb.applet().showStatus("Couldn't display " + _pick.label());
      }
    }
  }
	waitPainted();
	repaint();
	setPainted();
	return true;
}

public synchronized boolean mouseDrag(Event evt, int x, int y) {
	x = x + _sbh.getValue();
	y = y + _sbv.getValue();
		
	_pick.XY( x, y );
	waitPainted();
	repaint();
	setPainted();
	return true;
}

public synchronized boolean mouseUp(Event evt, int x, int y) {
	x = x + _sbh.getValue();
	y = y + _sbv.getValue();

	_pick.XY( x, y );
	_pick.fix( _pickfixed );
	_pick.pick( false );

	if( _extending ) {
		for (Enumeration i = _pick.edges() ; i.hasMoreElements() ;) {
			Edge e = (Edge) i.nextElement();
			e.updateLength();
		}
	}
	_extending = false;
	_pick = null;

	waitPainted();
	repaint();
	setPainted();
	return true;
}

private final int margin = 200;
protected void addBoundingBoxPoint( int x, int y) {
	if( x > _maxx )      _maxx = x;
	else if( x < _minx ) _minx = x;
	if( y > _maxy )      _maxy = y;
	else if( y < _miny ) _miny = y;
}
protected void adjustScrollbars(int width, int height) {
	int oldminx = _minx;
	int oldmaxx = _maxx;
	int oldminy = _miny;
	int oldmaxy = _maxy;
	_minx = _maxx = _miny = _maxy = 0;
	Vector nodes = _bb.nodes();
	int nodecnt = nodes.size();
  for( int i = 0; i < nodecnt; ++i ) {
		Node n = (Node) nodes.elementAt(i);
		addBoundingBoxPoint( n.X(), n.Y() );
	}
  _iminx = _minx;
  _iminy = _miny;
	int newwidth = _maxx-_minx;
	int newheight = _maxy-_miny;
  _minx -= newwidth;	
  _maxx += newwidth;	
  _miny -= newheight;	
  _maxy += newheight;	
	if( _minx-margin > oldminx )  _minx = oldminx+margin;
	else                          _minx = (_minx<oldminx)?_minx:oldminx; // Min
	if( _maxx+margin < oldmaxx )  _maxx = oldmaxx-margin;
	else                          _maxx = (_maxx>oldmaxx)?_maxx:oldmaxx; // Max
	if( _miny-margin > oldminy )  _miny = oldminy+margin;
	else                          _minx = (_miny<oldminy)?_miny:oldminy; // Min
	if( _maxy+margin < oldmaxy )  _maxy = oldmaxy-margin;
	else                          _maxx = (_maxy>oldmaxy)?_maxy:oldmaxy; // Max
	super.adjustScrollbars( width, height );
}

} // class GraphPanel


//**************************************************************************************************************

class ScrollingPanel extends Panel {

protected Scrollbar _sbv; 
protected Scrollbar _sbh; 
protected int _maxx, _maxy, _minx, _miny;      // Corner points
protected int _iminx, _iminy;                  // Upper left corner points
protected int _iwidth, _iheight;               // Initial width and height
public int minx() { return _minx; }
public int maxx() { return _maxx; }
public int miny() { return _miny; }
public int maxy() { return _maxy; }

public ScrollingPanel( int minx, int miny, int maxx, int maxy ) {
	_minx = _iminx = minx;
	_maxx = maxx;
	_miny = _iminy = miny;
	_maxy = maxy;
  _iwidth = _maxx-_minx;
  _iheight = _maxy-_miny;
}

protected void setScrollbars( Scrollbar v, Scrollbar h ) {
	_sbv = v;
	_sbh = h;
}
protected void adjustScrollbars(int width, int height) {
	_sbh.setValues( _sbh.getValue(), width, _minx, _maxx-_minx-width );
	_sbh.setPageIncrement( width );
	_sbv.setValues( _sbv.getValue(), height, _miny, _maxy-_miny-height );
	_sbv.setPageIncrement( height );
}
protected void focusScrollbars() {
  _sbh.setValue( _iminx );
  _sbv.setValue( _iminy );
}
public void reshape(int x, int y, int width, int height) {
	adjustScrollbars( width, height );
	super.reshape(x,y,width,height);
}
protected int defaultedge = 400;
public Dimension minimumSize() {
	return new Dimension( Math.min( defaultedge, _iwidth ), 
												Math.min( defaultedge, _iheight ) );
}
public Dimension preferredSize() {
	return new Dimension( Math.min( defaultedge, _iwidth ), 
												Math.min( defaultedge, _iheight ) );
}

} // class ScrollingPanel


//***************************************************************************************************************

class Globals {

protected Blackboard _bb;
public Globals( Blackboard black ) {
	_bb = black;
}

	// Localization depth constant
	//
protected int _localizationDepth;
public final int localizationDepth() {
	return _localizationDepth;
}
public final void localizationDepth( int d ) {
	_localizationDepth = d;
}

  // Cycle length constant
  //
protected int _cycleLength;
public final int cycleLength() {
	return _cycleLength;
}
public final void cycleLength( int l ) {
	_cycleLength = l;
}

	// Sundry placement constants
	//
protected double _depth3D;
public final double depth3D() {
	return _depth3D;
}
public final void depth3D( double d ) {
	_depth3D = d;
}

protected double _D;
public final double D() {
	return _D;
}
public final void D( double d ) {
	_D = d;
}
public final double L() {
  return _D*_bb.nodes().size();
}
public final double area() {
	return L()*L();
}
public final double k() {
	return Math.sqrt( area()/_bb.nodes().size() );
}

protected double _minTemp;
public final double minTemp() {
	return _minTemp;
}
public final void minTemp( double d ) {
	_minTemp = d;
}
protected double _temp;
public final double Temp() {
	return _temp;
}
public final void Temp( double d ) {
	_temp = d;
}

protected double _ac;
public final double ac() {
	return _ac;
}
public final void ac( double d ) {
	_ac = d;
}
protected double _ae;
public final double ae() {
	return _ae;
}
public final void ae( double d ) {
	_ae = d;
}
protected double _rc;
public final double rc() {
	return _rc;
}
public final void rc( double d ) {
	_rc = d;
}
protected double _re;
public final double re() {
	return _re;
}
public final void re( double d ) {
	_re = d;
}


	// Applet environment stuff
	//
protected String _basedir;
public void setBasedir( String d ) {
	_basedir = d;
}
public String basedir() {
	return _basedir;
}


} // class Globals


//***************************************************************************************************

class Matrix3D {
protected double xx, xy, xz;
protected double yx, yy, yz;
protected double zx, zy, zz;
protected static final double pi = 3.14159265;
	// Create a new unit matrix
public Matrix3D () {
	xx = 1.0;
	yy = 1.0;
	zz = 1.0;
}
	// rotate theta degrees about the y axis 
public final void rotateY(double theta) {
	theta *= (pi / 180);
	double ct = Math.cos(theta);
	double st = Math.sin(theta);

	double Nxx = (double) (xx * ct + zx * st);
	double Nxy = (double) (xy * ct + zy * st);
	double Nxz = (double) (xz * ct + zz * st);

	double Nzx = (double) (zx * ct - xx * st);
	double Nzy = (double) (zy * ct - xy * st);
	double Nzz = (double) (zz * ct - xz * st);

	xx = Nxx;
	xy = Nxy;
	xz = Nxz;
	zx = Nzx;
	zy = Nzy;
	zz = Nzz;
}
	// rotate theta degrees about the x axis
public final void rotateX(double theta) {
	theta *= (pi / 180);
	double ct = Math.cos(theta);
	double st = Math.sin(theta);

	double Nyx = (double) (yx * ct + zx * st);
	double Nyy = (double) (yy * ct + zy * st);
	double Nyz = (double) (yz * ct + zz * st);

	double Nzx = (double) (zx * ct - yx * st);
	double Nzy = (double) (zy * ct - yy * st);
	double Nzz = (double) (zz * ct - yz * st);

	yx = Nyx;
	yy = Nyy;
	yz = Nyz;
	zx = Nzx;
	zy = Nzy;
	zz = Nzz;
}
	// rotate theta degrees about the z axis
public final void rotateZ(double theta) {
	theta *= (pi / 180);
	double ct = Math.cos(theta);
	double st = Math.sin(theta);

	double Nyx = (double) (yx * ct + xx * st);
	double Nyy = (double) (yy * ct + xy * st);
	double Nyz = (double) (yz * ct + xz * st);

	double Nxx = (double) (xx * ct - yx * st);
	double Nxy = (double) (xy * ct - yy * st);
	double Nxz = (double) (xz * ct - yz * st);

	yx = Nyx;
	yy = Nyy;
	yz = Nyz;
	xx = Nxx;
	xy = Nxy;
	xz = Nxz;
}
public final int projectX( double x, double y, double z ) {
	return (int) (x * xx + y * xy + z * xz);
}
public final int projectY( double x, double y, double z ) {
	return (int) (x * yx + y * yy + z * yz);
}
public final int projectZ( double x, double y, double z ) {
	return (int) (x * zx + y * zy + z * zz);
}
protected double _den; 
public final double inverseX( int X, int Y ) {
	double f1 = yy*zz - yz*zy;
	double f2 = xz*zy - xy*zz;
	_den = xx*f1 + yx*f2 + zx*(xy*yz - xz*yy);
	return (X*f1 +  Y*f2) / _den;
}
public final double inverseY( int X, int Y ) {
	return (X*(yz*zx - yx*zz) +  Y*(xx*zz - xz*zx)) / _den;
}
public final double inverseZ( int X, int Y ) {
	return -(X*(yy*zx - yx*zy) +  Y*(xx*zy - xy*zx)) / _den;
}
}


//***************************************************************************************************************

class PointDelta {
public double dx, dy, dz;
public PointDelta( double ddx, double ddy, double ddz ) {
	dx = ddx;
	dy = ddy;
	dz = ddz;
}
} // class PointDelta


class Node {

protected Vector _in;
protected Vector _out;

protected String _label;
public final String label() { return _label; }
public final String toString() {
 String r =  "LABEL: " + _label
	      + ", lev "  + _level
	      + ", deg "  + degree()
	      + ", bar "  + barycenter()
	      + ", ("  + _x + "," + _y + "," + _z + ")";
 r = r + "\n\tIN:  "  + _in.toString()
       + "\n\tOUT: "  + _out.toString();
 return r;
}

	// Basic stuff
	//
protected double _x, _y, _z;
public final double x() { return _x; }
public final double y() { return _y; }
public final double z() { return _z; }
public final void x(double d) { _x = d; }
public final void y(double d) { _y = d; }
public final void z(double d) { _z = d; }

protected boolean _fixed = false;
protected boolean _picked = false;
public final boolean fixed() { return _fixed; }
public final boolean picked() { return _picked; }
public final void fix(boolean b) { _fixed = b; }
public final void pick(boolean b) { _picked = b; }

protected boolean _dummy = false;
public final boolean dummy() { return _dummy; }

protected Blackboard _bb;
public Node( Blackboard b, String label, boolean fix, boolean pick ) {
  _bb = b;
	_in = new Vector();
	_out = new Vector();
	_label = label;
	_fixed = fix;
	_picked = pick;
	_x = _y = _z = _dx = _dy = _dz = 0.0; 
}
public Node( Blackboard b, String label ) {
	this( b, label, false, false );
}
public Node( Blackboard b ) {
	this( b, "DUMMY", false, false );
	_dummy = true;
}


	// DFS stuff
	//
protected boolean _marked = false;
protected final void mark() {
	_marked = true;
}
protected final void unmark() {
	_marked = false;
}
protected final boolean marked() {
	return _marked;
}

	// Level stuff
	//
protected int _level;
public final int level() {
	return _level;
}
public final void level( int l ) {
	_level = l;
}
protected int _useage_level;
public final int useage_level() {
	return _useage_level;
}
public final void useage_level( int l ) {
	_useage_level = l;
}
protected double _barycenter;
public final double barycenter() {
	return _barycenter;
}
public final void barycenter( double b ) {
	_barycenter = b;
}
public final double computeBarycenter(boolean doin, boolean doout) {
  double insum = 0.0;
	int insize = _in.size();
	if( doin ) {
    for( int i = 0; i < insize; ++i )
      insum += ((Edge)_in.elementAt( i )).from().x();
		if( insize == 0 ) {
			insize = 1;
			insum = x();
		}
	}
  double outsum = 0.0;
	int outsize = _out.size();
	if( doout ) {
    for( int i = 0; i < outsize; ++i )
      outsum += ((Edge)_out.elementAt( i )).to().x();
		if( outsize == 0 ) {
			outsize = 1;
			outsum = x();
		}
	}
	if( doin && doout )
		return (insum+outsum)/(insize+outsize);
	else if( doin )
		return insum/insize;
	else if( doout )
		return outsum/outsize;
	Node n = null;    // Throw an exception if neither doin or dout are there.
	return n.level();
}


	// Localization stuff
	//
protected boolean _center;
public final boolean center() { return _center; }
public final void center(boolean b) { _center = b; }
protected boolean _showing = true;
public final boolean showing() { return _showing; }
public final void showing(boolean b) { _showing = b; }

	// Initial placement stuff
	//
public final void randomPlacement( double xf, double yf, double zf ) {
	XY( xf*(Math.random() - 0.5), yf*(Math.random() - 0.5) );
	_z = zf*(Math.random() - 0.5);
	stabilize();
}

	// Force-directed placement stuff
	//
protected double _dx, _dy, _dz;
public final void stabilize() {
	_dx = _dy = _dz = 0.0;
}
public final void forced( Node other, double factor ) {
	_dx += (_x - other._x) * factor;
	_dy += (_y - other._y) * factor;
	_dz += (_z - other._z) * factor;
}
public final void scaleDelta( double factor ) {
	_dx = _dx * factor;
	_dy = _dy * factor;
	_dz = _dz * factor;
}
public final double deltaForce() {
	return Math.sqrt( _dx*_dx + _dy*_dy + _dz*_dz );
}
public final void moveDelta( double factor ) {
	_x += _dx * factor;
	_y += _dy * factor;
	_z += _dz * factor;
}
private final double __min( double a, double b ) { return (a<b)?a:b; }
private final double __max( double a, double b ) { return (a>b)?a:b; }
public final void boundedMoveDelta( double bound ) {
	_x += __max( -bound, __min(bound, _dx) );
	_y += __max( -bound, __min(bound, _dy) );
	_z += __max( -bound, __min(bound, _dz) );
}
public final PointDelta getDelta() {
	return new PointDelta( _dx, _dy, _dz );
}
public final void addDelta( PointDelta d ) {
	_dx += d.dx;
	_dy += d.dy;
	_dz += d.dz;
}

	// Edge stuff
	//
public final void add_in( Edge e ) {
	_in.addElement(e);
}
public final void add_out( Edge e ) {
	_out.addElement(e);
}
public final void remove_in( Edge e ) {
	_in.removeElement(e);
}
public final void remove_out( Edge e ) {
	_out.removeElement(e);
}
public final void replace_in( Edge e, Edge newe ) {
	int pos = _in.indexOf(e);
	_in.setElementAt( newe, pos );
}
public final void replace_out( Edge e, Edge newe ) {
	int pos = _out.indexOf(e);
	_out.setElementAt( newe, pos );
}
public final int degree() { 
	return _in.size()+_out.size(); 
}
public final Enumeration edges() {
	return new NodeEdgeEnumerator(_in,_out);
}
public final Vector inedges() {
  return _in;
}
public final Vector outedges() {
  return _out;
}

	// Projection stuff
	//
public final int X() {
	return _bb.projection.projectX( _x, _y, _z );
}
public final int Y() {
	return _bb.projection.projectY( _x, _y, _z );
}
public final int Z() {
	return _bb.projection.projectZ( _x, _y, _z );
}
public final void XY( int newx, int newy ) {
	_x = _bb.projection.inverseX( newx, newy );
	_y = _bb.projection.inverseY( newx, newy );
}
public final void XY( double newx, double newy ) {
	XY( (int) newx, (int) newy );
}
	// Paint stuff
	//
protected final Color fixedColor = Color.red;
protected final Color nodeColor = Color.white;
protected final Color selectColor = Color.pink;
private final int xmarginSize = 10;
private final int ymarginSize = 4;
private final int dummySize = 8;
protected int _boundingWidth;
public final int boundingWidth() {
	return _boundingWidth;
}
protected int _boundingHeight;
public final int boundingHeight() {
	return _boundingHeight;
}
public void paint(Graphics g) {
	int x = _bb.projection.projectX( _x, _y, _z );
	int y = _bb.projection.projectY( _x, _y, _z );

	if( dummy() ) {
		//g.setColor(Color.blue);
		//g.fillOval( x-dummySize/2, y-dummySize/2, dummySize, dummySize );
		_boundingHeight = dummySize;
		_boundingWidth = dummySize;
	}
	else {
		g.setColor( ((_picked) ? selectColor : (_fixed ? fixedColor : nodeColor)) );
		int depdelta = (int)( 10*_bb.projection.projectZ( _x, _y, _z )
													/ _bb.globals.L() );
		FontMetrics fm = g.getFontMetrics();
		int w = _boundingWidth = fm.stringWidth(_label) + xmarginSize + depdelta;
		int h = _boundingHeight = fm.getHeight() + ymarginSize + depdelta;
		g.fillRect( x-w/2, y-h/2, w, h );
		g.setColor(Color.black);
		g.drawRect( x-w/2, y-h/2, w-1, h-1 );
		if( _center ) {
			g.drawRect( x-w/2-1, y-h/2-1, w+1, h+1 );
			g.drawRect( x-w/2-2, y-h/2-2, w+3, h+3 );
			g.drawRect( x-w/2-3, y-h/2-3, w+5, h+5 );
		}
		g.drawString( _label,
								  x-(w-xmarginSize)/2,
								  y-(h-ymarginSize)/2 + fm.getAscent());
	}
}

} // class Node


final
class NodeEdgeEnumerator implements Enumeration {
private Vector _in;
private Vector _out;
private int _incount;
private int _outcount;

public NodeEdgeEnumerator(Vector i, Vector o) {
	_in = i;
	_out = o;
	_incount = 0;
	_outcount = 0;
}

public final boolean hasMoreElements() {
	synchronized (_in) {
		synchronized (_out) {
			if( _incount == _in.size() ) 
				return _outcount < _out.size();
			return _incount < _in.size();
		}
	}
}

public final Object nextElement() {
	synchronized (_in) {
		synchronized (_out) {
			if (_incount < _in.size())
				return _in.elementAt(_incount++);
			if (_outcount < _out.size())
				return _out.elementAt(_outcount++);
		}
	}
	throw new NoSuchElementException("NodeEdgeEnumerator");
}

} // class NodeEdgeEnumerator


//**************************************************************************************************************

class Edge {

protected Node _from;
protected Node _to;
public final Node from() { return _from; }
public final Node to() { return _to; }

public final String toString() {
	return "{" + _from.label() + ";" + _to.label() + "-" + _length + "}";
}

protected Blackboard _bb;
public Edge( Blackboard b, Node f, Node t ) {
  _bb = b;
	_from = f;
	_to = t;
}

protected double _length;
public final double currLength() {
	return _bb.Norm( _from, _to );
}
public final void updateLength() {
	_length = currLength();
}
public final double length() {
	return _length;
}

protected final Color edgeColor = Color.black;
protected final int arrowSize = 4;
protected int _dir = 1;
public final boolean reversed() {
	return _dir == -1;
}
public final void reverseDir() {
	if( _dir == 1 ) 
		_dir = -1;
}
public final void swapDir() {
	Node n = _from;
	_from = _to;
	_to = n;
}
protected Polygon getArrow() {
	int dX = _to.X()-_from.X();
	int dY = _to.Y()-_from.Y();
	double len = Math.sqrt( dX*dX + dY*dY );
	double ndx = _dir*arrowSize*dX/len;
	double ndy = _dir*arrowSize*dY/len;
	double cx = (_to.X()+_from.X())/2;
	double cy = (_to.Y()+_from.Y())/2;
	Polygon tmp = new Polygon();
	tmp.addPoint( (int)(cx - ndy), (int)(cy + ndx) );
	tmp.addPoint( (int)(cx + ndx), (int)(cy + ndy) );
	tmp.addPoint( (int)(cx + ndy), (int)(cy - ndx) );
	tmp.addPoint( (int)(cx - ndy), (int)(cy + ndx) );
	return tmp;
}
public void paint(Graphics g) {
	g.setColor( edgeColor );
	g.drawLine( (int)_from.X(), (int)_from.Y(), (int)_to.X(), (int)_to.Y() );
	g.fillPolygon( getArrow() );
}

} // class Edge


//*************************************************************************************************************

class Slider extends Canvas {
private final static int THUMB_SIZE = 14;
private final static int BUFFER = 2;

private final static int TEXT_HEIGHT = 18;
private final static int TEXT_BUFFER = 3;
    
private final static int DEFAULT_WIDTH = 100;
private final static int DEFAULT_HEIGHT = 15;

private final static int MIN_WIDTH = 2 * (THUMB_SIZE + BUFFER + 1);
private final static int MIN_HEIGHT = 2 * (BUFFER + 1);

private final static int DEFAULT_MIN = 1;
private final static int DEFAULT_MAX = 100;    
    
	int min_, max_, value_, pixel_;
	int pixelMin_, pixelMax_;
	Color backgroundColor_, thumbColor_, barColor_, slashColor_, textColor_;
	Font font_;
    
	/**
	 * Constructs a slider.
	 * @param container The container for this slider.
	 */
public Slider () {
	min_ = DEFAULT_MIN;
	max_ = DEFAULT_MAX;
	resize(DEFAULT_WIDTH, DEFAULT_HEIGHT + TEXT_HEIGHT);
	font_ = new Font("TimesRoman", Font.PLAIN, 12);
	backgroundColor_ = Color.lightGray;
	thumbColor_ = Color.lightGray;
	barColor_ = Color.lightGray.darker();
	slashColor_ = Color.black;
	textColor_ = Color.black;
	SetValue(1);
}

	/**
	 * This method is called when the "thumb" of the slider is dragged by
	 * the user. Must be overridden to give the slider some behavior.
	 *   */
public void Motion () { ; }

	/**
	 * This method is called when the "thumb" of the slider is released
	 * after being dragged. Must be overridden to give the slider some
	 * behavior.
	 *   */
public void Release () { ; }
    
	/**
	 * Sets the maximum value for the slider.
	 * @param num The new maximum.
	 */
public void SetMaximum (int num) {
	max_ = num;
	if (max_ < min_) {
		int t = min_;
		min_ = max_;
		max_ = t;
	}
	SetValue(value_);
}
    
	/**
	 * Sets the minimum value for the slider.
	 * @param num The new minimum.
	 */
public void SetMinimum (int num) {
	min_ = num;
	if (max_ < min_) {
		int t = min_;
		min_ = max_;
		max_ = t;
	}
	SetValue(value_);
}
    
	/**
	 * Sets the current value for the slider. The thumb will move to
	 * reflect the new setting.
	 * @param num The new setting for the slider.
	 *   */
public void SetValue (int num) {
	value_ = num;
	
	if (value_ < min_)
		value_ = min_;
	else if (value_ > max_)
		value_ = max_;
	
	if (value_ != min_)
		pixel_ = (int)(Math.round(Math.abs((double)(value_ - min_) /
																			 (double)(max_ - min_)) *
															(double)(pixelMax_ - pixelMin_)) +
									 pixelMin_);
	else
		pixel_ = pixelMin_;

	repaint();
}
    
	/**
	 * Sets the height of the slider. This is the height of the entire
	 * slider canvas, including space reserved for displaying the
	 * current value.
	 * @param num The new height.
	 *   */
public void SetHeight (int num) {
	if (num < MIN_HEIGHT + TEXT_HEIGHT)
		num = MIN_HEIGHT + TEXT_HEIGHT;
	resize(size().width, num);
	repaint();
}
    
	/**
	 * Sets the width of the slider. This is the width of the actual
	 * slider box.
	 * @param num The new width.
	 *   */
public void SetWidth (int num) {
	if (num < MIN_WIDTH)
		num = MIN_WIDTH;
	resize(num, size().height);
	repaint();	
}
    
	/**
	 * Returns the current value for the slider.
	 * @return The current value for the slider.
	 */
public int GetValue () {
	return value_;
}

	/**
	 * Sets the background color for the slider. The "background" is the
	 * area outside of the bar.
	 * @param color The new background color.
	 */
public void SetBackgroundColor(Color color) {
	backgroundColor_ = color;
	repaint();
}

	/**
	 * Sets the color for the slider's thumb. The "thumb" is the box that
	 * the user can slide back and forth.
	 * @param color The new thumb color.
	 */
public void SetThumbColor(Color color) {
	thumbColor_ = color;
	repaint();
}

	/**
	 * Sets the color for the slider's bar. The "bar" is the rectangle
	 * that the thumb slides around in.
	 * @param color The new bar color.
	 */
public void SetBarColor (Color color) {
	barColor_ = color;
	repaint();
}

	/**
	 * Sets the slash color for the slider. The "slash" is the little
	 * vertical line on the thumb.
	 * @param color The new slash color.
	 */
public void SetSlashColor(Color color) {
	slashColor_ = color;
	repaint();
}

	/**
	 * Sets the color for the slider`s text.
	 * @param color The new text color.
	 */
public void SetTextColor(Color color) {
	textColor_ = color;
	repaint();
}

	/**
	 * Sets the font for the slider`s text.
	 * @param font The new font.
	 */
public void SetFont(Font font) {
	font_ = font;
	repaint();
}
    
	/**
	 * An internal method used to handle repaint events.
	 */
public void paint(Graphics g) {
	int width = size().width;	
	int height = size().height;

	g.setColor(backgroundColor_);
	g.fillRect(0, 0, width, TEXT_HEIGHT);

	g.setColor(barColor_);
	g.fill3DRect(0, TEXT_HEIGHT,
							 width, height - TEXT_HEIGHT, false);

	g.setColor(thumbColor_);	
	g.fill3DRect(pixel_ - THUMB_SIZE, TEXT_HEIGHT + BUFFER,
							 THUMB_SIZE * 2 + 1, height - 2 * BUFFER - TEXT_HEIGHT,
							 true);
	
	g.setColor(slashColor_);
	g.drawLine(pixel_, TEXT_HEIGHT + BUFFER + 1,
						 pixel_, height - 2 * BUFFER);

	g.setColor(textColor_);
	g.setFont(font_);		
	String str = String.valueOf(value_);
	g.drawString(str, pixel_ -
							 (int)(getFontMetrics(font_).stringWidth(str) / 2),
							 TEXT_HEIGHT - TEXT_BUFFER);
}

	void HandleMouse(int x) {
		double percent;
		int width = size().width;
		pixel_ = Math.max(x, pixelMin_);
		pixel_ = Math.min(pixel_, pixelMax_);

		if (pixel_ != pixelMin_)
	    percent = (((double)pixel_ - pixelMin_) /
								 (pixelMax_ - pixelMin_));
		else
	    percent = 0;
	
		value_ = (int)(Math.round(percent * (double)(max_ - min_))) + min_;
	
		paint(getGraphics());
	}
    
	/**
	 * An internal method used to handle mouse down events.
	 */
public boolean mouseDown (Event e, int x, int y) {
	HandleMouse(x);
	Motion();
	return true;
}

	/**
	 * An internal method used to handle mouse drag events.
	 */
public boolean mouseDrag (Event e, int x, int y) {
	HandleMouse(x);
	Motion();	
	return true;
}

	/**
	 * An internal method used to handle mouse up events.
	 */
public boolean mouseUp (Event e, int x, int y) {
	HandleMouse(x);
	Release();
	return true;
}

	/**
	 * An internal method used to handle resizing.
	 */
public void reshape(int x, int y, int width, int height) {
	super.reshape(x, y, width, height);
	pixelMin_ = THUMB_SIZE + BUFFER;
	pixelMax_ = width - THUMB_SIZE - BUFFER - 1;
	if (value_ != min_)
		pixel_ = (int)(Math.round(Math.abs((double)(value_ - min_) /
																			 (double)(max_ - min_)) *
															(double)(pixelMax_ - pixelMin_)) +
									 pixelMin_);
	else
		pixel_ = pixelMin_;
}
    
}

//***************************************************************************************************************

class ChangeDSlider extends Slider {
protected Blackboard _bb;
public ChangeDSlider( Blackboard black ) {
	_bb = black;
	SetMinimum( 1 );
	SetMaximum( 100 );
	SetValue( 20 );
	SetWidth( 100 );
	_bb.globals.D( GetValue() );
}
public void Motion() { 
	_bb.globals.D( GetValue() ); 
}
public void Release() { 
	_bb.globals.D( GetValue() ); 
}
} // class ChangeDSlider

class ChangeLocSlider extends Slider {
protected Blackboard _bb;
public ChangeLocSlider( Blackboard black ) {
	_bb = black;
	SetMinimum( 1 );
	SetMaximum( 10 );
	SetValue( 2 );
	SetWidth( 100 );
	_bb.globals.localizationDepth( GetValue() );
}
public void Motion() { 
	_bb.globals.localizationDepth( GetValue() ); 
}
public void Release() { 
	_bb.globals.localizationDepth( GetValue() ); 
}
} // class ChangeLoclider

class ChangeCycSlider extends Slider {
protected Blackboard _bb;
public ChangeCycSlider( Blackboard black ) {
	_bb = black;
	SetMinimum( 2 );
	SetMaximum( 20 );
	SetValue( 4 );
	SetWidth( 100 );
	_bb.globals.cycleLength( GetValue() );
}
public void Motion() { 
	_bb.globals.cycleLength( GetValue() );
}
public void Release() { 
	_bb.globals.cycleLength( GetValue() );
}
} // class ChangeLoclider

class ChangeTempSlider extends Slider {
protected Blackboard _bb;
public ChangeTempSlider( Blackboard black ) {
	_bb = black;
	SetMinimum( 0 );
	SetMaximum( 1000 );
	SetValue( 0 );
	SetWidth( 100 );
	_bb.globals.minTemp( GetValue()/100.0 );
}
public void Motion() { 
	_bb.globals.minTemp( GetValue()/100.0 ); 
}
public void Release() { 
	_bb.globals.minTemp( GetValue()/100.0 ); 
}
} // class ChangeTempSlider

class ChangeACSlider extends Slider {
protected Blackboard _bb;
public ChangeACSlider( Blackboard black ) {
	_bb = black;
	SetMinimum( 0 );
	SetMaximum( 100 );
	SetValue( 10 );
	SetWidth( 100 );
	_bb.globals.ac( GetValue()/10.0 );
}
public void Motion() { 
	_bb.globals.ac( GetValue()/10.0 ); 
}
public void Release() { 
	_bb.globals.ac( GetValue()/10.0 ); 
}
} // class ChangeACSlider

class ChangeAESlider extends Slider {
protected Blackboard _bb;
public ChangeAESlider( Blackboard black ) {
	_bb = black;
	SetMinimum( 0 );
	SetMaximum( 100 );
	SetValue( 20 );
	SetWidth( 100 );
	_bb.globals.ae( GetValue()/10.0 );
}
public void Motion() { 
	_bb.globals.ae( GetValue()/10.0 ); 
}
public void Release() { 
	_bb.globals.ae( GetValue()/10.0 ); 
}
} // class ChangeAESlider

class ChangeRCSlider extends Slider {
protected Blackboard _bb;
public ChangeRCSlider( Blackboard black ) {
	_bb = black;
	SetMinimum( 0 );
	SetMaximum( 100 );
	SetValue( 10 );
	SetWidth( 100 );
	_bb.globals.rc( GetValue()/10.0 );
}
public void Motion() { 
	_bb.globals.rc( GetValue()/10.0 ); 
}
public void Release() { 
	_bb.globals.rc( GetValue()/10.0 ); 
}
} // class ChangeRCSlider

class ChangeRESlider extends Slider {
protected Blackboard _bb;
public ChangeRESlider( Blackboard black ) {
	_bb = black;
	SetMinimum( 0 );
	SetMaximum( 100 );
	SetValue( 10 );
	SetWidth( 100 );
	_bb.globals.re( GetValue()/10.0 );
}
public void Motion() { 
	_bb.globals.re( GetValue()/10.0 ); 
}
public void Release() { 
	_bb.globals.re( GetValue()/10.0 ); 
}
} // class ChangeRESlider

class ControlWindow extends Frame implements Runnable {

protected Blackboard _bb;
protected Slider _sliderLoc, _sliderCyc, _sliderD, _sliderTemp,
                 _sliderAC, _sliderAE, _sliderRC, _sliderRE;
RotateWindow _rotation;
protected Choice _embedder, _dimension;
protected Label _currTemp;

protected boolean _showing = false;
public boolean showing() { 
	return _showing;
}
public void showing( boolean s ) { 
	_showing = s;
}

public ControlWindow( Blackboard black ) {
	super( "Embedding Controls" );
	_bb = black;

	setLayout(new GridLayout( 0, 2, 4, 4 ));

	_embedder = new Choice();
	_embedder.addItem("Stable");
	_embedder.addItem("Relax");
	_embedder.addItem("Random");
	_embedder.addItem("Circular");
	_embedder.addItem("BaryCentric");
	_embedder.addItem("ForceDir");
	_embedder.addItem("Level");
	_embedder.select("Random");
	add(_embedder);
  _dimension = new Choice();
	_dimension.addItem("2D");
	_dimension.addItem("3D");
	_dimension.select("2D");
	add(_dimension);
	add(new Button("Back"));
	add(new Button("Rotate"));

	_sliderD = new ChangeDSlider( _bb );
	add( new Label("Area Constant:", Label.RIGHT) );
	add( _sliderD );
	_sliderLoc = new ChangeLocSlider( _bb );
	add( new Label("Localiz. Depth:", Label.RIGHT) );
	add( _sliderLoc );
	_sliderCyc = new ChangeCycSlider( _bb );
	add( new Label("Cycle Length:", Label.RIGHT) );
	add( _sliderCyc );
	_sliderTemp = new ChangeTempSlider( _bb );
	add( new Label("100 * Min.Temp:", Label.RIGHT) );
	add( _sliderTemp );
	_sliderAC = new ChangeACSlider( _bb );
	add( new Label("100 * Attr. Const:", Label.RIGHT) );
	add( _sliderAC );
	_sliderAE = new ChangeAESlider( _bb );
	add( new Label("100 * Attr. Exp.:", Label.RIGHT) );
	add( _sliderAE );
	_sliderRC = new ChangeRCSlider( _bb );
	add( new Label("100 * Rep. Const:", Label.RIGHT) );
	add( _sliderRC );
	_sliderRE = new ChangeRESlider( _bb );
	add( new Label("100 * Rep. Exp.:", Label.RIGHT) );
	add( _sliderRE );
	add( new Label("Current Temp:", Label.RIGHT) );
	_currTemp = new Label( "0" );
	add( _currTemp );

  _rotation = new RotateWindow( _bb );
  _rotation.pack();

	_bb.addEmbedder( "Stable", new Stabilizer( _bb ) );
	_bb.addEmbedder( "Random", new Randomizer( _bb ) );
	_bb.addEmbedder( "BaryCentric", new Cycleizer( _bb ) );
	_bb.addEmbedder( "Circular", new Circularizer( _bb ) );
	_bb.addEmbedder( "Relax", new Relaxer( _bb ) );
	_bb.addEmbedder( "Level", new Leveller( _bb ) );
	_bb.addEmbedder( "ForceDir", new ForceDirect( _bb ) );
	_bb.setEmbedding( "Random" );
}

	// Thread stuff
	//
protected synchronized void refresh() {
	_currTemp.setText( Double.toString(_bb.globals.Temp()) );
}
protected Thread _updater;
public void run() {
	Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
	while (true) {
		refresh();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			break;
		}
	}
}
public void start() {
	_updater = new Thread(this);
	_updater.start();
}
public void stop() {
	if( _updater != null )
		_updater.stop();
	_updater = null;
}

public boolean action(Event evt, Object arg) {
	if (evt.target == _embedder) {
		_bb.setEmbedding( ((Choice)evt.target).getSelectedItem() );
		_bb.Init();
	}
	if (evt.target == _dimension ) {
		String s = ((Choice)evt.target).getSelectedItem();
    _rotation.origo();
		if( "2D".equals(s) ) {
			_bb.globals.depth3D( 0.0 );
		}
		else {
			_bb.globals.depth3D( _bb.globals.L() );
		}
		_bb.setEmbedding( "Random" );
		_bb.Init();
		return true;
	}
	else if (evt.target instanceof Button && "Back".equals(arg)) {
		_bb.backtrack();
	}
	else if (evt.target instanceof Button && "Rotate".equals(arg)) {
		if( !_rotation.showing() ) {
			_rotation.start();
			_rotation.show();
			_rotation.showing( true );
		}
		else {
			_rotation.showing( false );
			_rotation.stop();
			_rotation.handleEvent(new Event(_rotation,Event.WINDOW_DESTROY,null));
		}
	}
	else
		return false;
	return true;
}

public boolean handleEvent(Event evt) {
	if (evt.id == Event.WINDOW_ICONIFY) {
		hide();
		return true;
	}
	if (evt.id == Event.WINDOW_DESTROY) {
		dispose();
		return true;
	}
	return super.handleEvent(evt);
}

} // class GraphWindow


//**************************************************************************************************

class RotateSlider extends Slider {
protected Blackboard _bb;
protected int _oldvalue;
public RotateSlider( Blackboard black ) {
	_bb = black;
	SetMinimum( 0 );
	SetMaximum( 360 );
	_oldvalue = 0;
	SetValue( _oldvalue );
	SetWidth( 100 );
}
} // class RotateSlider

class RotateXSlider extends RotateSlider {
public RotateXSlider( Blackboard black ) {
	super( black );
}
public void Motion() { 
	_bb.projection.rotateX( GetValue()-_oldvalue ); 
	_oldvalue = GetValue();
}
public void Release() { 
	_bb.projection.rotateX( GetValue()-_oldvalue ); 
	_oldvalue = GetValue();
}
} // class RotateXSlider

class RotateYSlider extends RotateSlider {
public RotateYSlider( Blackboard black ) {
	super( black );
}
public void Motion() { 
	_bb.projection.rotateY( GetValue()-_oldvalue ); 
	_oldvalue = GetValue();
}
public void Release() { 
	_bb.projection.rotateY( GetValue()-_oldvalue ); 
	_oldvalue = GetValue();
}
} // class RotateYSlider

class RotateZSlider extends RotateSlider {
public RotateZSlider( Blackboard black ) {
	super( black );
}
public void Motion() { 
	_bb.projection.rotateZ( GetValue()-_oldvalue ); 
	_oldvalue = GetValue();
}
public void Release() { 
	_bb.projection.rotateZ( GetValue()-_oldvalue ); 
	_oldvalue = GetValue();
}
} // class RotateZSlider


class RotateWindow extends Frame implements Runnable {

protected Blackboard _bb;
protected Slider _sliderX, _sliderY, _sliderZ;

protected boolean _showing = false;
public boolean showing() { 
	return _showing;
}
public void showing( boolean s ) { 
	_showing = s;
}

public void origo() {
	_sliderX.SetValue( 0 );
	_sliderY.SetValue( 0 );
	_sliderZ.SetValue( 0 );
}

public RotateWindow( Blackboard black ) {
	super( "Rotation Controls" );
	_bb = black;

	setLayout(new GridLayout( 0, 2, 4, 4 ));

	add( new Label("X Rotation:", Label.RIGHT) );
	_sliderX = new RotateXSlider( _bb );
	add( _sliderX );
	add( new Label("Y Rotation:", Label.RIGHT) );
	_sliderY = new RotateYSlider( _bb );
	add( _sliderY );
	add( new Label("Z Rotation:", Label.RIGHT) );
	_sliderZ = new RotateZSlider( _bb );
	add( _sliderZ );
}

	// Thread stuff
	//
protected Thread _updater;
public void run() {
	Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
	while (true) {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			break;
		}
	}
}
public void start() {
	_updater = new Thread(this);
	_updater.start();
}
public void stop() {
	if( _updater != null )
		_updater.stop();
	_updater = null;
}

public boolean handleEvent(Event evt) {
	if (evt.id == Event.WINDOW_ICONIFY) {
		hide();
		return true;
	}
	if (evt.id == Event.WINDOW_DESTROY) {
		dispose();
		return true;
	}
	return super.handleEvent(evt);
}

} // class GraphWindow

//**************************************************************************************************

class Stabilizer implements Embedder {

protected Blackboard _bb;
public Stabilizer( Blackboard black ) {
	_bb = black;
}

	// Implementation of embedder interface, Init and Embed.
	//
public final void Init() {
	Vector nodes = _bb.nodes();
	int nodecnt = nodes.size();
	for (int i = 0; i < nodecnt; ++i ) {
		Node n = (Node) nodes.elementAt(i);
		n.stabilize();
	}
}

public final void Embed() {
}

} // class Randomizer

//***************************************************************************************************

class Randomizer implements Embedder {

protected Blackboard _bb;
public Randomizer( Blackboard black ) {
	_bb = black;
}

protected final void randomize() {
	double X = _bb.ux()-_bb.lx();
	double Y = _bb.uy()-_bb.ly();
	Vector nodes = _bb.nodes();
	int nodecnt = nodes.size();
	for (int i = 0; i < nodecnt; ++i ) {
		Node n = (Node) nodes.elementAt(i);
		n.randomPlacement( X, Y, _bb.globals.depth3D() );
	}
}

	// Implementation of embedder interface, Init and Embed.
	//
public final void Init() {
	double L = _bb.globals.L();
	_bb.setArea( -L/2, -L/2, L/2, L/2 );
  _bb.removeDummies();
	randomize();
}

protected boolean _updated = false;
public final void Embed() {
	if( !_updated ) {
		_bb.Update();
		_updated = true;
	}
}

} // class Randomizer

//****************************************************************************************************

class Cycleizer implements Embedder {

protected Blackboard _bb;
public Cycleizer( Blackboard black ) {
	_bb = black;
}

Vector _visitedNodes;
Vector _theCycle;
Vector _theRest;

protected final void save_new_cycle( Node begin, Node end ) {
  if( end.level()-begin.level()+1 != _theCycleLength
      || _theCycle != null ) 
    return;

  _theCycle = new Vector();
  int b = _visitedNodes.indexOf(begin); 
  int e = _visitedNodes.indexOf(end); 
  for( int i = b; i <= e; ++i )
    _theCycle.addElement( _visitedNodes.elementAt(i) );
}

protected final void find_cycle( Node curr, int level ) {
  _visitedNodes.addElement( curr );
  curr.mark();
  curr.pick( true );
  curr.level( level );
  Vector outedges = curr.outedges();
  int outedgecnt = outedges.size();
  for (int i = 0; i < outedgecnt; ++i ) {
    Node n = ((Edge) outedges.elementAt(i)).to();
    if( n.picked() )       save_new_cycle( n, curr );
    else if( !n.marked() ) find_cycle( n, level+1 );
  }
  Vector inedges = curr.inedges();
  int inedgecnt = inedges.size();
  for (int i = 0; i < inedgecnt; ++i ) {
    Edge e = (Edge) inedges.elementAt(i);
    if( !e.reversed() ) continue;
    Node n = e.from();
    if( n.picked() )       save_new_cycle( n, curr );
    else if( !n.marked() ) find_cycle( n, level+1 );
  }
  curr.pick( false );
  _visitedNodes.removeElement( curr );
}

protected final void get_cycle() {
  _theCycle = null;
  _bb.unmarkNodes();
  Vector nodes = _bb.nodes();
  int nodecnt = nodes.size();
  _visitedNodes = new Vector();
  for (int i = 0; i < nodecnt; ++i ) {
    Node n = (Node) nodes.elementAt(i);
    if( !n.marked() ) find_cycle( n, 0 );      
    if( _theCycle != null ) break;
  }
}

protected final void get_fixed() {
  _theCycle = _bb.get_fixed();
  if( _theCycle.size() == 0 )
    _theCycle = null;
}

protected final void circularize() {
  if( _theCycle == null ) return;
	double rX = (_bb.ux()-_bb.lx())/2;
	double rY = (_bb.uy()-_bb.ly())/2;
	double theta = 0;
	Vector nodes = _theCycle;
	int nodecnt = nodes.size();
	double delta = 2*Math.PI / nodecnt;
	for (int i = 0; i < nodecnt; ++i ) {
		Node n = (Node) nodes.elementAt(i);
		n.XY( rX*Math.cos(theta), rY*Math.sin(theta) );
		theta += delta;
	}
}

protected final void get_rest() {
  if( _theCycle == null )
    _theRest = (Vector) _bb.nodes().clone();
  else {
    _theRest = new Vector();
    Vector nodes = _bb.nodes();
    int nodecnt = nodes.size();
    int pos = 0;
    for (int i = 0; i < nodecnt; ++i ) {
      Node n = (Node) nodes.elementAt(i);
      if( !_theCycle.contains( n ) ) {
        n.level( pos );                      // We use the level for pos. info
        _theRest.addElement( n );
        ++pos;
      }
      else
        n.level( -1 );                     // And it's -1 if node is in cycle
    }
  }
}

// See the GraphPack paper for a discussion of this
protected final void layout_rest() {
  int restcnt = _theRest.size();
  if( restcnt == 0 ) return;
  double M[][] = new double[restcnt][];
  for( int i = 0; i < restcnt; ++i )
    M[i] = new double[restcnt+2];      // The last 2 cols are the x and y's.

  // Fill in the matrix with -1.0 if i,j are adjacent, 0.0 otherwise (default)
  for( int i = 0; i < restcnt; ++i ) {
    Node n = (Node) _theRest.elementAt( i );
    int degree = 0;
    Vector outedges = n.outedges();
    int outedgecnt = outedges.size();
    for( int j = 0; j < outedgecnt; ++j ) {
      Node to = ((Edge) outedges.elementAt( j )).to();
      int col = to.level();
      if( col == -1 ) continue;
      M[i][col] = -1.0;
      ++degree;
    }
    Vector inedges = n.inedges();
    int inedgecnt = inedges.size();
    for( int j = 0; j < inedgecnt; ++j ) {
      Node from = ((Edge) inedges.elementAt( j )).from();
      int col = from.level();
      if( col == -1 ) continue;
      M[i][col] = -1.0;
      ++degree;
    }
    M[i][i] = degree+1;  // This was lacking in the GraphPack paper?!?!?!
  }
  
  // Fill in the x and y columns with the sum of adjacent nodes' x and y's.
  for( int i = 0; i < restcnt; ++i ) {
    Node u = (Node) _theRest.elementAt( i );
    double x = 0.0, y = 0.0;
    Vector inedges = u.inedges();
    int inedgecnt = inedges.size();
    for( int j = 0; j < inedgecnt; ++j ) {
      Node v = ((Edge) inedges.elementAt(j)).from();
      x += v.X();
      y += v.Y();
    }
    Vector outedges = u.outedges();
    int outedgecnt = outedges.size();
    for( int j = 0; j < outedgecnt; ++j ) {
      Node v = ((Edge) outedges.elementAt(j)).to();
      x += v.X();
      y += v.Y();
    }
    M[i][ restcnt ] = x;
    M[i][restcnt+1] = y;
  }

  // We do a Gauss-Jordan Upper Triangularization of the matrix
  int n = restcnt;   // Number of rows
  int m = restcnt+2; // Number of columns

  for( int i = 0; i < n-1; ++i ) {
		double fac = M[i][i];
		for( int j = 0; j < m; ++j )
      M[i][j] /= fac;
		for( int j = i+1; j < n; ++j ) {
			fac = M[j][i];
			for( int k = i; k < m; ++k ) 
        M[j][k] = M[j][k] - fac*M[i][k];
		}
	}

	double factor = M[n-1][n-1];
	for( int j = 0; j < m; ++j )
    M[n-1][j] /= factor;

  // We solve for the position of the vertices from the upper triangular matrix
	for( int i = n-1; i > 0; --i ) {
    for( int j = 0; j < i; ++j ) {
      double fac = M[j][i];
      for( int k = i-1; k < m; ++k )
        M[j][k] = M[j][k] - fac*M[i][k];
    }
  }
  for( int i = 0; i < n; ++i ) {
    Node u = (Node) _theRest.elementAt( i );
    u.XY( M[i][n]/M[i][i], M[i][n+1]/M[i][i] );
  }
}

protected synchronized final void cycleize() {
	Vector nodes = _bb.nodes();
	int nodecnt = nodes.size();
	for (int i = 0; i < nodecnt; ++i ) {
		Node n = (Node) nodes.elementAt(i);
    if( !n.fixed() )
      n.XY( 0.0, 0.0 );
	}
  get_fixed();
  if( _theCycle == null ) {
    get_cycle();
    circularize();
  }
  if( _theCycle != null ) {
    get_rest();
    layout_rest();
  }
}


	// Implementation of embedder interface, Init and Embed.
	//
protected int _theCycleLength;
public final void Init() {
  _bb.removeDummies();
	double L = _bb.globals.L();
	_bb.setArea( -L/2, -L/2, L/2, L/2 );
  _theCycleLength = _bb.globals.cycleLength();
	cycleize();
}

protected boolean _updated = false;
public final void Embed() {
	if( !_updated ) {
		_bb.Update();
		_updated = true;
	}
  int cycleLength = _bb.globals.cycleLength();
  if( cycleLength != _theCycleLength ) {
    double L = _bb.globals.L();
    _bb.setArea( -L/2, -L/2, L/2, L/2 );
    _theCycleLength = cycleLength;
    cycleize();
  }
}

} // class Circularizer

//**************************************************************************************************

class Circularizer implements Embedder {

protected Blackboard _bb;
public Circularizer( Blackboard black ) {
	_bb = black;
}

protected final void circularize() {
	double rX = (_bb.ux()-_bb.lx())/2;
	double rY = (_bb.uy()-_bb.ly())/2;
	double theta = 0;
	double delta = 2*Math.PI / _bb.nodes().size();
	Vector nodes = _bb.nodes();
	int nodecnt = nodes.size();
	for (int i = 0; i < nodecnt; ++i ) {
		Node n = (Node) nodes.elementAt(i);
		n.randomPlacement( 0, 0, _bb.globals.depth3D() );
		n.XY( rX*Math.cos(theta), rY*Math.sin(theta) );
		theta += delta;
	}
}

	// Implementation of embedder interface, Init and Embed.
	//
public final void Init() {
  _bb.removeDummies();
	double L = _bb.globals.L();
	_bb.setArea( -L/2, -L/2, L/2, L/2 );
	circularize();
}

protected boolean _updated = false;
public final void Embed() {
	if( !_updated ) {
		_bb.Update();
		_updated = true;
	}
}

} // class Circularizer


//*****************************************************************************************************************

class Relaxer implements Embedder {

protected Blackboard _bb;
public Relaxer( Blackboard black ) {
	_bb = black;
}

	// Implementation of embedder interface, Init and Embed.
	//
public final void Init() {
  _bb.removeDummies();
	double L = _bb.globals.L();
	_bb.setArea( -L/2, -L/2, L/2, L/2 );
}

public final void Embed() {
	relaxer();
}
protected synchronized void relaxer() {
	double k = _bb.globals.k();
	Vector edges = _bb.edges();
	int edgecnt = edges.size();
	for (int i = 0; i < edgecnt; ++i ) {
		Edge e = (Edge) edges.elementAt(i);
		Node to = e.to();
		Node from = e.from();
		if( to == from ) 
			continue;
		double distance = e.currLength();
		if( distance == 0.0 ) distance = 0.0001;
		double force = (e.length() - distance)/(3*distance);
		to.forced( from, force );
		from.forced( to, force );
	}
	Vector nodes = _bb.nodes();
	int nodecnt = nodes.size();
	for (int i = 0; i < nodecnt; ++i ) {
		Node u = (Node) nodes.elementAt(i);
		PointDelta olddelta = u.getDelta();
		u.stabilize();
		for (int j = 0; j < nodecnt; ++j ) {
			Node v = (Node) nodes.elementAt(j);
			double distance = _bb.Norm( u, v );
			if( distance > 3*k ) 
				continue;
			if( distance == 0.0 ) distance = 0.0001;
			u.forced( v, 1.0/distance );
		}
		double force = u.deltaForce();
		if( force > 0.0 )
			u.scaleDelta( 2.0/force );
		u.addDelta( olddelta );
	}
	for (int i = 0; i < nodecnt; ++i ) {
		Node n = (Node) nodes.elementAt(i);
		if( !n.fixed() && !n.picked() ) {
			n.boundedMoveDelta( 5.0 );
		}
		n.scaleDelta( 0.5 );
	}
}

} // class Relaxer

//******************************************************************************************************

class Leveller implements Embedder {

protected Blackboard _bb;
public Leveller( Blackboard black ) {
	_bb = black;
}

protected int _maxlevel;
protected Vector _levels[];

public final void initialOrderNodes( Node curr ) {
	curr.mark();
	Vector inedges = curr.inedges();
	int edgecnt = inedges.size();
	for (int i = 0; i < edgecnt; ++i ) {
		Node n = ((Edge) inedges.elementAt(i)).from();
		if( n.marked() == false )
			initialOrderNodes( n );
	}
	_levels[ curr.level() ].addElement( curr );
}

protected synchronized final void makelevels() {
	_maxlevel = -1;
	Node maxlevelnode = null;
	Vector nodes = _bb.nodes();
	int nodecnt = nodes.size();
	for (int i = 0; i < nodecnt; ++i ) {             // Find maximum level
		Node n = (Node) nodes.elementAt(i);
    if( _maxlevel < n.level() ) {
      _maxlevel = n.level();
			maxlevelnode = n;
		}
	}
  _levels = new Vector[_maxlevel+1];              // Make and initialize levels
	for( int j = 0; j <= _maxlevel; ++j ) {
		_levels[j] = new Vector();
	}
	_bb.unmarkNodes();
	initialOrderNodes( maxlevelnode );              // DFS order most the nodes
	for (int k = 0; k < nodecnt; ++k ) {             // Find maximum level
		Node n = (Node) nodes.elementAt(k);
		if( !n.marked() )
			initialOrderNodes( n );
	}
}

protected final void placeLevel( double L, double y, 
																 int levelcnt, Vector nodes ) {
	double xstep = L/(levelcnt+1);
	for( int i = 0; i < levelcnt; ++i ) {
		Node n = (Node) nodes.elementAt( i );
    n.x( xstep*(i+1) );
    n.y( y );
	}  
}

protected final void placeNodes() {
	double L = _bb.globals.L();
  double ystep = L/(_maxlevel+1);
	double y = 0.0;
	for( int i = 0; i <= _maxlevel; ++i ) {
		Vector nodes = _levels[i];
		placeLevel( L, y, nodes.size(), nodes );
		y += ystep;
	}
}

protected final void sortLevel( Vector nodes ) {
  // Do insertionsort on the level based on the barycenters, then reorder
	int len = nodes.size();
  for( int P = 1; P < len; ++P ) {
    Node tmp = (Node) nodes.elementAt( P );
		double barycent = tmp.barycenter();
    int j;
    for( j = P; j > 0; --j ) {
      Node tmp2 = (Node) nodes.elementAt( j-1 );
      if( barycent >= tmp2.barycenter() ) break;
      nodes.setElementAt( tmp2, j );
		}
		nodes.setElementAt( tmp, j );
	}
}

protected final void orderLevel( Vector nodes,
																 double L, double y, 
																 boolean doin, boolean doout ) {
	int levelcnt = nodes.size();
	for( int j = 0; j < levelcnt; ++j ) {
		Node curr = (Node) nodes.elementAt( j );
		curr.barycenter( curr.computeBarycenter(doin, doout) );
	}
	sortLevel( nodes );
	placeLevel( L, y, levelcnt, nodes );
}

	// Do downwards barycentering on first pass, upwards on second, then average
protected synchronized final void orderNodes( double L, int op ) {
	boolean doup = ((op & 0x1) == 1);
	boolean doin = (op > 5 || !doup);
	boolean doout = (op > 5 || doup);
  double ystep = (_maxlevel>0) ? (L/_maxlevel) : 0.0;
	if( doup ) {
		double y = 0.0;
		for( int i = 0; i <= _maxlevel; ++i ) {         // Going upwards
			Vector nodes = _levels[i];
			orderLevel( nodes, L, y, doin, doout );
			y += ystep;
		}
	}
	else {
		double y = L;
		for( int i = _maxlevel; i >= 0; --i ) {         // Going downwards
			Vector nodes = _levels[i];
			orderLevel( nodes, L, y, doin, doout );
			y -= ystep;
		}
	}
}


protected final void straightenDummy( Node n ) {
	Node from = ((Edge) n.inedges().firstElement()).from();
	Node to = ((Edge) n.inedges().firstElement()).to();
	double avg = (n.x() + from.x() + to.x()) / 3;
	n.x( avg );
}
private final int xmarginSize = 10;
protected synchronized final void straightenLayout( double L ) {
  double ystep = L/(_maxlevel+1);
	double y = 0.0;
	for( int i = 0; i <= _maxlevel; ++i ) {
		Vector nodes = _levels[i];
		int levelcnt = nodes.size();
		for( int j = 0; j < levelcnt; ++j ) {
			Node n = (Node) nodes.elementAt( j );
			if( n.dummy() )
				straightenDummy( n );
		}
		for( int j = 1; j < levelcnt; ++j ) {
			Node n = (Node) nodes.elementAt( j );
			Node prev = (Node) nodes.elementAt( j-1 );
			double prevright = prev.x() + prev.boundingWidth()/2 + xmarginSize;
			double thisleft =  n.x()    - n.boundingWidth()/2    - xmarginSize;
			double overlap = prevright - thisleft;
			if( overlap > 0 ) {
				prev.x( prev.x() - overlap/2 );
				n.x( n.x() + overlap/2 );
			}
			n.y( y ); 
		}
		y += ystep;
	}
}

	// Implementation of embedder interface, Init and Embed.
	//
public final void Init() {
  // NB: The order here matters, L depends on no. of nodes
  _bb.addDummies();
	double L = _bb.globals.L();
	_bb.setArea( 0, 0, L, L );
	makelevels();
  placeNodes();
	_operation = 0;
}

protected int _operation = 0;
protected final int _Order = 100;
public final void Embed() {
	double L = _bb.globals.L();
	_bb.setArea( 0, 0, L, L );
  if( _operation < _Order ) {
    orderNodes( L, _operation );
  }
  else {
		straightenLayout( L );
	}
  _bb.Update();
  ++_operation;
  _bb.globals.Temp( (double)_operation );
}

} // class Leveller

//********************************************************************************************************

class ForceDirect implements Embedder {

protected Blackboard _bb;
public ForceDirect( Blackboard black ) {
	_bb = black;
}

protected double _time;
protected final double fa(double x) {
	double k = _bb.globals.k();
	double ac = _bb.globals.ac();
	double ae = _bb.globals.ae();
	return ac*Math.pow(x,ae)/k;
}
protected final double fr(double x) {
	double k = _bb.globals.k();
	double rc = _bb.globals.rc();
	double re = _bb.globals.re();
	return rc*(k*k)/Math.pow(x,re);
}
protected final double temp(double t) {
	return _bb.globals.L()/(2*_bb.globals.D())/(1+Math.exp(t/8-5));
}

	// Implementation of embedder interface, Init and Embed.
	//
public final void Init() {
  _bb.removeDummies();
	double L = _bb.globals.L();
	_time = 1.0;
	_bb.setArea( -L/2, -L/2, L/2, L/2 );
}

public final void Embed() {
	force_directed_placement();
	_bb.Update();
}
private final double __min( double a, double b ) { return (a<b)?a:b; }
private final double __max( double a, double b ) { return (a>b)?a:b; }
protected synchronized void force_directed_placement() {
	// Calculate repulsive forces
	//
	double k = _bb.globals.k();
	Vector nodes = _bb.nodes();
	int nodecnt = nodes.size();
	for (int i = 0; i < nodecnt; ++i ) {
		Node u = (Node) nodes.elementAt(i);
		u.stabilize();
		for (int j = 0; j < nodecnt; ++j ) {
			Node v = (Node) nodes.elementAt(j);
			double distance = _bb.Norm( u, v );
			if( distance > 3*k ) 
				continue;
			if( distance == 0.0 ) distance = 0.0001;
			u.forced( v, 1.0/distance*fr(distance) );
		}
	}
	// Calculate attractive forces
	//
	Vector edges = _bb.edges();
	int edgecnt = edges.size();
	for( int i = 0; i < edgecnt; ++i ) {
		Edge e = (Edge) edges.elementAt(i);
		Node from = e.from();
		Node to = e.to();
		double distance = _bb.Norm( to, from );
		if( distance > 0.00001 ) {
			to.forced( from, -1.0/distance*fa(distance) );
			from.forced( to, -1.0/distance*fa(distance) );
		}
	}
	// Displace with regard to temperature
	//
	double Temp = temp(_time)+_bb.globals.minTemp();
	_bb.globals.Temp( Temp );
	for (int j = 0; j < nodecnt; ++j ) {
		Node v = (Node) nodes.elementAt(j);
		double force = v.deltaForce();
		if( force < 0.00001 ) continue;
		if ( !v.fixed() && !v.picked() ) {
			v.moveDelta( 1.0/force * __min(force,Temp) );
		}
	}
	_time += 1.0;
}

} // class ForceDirect

//****************************************************************************************************************

interface Embedder { 

public void Init();
public void Embed();

} // interface Embedder
