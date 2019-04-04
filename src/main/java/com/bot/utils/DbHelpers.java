package com.bot.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbHelpers {

    public static void close(PreparedStatement preparedStatement, ResultSet resultSet, Connection connection) {
        try {
            if (connection != null)
                connection.close();
            if (preparedStatement != null)
                preparedStatement.close();
            if (resultSet != null)
                resultSet.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}