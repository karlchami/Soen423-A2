package StoreApp;


/**
* StoreApp/DSMSOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from storeApp.idl
* Friday, October 23, 2020 4:50:23 o'clock PM EDT
*/

public interface DSMSOperations 
{
  boolean addItem (String managerID, String itemID, String itemName, int quantity, long price);
  boolean removeItem (String managerID, String itemID, int quantity);
  String listItemAvailability (String managerID);
  String purchaseItem (String customerID, String itemID, String dateOfPurchase);
  String findItem (String customerID, String itemName);
  boolean returnItem (String customerID, String itemID, String dateOfReturn);
  boolean exchangeItem (String customerID, String newitemID, String oldItemID, String dateOfExchange);
  void addCustomerWaitList (String customerID, String itemID);
  void addCustomer (String customerID);
  void addManager (String managerID);
  void shutdown ();
} // interface DSMSOperations
