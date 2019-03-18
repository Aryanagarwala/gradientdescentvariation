import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
public class Handler { 
	static Connection conn;
	static Statement stmt;
	static Scanner sc = new Scanner(System.in);
	public static void main(String[] args) throws ClassNotFoundException, SQLException{
		System.out.println("Connecting to DataBase...");
		Class.forName("com.mysql.cj.jdbc.Driver");  
		conn=DriverManager.getConnection("jdbc:mysql://85.10.205.173:3306/linregbin","aryanagarwala","aryan123123");
		stmt = conn.createStatement();
		System.out.println("Connected!\n\nEnter 1 to use a previously created model or 2 to create a model");
		int ch = sc.nextInt();sc.nextLine();
		switch(ch){
		case 1: login();
			break;
		case 2: createprompt();
			break;
		}
	}
	public static void login() throws ClassNotFoundException, SQLException{
		System.out.println("Enter the name of the model.");
		String name = sc.nextLine();
		System.out.println("Enter the password.");
		String pass = sc.nextLine();
		Scanner sc = new Scanner(System.in);
		String query = "SELECT Password FROM Maintb WHERE Name = \"" + name + "\"";
		ResultSet rs = stmt.executeQuery(query);
		String passver = "";
		int exists = 0;
		while(rs.next()){
			exists++;
	         passver = rs.getString("Password");
	     }
		if(exists==0){
			System.out.println("Incorrect Name/Password.");
			login();
		}
		else{
			if(passver.equals(pass)){
				postlogin(name);
			}
			else{
				System.out.println("Incorrect Name/Password.");
				login();
			}
		}
	}
	public static void postlogin(String name) throws ClassNotFoundException, SQLException{
		String query = "SELECT Numdep FROM Maintb WHERE Name = \"" + name + "\"";
		ResultSet rs = stmt.executeQuery(query);
		rs.next();
		int m = rs.getInt("Numdep");
		System.out.println("Enter 1 to add a test case or 2 to predict a value.");
		int ch = sc.nextInt();sc.nextLine();
		switch(ch){
		case 1: Regressions.testadd(name, m, conn, stmt);
			break;
		case 2: Regressions.pred(name, conn, stmt);
			break;
		default: System.out.println("Please enter a valid option.");
			postlogin(name);
		}
	}
	public static void createprompt() throws SQLException, ClassNotFoundException{
		System.out.println("Enter the name of the model.");
		String name = sc.nextLine();
		//check if model with the current name already exists
		System.out.println("Create a password for this model");
		String password;
		while(true){
			password = sc.nextLine();
			if(password.length()<5){
				System.out.println("Password must be at least 5 characters long. Please enter a new password.");
			}
			else{
				break;
			}
		}
		System.out.println("Enter the number of independent variables.");
		int n;
		while(true){
			n = sc.nextInt();
			if(n<1){
				System.out.println("The number of independent variables must be at least one. Please re-enter.");
			}
			else{
				break;
			}
		}
		String vals = "1";
		for(int i = 0;i<n;i++){
			vals += " 1";
		}
		System.out.println("In the next n lines, specify how the dependent variable depends upon the ith independent variable.\nEnter 1 for linear model, 2 for quadratic model, 1/2 for square root model");
		String depnature = "1";
		for(int i = 0;i<n;i++){
			depnature += " " + sc.next();
		}
		System.out.println("Enter number of test cases.");
		int t = sc.nextInt();
		String query = "INSERT INTO Maintb(Name, Password, Numdep, Numtest, Naturedep, Vals) VALUES (\"" + name+ "\", \"" + password+ "\", \"" + n+ "\", \"" + t+ "\", \"" + depnature+ "\", \"" + vals+ "\");";
		System.out.println("Creating entry...");
		stmt.executeUpdate(query);
	    query = "CREATE TABLE " + name + "( ";
	    for(int i = 1;i<=(n+1);i++){
	    	query+="x" + i + " DOUBLE, ";
	    }
	    query+="y" + " DOUBLE)";
	    stmt.executeUpdate(query);
	    Regressions.init(t, n, name, conn, stmt);
	    System.out.println("Completed entry and processing.");
	}
}
