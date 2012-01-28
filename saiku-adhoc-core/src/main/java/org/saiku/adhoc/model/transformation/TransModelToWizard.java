/*
 * Copyright (C) 2011 Marius Giepz
 *
 * This program is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 2 of the License, or (at your option) 
 * any later version.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along 
 * with this program; if not, write to the Free Software Foundation, Inc., 
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
 *
 */

package org.saiku.adhoc.model.transformation;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.metadata.model.LogicalColumn;
import org.pentaho.metadata.model.LogicalModel;
import org.pentaho.reporting.engine.classic.core.function.CountDistinctFunction;
import org.pentaho.reporting.engine.classic.wizard.model.DefaultDetailFieldDefinition;
import org.pentaho.reporting.engine.classic.wizard.model.DefaultGroupDefinition;
import org.pentaho.reporting.engine.classic.wizard.model.DefaultWizardSpecification;
import org.pentaho.reporting.engine.classic.wizard.model.DetailFieldDefinition;
import org.pentaho.reporting.engine.classic.wizard.model.GroupDefinition;
import org.pentaho.reporting.engine.classic.wizard.model.GroupType;
import org.pentaho.reporting.engine.classic.wizard.model.Length;
import org.pentaho.reporting.engine.classic.wizard.model.LengthUnit;
import org.pentaho.reporting.engine.classic.wizard.model.WizardSpecification;
import org.saiku.adhoc.exceptions.ReportException;
import org.saiku.adhoc.model.master.SaikuColumn;
import org.saiku.adhoc.model.master.SaikuGroup;
import org.saiku.adhoc.model.master.SaikuMasterModel;
import org.saiku.adhoc.rest.MetadataDiscoverResource;
import org.saiku.adhoc.utils.TemplateUtils;


public class TransModelToWizard {
	
    private static final Log log = LogFactory.getLog(TransModelToWizard.class);

	public WizardSpecification doIt(SaikuMasterModel smm) throws ReportException {
		
		WizardSpecification wizardSpec = new DefaultWizardSpecification();		
		wizardSpec.setAutoGenerateDetails(false);
		
		//Domain domain = smm.getDerivedModels().getDomain();
		LogicalModel model = smm.getDerivedModels().getLogicalModel();

		String locale = "en_En";

		List<SaikuColumn> columns = smm.getColumns();
		final int columnCount = columns.size();		
		final DetailFieldDefinition[] detailFields = new DetailFieldDefinition[columnCount];
			
		//Add the columns
		int i = 0;
		Double widthCumul = Double.valueOf(0);
		for (SaikuColumn saikuColumn : columns) {
			
			//LogicalColumn column = model.findLogicalColumn(saikuColumn.getId());
			//String name = column!=null ? column.getName(locale) : saikuColumn.getName();	
			String name = saikuColumn.getName();
			
			DefaultDetailFieldDefinition detailFieldDef = new DefaultDetailFieldDefinition(name);		

			detailFieldDef.setDisplayName(name);
			//ElementAlignmentValueConverter converter = new ElementAlignmentValueConverter();
	
			detailFieldDef.setAggregationFunction(TemplateUtils.strToAggfunctionClass(saikuColumn.getSelectedSummaryType()));
	
			//TODO: Wann ist die breite hier 0?
			Double colWidth = saikuColumn.getColumnHeaderFormat().getWidth();
			if(colWidth!=null){		
				
				if(columns.indexOf(saikuColumn) == columnCount -1){
				//	colWidth = Double.valueOf(100) - widthCumul;
				}
				Length width = new Length(LengthUnit.PERCENTAGE,colWidth);
				log.info("col["+i+"]:" + colWidth);
				detailFieldDef.setWidth(width);				
				widthCumul+=colWidth;
			}

			detailFieldDef.setDataFormat(saikuColumn.getFormatMask());

			detailFields[i] = detailFieldDef;
			i++;			
		}
		
		//Add the groups
		final List<GroupDefinition> groupDefs = new ArrayList<GroupDefinition>();

		List<SaikuGroup> sGroups = smm.getGroups();
		
		//TODO: This needs to be externalized
		final Class<CountDistinctFunction> aggFunctionClass = CountDistinctFunction.class;
		
		for (SaikuGroup saikuGroup : sGroups) {
			final GroupDefinition def = new DefaultGroupDefinition(GroupType.RELATIONAL, saikuGroup.getColumnName());			
			def.setAggregationFunction(aggFunctionClass);
			def.setDisplayName(saikuGroup.getColumnName());
			def.setGroupTotalsLabel(saikuGroup.getGroupTotalsLabel());
			groupDefs.add(def);
		}

		wizardSpec.setDetailFieldDefinitions(detailFields);	
		GroupDefinition[] groups = groupDefs.toArray(new GroupDefinition[groupDefs.size()]);
		wizardSpec.setGroupDefinitions(groups);
		
		return wizardSpec;
	}

}
