package me.spywhere.MFSSample;

import java.util.logging.Logger;

import lib.spywhere.MFS.DataType;
import lib.spywhere.MFS.Database;
import lib.spywhere.MFS.Field;
import lib.spywhere.MFS.MFS;
import lib.spywhere.MFS.Record;
import lib.spywhere.MFS.Result;
import lib.spywhere.MFS.StorageType;
import lib.spywhere.MFS.Table;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class MFSSample extends JavaPlugin{ 
	private Logger log=Logger.getLogger("Minecraft");
	private PluginDescriptionFile pdf=null;
	MFS mfs=null;

	public void onEnable() {
		pdf=this.getDescription();

		if(MFSConnector.prepareMFS(pdf,this.getServer().getPluginManager())){
			//MFS prepared and can be connected now
			//Connect as MySQL
			//   mfs = MFSConnector.getMFS(this.getServer().getPluginManager(), "localhost:8889","root","root",StorageType.MYSQL);
			//Connect as PostgreSQL
			//   mfs = MFSConnector.getMFS(this.getServer().getPluginManager(), "localhost:8889","root","root",StorageType.POSTGRE);
			//Connect as H2
			//   mfs = MFSConnector.getMFS(this.getServer().getPluginManager(), "localhost","sa","",StorageType.H2);
			//Connect as SQLite
			   mfs = MFSConnector.getMFS(this.getServer().getPluginManager(), StorageType.SQLITE);
			//Connect as YML
			//mfs = MFSConnector.getMFS(this.getServer().getPluginManager(), StorageType.YML);
			//Connect as FlatFile
			//   mfs = MFSConnector.getMFS(this.getServer().getPluginManager(), StorageType.FLATFILE);
			log.info("["+pdf.getName()+"] MFS found and connected.");
		}else{
			//MFS failed to download/install/run
			log.severe("["+pdf.getName()+"] Failed to run MFS. Plugin now disabled.");
			getServer().getPluginManager().disablePlugin(getServer().getPluginManager().getPlugin(pdf.getName()));
			return;
		}
		log.info("["+pdf.getName()+"] v"+pdf.getVersion()+" successfully enabled.");
	}

	public void onDisable() {
		log.info("["+pdf.getName()+"] v"+pdf.getVersion()+" successfully disabled.");
	}

	private boolean isNumber(String str)
	{
		return str.matches("-?\\d+(.\\d+)?");
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(sender.isOp()){
			if(args.length==1){
				if(args[0].equalsIgnoreCase("demo")){
					Database db=mfs.getDB("Shop");
					if(db!=null){
						Table tbl=db.getTable("Cart");
						if(tbl!=null){
							if(tbl.addFieldAfter(new Field("TestField",DataType.String),new Field("Price"))){
								sender.sendMessage(ChatColor.GREEN+"Done");
								return true;
							}
						}
					}
					sender.sendMessage(ChatColor.GREEN+"Failed");
					return true;
				}
				if(args[0].equalsIgnoreCase("demo2")){
					Database db=mfs.getDB("Shop");
					if(db!=null){
						Table tbl=db.getTable("Cart");
						if(tbl!=null){
							if(tbl.removeField(new Field("TestField"))){
								sender.sendMessage(ChatColor.GREEN+"Done");
								return true;
							}
						}
					}
					sender.sendMessage(ChatColor.GREEN+"Failed");
					return true;
				}
			}
			if(args.length==2){
				if(args[0].equalsIgnoreCase("view")){
					//Command:
					//   /cart view [CustomerName]
					//

					//Get database name "Shop"
					Database db=mfs.getDB("Shop");
					//If database exist
					if(db!=null){
						//Get table name "Cart"
						Table tbl=db.getTable("Cart");
						//If table exist
						if(tbl!=null){
							//Select all from table where Customer=args[1]
							Result result = tbl.filterRecord("Customer", args[1]);
							sender.sendMessage(ChatColor.AQUA+"Show all items in "+args[1]+"'s cart: ");
							sender.sendMessage(ChatColor.AQUA+"ID : Item Name : Amount : Price/Each : Total Price");
							int sumprice=0;
							int sumitem=0;
							//If result is not empty
							if(result.totalRecord()>0){
								//Loop each record
								for(int i=0;i<result.totalRecord();i++){
									Record record = result.getRecord(i);
									int totalprice=(Integer.parseInt(record.getData(new Field("Price")))*Integer.parseInt(record.getData(new Field("Amount"))));
									sumprice+=totalprice;
									sumitem+=Integer.parseInt(record.getData(new Field("Amount")));
									sender.sendMessage(ChatColor.AQUA+record.getData(new Field("ID"))+" : "+record.getData(new Field("Customer"))+" : "+record.getData(new Field("Item"))+" : "+record.getData(new Field("Price"))+" : "+totalprice);
								}
								sender.sendMessage(ChatColor.AQUA+"Total Price: "+sumprice);
								sender.sendMessage(ChatColor.AQUA+"Total Item: "+sumitem);
								sender.sendMessage(ChatColor.AQUA+"================");
							}else{
								sender.sendMessage(ChatColor.AQUA+"No item in "+args[1]+"'s cart.");
							}
							return true;
						}else{
							sender.sendMessage(ChatColor.AQUA+"No item in "+args[1]+"'s cart.");
							return true;
						}
					}else{
						sender.sendMessage(ChatColor.AQUA+"No item in "+args[1]+"'s cart.");
						return true;
					}
				}
			}
			if(args.length==5){
				if(args[0].equalsIgnoreCase("add")){
					//Command:
					//   /cart add [CustomerName] [ItemName] [Price] [Amount]
					//
					if(isNumber(args[3])&&isNumber(args[4])){
						//Create new database called "Shop"
						Database db=mfs.createNewDB("Shop");
						//If a new database is exist
						if(db==null){
							//Load it
							db=mfs.getDB("Shop");
						}
						//Create new table called "Cart" and have "Customer, Item, Price, Amount" as a field
						Table tbl = db.createNewTable("Cart",new Field("ID",DataType.Integer),new Field("Customer",DataType.String),new Field("Item",DataType.String),new Field("Price",DataType.Integer),new Field("Amount",DataType.Integer));
						//If a new table is exist
						if(tbl==null){
							//Load it
							tbl=db.getTable("Cart");
						}
						//Add a new record (item) into Cart
						int amount = Integer.parseInt(args[4]);
						//Select all from table where Customer=args[1] and Item=args[2] and Price=args[3]
						Result existitem = tbl.filterRecord("Customer", args[1]).filterBy("Item", args[2]).filterBy("Price", args[3]);
						//If result is not empty
						if(existitem.totalRecord()>0){
							amount=Integer.parseInt(existitem.getRecord(0).getData(3));
							amount+=Integer.parseInt(args[4]);
							//Update Amount=amount to  all record which select from table where Customer=args[1] and Item=args[2] and Price=args[3]
							tbl.updateRecords(tbl.filterRecord("Customer", args[1]).filterBy("Item", args[2]).filterBy("Price", args[3]), "Amount", Integer.toString(amount));
							sender.sendMessage(ChatColor.AQUA+"Item in cart updated.");
						}else{
							//Add new record
							//   Customer | Item | Price | Amount
							//   args[1] | args[2] | args[3] | args[4]
							//
							tbl.addRecord(tbl.autoIncrement(new Field("ID")),args[1],args[2],args[3],Integer.toString(amount));
							sender.sendMessage(ChatColor.AQUA+"Item added to cart.");
						}
						return true;
					}else{
						sender.sendMessage(ChatColor.YELLOW+"/cart add [CustomerName] [ItemName] [Price] [Amount]");
						sender.sendMessage(ChatColor.YELLOW+"/cart add [Text/String] [Text/String] [Number] [Number]");
						return true;
					}
				}
			}
		}
		return false;
	}
}