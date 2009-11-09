package org.xwiki.store.value;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Map.Entry;

import org.xwiki.context.Execution;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.objects.BaseProperty;
import com.xpn.xwiki.objects.classes.BaseClass;

/**
 * Converts value objects to/from xwiki business objects.
 */
public class ValueConverter
{
    private Execution execution;

    public ValueConverter(Execution execution)
    {
        this.execution = execution;
    }

    public DocumentId getId(XWikiDocument doc)
    {
        return new DocumentId(doc.getDatabase(), doc.getSpace(), doc.getName(), doc.getLanguage());
    }

    public ObjectId getId(DocumentId docId, BaseObject obj)
    {
        return new ObjectId(docId, obj.getNumber());
    }

    public DocumentValue toValue(XWikiDocument doc) throws Exception
    {
        return reflectToValue(doc, new DocumentValue());        
    }

    public XWikiDocument fromValue(DocumentValue value, XWikiDocument doc) throws Exception
    {
        return reflectFromValue(value, doc);
    }

    @SuppressWarnings("unchecked")
    public ObjectValue toValue(BaseObject obj) throws Exception
    {
        ObjectValue val = reflectToValue(obj, new ObjectValue());
        for (BaseProperty prop : (Set<BaseProperty>) obj.getPropertyList()) {
            val.properties.put(prop.getName(), prop.getValue());
        }
        val.jcrName = ""+val.number;
        return val;
    }

    public BaseObject fromValue(ObjectValue ovalue) throws Exception
    {
        BaseObject obj = BaseClass.newCustomClassInstance(ovalue.className, getContext());
        obj = reflectFromValue(ovalue, obj);
        for (Entry<String, Object> prop : ovalue.properties.entrySet()) {
            obj.set(prop.getKey(), prop.getValue(), getContext());
        }
        return obj;
    }

    protected <T, V> V reflectToValue(T obj, V valueObj) throws Exception 
    {
        for (Field f : valueObj.getClass().getFields()) {
            try {
                Method mgetter = obj.getClass().getMethod(getGetterName(f));
                Object value = mgetter.invoke(obj);
                f.set(valueObj, value);
            } catch (NoSuchMethodException e) {
            }
        }
        return valueObj;
    }

    protected <T, V> T reflectFromValue(V valueObj, T obj) throws Exception
    {
        for (Field f : valueObj.getClass().getFields()) {
            Object value = f.get(valueObj);
            try {
                Method msetter = obj.getClass().getMethod(getSetterName(f), f.getType());
                msetter.invoke(obj, value);
            } catch (NoSuchMethodException e) {
            }
        }
        return obj;
    }

    protected String getGetterName(Field f)
    {
        String res = f.getType().equals(boolean.class) ? "is" : "get";
        res += Character.toUpperCase(f.getName().charAt(0)) + f.getName().substring(1);
        return res;
    }

    protected String getSetterName(Field f)
    {
        return "set" + Character.toUpperCase(f.getName().charAt(0)) + f.getName().substring(1);
    }

    protected XWikiContext getContext()
    {
        return (XWikiContext) execution.getContext().getProperty("xwikicontext");
    }
}
