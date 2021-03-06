package controller;

import model.*;
import view.*;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;

public class WarehouseController
{
	private InventoryController inventoryController;
	private Warehouse warehouse;
	private OperatorUI operatorUI;	
	private ButtonHandler buttonHandler;
	private ModelObserver modelObserver;
			
	public WarehouseController()
	{
		this.inventoryController = new InventoryController();
		this.warehouse = new Warehouse();
		this.operatorUI = new OperatorUI();
		this.buttonHandler = new ButtonHandler(this);
		this.modelObserver = new ModelObserver(this);
	}
	
	public InventoryController getInventoryController()
	{
		return this.inventoryController;
	}
	public Warehouse getWarehouse()
	{
		return this.warehouse;
	}
	public OperatorUI getOperatorUI()
	{
		return this.operatorUI;
	}	
	public ButtonHandler getButtonHandler()
	{
		return this.buttonHandler;
	}
	public ModelObserver getModelObserver()
	{
		return this.modelObserver;
	}
	public void init()
	{
		// Code to initialize state of the warehouse and establish listeners
		this.inventoryController.init();
		this.buttonHandler.attachListeners();		
		this.warehouse.initWarehouse();
		this.modelObserver.addObserver();
				
		// Code to add eventlister to the orderSim 
	}
	public void handleOrder(Product product, int qty)
	{
		// Display the incoming order on the UI
		this.getOperatorUI().getOrderedProductLabel().setText(product.getProdID() + "      " + qty);
		
		// Select the appropriate RetBot
		RetBot retbot = this.warehouse.assignRetBot();
		
		// Find the pallet with the requested products
		Pallet pallet = this.warehouse.findPallet(product);

		// Command RetBot to "retrieve product" asynchronously
		new Thread(new Runnable() {
		    public void run() {
		    	retbot.retrieveProduct(pallet, qty);
		    }
		}).start();	
		
		// TODO:  Code to adjust the database	
		this.inventoryController.removeFromInventory("inventory", product.getProdID(), qty);
	}
	public void handleScanPallet(Product product, int qty, String location)
	{
		Pallet pallet = new Pallet(product, qty, location);
		this.warehouse.addPallet(pallet);
		pallet.addObserver(this.modelObserver);
		pallet.indicateChange();
		this.inventoryController.addNewItem("inventory", product.getProdID(), 
											product.getProdType(), qty);
	}
	public void handleMovePallet(Pallet pallet, String locB)
	{
		// Select the appropriate Forklift
		Forklift forklift = this.warehouse.assignForklift();
		
		// Command Forklift to perform the "move pallet" task asynchronously
		new Thread(new Runnable() {
		    public void run() {
		    	forklift.movePallet(pallet, locB);
		    }
		}).start();	
		
		// TODO:  Add code to adjust database	
		this.inventoryController.addToInventory("inventory", pallet.getComposedOf().getProdID(),
												pallet.getQty());
	}
	public int handleCancelMove(Forklift forklift)
	{
		// TODO
		return 1;		
	}
	public void handleDisableRobot(Robot robot)
	{
		if (robot.getFunctional()==true) robot.setFunctional(false);
		else robot.setFunctional(true);		
	}
	
}
