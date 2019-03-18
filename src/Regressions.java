import java.util.Scanner;
import java.sql.*;
public class Regressions{
	public static void testadd(String name, int m, Connection conn, Statement stmt) throws SQLException{
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter the values for the independent variables followed by the value of the dependent variable.");
		double x[] = new double[m+1];
		double y;
		x[0] = 1;
		for(int i = 1;i<x.length;i++){
			x[i] = sc.nextDouble();
		}
		y = sc.nextDouble();
		String inq = "";
		for(int i = 1;i<=(m+1);i++){
	    	inq+="x" + i + ", ";
	    }
		String xpush = x[0] + "";
		for(int j = 1;j<x.length;j++){
			xpush += ", " + x[j];
		}
		String query = "INSERT INTO " + name + "(" + inq + "y) VALUES (" + xpush + ", " + y + ");";
		stmt.executeUpdate(query);
		query = "UPDATE Maintb SET Numtest = Numtest+1 WHERE Name = \"" + name + "\"";
		stmt.executeUpdate(query);
		recalculate(name, conn, stmt);
		System.out.println("Added test case.");
		
	}
	public static void pred(String name, Connection conn, Statement stmt)throws SQLException{
		Scanner sc = new Scanner(System.in);
		String query = "SELECT Vals FROM Maintb WHERE Name = \"" + name + "\"";
		ResultSet rs = stmt.executeQuery(query);
		while(rs.next()){
	         query = rs.getString("Vals");
	     }
		String[] v1 = query.split("\\s");
		double[] vals = new double[v1.length];
		for(int i = 0;i<vals.length;i++){
			vals[i] = Double.parseDouble(v1[i]);
		}
		double x[] = new double[vals.length];
		x[0] = 1;
		System.out.println("Enter the independent variables separated by a space.");
		for(int i = 1;i<vals.length;i++){
			x[i] = sc.nextDouble();
		}
		double y = predictor(x, vals);
		System.out.println("The prediction for the dependent variable is " + y);
	}
	public static void recalculate(String name, Connection conn, Statement stmt) throws SQLException{
		String query = "SELECT Vals,Numtest FROM Maintb WHERE Name = \"" + name + "\"";
		ResultSet rs = stmt.executeQuery(query);
		int n = 0;
		while(rs.next()){
	         query = rs.getString("Vals");
	         n = rs.getInt("Numtest");
	    }
		String[] v1 = query.split("\\s");
		double[] vals = new double[v1.length];
		query = "SELECT * FROM " + name;
		rs = stmt.executeQuery(query);
		double tests[][] = new double[n][vals.length];
		for(int i = 0;i<vals.length;i++){
			vals[i] = Double.parseDouble(v1[i]);
		}
		int m = vals.length;
		double y[] = new double[n];
		for(int i = 0;i<n;i++){
			rs.next();
			for(int j = 0;j<m;j++){
				tests[i][j] = rs.getDouble(j+1);
			}
			y[i] = rs.getDouble(m+1);
		}
		double cost = costfunction(y, vals, tests, n);
		vals = gradientdescent(vals, n, tests, y, 1, cost);
		String valstopush = "" + vals[0];
		for(int i = 1;i<vals.length;i++){
			valstopush+=" " + vals[i];
		}
		query = "UPDATE Maintb SET Vals = \"" + valstopush + "\" WHERE Name = \"" + name + "\";";
		stmt.executeUpdate(query);
		
	}
	public static void init(int n, int m, String name, Connection conn, Statement stmt) throws SQLException, ClassNotFoundException{
		Scanner sc = new Scanner(System.in);
		double tests[][] = new double[n][m+1];
		double y[] = new double[n];
		System.out.println("On each line, enter the value of the independent variables for that test case followed by the value of the dependent variable.");
		for(int i = 0;i<n;i++){
			tests[i][0] = 1;
			for(int j = 1;j<=m;j++){
				tests[i][j]= sc.nextDouble();
			}
			y[i] = sc.nextDouble();
		}
		double vals[] = new double[m+1];
		for(int j = 0;j<=m;j++){
			vals[j] = 1;
		}
		double cost = costfunction(y, vals, tests, n);
		vals = gradientdescent(vals, n, tests, y, 1, cost);
		String valstopush = "" + vals[0];
		for(int i = 1;i<vals.length;i++){
			valstopush+=" " + vals[i];
		}
		String inq = "";
		for(int i = 1;i<=(m+1);i++){
	    	inq+="x" + i + ", ";
	    }
		for(int i = 0;i<n;i++){
			String xpush = tests[i][0] + "";
			for(int j = 1;j<tests[i].length;j++){
				xpush += ", " + tests[i][j];
			}
			String query = "INSERT INTO " + name + "(" + inq + "y) VALUES (" + xpush + ", " + y[i] + ");";
			stmt.executeUpdate(query);
		}
		String query = "UPDATE Maintb SET Vals = \"" + valstopush + "\" WHERE Name = \"" + name + "\";";
		stmt.executeUpdate(query);
	}
	public static double predictor(double[] x, double[] vals){
	    double y = 0;
	    for(int i = 0;i<x.length;i++){
	        y+=(x[i] * vals[i]);
	    }
	    return y;
	}
	public static double costfunction(double y[], double vals[], double tests[][], int n){
	    double cost = 0;
	    for(int i = 0;i<n;i++){
	        cost+=Math.pow((predictor(tests[i], vals) - y[i]), 2);
	    }
	    cost/=(2*n);
	    return cost;
	}
	public static double derivative(int n, double tests[][], double y[], double vals[], int ind){
	    double answer = 0;
	    for(int i = 0;i<n;i++){
	        answer+=(predictor(tests[i], vals)-y[i])*tests[i][ind];
	    }
	    answer/=n;
	    return answer;
	}
	public static double[] gradientdescent(double[] vals, int n, double tests[][], double y[], double alpha, double initcost){
		double mod[] = new double[vals.length];
		double cost = 0;
		do{
			for(int i = 0;i<tests[0].length;i++){
				mod[i] = vals[i] - (alpha * derivative(n, tests, y, vals, i));
			}
			cost = costfunction(y, mod, tests, n);
			if(cost>initcost){
				alpha/=2;
				continue;
			}
			if(initcost-cost<=(0.0000001*initcost)){
				break;
			}
			else{
				for(int i = 0;i<tests[0].length;i++){
					vals[i] = mod[i];
				}
				initcost = cost;
				continue;
			}
		} while(true);
		return mod;
	}
} 