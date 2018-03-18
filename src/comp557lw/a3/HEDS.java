package comp557lw.a3;

//Milan Singh 260654803

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Half edge data structure.
 * Maintains a list of faces (i.e., one half edge of each) to allow for easy display of geometry.
 * 
 * @author Milan Singh
 */
public class HEDS {

    /**
     * List of faces 
     */
    List<Face> faces = new ArrayList<Face>();
        
    /**
     * Constructs an empty mesh (used when building a mesh with subdivision)
     */
    public HEDS() {
        // do nothing
    }
        
    
    /**
     * Data structure for storing edges uniquely and mapping halfEdges to edges
     * Sets twin for edges added to the same edge
     */
    static class EdgeToHalfEdgeMap
    {
    	ArrayList< Vertex[] > edges;
    	HashMap< Vertex[], HalfEdge[] > halfEdgeMap;
    	
    	EdgeToHalfEdgeMap()
    	{
    		edges = new ArrayList<Vertex[]>();
    		halfEdgeMap = new HashMap<Vertex[], HalfEdge[]>();
    	}
    	
    	public void add( Vertex v0, Vertex v1, HalfEdge he )
    	{
    		Vertex u;
    		Vertex v;
    		//Ensure that edges are only stored once by ordering them
    		if (v0.p.x != v1.p.x)
    		{
				u = ( v0.p.x < v1.p.x ) ? v0 : v1;
				v = ( v0.p.x < v1.p.x ) ? v1 : v0;
    		} 
    		else if ( v0.p.y != v1.p.y )
    		{
				u = ( v0.p.y < v1.p.y ) ? v0 : v1;
				v = ( v0.p.y < v1.p.y ) ? v1 : v0;
    		}
    		else
    		{
				u = ( v0.p.z < v1.p.z ) ? v0 : v1;
				v = ( v0.p.z < v1.p.z ) ? v1 : v0;
    		}
			
			for( Vertex[] edge : edges )
			{
				if( edge[0] == u && edge[1] == v )
				{
					HalfEdge[] twinPair = halfEdgeMap.get( edge );
					twinPair[1] = he;
					twinPair[0].twin = he;
					he.twin = twinPair[0];
					return;
				}
			}
			Vertex[] edge = new Vertex[] { u, v };
			edges.add( edge );
			HalfEdge[] halfEdges = new HalfEdge[2];
			halfEdges[0] = he;
			halfEdgeMap.put( edge, halfEdges );
    	}
    }
    
    
    /**
     * Builds a half edge data structure from the polygon soup   
     * @param soup
     */
    public HEDS( PolygonSoup soup ) 
    { 
    	
    	List<Vertex> verts 		= soup.vertexList;
    	List<int[]> soupFaces 	= soup.faceList;

    	EdgeToHalfEdgeMap eheMap = new EdgeToHalfEdgeMap();
    	
    	for( int[] face : soupFaces )
    	{
    		int sides = face.length;
    		HalfEdge[] hes = new HalfEdge[ sides ];
    		
    		for( int i = 0; i < sides; i++ )
    		{
    			HalfEdge he = new HalfEdge();
    			Vertex u = verts.get( face[i] );
    			Vertex v = verts.get( face[(i+1)%sides] );
    			he.head = v;
    			eheMap.add( u, v, he);

    			hes[i] = he;
    		}
    		
    		for( int i = 0; i < sides; i++ )
    		{
    			hes[i].next = hes[(i+1)%sides];
    			
    			 //Enforce order to which vert is stored first so edges are stored 
    		}
    		faces.add( new Face( hes[0] ) );
    	}
    	
        
        
    }  
    
    /**
     * Draws the half edge data structure by drawing each of its faces.
     * Per vertex normals are used to draw the smooth surface when available,
     * otherwise a face normal is computed. 
     * @param drawable
     */
    public void display() {
        // note that we do not assume triangular or quad faces, so this method is slow! :(     
        Point3d p;
        Vector3d n;        
        for ( Face face : faces ) {
            HalfEdge he = face.he;
            if ( he.head.n == null ) { // don't have per vertex normals? use the face
                glBegin( GL_POLYGON );
                n = he.leftFace.n;
                glNormal3d( n.x, n.y, n.z );
                HalfEdge e = he;
                do {
                    p = e.head.p;
                    glVertex3d( p.x, p.y, p.z );
                    e = e.next;
                } while ( e != he );
                glEnd();
            } else {
                glBegin( GL_POLYGON );                
                HalfEdge e = he;
                do {
                    p = e.head.p;
                    n = e.head.n;
                    glNormal3d( n.x, n.y, n.z );
                    glVertex3d( p.x, p.y, p.z );
                    e = e.next;
                } while ( e != he );
                glEnd();
            }
        }
    }
    
    /** 
     * Draws all child vertices to help with debugging and evaluation.
     * (this will draw each points multiple times)
     * @param drawable
     */
    public void drawChildVertices() {
    	glDisable( GL_LIGHTING );
        glPointSize(8);
        glBegin( GL_POINTS );
        for ( Face face : faces ) {
            if ( face.child != null ) {
                Point3d p = face.child.p;
                glColor3f(0,0,1);
                glVertex3d( p.x, p.y, p.z );
            }
            HalfEdge loop = face.he;
            do {
                if ( loop.head.child != null ) {
                    Point3d p = loop.head.child.p;
                    glColor3f(1,0,0);
                    glVertex3d( p.x, p.y, p.z );
                }
                if ( loop.child1 != null && loop.child1.head != null ) {
                    Point3d p = loop.child1.head.p;
                    glColor3f(0,1,0);
                    glVertex3d( p.x, p.y, p.z );
                }
                loop = loop.next;
            } while ( loop != face.he );
        }
        glEnd();
        glEnable( GL_LIGHTING );
    }
}
