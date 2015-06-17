package eu.riscoss.db;

import java.util.ArrayList;
import java.util.Iterator;

// /path/to/element/@link#index/child#index/leaf:attr

public class DomPath implements Iterable<DomPath.PathPart>
{
	public class PathPart
	{
		String	name = "";
		int		index = 0;
		String	attr = null;
		boolean	leaf = false;
		public PathPart parent;
		
		PathPart( String part, boolean isLeaf )
		{
			this.leaf = isLeaf;
			
			int pAtChar = part.indexOf( "@" );
			int pBrkChar = part.indexOf( "##" );
			
			String name = null;
			String index = null;
			String attr = null;
			
			if( (pBrkChar == -1) & (pAtChar == -1) )
			{
				name = part;
				index = null;
				attr = null;
			}
			else
			{
				if( (pBrkChar != -1) & (pAtChar == -1) )
				{
					name = part.substring( 0, pBrkChar );
					index = part.substring( pBrkChar +1 ); //, part.indexOf( "]" ) );
					attr = null;
				}
				else if( (pBrkChar == -1) & (pAtChar != -1) )
				{
					name = part.substring( 0, pAtChar );
					index = null;
					attr = part.substring( pAtChar +1 );
				}
				else if( (pBrkChar != -1) & (pAtChar != -1) )
				{
					name = part.substring( 0, pBrkChar );
					index = part.substring( pBrkChar +1 ); //, part.indexOf( "]" ) );
					attr = null; //part.substring( pAtChar +1 );
				}
			}
			
			if( index != null )
			{
				this.index = Integer.parseInt( index );
			}
			this.attr = attr;
			this.name = name;
		}
		
		public boolean isLeaf()
		{
			return leaf;
		}

		public int getIndex()
		{
			return this.index;
		}

		public String getName()
		{
			return this.name;
		}

		public String getLink()
		{
			return this.attr;
		}
		
		public String toString()
		{
			return getName();
		}
		
		public String getQuantifiedName()
		{
			if( index > 0 )
				return name + "[" + index + "]";
			else
				return name;
		}
	}
	
	public class PathIterator implements Iterator<DomPath.PathPart>
	{
		int		n = 1;
		DomPath	path;
		
		public PathIterator( DomPath path )
		{
			this.path = path;
		}
		
		@Override
		public boolean hasNext()
		{
			return n < path.parts.size();
		}
		
		@Override
		public DomPath.PathPart next()
		{
			DomPath.PathPart part = 
					path.parts.get( n );
//					new PathPart( path.parts.get( n ), (n == path.parts.size() -1) );
			
			n++;
			
			return part;
		}

		@Override
		public void remove() {}
	}
	
	String fullPath;
	ArrayList<PathPart> parts = new ArrayList<PathPart>();
	
	public DomPath( String path )
	{
		init( path );
	}
	
	public DomPath( String path, String filename )
	{
		if( "/".compareTo( path ) == 0 )
			path = "";
		
		init( path + "/" + filename );
	}

	public DomPath( DomPath path, String filename )
	{
		this( path.fullPath, filename );
	}

	public DomPath()
	{
		init( "/" );
	}
	
	public DomPath( DomPath parentPath, String value, int n )
	{
		init( parentPath + "/" + value + "[" + n + "]" );
	}

	private void init( String path )
	{
		fullPath = path.trim();
		
		if( !fullPath.startsWith( "/" ) )
			if( !fullPath.startsWith( "." ) )
				fullPath = "/" + fullPath;
		
		
		
		fullPath = path;
		
		String[] p = fullPath.split( "[/]" );
		
		if( p.length < 1 ) p = new String[] { "/" };
		
		p[0] = "/";
		
		for( String s : p )
		{
			append( new PathPart( s, false ) );
		}
	}

	private void append( PathPart pathPart )
	{
		PathPart parent = getLeaf();
		
		if( parent != null )
			parent.leaf = false;
		
		parts.add( pathPart );
		pathPart.parent = parent;
	}

	@Override
	public Iterator<DomPath.PathPart> iterator()
	{
		return new PathIterator( this );
	}
	
	public String toString()
	{
		return fullPath;
	}
	
//	public DomPath getParent()
//	{
//		return new DomPath( getParentPath() );
//	}

	public String getName()
	{
		return parts.get( parts.size() -1 ).getName();
	}

	public String getPath()
	{
		String parentPath = "/";
		String sep = "";
		
		for( int i = 1; i < parts.size(); i++ )
		{
			parentPath += sep + parts.get( i ).getName();
			sep = "/";
		}
		
		return parentPath;
	}
	
	public String getParentPath()
	{
		String parentPath = "/";
		String sep = "";
		
		for( int i = 1; i < parts.size() -1; i++ )
		{
			parentPath += sep + parts.get( i ).getName();
			sep = "/";
		}
		
		return parentPath;
	}

	public int pathLength()
	{
		return parts.size();
	}

	public boolean isRoot()
	{
		return parts.size() == 1;
		
//		if( parts.size() > 1 ) return false;
//		
//		return "/".compareTo( fullPath ) == 0;
	}

//	public void pushPart( String name )
//	{
//		parts.add( name );
//	}

//	public void popPart()
//	{
//		parts.remove( parts.size() -1 );
//	}

	public PathPart getLeaf()
	{
		if( parts.size() < 1 )
			return null;
			
		return parts.get( parts.size() -1 );
	}

	public String getQuantifiedName()
	{
		PathPart leaf = getLeaf();
		if( leaf.index > 0 )
			return leaf.name + "[" + leaf.index + "]";
		else
			return leaf.name;
	}

	public int getIndex()
	{
		return getLeaf().index;
	}

}