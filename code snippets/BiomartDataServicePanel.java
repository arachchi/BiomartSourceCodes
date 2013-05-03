package org.pathvisio.facets.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.bridgedb.DataSource;
import org.bridgedb.bio.Organism;
import org.pathvisio.facets.gui.ManageDataServicesDialog.DataServicesAddedPanel;
import org.pathvisio.facets.main.Utility;
import org.pathvisio.facets.model.BiomartDataService;
import org.pathvisio.facets.model.FacetManager;

/**
 * singleton dataservicepanel meant only for the biomartdataservice
 * @author jakefried
 *
 */
public class BiomartDataServicePanel extends AbstractDataServicePanel {
	private static BiomartDataServicePanel instance = null;
	private static final String datasetsListSite = "http://www.biomart.org/biomart/martservice?type=datasets&mart=ensembl";
	//private static JComboBox identifierDataSourceBox;
	private static BiomartDataService biomartDataService;
	private static boolean active = false;
	private static DataServicesAddedPanel fap;

	protected BiomartDataServicePanel(BiomartDataService d) {
		super(d);
		biomartDataService = d;
		inactivate();
		remove(boxOne);
		remove(boxTwo);
		serviceNameLabel.setText(biomartDataService.getServiceName());
		addActionListeners();
	}
	public void addActionListeners() {		
		btn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(Utility.getActivePathway() == null) {
					JOptionPane.showMessageDialog(BiomartDataServicePanel.this, "Pathway needs to be loaded in order to add a dataservice");
					return;
				}

				if(Utility.getCurrentOrganism() == null) {
					JOptionPane.showMessageDialog(BiomartDataServicePanel.this, "Organism must be set in order to do ID mapping");
					return;
				}

				boolean on = toggleActivate();
				if(on) {
					fap.addDataService(dataService);
					FacetManager.getInstance().addToReload(biomartDataService);

				}
				else {
					fap.removeDataService(dataService);
				}
			}
		}); 
	}

	/**
	 * This method changes the state of the dataserivice (active/inactive)
	 * @return false if biomart is turned off and true if it is turned on
	 */
	public boolean toggleActivate() {
		if(active) {
			inactivate();
		}
		else {
			activate();
		}
		return active;
	}
	/**
	 * Deactivates the DataService
	 */
	public void inactivate() {
		active = false;
		btn.setText("Off");
		repaint();
	}
	/**
	 * activates the DataService 
	 */
	private void activate() {
		biomartDataService.setIdentifierDataSource(getIdentifierDataSource());
		boolean successful = biomartDataService.populateAttributes();
		if( successful ) {
			active = true;
			btn.setText("On");
			FacetManager.getInstance().addToReload(biomartDataService);
			repaint();
		}
		else {
			//if there is no internet and you are trying to activate biomart
			JOptionPane.showMessageDialog(this, "Internet Required For Biomart Webservices");
		}
	}

	public boolean isActive() {
		return active;
	}

	public void setFAP(DataServicesAddedPanel fap) {
		this.fap = fap;
	}
	public BiomartDataService getBiomartDataService() {
		return biomartDataService ;
	}

	/**
	 * @return the singleton instance
	 */
	public static BiomartDataServicePanel getInstance(BiomartDataService d) {
		if(instance == null) {
			instance = new BiomartDataServicePanel(d);
		}
		return instance;
	}
	/**
	 * @return the singleton instance
	 */
	public static BiomartDataServicePanel getInstance() {
		return instance;
	}
	
	/**
	 * Try to figure out the Ensembl DataSource from the pathway
	 */
	@Override
	public DataSource getIdentifierDataSource() {
		Organism org = Utility.getFSP().getEngine().getCurrentOrganism();
		return DataSource.getByFullName("Ensembl " + org.shortName());
	}

}
