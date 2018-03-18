package comp557lw.a3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Class implementing the Catmull-Clark subdivision scheme
 * 
 * @author Milan Singh
 */
public class CatmullClark {
	
	static int getHeadDegree( HalfEdge he )
	{
		int degree = 0;
		HalfEdge loop = he;
		do 
		{
			loop = loop.next;
			if( loop == null )
				break;
			degree++;
		} while( loop != he && loop != null );
		return degree;
	}
	
	static Set<HalfEdge> getOutwardHalfEdgesOfHead( HalfEdge he )
	{
		Set<HalfEdge> outEdges = new HashSet<>();
    	HalfEdge loop = he;
    	do {
    		loop = loop.next;
    		if(loop == null) 
    			break;
    		outEdges.add(loop);
    		loop = loop.twin;
    	} while(loop != he && loop != null);
    	
    	if(loop == null) 
    	{
    		while(loop.twin != null) 
    		{
    			loop = loop.twin;
        		outEdges.add(loop);
	    		loop = loop.prev();
    		}

    		loop = loop.prev();
    		outEdges.add(loop);
    	}
    	return outEdges;
	}
	
	static HalfEdge getBoundaryHaflEdge( HalfEdge he ) 
	{
		HalfEdge loop = he;
		do 
		{
			loop = loop.next;
			if( loop.twin == null )
				return loop;
			else
				loop = loop.twin;
		} while( loop != he );
		
		return null;
	}
	
	static HalfEdge getBoundaryMirrorHaflEdge( HalfEdge he ) 
	{
		HalfEdge loop = he;
		while( loop.twin != null ) 
		{
			loop = loop.twin.prev();
			if( loop.twin == he )
				return null;
		} 
		
		return loop;
	}
	
	static void setEvenChildren( Face face )
	{
    	HalfEdge loop = face.he;
    	do {
    		if(loop.head.child != null) 
    		{
            	loop = loop.next;
    			continue;
    		}
    		loop.head.child = new Vertex();
    		HalfEdge boundaryHE = getBoundaryHaflEdge(loop);
        	if(boundaryHE != null) {
        		HalfEdge mirrorHE = getBoundaryMirrorHaflEdge(loop);
        		Point3d temp;
        		temp = new Point3d();
        		temp.set(loop.head.p);
        		temp.scale(0.75);
        		loop.head.child.p.add(temp);
        		temp = new Point3d();
        		temp.add(boundaryHE.head.p);
        		temp.add(mirrorHE.prev().head.p);
        		temp.scale(0.125);
        		loop.head.child.p.add(temp);
        	}
        	else {
        		Set<HalfEdge> out = getOutwardHalfEdgesOfHead(loop);
        		int k = out.size();
        		double beta = 3.0 / (2.0 * k);
        		double gamma = 1.0 / (4.0 * k);

        		Point3d temp;
        		temp = new Point3d();
        		temp.set(loop.head.p);
        		temp.scale(1.0 - beta - gamma);
        		loop.head.child.p.add(temp);
        		
        		Point3d betaPoint = new Point3d();
        		Point3d gammaPoint = new Point3d();
        		for(HalfEdge o : out)
        		{
        			betaPoint.add(o.head.p);
        			gammaPoint.add(o.next.head.p);
        		}
        		betaPoint.scale(beta / k);
        		gammaPoint.scale(gamma / k);
        		loop.head.child.p.add(betaPoint);
        		loop.head.child.p.add(gammaPoint);
        	}
        	loop = loop.next;
    	} while( loop != face.he);
	}
	
	static void setFaceChild( Face face )
	{
		if( face.child != null )
			return;
		
		face.child = new Vertex();
		HalfEdge he = face.he;
		
		HalfEdge loop = he;
		int i = 0;
		do 
		{
			i++;
			face.child.p.add( loop.head.p );
			loop = loop.next;
		} while( loop != he );
		face.child.p.scale( 1.0 / i );
	}
	
	static void setEdgeChildren( Face face )
	{
		HalfEdge loop = face.he;
		do 
		{
			setHalfEdgeChildVert( loop );
			loop = loop.next;
		} while( loop != face.he );
	}
	
	public static void setHalfEdgeChildVert( HalfEdge he ) {
    	if( he.childVert != null) 
    		return;
    	
    	if( he.twin != null) 
    	{
        	he.twin.childVert = new Vertex();
        	he.childVert = he.twin.childVert;
        	he.twin.childVert.p.add( he.twin.leftFace.child.p);
    		he.twin.childVert.p.add( he.head.p );
    		he.twin.childVert.p.add( he.twin.head.p);
    		he.twin.childVert.p.add( he.leftFace.child.p );
    		he.twin.childVert.p.scale( 0.25 );
    	} 
    	else
    	{
        	he.childVert = new Vertex();
        	he.childVert.p.add(he.prev().head.p);
        	he.childVert.p.add(he.head.p);
    		he.childVert.p.scale(0.5);
    	}
    }
	
