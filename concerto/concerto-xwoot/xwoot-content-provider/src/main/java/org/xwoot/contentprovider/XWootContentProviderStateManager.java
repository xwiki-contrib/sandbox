/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwoot.contentprovider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class XWootContentProviderStateManager
{
    final Log logger = LogFactory.getLog(XWootContentProviderStateManager.class);

    private Connection connection;

    private static final String DB_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    private static final String DB_PROTOCOL = "jdbc:derby:";

    public XWootContentProviderStateManager(String dbName, boolean createDB) throws Exception
    {
        Thread.currentThread().getContextClassLoader().loadClass(DB_DRIVER).newInstance();
        // FIXME: define a working directory and set the database to that absolute path.
        this.connection = DriverManager.getConnection(String.format("%s%s;create=true", DB_PROTOCOL, dbName));
        this.connection.setAutoCommit(true);

        Statement s = this.connection.createStatement();

        if (createDB) {
            this.logger.info("Dropping modifications table.");
            try {
                s.execute("DROP TABLE modifications");
            } catch (SQLException e) {
                /* Table doesn't exist */
                if (e.getErrorCode() != 30000) {
                    throw e;
                }
            }

            this.logger.info("Dropping last cleared table.");
            try {
                s.execute("DROP TABLE lastCleared");
            } catch (SQLException e) {
                /* Table doesn't exist */
                if (e.getErrorCode() != 30000) {
                    throw e;
                }
            }
        }

        try {
            s
                .execute("CREATE TABLE modifications (pageId VARCHAR(64), timestamp BIGINT, version INT, minorVersion INT, cleared SMALLINT DEFAULT 0, PRIMARY KEY(pageId, timestamp, version, minorVersion), UNIQUE(pageId, timestamp))");

            this.logger.info("Modifications table created.");
        } catch (SQLException e) {
            /* Table already exists */
            if (e.getErrorCode() != 30000) {
                throw e;
            }

            this.logger.info("Modifications table already exists.");
        }

        try {
            s
                .execute("CREATE TABLE lastCleared (pageId VARCHAR(64), timestamp BIGINT, version INT, minorVersion INT, PRIMARY KEY(pageId))");

            this.logger.info("Last cleared table created.");
        } catch (SQLException e) {
            /* Table already exists */
            if (e.getErrorCode() != 30000) {
                throw e;
            }

            this.logger.info("Last cleared table already exists.");
        }

        s.close();
    }

    public void dispose()
    {
        try {
            DriverManager.getConnection("jdbc:derby:;shutdown=true");
        } catch (SQLException e) {
        }

        logger.info("Shutdown");
    }

    /**
     * Add an XWootId into the modification table.
     * 
     * @param xwootId The XWootId to be added.
     * @param cleared true if the cleared flag should be set.
     * @return True if successfully inserted. False if a duplicate exist.
     * @throws Exception On error.
     */
    public boolean addModification(XWootId xwootId, boolean cleared) throws Exception
    {
        PreparedStatement ps = connection.prepareStatement("INSERT INTO modifications VALUES (?, ?, ?, ?, ?)");
        ps.setString(1, xwootId.getPageId());
        ps.setLong(2, xwootId.getTimestamp());
        ps.setInt(3, xwootId.getVersion());
        ps.setInt(4, xwootId.getMinorVersion());
        ps.setBoolean(5, cleared);

        try {
            ps.executeUpdate();
        } catch (SQLException e) {
            /* Ignore duplicated entries */
            if (e.getErrorCode() != 30000) {
                throw e;
            }

            /* logger.info(String.format("Modification %s already present in modification list.", xwootId)); */

            /* Return false on duplicated entry */
            return false;
        }

        return true;
    }

    /*
     * Equivalent to addModification(xwootId, false)
     */
    public boolean addModification(XWootId xwootId) throws Exception
    {
        return addModification(xwootId, false);
    }

    public long getHighestModificationTimestamp() throws Exception
    {
        Statement s = connection.createStatement();
        ResultSet rs = s.executeQuery("SELECT MAX(timestamp) FROM modifications");

        long maxTimestamp = 0;
        if (rs.next()) {
            maxTimestamp = rs.getLong(1);
        }

        s.close();

        return maxTimestamp;
    }

    /**
     * Get a list of XWootId corresponding to the modifications stored in the database where the cleared flag is equal
     * to the passed parameter.
     * 
     * @param pageId
     * @param cleared
     * @return
     * @throws SQLException
     */
    public List<XWootId> getModificationsFor(String pageId, boolean cleared) throws SQLException
    {
        List<XWootId> result = new ArrayList<XWootId>();

        PreparedStatement ps = connection.prepareStatement("SELECT * FROM modifications WHERE pageId=? AND cleared=?");
        ps.setString(1, pageId);
        ps.setBoolean(2, cleared);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            XWootId xwootId = new XWootId(rs.getString(1), rs.getLong(2), rs.getInt(3), rs.getInt(4));
            result.add(xwootId);
        }

        return result;
    }

    /**
     * Get last modification cleared with the clearModification method
     * 
     * @param pageId The page id referring to the modification to be retrieved
     * @return An XWootId representing the modification.
     * @throws SQLException
     */
    public XWootId getLastCleared(String pageId) throws SQLException
    {
        XWootId result = null;

        PreparedStatement ps = connection.prepareStatement("SELECT * FROM lastCleared WHERE pageId=?");

        ps.setString(1, pageId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            result = new XWootId(rs.getString(1), rs.getLong(2), rs.getInt(3), rs.getInt(4));
        }

        return result;
    }

    /**
     * Clears all the modifications except the one identified by the parameter.
     * 
     * @param xwootId An XWootId representing a modification.
     * @throws Exception
     */
    public void clearAllModificationExcept(XWootId xwootId) throws Exception
    {
        PreparedStatement ps =
            connection.prepareStatement("UPDATE modifications SET cleared=1 WHERE pageId=? AND timestamp<>?");
        ps.setString(1, xwootId.getPageId());
        ps.setLong(2, xwootId.getTimestamp());

        int rowsUpdated = ps.executeUpdate();

        /*
         * logger.info(String.format("Cleared all pages '%s' with timestamp different from at %d. %d rows updated",
         * xwootId.getPageId(), xwootId.getTimestamp(), rowsUpdated));
         */

        ps.close();
    }

    /**
     * Clear the modification represented by the xwootId passed as parameter and updates the last cleared modification.
     * If the modification doesn't already exist in the database, it is created.
     * 
     * @param xwootId The xwootId representing the modification to be cleared.
     * @throws Exception
     */
    public void clearModification(XWootId xwootId) throws Exception
    {
        /* Try to clear the line if it exists */
        PreparedStatement ps =
            connection.prepareStatement("UPDATE modifications SET cleared=1 WHERE pageId=? AND timestamp=?");
        ps.setString(1, xwootId.getPageId());
        ps.setLong(2, xwootId.getTimestamp());

        int rowsUpdated = ps.executeUpdate();

        ps.close();

        if (rowsUpdated > 0) {
            logger.info(String.format("%s cleared. %d rows updated", xwootId.getPageId(), rowsUpdated));

        } else {
            /*
             * If the entry doesn't exist then insert it and mark it as cleared so the next time it will not be returned
             * in the modification list
             */
            ps = connection.prepareStatement("INSERT INTO modifications VALUES (?, ?, ?, ?, 1)");

            ps.setString(1, xwootId.getPageId());
            ps.setLong(2, xwootId.getTimestamp());
            ps.setInt(3, xwootId.getVersion());
            ps.setInt(4, xwootId.getMinorVersion());
            ps.executeUpdate();

            ps.close();

            logger.info(String.format("%s inserted and cleared.", xwootId));
        }

        /* Update the last cleared table */

        ps =
            connection.prepareStatement("UPDATE lastCleared SET timestamp=?, version=?, minorVersion=? WHERE pageId=?");
        ps.setLong(1, xwootId.getTimestamp());
        ps.setInt(2, xwootId.getVersion());
        ps.setInt(3, xwootId.getMinorVersion());
        ps.setString(4, xwootId.getPageId());

        rowsUpdated = ps.executeUpdate();

        ps.close();

        if (rowsUpdated > 0) {
            logger.info(String.format("%s set to last cleared. %d rows updated", xwootId, rowsUpdated));

        } else {
            /*
             * If the entry doesn't exist then insert it and mark it as cleared so the next time it will not be returned
             * in the modification list
             */
            ps = connection.prepareStatement("INSERT INTO lastCleared VALUES (?, ?, ?, ?)");

            ps.setString(1, xwootId.getPageId());
            ps.setLong(2, xwootId.getTimestamp());
            ps.setInt(3, xwootId.getVersion());
            ps.setInt(4, xwootId.getMinorVersion());
            ps.executeUpdate();

            ps.close();

            logger.info(String.format("%s inserted and set to last cleared.", xwootId));
        }
    }

    private XWootId getModificationByPageIdAndTimestamp(String pageId, long timestamp)
    {
        XWootId result = null;
        try {
            PreparedStatement ps =
                connection.prepareStatement("SELECT * FROM modifications WHERE pageId=? AND timestamp=?");
            ps.setString(1, pageId);
            ps.setLong(2, timestamp);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result = new XWootId(rs.getString(1), rs.getLong(2), rs.getInt(3), rs.getInt(4));
            }

            ps.close();
        } catch (SQLException e) {
        }

        return result;
    }

    /**
     * Returns a set of xwoot ids representing, for each page id, the modification with the lowest timestamp that has
     * not been cleared.
     * 
     * @return
     * @throws Exception
     */
    public Set<XWootId> getNonClearedModificationsWithLowestTimestamp() throws Exception
    {
        Set<XWootId> result = new TreeSet<XWootId>(new XWootIdComparatorAscending());

        PreparedStatement ps =
            connection
                .prepareStatement("SELECT pageId, MIN(timestamp) FROM modifications WHERE cleared=0 GROUP BY pageId");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            XWootId xwootId = getModificationByPageIdAndTimestamp(rs.getString(1), rs.getLong(2));

            if (xwootId != null) {
                result.add(xwootId);
            } else {
                logger.warn(String.format("Unable to retrieve XWootId for (%s, %d)", rs.getString(1), rs.getLong(2)));
            }
        }

        ps.close();

        return result;
    }

    public List<XWootId> getModificationsInRange(String pageId, long lowestTimestamp, long highestTimestamp)
        throws Exception
    {
        List<XWootId> result = new ArrayList<XWootId>();

        PreparedStatement ps =
            connection
                .prepareStatement("SELECT * FROM modifications WHERE pageId=? AND timestamp >= ? AND timestamp <= ? ORDER BY timestamp ASC");
        ps.setString(1, pageId);
        ps.setLong(2, lowestTimestamp);
        ps.setLong(3, highestTimestamp);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            XWootId xwootId = new XWootId(rs.getString(1), rs.getLong(2), rs.getInt(3), rs.getInt(4));
            result.add(xwootId);
        }

        ps.close();

        return result;
    }

    public XWootId getPreviousModification(XWootId xwootId) throws Exception
    {
        XWootId result = null;

        PreparedStatement ps =
            connection
                .prepareStatement("SELECT * FROM modifications WHERE pageId=? AND timestamp < ? ORDER BY timestamp DESC");
        ps.setString(1, xwootId.getPageId());
        ps.setLong(2, xwootId.getTimestamp());

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            result = new XWootId(rs.getString(1), rs.getLong(2), rs.getInt(3), rs.getInt(4));
        }

        return result;
    }

    public void dumpDbLines()
    {
        dumpDbLines(null, null, -1);
    }

    public void dumpDbLines(String message, String pageId, int n)
    {
        try {
            PreparedStatement ps = null;

            if (pageId != null) {
                ps = connection.prepareStatement("SELECT * FROM modifications WHERE pageId=? ORDER by timestamp DESC");
                ps.setString(1, pageId);
            } else {
                ps = connection.prepareStatement("SELECT * FROM modifications ORDER by timestamp DESC");
            }

            ResultSet rs = ps.executeQuery();

            System.out.format("Database dump for Page ID: '%s'\n", pageId != null ? pageId : "ANY");
            if (message != null) {
                System.out.format("%s\n", message);
            }
            System.out.format("-----------------------------------------\n");

            int i = 0;
            while (rs.next()) {
                System.out.format("%-30s\t| %d\t| %d.%d\t| %d |\n", rs.getString(1), rs.getLong(2), rs.getInt(3), rs
                    .getInt(4), rs.getShort(5));
                i++;
                if (n > 0 && i >= n) {
                    break;
                }
            }

            ps.close();
            System.out.format("-----------------------------------------\n");

            ps = connection.prepareStatement("SELECT * FROM lastCleared ORDER by pageId ASC");
            rs = ps.executeQuery();
            while (rs.next()) {
                System.out.format("%-30s\t| %d\t| %d.%d |\n", rs.getString(1), rs.getLong(2), rs.getInt(3), rs
                    .getInt(4));
            }

            ps.close();
            System.out.format("-----------------------------------------\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearAllModifications() throws Exception
    {
        PreparedStatement ps = connection.prepareStatement("UPDATE modifications SET cleared=1");
        
        int rowsUpdated = ps.executeUpdate();
        
        logger.info(String.format("Cleared all modifications. %d rows updated", rowsUpdated));
        
        ps.close();
    }

    public List<Entry> getEntries(String pageId, int start, int number)
    {
        PreparedStatement ps = null;
        List<Entry> result = new ArrayList<Entry>();

        try {
            if (pageId != null) {
                ps = connection.prepareStatement("SELECT * FROM modifications WHERE pageId=? ORDER by timestamp DESC");
                ps.setString(1, pageId);
            } else {
                ps = connection.prepareStatement("SELECT * FROM modifications ORDER by timestamp DESC");
            }

            ResultSet rs = ps.executeQuery();

            int i = 0;
            int n = 0;
            while (rs.next()) {
                if (i >= start) {
                    if (number == -1 || n <= number) {
                        Entry entry =
                            new Entry(rs.getString(1), rs.getLong(2), rs.getInt(3), rs.getInt(4), rs.getBoolean(5));
                        result.add(entry);
                        n++;
                    }
                }
                i++;
            }

            ps.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public List<Entry> getLastClearedEntries(String pageId, int start, int number)
    {
        PreparedStatement ps = null;
        List<Entry> result = new ArrayList<Entry>();

        try {
            if (pageId != null) {
                ps = connection.prepareStatement("SELECT * FROM lastCleared WHERE pageId=? ORDER by timestamp DESC");
                ps.setString(1, pageId);
            } else {
                ps = connection.prepareStatement("SELECT * FROM lastCleared ORDER by timestamp DESC");
            }

            ResultSet rs = ps.executeQuery();

            int i = 0;
            int n = 0;
            while (rs.next()) {
                if (i >= start) {
                    if (number == -1 || n <= number) {
                        Entry entry =
                            new Entry(rs.getString(1), rs.getLong(2), rs.getInt(3), rs.getInt(4), true);
                        result.add(entry);
                        n++;
                    }
                }
                i++;
            }

            ps.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
