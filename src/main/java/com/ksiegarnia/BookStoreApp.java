package com.ksiegarnia;

import java.sql.*;


public class BookStoreApp
{
    public static void main( String[] args ) throws ClassNotFoundException, SQLException
    {
        Class.forName("com.mysql.cj.jdbc.Driver");
        new BookStoreFrame();
    }
}
