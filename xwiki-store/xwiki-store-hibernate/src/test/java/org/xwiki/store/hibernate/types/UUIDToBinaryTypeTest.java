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
 *
 */

package org.xwiki.store.hibernate.types;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.UUID;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.Test;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.Expectations;


/**
 * Tests UUIDToBinaryType.
 *
 * @version $Id$
 */
@RunWith(JMock.class)
public class UUIDToBinaryTypeTest
{
    private final UUIDToBinaryType type = new UUIDToBinaryType();

    private final Mockery mocker = new JUnit4Mockery();

    @Test
    public void assembleDisassembleTest() throws Exception
    {
        final UUID id = UUID.randomUUID();
        final byte[] bytes = (byte[]) this.type.disassemble(id);
        final UUID newId = (UUID) this.type.assemble(bytes, null);
        Assert.assertEquals(id, newId);
    }

    @Test
    public void setGetTest() throws Exception
    {
        final byte[] sixteenBytes = "0123456789ABCDEF".getBytes("US-ASCII");

        final ResultSet results = this.mocker.mock(ResultSet.class);
        final PreparedStatement statement = this.mocker.mock(PreparedStatement.class);

        this.mocker.checking(new Expectations(){{
            one(results).getBytes("uuid"); will(returnValue(sixteenBytes));
            one(statement).setBytes(0, sixteenBytes);
        }});

        this.type.nullSafeSet(statement, this.type.nullSafeGet(results, new String[] {"uuid"}, null), 0);
    }
}
