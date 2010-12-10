package org.apache.isis.viewer.bdd.common.fixtures;

import org.apache.isis.core.commons.lang.StringUtils;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.viewer.bdd.common.AliasRegistry;
import org.apache.isis.viewer.bdd.common.CellBinding;
import org.apache.isis.viewer.bdd.common.ScenarioBoundValueException;
import org.apache.isis.viewer.bdd.common.ScenarioCell;

public class AliasItemsInListPeer extends AbstractListFixturePeer {

    private final CellBinding titleBinding;
    private final CellBinding typeBinding;
    private final CellBinding aliasBinding;
    

    public AliasItemsInListPeer(final AliasRegistry aliasesRegistry,
            final String listAlias, 
            final CellBinding titleBinding,
            final CellBinding typeBinding, final CellBinding aliasBinding) {
    	super(aliasesRegistry, listAlias, titleBinding, typeBinding, aliasBinding);

    	this.titleBinding = titleBinding;
        this.typeBinding = typeBinding;
        this.aliasBinding = aliasBinding;
    }

	public CellBinding getTitleBinding() {
		return titleBinding;
	}
	
	public CellBinding getTypeBinding() {
		return typeBinding;
	}
	
	public CellBinding getAliasBinding() {
		return aliasBinding;
	}

	public ScenarioCell findAndAlias() throws ScenarioBoundValueException {
    	ObjectAdapter foundAdapter = findAdapter();
        if (foundAdapter == null) {
        	throw ScenarioBoundValueException.current(titleBinding, "not found");
        }
        
        ScenarioCell currentCell = aliasBinding.getCurrentCell();
		String currentCellText = currentCell.getText();
		getAliasRegistry().aliasAs(currentCellText, foundAdapter);
		return currentCell;
	}

	private ObjectAdapter findAdapter() {
        for (final ObjectAdapter adapter : collectionAdapters()) {
        	
            if (!titleMatches(adapter)) {
                continue; // keep looking
            }
            if (!typeMatches(adapter)) {
                continue; // keep looking
            }
            
            return adapter;
        }
		return null;
	}

    private boolean titleMatches(final ObjectAdapter adapter) {
        final String adapterTitle = adapter.titleString();
        final String requiredTitle = titleBinding.getCurrentCell().getText();
        return StringUtils.nullSafeEquals(adapterTitle, requiredTitle);
    }

    private boolean typeMatches(final ObjectAdapter adapter) {
        if (!typeBinding.isFound()) {
            return true;
        }

        final ObjectSpecification spec = adapter.getSpecification();
        final String requiredTypeName = typeBinding.getCurrentCell().getText();
        final String specFullName = spec.getFullName();
        if (specFullName.equals(requiredTypeName)) {
            return true;
        }

        final String simpleSpecName = StringUtils.simpleName(specFullName);
        final String simpleRequiredType = StringUtils
                .simpleName(requiredTypeName);
        return simpleSpecName.equalsIgnoreCase(simpleRequiredType);
    }

}
