package org.xwiki.model;

import javax.jcr.Node;

/**
 * Note: we cannot map an Object Definition Property to a JCR property since it has several properties such as
 * validation script, edit sheet, etc.
 */
public interface ObjectDefinitionProperty extends Node
{

}