	public static void connectFace( Face face, HEDS heds ) {
    	HalfEdge last = null;
    	HalfEdge loop = face.he;
    	do {
    		loop.child1 = new HalfEdge();
    		loop.child1.head = loop.childVert;
    		loop.child1.parent = loop;
    		if(loop.twin != null && loop.twin.child2 != null)
    			loop.child1.twin = loop.twin.child2;
    		
    		loop.child2 = new HalfEdge();
    		loop.child2.head = loop.head.child;
    		loop.child2.parent = loop;
    		if(loop.twin != null && loop.twin.child1 != null) 
    			loop.child2.twin = loop.twin.child1;

			HalfEdge to = new HalfEdge();
			HalfEdge aft = new HalfEdge();
			
			to.head = face.child;
			aft.head = loop.childVert;
			
			to.twin = aft;
			aft.twin = to;
			
			loop.child1.next = to;
			aft.next = loop.child2;
			
    		if(last != null) 
    		{
    			last.next.next = loop.child1;
    			to.next = last;
    			heds.faces.add(new Face(loop.child1));
    		}
    		last = aft;
    		loop = loop.next;
    	} while( loop != face.he );
    	
		last.next.next = loop.child1;
		loop.child1.next.next = last;
		heds.faces.add( new Face(loop.child1) );
    }
	
	public static void computeNormals( Face face )
	{
    	HalfEdge he = face.he;
    	
    	do {
    		if(he.head.n == null) 
    			he.head.n = new Vector3d();
            Vector3d a = new Vector3d();
            Vector3d b = new Vector3d();
            Vector3d n = new Vector3d();
            
            a.sub(he.head.p, he.prev().head.p);
            b.sub(he.head.p, he.next.head.p);
            n.cross( b,a ); 
            
            he.head.n.add(n);
    	} while((he = he.next) != face.he);
    	
        he.head.n.normalize();
	}
	
	/*
	static void setEdgeChildren( Face face ) 
	{
		HalfEdge he = face.he;
		
		HalfEdge loop = he;
		do 
		{
			if ( loop.twin == null )
			{
				Vertex newVert = new Vertex();
	    		newVert.p.add(loop.head.p);
	    		newVert.p.add(loop.twin.head.p);
	    		newVert.p.scale(0.5);
			}
			else
			{
				if( loop.child1 == null && loop.child2 == null )
				{
					Vertex newVert = new Vertex();
					
		    		newVert.p.add(loop.head.p);
		    		newVert.p.add(loop.twin.head.p);
		    		newVert.p.add(loop.leftFace.child.p);
		    		newVert.p.add(loop.twin.leftFace.child.p);
		    		newVert.p.scale(0.25);
					
					loop.child1				= new HalfEdge();
					loop.child1.head 		= newVert;
					loop.child1.parent		= loop;
					loop.child1.leftFace	= face;

					loop.child2				= new HalfEdge();
					loop.child2.head 		= loop.head.child;
					loop.child2.parent		= loop;
					loop.child2.leftFace	= face;
					if ( loop.twin.child1 != null && loop.twin.child2 != null )
					{
						loop.twin.child2			= new HalfEdge();
						loop.twin.child2.head 		= newVert;
						loop.twin.child2.parent		= loop;
						loop.twin.child2.leftFace	= loop.twin.leftFace;

						loop.twin.child1			= new HalfEdge();
						loop.twin.child1.head 		= loop.head.child;
						loop.twin.child1.parent		= loop;
						loop.twin.child1.leftFace	= loop.twin.leftFace;

						loop.child1.twin = loop.twin.child2;
						loop.twin.child1.twin = loop.child2;
					}
				}
			}
			loop = loop.next;
			
		} while ( loop != he );
	}
	*/
    /**
     * Subdivides the provided half edge data structure
     * @param heds
     * @return the subdivided mesh
     */
    public static HEDS subdivide( HEDS heds ) {
        HEDS heds2 = new HEDS();
        
        List<Face> faces = heds.faces;
        for( Face face : faces )
        {
        	setEvenChildren(face);
        	setFaceChild(face);
        } 
        for(Face face : heds.faces) 
            setEdgeChildren(face);
        
        for( Face face : faces )
        	connectFace(face, heds2);
        
        for( Face face: heds2.faces )
        	computeNormals(face);
        
        return heds2;        
    }
    
}
