//package programElements;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//
//public class GetConnectionToMySQLDatabase {
//	public void getConnection() throws ClassNotFoundException, SQLException {
//		Class.forName("com.mysql.jdbc.Driver");
//		String un = "root";
//		String ps = "root";
//		Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/work",un,ps);
//		System.out.println("connection done");
//		connection.close();
//		System.out.println("connection is closed");
//	}
//	public static void main(String[] args) throws Throwable  {
//		GetConnectionToMySQLDatabase g = new GetConnectionToMySQLDatabase();
//		g.getConnection();
//	}
//}
package programElements;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class GetConnectionToMySQLDatabase {
	public void getConnection(String un,String ps,String DbURL,String tableName) throws ClassNotFoundException, SQLException {
		Class.forName("com.mysql.jdbc.Driver");
		Connection connection = DriverManager.getConnection(DbURL+tableName,un,ps);
		System.out.println("connection done");
		connection.close();
		System.out.println("connection is closed");
	}
	public static void main(String[] args) throws Throwable  {
		GetConnectionToMySQLDatabase g = new GetConnectionToMySQLDatabase();
		String DbURL = "jdbc:mysql://localhost:3306/";
		String tableName ="work";
		String un = "root";
		String ps = "root";
		g.getConnection(un,ps,DbURL,tableName);
	}
} 
