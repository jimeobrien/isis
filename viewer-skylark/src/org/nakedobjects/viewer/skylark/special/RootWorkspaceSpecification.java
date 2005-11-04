package org.nakedobjects.viewer.skylark.special;

import org.nakedobjects.object.NakedObject;
import org.nakedobjects.object.UserContext;
import org.nakedobjects.viewer.skylark.CompositeViewBuilder;
import org.nakedobjects.viewer.skylark.Content;
import org.nakedobjects.viewer.skylark.ObjectContent;
import org.nakedobjects.viewer.skylark.Size;
import org.nakedobjects.viewer.skylark.View;
import org.nakedobjects.viewer.skylark.ViewAxis;
import org.nakedobjects.viewer.skylark.Workspace;
import org.nakedobjects.viewer.skylark.basic.DefaultWorkspace;
import org.nakedobjects.viewer.skylark.basic.RootWorkspaceBuilder;
import org.nakedobjects.viewer.skylark.basic.UserContextWorkspace;

public class RootWorkspaceSpecification implements WorkspaceSpecification {
	RootWorkspaceBuilder builder = new RootWorkspaceBuilder();
	
	public View createView(Content content, ViewAxis axis) {
			Workspace workspace;
			NakedObject contentType = ((ObjectContent) content).getObject();
			if(contentType instanceof UserContext) {
				workspace = new UserContextWorkspace(content, this, axis);
			} else {
				workspace = new DefaultWorkspace(content, this, axis);
			}
			return workspace;
		}
	

	public CompositeViewBuilder getSubviewBuilder() {
		return builder;
	}
	
	public void setRequiredSize(Size size) {
		builder.setRequiredSize(size);
	}
	
	public String getName() {
		return "Root Workspace";
	}

	public boolean isOpen() {
		return true;
	}

	public boolean isReplaceable() {
		return false;
	}

	public boolean isSubView() {
		return false;
	}

	public boolean canDisplay(Content content) {
		return content.isObject() && content.getNaked() instanceof UserContext;
	}
}


/*
Naked Objects - a framework that exposes behaviourally complete
business objects directly to the user.
Copyright (C) 2000 - 2005  Naked Objects Group Ltd

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

The authors can be contacted via www.nakedobjects.org (the
registered address of Naked Objects Group is Kingsway House, 123 Goldworth
Road, Woking GU21 1NR, UK).
*/