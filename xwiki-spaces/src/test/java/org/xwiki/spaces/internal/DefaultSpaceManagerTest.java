package org.xwiki.spaces.internal;

import java.lang.reflect.Field;

import junit.framework.Assert;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xwiki.component.util.ReflectionUtils;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.spaces.SpaceManager;
import org.xwiki.spaces.SpaceManagerConfiguration;

@RunWith(JMock.class)
public class DefaultSpaceManagerTest
{
    private Mockery mockery = new Mockery();

    private SpaceManagerConfiguration configuration;

    private ConfigurationSource mockSource;

    private SpaceManager manager;

    private String defaultValidationRegex;

    @Before
    public void setUp() throws Exception
    {
        this.mockSource = this.mockery.mock(ConfigurationSource.class);
        this.configuration = new DefaultSpaceManagerConfiguration();

        ReflectionUtils.setFieldValue(this.configuration, "source", this.mockSource);

        // Read the default space key validation REGEX from the class definition. We will use if to define expectations
        // of the configuration source mocks.
        for (Field field : DefaultSpaceManagerConfiguration.class.getDeclaredFields()) {
            if (field.getName().equals("DEFAULT_VALIDATION_REGEX")) {
                field.setAccessible(true);
                this.defaultValidationRegex = (String) field.get(null);
            }
        }

        manager = new DefaultSpaceManager();
        ReflectionUtils.setFieldValue(this.manager, "configuration", this.configuration);
    }

    @Test
    public void testSpaceKeyValidationWhenBlankRegex()
    {
        this.mockery.checking(new Expectations()
        {
            {
                allowing(mockSource).getProperty("spaces.nameValidationRegex", defaultValidationRegex);
                will(returnValue(""));
            }
        });

        Assert.assertTrue(manager.isLegalSpaceKey("1!@$%^&*| A Key that could be refused if there was a regex !"));
    }

    @Test
    public void testSpaceKeyValidation()
    {
        this.mockery.checking(new Expectations()
        {
            {
                allowing(mockSource).getProperty("spaces.nameValidationRegex", defaultValidationRegex);
                will(returnValue("^\\w{3}\\w*$"));
            }
        });

        Assert.assertTrue(manager.isLegalSpaceKey("XWiki"));
        Assert.assertTrue(manager.isLegalSpaceKey("XWi"));

        Assert.assertFalse(manager.isLegalSpaceKey("XWiki 3.0"));
        Assert.assertFalse(manager.isLegalSpaceKey("XW"));
        Assert.assertFalse(manager.isLegalSpaceKey("XWiki=^-^="));
    }
}
